package net.Indyuce.mmocore.manager.data.mysql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;

import com.github.jasync.sql.db.ResultSet;
import com.github.jasync.sql.db.RowData;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import net.Indyuce.mmocore.manager.data.mysql.MySQLTableEditor.Table;
import net.mmogroup.mmolib.MMOLib;

public class MySQLPlayerDataManager extends PlayerDataManager {
	private final MySQLDataProvider provider;

	public MySQLPlayerDataManager(MySQLDataProvider provider) {
		this.provider = provider;
	}

	@Override
	public void loadData(PlayerData data) {
		ResultSet result = provider
				.getResult("SELECT * FROM mmocore_playerdata WHERE uuid = '" + data.getUniqueId() + "';");
		if (result == null) {
			MMOCore.log(Level.SEVERE,
					"Failed to load playerdata of '" + data.getPlayer().getName() + "' from MySQL server");
			return;
		}

		// player data not initialized yet
		if (result.size() < 1)
			return;

		RowData row = result.get(0);

		data.setClassPoints(row.getInt("class_points"));
		data.setSkillPoints(row.getInt("skill_points"));
		data.setAttributePoints(row.getInt("attribute_points"));
		data.setAttributeReallocationPoints(row.getInt("attribute_realloc_points"));
		data.setLevel(row.getInt("level"));
		data.setExperience(row.getInt("experience"));
		if (!isEmpty(row.getString("class")))
			data.setClass(MMOCore.plugin.classManager.get(row.getString("class")));
		data.setMana(data.getStats().getStat(StatType.MAX_MANA));
		data.setStamina(data.getStats().getStat(StatType.MAX_STAMINA));
		data.setStellium(data.getStats().getStat(StatType.MAX_STELLIUM));
		if (!isEmpty(row.getString("guild")))
			data.setGuild(MMOCore.plugin.dataProvider.getGuildManager().stillInGuild(data.getUniqueId(),
					row.getString("guild")));
		if (!isEmpty(row.getString("attributes")))
			data.getAttributes().load(row.getString("attributes"));
		if (!isEmpty(row.getString("professions")))
			data.getCollectionSkills().load(row.getString("professions"));
		String quests = row.getString("quests");
		if (!isEmpty(quests))
			data.getQuestData().load(quests);
		data.getQuestData().updateBossBar();
		if (!isEmpty(row.getString("waypoints")))
			data.getWaypoints().addAll(getJSONArray(row.getString("waypoints")));
		if (!isEmpty(row.getString("friends")))
			getJSONArray(row.getString("friends")).forEach(str -> data.getFriends().add(UUID.fromString(str)));
		if (!isEmpty(row.getString("skills"))) {
			JsonObject object = MMOLib.plugin.getJson().parse(row.getString("skills"), JsonObject.class);
			for (Entry<String, JsonElement> entry : object.entrySet())
				data.setSkillLevel(entry.getKey(), entry.getValue().getAsInt());
		}
		if (!isEmpty(row.getString("bound_skills")))
			for (String skill : getJSONArray(row.getString("bound_skills")))
				if (data.getProfess().hasSkill(skill))
					data.getBoundSkills().add(data.getProfess().getSkill(skill));
		if (!isEmpty(row.getString("class_info"))) {
			JsonObject object = MMOLib.plugin.getJson().parse(row.getString("class_info"), JsonObject.class);
			for (Entry<String, JsonElement> entry : object.entrySet()) {
				try {
					PlayerClass profess = MMOCore.plugin.classManager.get(entry.getKey());
					Validate.notNull(profess, "Could not find class '" + entry.getKey() + "'");
					data.applyClassInfo(profess, new SavedClassInformation(entry.getValue().getAsJsonObject()));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING,
							"Could not load class info '" + entry.getKey() + "': " + exception.getMessage());
				}
			}
		}
	}

	private boolean isEmpty(String s) {
		return s.equalsIgnoreCase("null") || s.equalsIgnoreCase("{}") || s.equalsIgnoreCase("[]")
				|| s.equalsIgnoreCase("");
	}

	@Override
	public void saveData(PlayerData data) {
		MySQLTableEditor sql = new MySQLTableEditor(Table.PLAYERDATA, data.getUniqueId());

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
		sql.updateJSONArray("friends",
				data.getFriends().stream().map(uuid -> uuid.toString()).collect(Collectors.toList()));
		sql.updateJSONArray("bound_skills",
				data.getBoundSkills().stream().map(skill -> skill.getSkill().getId()).collect(Collectors.toList()));

		sql.updateJSONObject("skills", data.mapSkillLevels().entrySet());

		sql.updateData("attributes", data.getAttributes().toJsonString());
		sql.updateData("professions", data.getCollectionSkills().toJsonString());
		sql.updateData("quests", data.getQuestData().toJsonString());

		sql.updateData("class_info", createClassInfoData(data).toString());
	}

	private JsonObject createClassInfoData(PlayerData data) {
		JsonObject json = new JsonObject();
		for (String c : data.getSavedClasses()) {
			SavedClassInformation info = data.getClassInfo(c);
			JsonObject classinfo = new JsonObject();
			classinfo.addProperty("level", info.getLevel());
			classinfo.addProperty("experience", info.getExperience());
			classinfo.addProperty("skill-points", info.getSkillPoints());
			classinfo.addProperty("attribute-points", info.getAttributePoints());
			classinfo.addProperty("attribute-realloc-points", info.getAttributeReallocationPoints());
			JsonObject skillinfo = new JsonObject();
			for (String skill : info.getSkillKeys())
				skillinfo.addProperty(skill, info.getSkillLevel(skill));
			classinfo.add("skill", skillinfo);
			JsonObject attributeinfo = new JsonObject();
			for (String attribute : info.getAttributeKeys())
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

	private Collection<String> getJSONArray(String json) {
		Collection<String> collection = new ArrayList<String>();

		for (String s : MMOLib.plugin.getJson().parse(json, String[].class))
			collection.add(s);

		return collection;
	}

	public class MySQLOfflinePlayerData extends OfflinePlayerData {
		private int level;
		private long lastLogin;
		private PlayerClass profess;
		private List<UUID> friends;

		public MySQLOfflinePlayerData(UUID uuid) {
			super(uuid);

			ResultSet result = provider.getResult("SELECT * FROM mmocore_playerdata WHERE uuid = '" + uuid + "';");
			if (result.size() < 1) {
				level = 0;
				lastLogin = 0;
				profess = MMOCore.plugin.classManager.getDefaultClass();
				friends = new ArrayList<UUID>();
			} else {
				RowData row = result.get(0);
				
				level = row.getInt("level");
				lastLogin = row.getLong("last_login");
				profess = row.getString("class").equalsIgnoreCase("null")
						? MMOCore.plugin.classManager.getDefaultClass()
						: MMOCore.plugin.classManager.get(row.getString("class"));
				if (!row.getString("friends").equalsIgnoreCase("null"))
					getJSONArray(row.getString("friends")).forEach(str -> friends.add(UUID.fromString(str)));
				else
					friends = new ArrayList<UUID>();
			}
		}

		@Override
		public void removeFriend(UUID uuid) {
			friends.remove(uuid);
			new MySQLTableEditor(Table.PLAYERDATA, uuid).updateData("friends",
					friends.stream().map(friend -> friend.toString()).collect(Collectors.toList()));
		}

		@Override
		public boolean hasFriend(UUID uuid) {
			return friends.contains(uuid);
		}

		@Override
		public PlayerClass getProfess() {
			return profess;
		}

		@Override
		public int getLevel() {
			return level;
		}

		@Override
		public long getLastLogin() {
			return lastLogin;
		}
	}

	@Override
	public void remove(PlayerData data) {
		saveData(data);
		remove(data.getUniqueId());
	}
}
