package net.Indyuce.mmocore.manager.data.mysql;

import com.google.gson.JsonObject;
import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MySQLPlayerDataManager extends PlayerDataManager {
    private final MySQLDataProvider provider;

    public MySQLPlayerDataManager(MySQLDataProvider provider) {
        this.provider = provider;
    }

    public MySQLDataProvider getProvider() {
        return provider;
    }

    @Override
    public void loadData(PlayerData data) {
        new MMOCoreDataSynchronizer(this, data).fetch();
    }

    @Override
    public void saveData(PlayerData data, boolean logout) {
        MythicLib.debug("MMOCoreSQL", "Saving data for: '" + data.getUniqueId() + "'...");

        final PlayerDataTableUpdater updater = new PlayerDataTableUpdater(provider, data.getUniqueId());
        updater.addData("class_points", data.getClassPoints());
        updater.addData("skill_points", data.getSkillPoints());
        updater.addData("skill_reallocation_points", data.getSkillReallocationPoints());
        updater.addData("attribute_points", data.getAttributePoints());
        updater.addData("attribute_realloc_points", data.getAttributeReallocationPoints());
        updater.addJSONArray("waypoints", data.getWaypoints());
        updater.addData("skill_tree_reallocation_points", data.getSkillTreeReallocationPoints());
        updater.addData("mana", data.getMana());
        updater.addData("stellium", data.getStellium());
        updater.addData("stamina", data.getStamina());
        updater.addData("level", data.getLevel());
        updater.addData("experience", data.getExperience());
        updater.addData("class", data.getProfess().getId());
        updater.addData("last_login", data.getLastLogin());
        updater.addData("guild", data.hasGuild() ? data.getGuild().getId() : null);
        updater.addJSONArray("waypoints", data.getWaypoints());
        updater.addJSONArray("friends", data.getFriends().stream().map(UUID::toString).collect(Collectors.toList()));
        List<String> boundSkills = new ArrayList<>();
        data.getBoundSkills().forEach(skill -> boundSkills.add(skill.getSkill().getHandler().getId()));
        data.getBoundPassiveSkills().forEach(skill -> boundSkills.add(skill.getTriggeredSkill().getHandler().getId()));
        updater.addJSONArray("bound_skills", boundSkills);
        updater.addJSONObject("skills", data.mapSkillLevels().entrySet());
        updater.addJSONObject("times_claimed", data.getItemClaims().entrySet());
        updater.addJSONObject("skill_tree_points", data.mapSkillTreePoints().entrySet());
        updater.addJSONObject("skill_tree_levels", data.getNodeLevelsEntrySet());
        updater.addData("attributes", data.getAttributes().toJsonString());
        updater.addData("professions", data.getCollectionSkills().toJsonString());
        updater.addData("quests", data.getQuestData().toJsonString());
        updater.addData("class_info", createClassInfoData(data).toString());
        if (logout)
            updater.addData("is_saved", 1);
        updater.executeRequest();

        MythicLib.debug("MMOCoreSQL", "Saved data for: " + data.getUniqueId());
        MythicLib.debug("MMOCoreSQL", String.format("{ class: %s, level: %d }", data.getProfess().getId(), data.getLevel()));
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
            classinfo.addProperty("skill-reallocation-points", info.getSkillReallocationPoints());
            classinfo.addProperty("skill-tree-reallocation-points", info.getSkillTreeReallocationPoints());

            JsonObject skillinfo = new JsonObject();
            for (String skill : info.getSkillKeys())
                skillinfo.addProperty(skill, info.getSkillLevel(skill));
            classinfo.add("skill", skillinfo);
            JsonObject attributeInfo = new JsonObject();
            for (String attribute : info.getAttributeKeys())
                attributeInfo.addProperty(attribute, info.getAttributeLevel(attribute));
            classinfo.add("attribute", attributeInfo);

            JsonObject nodeLevelsInfo = new JsonObject();
            for (String node : info.getNodeKeys())
                nodeLevelsInfo.addProperty(node, info.getNodeLevel(node));
            classinfo.add("node-levels", nodeLevelsInfo);

            JsonObject skillTreePointsInfo = new JsonObject();
            for (String skillTreeId : info.getSkillTreePointsKeys())
                skillTreePointsInfo.addProperty(skillTreeId, info.getSkillTreePoints(skillTreeId));
            classinfo.add("skill-tree-points", skillTreePointsInfo);

            json.add(c, classinfo);
        }

        return json;
    }

    private boolean isEmpty(@Nullable String str) {
        return str == null || str.equalsIgnoreCase("null") || str.equalsIgnoreCase("{}") || str.equalsIgnoreCase("[]") || str.equalsIgnoreCase("");
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
                    MythicLib.debug("MMOCoreSQL", "Loading OFFLINE data for '" + uuid + "'.");
                    if (!result.next()) {
                        level = 0;
                        lastLogin = 0;
                        profess = MMOCore.plugin.classManager.getDefaultClass();
                        friends = new ArrayList<>();
                        MythicLib.debug("MMOCoreSQL", "Default OFFLINE data loaded.");
                    } else {
                        level = result.getInt("level");
                        lastLogin = result.getLong("last_login");
                        profess = isEmpty(result.getString("class")) ? MMOCore.plugin.classManager.getDefaultClass() : MMOCore.plugin.classManager.get(result.getString("class"));
                        if (!isEmpty(result.getString("friends")))
                            MMOCoreUtils.jsonArrayToList(result.getString("friends")).forEach(str -> friends.add(UUID.fromString(str)));
                        else friends = new ArrayList<>();
                        MythicLib.debug("MMOCoreSQL", "Saved OFFLINE data loaded.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void removeFriend(UUID uuid) {
            friends.remove(uuid);
            new PlayerDataTableUpdater(provider, uuid).updateData("friends", friends.stream().map(UUID::toString).collect(Collectors.toList()));
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



