package net.Indyuce.mmocore.manager.data.mysql;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.guild.provided.Guild;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import org.apache.commons.lang.Validate;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class MySQLPlayerDataManager extends PlayerDataManager {
    private final MySQLDataProvider provider;

    public MySQLPlayerDataManager(MySQLDataProvider provider) {
        this.provider = provider;
    }

    @Override
    public void loadData(PlayerData data) {
        long startTime = System.currentTimeMillis();
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {

                provider.getResult("SELECT * FROM mmocore_playerdata WHERE uuid = '" + data.getUniqueId() + "';", (result) -> {
                    try {
                        if (result.next()) {

                            //If the data couldn't be loaded for more than 2 seconds its probably due to a server crash and we load the old data
                            //If it the status is is_saved we load the data
                            if (System.currentTimeMillis() - startTime > 2000 || result.getInt("is_saved") == 1) {
                                MMOCore.sqlDebug("Time waited: " + (System.currentTimeMillis() - startTime));
                                MMOCore.sqlDebug("Loading data for: '" + data.getUniqueId() + "'...");

                                // Initialize custom resources
                                if (!data.hasUsedTemporaryData()) {
                                    data.setMana(data.getStats().getStat("MAX_MANA"));
                                    data.setStamina(data.getStats().getStat("MAX_STAMINA"));
                                    data.setStellium(data.getStats().getStat("MAX_STELLIUM"));
                                }

                                data.setClassPoints(result.getInt("class_points"));
                                data.setSkillPoints(result.getInt("skill_points"));
                                data.setAttributePoints(result.getInt("attribute_points"));
                                data.setAttributeReallocationPoints(result.getInt("attribute_realloc_points"));
                                data.setLevel(result.getInt("level"));
                                data.setExperience(result.getInt("experience"));
                                if (!isEmpty(result.getString("class")))
                                    data.setClass(MMOCore.plugin.classManager.get(result.getString("class")));

                                if (!isEmpty(result.getString("times_claimed"))) {
                                    JsonObject json = new JsonParser().parse(result.getString("times_claimed")).getAsJsonObject();
                                    json.entrySet().forEach(entry -> data.getItemClaims().put(entry.getKey(), entry.getValue().getAsInt()));
                                }

                                if (!isEmpty(result.getString("guild"))) {
                                    Guild guild = MMOCore.plugin.dataProvider.getGuildManager().getGuild(result.getString("guild"));
                                    data.setGuild(guild.hasMember(data.getUniqueId()) ? guild : null);
                                }
                                if (!isEmpty(result.getString("attributes")))
                                    data.getAttributes().load(result.getString("attributes"));
                                if (!isEmpty(result.getString("professions")))
                                    data.getCollectionSkills().load(result.getString("professions"));
                                if (!isEmpty(result.getString("quests")))
                                    data.getQuestData().load(result.getString("quests"));
                                data.getQuestData().updateBossBar();
                                if (!isEmpty(result.getString("waypoints")))
                                    data.getWaypoints().addAll(MMOCoreUtils.jsonArrayToList(result.getString("waypoints")));
                                if (!isEmpty(result.getString("friends")))
                                    MMOCoreUtils.jsonArrayToList(result.getString("friends")).forEach(str -> data.getFriends().add(UUID.fromString(str)));
                                if (!isEmpty(result.getString("skills"))) {
                                    JsonObject object = MythicLib.plugin.getJson().parse(result.getString("skills"), JsonObject.class);
                                    for (Entry<String, JsonElement> entry : object.entrySet())
                                        data.setSkillLevel(entry.getKey(), entry.getValue().getAsInt());
                                }
                                if (!isEmpty(result.getString("bound_skills")))
                                    for (String skill : MMOCoreUtils.jsonArrayToList(result.getString("bound_skills")))
                                        if (data.getProfess().hasSkill(skill))
                                            data.getBoundSkills().add(data.getProfess().getSkill(skill));
                                if (!isEmpty(result.getString("class_info"))) {
                                    JsonObject object = MythicLib.plugin.getJson().parse(result.getString("class_info"), JsonObject.class);
                                    for (Entry<String, JsonElement> entry : object.entrySet()) {
                                        try {
                                            PlayerClass profess = MMOCore.plugin.classManager.get(entry.getKey());
                                            Validate.notNull(profess, "Could not find class '" + entry.getKey() + "'");
                                            data.applyClassInfo(profess, new SavedClassInformation(entry.getValue().getAsJsonObject()));
                                        } catch (IllegalArgumentException exception) {
                                            MMOCore.log(Level.WARNING, "Could not load class info '" + entry.getKey() + "': " + exception.getMessage());
                                        }
                                    }
                                }

                                //We now change the saved status to false because the data on SQL won't be the same as in the RAM
                                MySQLTableEditor sql = new MySQLTableEditor(MySQLTableEditor.Table.PLAYERDATA, data.getUniqueId(), provider);

                                //We set the saved status to false
                                sql.updateData("is_saved", 0);
                                this.cancel();
                                data.setFullyLoaded();
                                MMOCore.sqlDebug("Loaded saved data for: '" + data.getUniqueId() + "'!");
                                MMOCore.sqlDebug(String.format("{ class: %s, level: %d }", data.getProfess().getId(), data.getLevel()));
                            } else {
                                MMOCore.sqlDebug("Failed to load data because is_saved is false.");
                            }

                        } else {
                            data.setLevel(getDefaultData().getLevel());
                            data.setClassPoints(getDefaultData().getClassPoints());
                            data.setSkillPoints(getDefaultData().getSkillPoints());
                            data.setAttributePoints(getDefaultData().getAttributePoints());
                            data.setAttributeReallocationPoints(getDefaultData().getAttrReallocPoints());
                            data.setExperience(0);
                            data.getQuestData().updateBossBar();

                            data.setFullyLoaded();
                            this.cancel();
                            MMOCore.sqlDebug("Loaded DEFAULT data for: '" + data.getUniqueId() + "' as no saved data was found.");
                            return;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            }
        };

        runnable.runTaskTimerAsynchronously(MMOCore.plugin, 0L, 10L);
    }

    @Override
    public void saveData(PlayerData data) {

        MySQLTableEditor sql = new MySQLTableEditor(MySQLTableEditor.Table.PLAYERDATA, data.getUniqueId(),provider);
        MMOCore.sqlDebug("Saving data for: '" + data.getUniqueId() + "'...");

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
        sql.updateJSONArray("friends", data.getFriends().stream().map(UUID::toString).collect(Collectors.toList()));
        sql.updateJSONArray("bound_skills", data.getBoundSkills().stream().map(skill -> skill.getSkill().getHandler().getId()).collect(Collectors.toList()));

        sql.updateJSONObject("skills", data.mapSkillLevels().entrySet());
        sql.updateJSONObject("times_claimed", data.getItemClaims().entrySet());

        sql.updateData("attributes", data.getAttributes().toJsonString());
        sql.updateData("professions", data.getCollectionSkills().toJsonString());
        sql.updateData("quests", data.getQuestData().toJsonString());

        sql.updateData("class_info", createClassInfoData(data).toString());
        sql.updateData("is_saved", 1);

        MMOCore.sqlDebug("Saved data for: " + data.getUniqueId());
        MMOCore.sqlDebug(String.format("{ class: %s, level: %d }", data.getProfess().getId(), data.getLevel()));
    }

    private JsonObject createClassInfoData(PlayerData playerData) {
        final JsonObject json = new JsonObject();
        for (String c : playerData.getSavedClasses()) {
            final SavedClassInformation info = playerData.getClassInfo(c);
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

    private boolean isEmpty(String s) {
        return s == null || s.equalsIgnoreCase("null") || s.equalsIgnoreCase("{}") || s.equalsIgnoreCase("[]") || s.equalsIgnoreCase("");
    }

    @NotNull
    @Override
    public OfflinePlayerData getOffline(UUID uuid) {
        return isLoaded(uuid) ? get(uuid) : new MySQLOfflinePlayerData(uuid);
    }

    public class MySQLOfflinePlayerData extends OfflinePlayerData {
        private int level;
        private long lastLogin;
        private PlayerClass profess;
        private List<UUID> friends;

        public MySQLOfflinePlayerData(UUID uuid) {
            super(uuid);

            provider.getResult("SELECT * FROM mmocore_playerdata WHERE uuid = '" + uuid + "';", (result) -> {
                try {
                    MMOCore.sqlDebug("Loading OFFLINE data for '" + uuid + "'.");
                    if (!result.next()) {
                        level = 0;
                        lastLogin = 0;
                        profess = MMOCore.plugin.classManager.getDefaultClass();
                        friends = new ArrayList<>();
                        MMOCore.sqlDebug("Default OFFLINE data loaded.");
                    } else {
                        level = result.getInt("level");
                        lastLogin = result.getLong("last_login");
                        profess = isEmpty(result.getString("class")) ? MMOCore.plugin.classManager.getDefaultClass() : MMOCore.plugin.classManager.get(result.getString("class"));
                        if (!isEmpty(result.getString("friends")))
                            MMOCoreUtils.jsonArrayToList(result.getString("friends")).forEach(str -> friends.add(UUID.fromString(str)));
                        else friends = new ArrayList<>();
                        MMOCore.sqlDebug("Saved OFFLINE data loaded.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void removeFriend(UUID uuid) {
            friends.remove(uuid);
            new MySQLTableEditor(MySQLTableEditor.Table.PLAYERDATA, uuid, provider).updateData("friends", friends.stream().map(UUID::toString).collect(Collectors.toList()));
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
}



