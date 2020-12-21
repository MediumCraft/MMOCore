package net.Indyuce.mmocore.manager.data.mysql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;

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
		try {
			provider.getResultAsync("SELECT * FROM mmocore_playerdata WHERE uuid = '" + data.getUniqueId() + "';",
					(result) -> {
						try {
							if (!result.first()) {
								data.setLevel(getDefaultData().getLevel());
								data.setClassPoints(getDefaultData().getClassPoints());
								data.setSkillPoints(getDefaultData().getSkillPoints());
								data.setAttributePoints(getDefaultData().getAttributePoints());
								data.setAttributeReallocationPoints(getDefaultData().getAttrReallocPoints());
								data.setExperience(0);
								data.setMana(data.getStats().getStat(StatType.MAX_MANA));
								data.setStamina(data.getStats().getStat(StatType.MAX_STAMINA));
								data.setStellium(data.getStats().getStat(StatType.MAX_STELLIUM));
								data.getQuestData().updateBossBar();

								return;
							}

							data.setClassPoints(result.getInt("class_points"));
							data.setSkillPoints(result.getInt("skill_points"));
							data.setAttributePoints(result.getInt("attribute_points"));
							data.setAttributeReallocationPoints(result.getInt("attribute_realloc_points"));
							data.setLevel(result.getInt("level"));
							data.setExperience(result.getInt("experience"));
							if (!isEmpty(result.getString("class")))
								data.setClass(MMOCore.plugin.classManager.get(result.getString("class")));
							data.setMana(data.getStats().getStat(StatType.MAX_MANA));
							data.setStamina(data.getStats().getStat(StatType.MAX_STAMINA));
							data.setStellium(data.getStats().getStat(StatType.MAX_STELLIUM));
							if (!isEmpty(result.getString("guild")))
								data.setGuild(MMOCore.plugin.dataProvider.getGuildManager()
										.stillInGuild(data.getUniqueId(), result.getString("guild")));
							if (!isEmpty(result.getString("attributes")))
								data.getAttributes().load(result.getString("attributes"));
							if (!isEmpty(result.getString("professions")))
								data.getCollectionSkills().load(result.getString("professions"));
							if (!isEmpty(result.getString("quests")))
								data.getQuestData().load(result.getString("quests"));
							data.getQuestData().updateBossBar();
							if (!isEmpty(result.getString("waypoints")))
								data.getWaypoints().addAll(getJSONArray(result.getString("waypoints")));
							if (!isEmpty(result.getString("friends")))
								getJSONArray(result.getString("friends"))
										.forEach(str -> data.getFriends().add(UUID.fromString(str)));
							if (!isEmpty(result.getString("skills"))) {
								JsonObject object = MMOLib.plugin.getJson().parse(result.getString("skills"),
										JsonObject.class);
								for (Entry<String, JsonElement> entry : object.entrySet())
									data.setSkillLevel(entry.getKey(), entry.getValue().getAsInt());
							}
							if (!isEmpty(result.getString("bound_skills")))
								for (String skill : getJSONArray(result.getString("bound_skills")))
									if (data.getProfess().hasSkill(skill))
										data.getBoundSkills().add(data.getProfess().getSkill(skill));
							if (!isEmpty(result.getString("class_info"))) {
								JsonObject object = MMOLib.plugin.getJson().parse(result.getString("class_info"),
										JsonObject.class);
								for (Entry<String, JsonElement> entry : object.entrySet()) {
									try {
										PlayerClass profess = MMOCore.plugin.classManager.get(entry.getKey());
										Validate.notNull(profess, "Could not find class '" + entry.getKey() + "'");
										data.applyClassInfo(profess,
												new SavedClassInformation(entry.getValue().getAsJsonObject()));
									} catch (IllegalArgumentException exception) {
										MMOCore.log(Level.WARNING, "Could not load class info '" + entry.getKey()
												+ "': " + exception.getMessage());
									}
								}
							}
							data.setFullyLoaded();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	private boolean isEmpty(String s) {
		return s == null || s.equalsIgnoreCase("null") || s.equalsIgnoreCase("{}") || s.equalsIgnoreCase("[]")
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
				data.getFriends().stream().map(UUID::toString).collect(Collectors.toList()));
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
		return new ArrayList<>(Arrays.asList(MMOLib.plugin.getJson().parse(json, String[].class)));
	}

	public class MySQLOfflinePlayerData extends OfflinePlayerData {
		private int level;
		private long lastLogin;
		private PlayerClass profess;
		private List<UUID> friends;

		public MySQLOfflinePlayerData(UUID uuid) {
			super(uuid);

			try {
				provider.getResultAsync("SELECT * FROM mmocore_playerdata WHERE uuid = '" + uuid + "';", (result) -> {
					try {
						if (!result.first()) {
							level = 0;
							lastLogin = 0;
							profess = MMOCore.plugin.classManager.getDefaultClass();
							friends = new ArrayList<>();
						} else {
							level = result.getInt("level");
							lastLogin = result.getLong("last_login");
							profess = isEmpty(result.getString("class")) ? MMOCore.plugin.classManager.getDefaultClass()
									: MMOCore.plugin.classManager.get(result.getString("class"));
							if (!isEmpty(result.getString("friends")))
								getJSONArray(result.getString("friends"))
										.forEach(str -> friends.add(UUID.fromString(str)));
							else
								friends = new ArrayList<>();
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void removeFriend(UUID uuid) {
			friends.remove(uuid);
			new MySQLTableEditor(Table.PLAYERDATA, uuid).updateDataAsync("friends",
					friends.stream().map(UUID::toString).collect(Collectors.toList()));
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
		if(data.isFullyLoaded()) saveData(data);
		remove(data.getUniqueId());
	}
}
