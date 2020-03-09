package net.Indyuce.mmocore.manager.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.util.MMOSQL;
import net.Indyuce.mmocore.api.util.MMOSQL.Table;

public class MySQLPlayerDataManager extends PlayerDataManager {
	@Override
	public void loadData(PlayerData data) {
		ResultSet result = MMOSQL.getResult("SELECT * FROM mmocore_playerdata WHERE uuid = '" + data.getUniqueId() + "';");
		if(result == null)
			MMOCore.log(Level.SEVERE, "Failed to load playerdata from MySQL!");
		try {
			if(!result.next()) {
				return;
			}
			Gson parser = new Gson();
			
			data.setClassPoints(result.getInt("class_points"));
			data.setSkillPoints(result.getInt("skill_points"));
			data.setAttributePoints(result.getInt("attribute_points"));
			data.setAttributeReallocationPoints(result.getInt("attribute_realloc_points"));
			data.setLevel(result.getInt("level"));
			data.setExperience(result.getInt("experience"));
			if(!isEmpty(result.getString("class")))
				data.setProfess(MMOCore.plugin.classManager.get(result.getString("class")));
			data.setMana(data.getStats().getStat(StatType.MAX_MANA));
			data.setStamina(data.getStats().getStat(StatType.MAX_STAMINA));
			data.setStellium(data.getStats().getStat(StatType.MAX_STELLIUM));
			if(!isEmpty(result.getString("guild")))
				data.setGuild(MMOCore.plugin.dataProvider.getGuildManager().stillInGuild(data.getUniqueId(), result.getString("guild")));
			if(!isEmpty(result.getString("attributes")))
				data.getAttributes().load(result.getString("attributes"));
			if(!isEmpty(result.getString("professions")))
				data.getCollectionSkills().load(result.getString("professions"));
			String quests = result.getString("quests");
			if(!isEmpty(quests)) 
				data.getQuestData().load(quests);
			data.getQuestData().updateBossBar();
			if(!isEmpty(result.getString("waypoints")))
				data.getWaypoints().addAll(MMOSQL.getJSONArray(result.getString("waypoints")));
			if(!isEmpty(result.getString("friends")))
				MMOSQL.getJSONArray(result.getString("friends")).forEach(str -> data.getFriends().add(UUID.fromString(str)));
			if(!isEmpty(result.getString("skills"))) {
				JsonObject object = parser.fromJson(result.getString("skills"), JsonObject.class);
				for(Entry<String, JsonElement> entry : object.entrySet())
					data.setSkillLevel(entry.getKey(), entry.getValue().getAsInt());
			}
			if(!isEmpty(result.getString("bound_skills")))
				for(String skill : MMOSQL.getJSONArray(result.getString("bound_skills")))
					if(MMOCore.plugin.skillManager.has(skill))
						data.getBoundSkills().add(data.getProfess().getSkill(skill));
			if(!isEmpty(result.getString("class_info"))) {
				JsonObject object = parser.fromJson(result.getString("class_info"), JsonObject.class);
				for(Entry<String, JsonElement> entry : object.entrySet()) {
					try {
						PlayerClass profess = MMOCore.plugin.classManager.get(entry.getKey());
						Validate.notNull(profess, "Could not find class '" + entry.getKey() + "'");
						data.applyClassInfo(profess, new SavedClassInformation(entry.getValue().getAsJsonObject()));
					} catch (IllegalArgumentException exception) {
						MMOCore.log(Level.WARNING, "Could not load class info '" + entry.getKey() + "': " + exception.getMessage());
					}
				}	
			}
		} catch (SQLException e) {
			MMOCore.log(Level.SEVERE, "Failed to load playerdata from MySQL!");
			e.printStackTrace();
		}
	}

	private boolean isEmpty(String s) {
		return s.equalsIgnoreCase("null") || s.equalsIgnoreCase("{}")
			|| s.equalsIgnoreCase("[]") || s.equalsIgnoreCase("");
	}

	@Override
	public void saveData(PlayerData data) {
		MMOSQL sql = new MMOSQL(Table.PLAYERDATA, data.getUniqueId());
		
		sql.updateData("class_points", data.getClassPoints());
		sql.updateData("skill_points", data.getSkillPoints());
		sql.updateData("attribute_points", data.getAttributePoints());
		sql.updateData("attribute_realloc_points", data.getAttributeReallocationPoints());
		sql.updateData("level", data.getLevel());
		sql.updateData("experience", data.getExperience());
		sql.updateData("class", data.getProfess().getId());
		sql.updateData("last_login", data.getLastLogin());
		sql.updateData("guild", data.hasGuild() ? data.getGuild().getId() : null);
		
		sql.updateJSONArray("waypoints", data.getWaypoints());
		sql.updateJSONArray("friends", data.getFriends().stream().map(uuid -> uuid.toString()).collect(Collectors.toList()));
		sql.updateJSONArray("bound_skills", data.getBoundSkills().stream().map(skill -> skill.getSkill().getId()).collect(Collectors.toList()));

		sql.updateJSONObject("skills", data.mapSkillLevels().entrySet());

		sql.updateData("attributes", data.getAttributes().toJsonString());
		sql.updateData("professions", data.getCollectionSkills().toJsonString());
		sql.updateData("quests", data.getQuestData().toJsonString());

		sql.updateData("class_info", createClassInfoData(data).toString());
	}
	
	private JsonObject createClassInfoData(PlayerData data) {
		JsonObject json = new JsonObject();
		for(String c : data.getSavedClasses()) {
			SavedClassInformation info = data.getClassInfo(c);
			JsonObject classinfo = new JsonObject();
			classinfo.addProperty("level", info.getLevel());
			classinfo.addProperty("experience", info.getExperience());
			classinfo.addProperty("skill-points", info.getSkillPoints());
			classinfo.addProperty("attribute-points", info.getAttributePoints());
			classinfo.addProperty("attribute-realloc-points", info.getAttributeReallocationPoints());
			JsonObject skillinfo = new JsonObject();
			for(String skill : info.getSkillKeys())
				skillinfo.addProperty(skill, info.getSkillLevel(skill));
			classinfo.add("skill", skillinfo);
			JsonObject attributeinfo = new JsonObject();
			for(String attribute : info.getAttributeKeys())
				attributeinfo.addProperty(attribute, info.getAttributeLevel(attribute));
			classinfo.add("attribute", attributeinfo);
			
			json.add(c, classinfo);
		}
		
		return json;
	}

	@Override
	public OfflinePlayerData getOffline(UUID uuid) {
		return isLoaded(uuid) ? get(uuid) : new MySQLOfflinePlayerData(uuid);
	}
}
