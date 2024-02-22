package net.Indyuce.mmocore.manager.data.sql;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.data.sql.SQLDataSource;
import io.lumine.mythic.lib.data.sql.SQLSynchronizedDataHandler;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.manager.data.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SQLDataHandler extends SQLSynchronizedDataHandler<PlayerData, OfflinePlayerData> {
    public SQLDataHandler(SQLDataSource dataSource) {
        super(dataSource);
    }

    private static final String[] NEW_COLUMNS = new String[]{
            "times_claimed", "LONGTEXT",
            "is_saved", "TINYINT",
            "skill_reallocation_points", "INT(11)",
            "skill_tree_reallocation_points", "INT(11)",
            "skill_tree_points", "LONGTEXT",
            "skill_tree_levels", "LONGTEXT",
            "unlocked_items", "LONGTEXT",
            "health", "FLOAT",
            "mana", "FLOAT",
            "stamina", "FLOAT",
            "stellium", "FLOAT"};

    @Override
    public void setup() {

        // Fully create table
        getDataSource().executeUpdateAsync("CREATE TABLE IF NOT EXISTS mmocore_playerdata(uuid VARCHAR(36)," +
                "class_points INT(11) DEFAULT 0," +
                "skill_points INT(11) DEFAULT 0," +
                "attribute_points INT(11) DEFAULT 0," +
                "attribute_realloc_points INT(11) DEFAULT 0," +
                "skill_reallocation_points INT(11) DEFAULT 0," +
                "skill_tree_reallocation_points INT(11) DEFAULT 0," +
                "skill_tree_points LONGTEXT," +
                "skill_tree_levels LONGTEXT," +
                "level INT(11) DEFAULT 1," +
                "experience INT(11) DEFAULT 0," +
                "class VARCHAR(20),guild VARCHAR(20)," +
                "last_login LONG," +
                "attributes LONGTEXT," +
                "professions LONGTEXT," +
                "times_claimed LONGTEXT," +
                "quests LONGTEXT," +
                "waypoints LONGTEXT," +
                "friends LONGTEXT," +
                "skills LONGTEXT," +
                "bound_skills LONGTEXT," +
                "health FLOAT," +
                "mana FLOAT," +
                "stamina FLOAT," +
                "stellium FLOAT," +
                "unlocked_items LONGTEXT," +
                "class_info LONGTEXT," +
                "is_saved TINYINT," +
                "PRIMARY KEY (uuid));");

        // Add columns that might not be here by default
        for (int i = 0; i < NEW_COLUMNS.length; i += 2) {
            final String columnName = NEW_COLUMNS[i];
            final String dataType = NEW_COLUMNS[i + 1];
            getDataSource().getResultAsync("SELECT * FROM information_schema.COLUMNS WHERE TABLE_NAME = 'mmocore_playerdata' AND COLUMN_NAME = '" + columnName + "'", result -> {
                try {
                    if (!result.next())
                        getDataSource().executeUpdate("ALTER TABLE mmocore_playerdata ADD COLUMN " + columnName + " " + dataType);
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            });
        }
    }

    @Override
    public MMOCoreDataSynchronizer newDataSynchronizer(PlayerData playerData) {
        return new MMOCoreDataSynchronizer(this, playerData);
    }

    @Override
    public void saveData(PlayerData data, boolean autosave) {
        final UUID effectiveId = data.getEffectiveId();
        UtilityMethods.debug(MMOCore.plugin, "SQL", "Saving data for: '" + effectiveId + "'...");

        final PlayerDataTableUpdater updater = new PlayerDataTableUpdater(getDataSource(), data);
        updater.addData("class_points", data.getClassPoints());
        updater.addData("skill_points", data.getSkillPoints());
        updater.addData("skill_reallocation_points", data.getSkillReallocationPoints());
        updater.addData("attribute_points", data.getAttributePoints());
        updater.addData("attribute_realloc_points", data.getAttributeReallocationPoints());
        updater.addData("skill_tree_reallocation_points", data.getSkillTreeReallocationPoints());
        updater.addData("health", data.getHealth());
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
        updater.addJSONObject("bound_skills", data.mapBoundSkills().entrySet());
        updater.addJSONObject("skills", data.mapSkillLevels().entrySet());
        updater.addJSONObject("times_claimed", data.getItemClaims().entrySet());
        updater.addJSONObject("skill_tree_points", data.mapSkillTreePoints().entrySet());
        updater.addJSONObject("skill_tree_levels", data.getNodeLevelsEntrySet());
        updater.addData("attributes", data.getAttributes().toJsonString());
        updater.addData("professions", data.getCollectionSkills().toJsonString());
        updater.addData("quests", data.getQuestData().toJsonString());
        updater.addData("class_info", createClassInfoData(data).toString());
        updater.addJSONArray("unlocked_items", data.getUnlockedItems());
        if (!autosave)
            updater.addData("is_saved", 1);

        updater.executeRequest(autosave);

        UtilityMethods.debug(MMOCore.plugin, "SQL", "Saved data for: " + effectiveId);
        UtilityMethods.debug(MMOCore.plugin, "SQL", String.format("{ class: %s, level: %d }", data.getProfess().getId(), data.getLevel()));
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
            classinfo.addProperty("health", info.getHealth());
            classinfo.addProperty("mana", info.getMana());
            classinfo.addProperty("stamina", info.getStamina());
            classinfo.addProperty("stellium", info.getStellium());

            JsonArray array = new JsonArray();
            for (String unlockedItem : playerData.getUnlockedItems()) {
                array.add(unlockedItem);
            }
            classinfo.add("unlocked-items", array);

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

            JsonObject boundSkillInfo = new JsonObject();
            for (int slot : info.mapBoundSkills().keySet())
                boundSkillInfo.addProperty(String.valueOf(slot), info.mapBoundSkills().get(slot));
            classinfo.add("bound-skills", boundSkillInfo);

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
        return new SQLOfflinePlayerData(uuid);
    }
}



