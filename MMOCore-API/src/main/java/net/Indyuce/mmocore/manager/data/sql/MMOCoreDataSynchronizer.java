package net.Indyuce.mmocore.manager.data.sql;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.data.sql.SQLDataSynchronizer;
import io.lumine.mythic.lib.gson.JsonArray;
import io.lumine.mythic.lib.gson.JsonElement;
import io.lumine.mythic.lib.gson.JsonObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.guild.provided.Guild;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class MMOCoreDataSynchronizer extends SQLDataSynchronizer<PlayerData> {
    public MMOCoreDataSynchronizer(SQLDataHandler handler, PlayerData data) {
        super("mmocore_playerdata", "uuid", handler.getDataSource(), data);
    }

    @Override
    public void loadData(ResultSet result) throws SQLException {

        // Reset stats linked to triggers
        getData().resetTriggerStats();

        getData().setClassPoints(result.getInt("class_points"));
        getData().setSkillPoints(result.getInt("skill_points"));
        getData().setSkillReallocationPoints(result.getInt("skill_reallocation_points"));
        getData().setSkillTreeReallocationPoints(result.getInt("skill_tree_reallocation_points"));
        getData().setAttributePoints(result.getInt("attribute_points"));
        getData().setAttributeReallocationPoints(result.getInt("attribute_realloc_points"));
        getData().setLevel(result.getInt("level"));
        getData().setExperience(result.getInt("experience"));

        if (!isEmpty(result.getString("class")))
            getData().setClass(MMOCore.plugin.classManager.get(result.getString("class")));

        if (!isEmpty(result.getString("times_claimed"))) {
            JsonObject json = MythicLib.plugin.getGson().fromJson(result.getString("times_claimed"), JsonObject.class);
            json.entrySet().forEach(entry -> getData().getItemClaims().put(entry.getKey(), entry.getValue().getAsInt()));
        }
        if (!isEmpty(result.getString("skill_tree_points"))) {
            JsonObject json = MythicLib.plugin.getGson().fromJson(result.getString("skill_tree_points"), JsonObject.class);
            for (SkillTree skillTree : MMOCore.plugin.skillTreeManager.getAll()) {
                getData().setSkillTreePoints(skillTree.getId(), json.has(skillTree.getId()) ? json.get(skillTree.getId()).getAsInt() : 0);
            }
            getData().setSkillTreePoints("global", json.has("global") ? json.get("global").getAsInt() : 0);
        }

        if (!isEmpty(result.getString("skill_tree_levels"))) {
            JsonObject json = MythicLib.plugin.getGson().fromJson(result.getString("skill_tree_levels"), JsonObject.class);
            for (SkillTreeNode skillTreeNode : MMOCore.plugin.skillTreeManager.getAllNodes()) {
                getData().setNodeLevel(skillTreeNode, json.has(skillTreeNode.getFullId()) ? json.get(skillTreeNode.getFullId()).getAsInt() : 0);
            }
        }
        Set<String> unlockedItems = new HashSet<>();
        if (!isEmpty(result.getString("unlocked_items"))) {
            JsonArray unlockedItemsArray = MythicLib.plugin.getGson().fromJson(result.getString("unlocked_items"), JsonArray.class);
            for (JsonElement item : unlockedItemsArray)
                unlockedItems.add(item.getAsString());
        }
        getData().setUnlockedItems(unlockedItems);
        if (!isEmpty(result.getString("guild"))) {
            final Guild guild = MMOCore.plugin.dataProvider.getGuildManager().getGuild(result.getString("guild"));
            if (guild != null) getData().setGuild(guild.hasMember(getData().getUniqueId()) ? guild : null);
        }
        if (!isEmpty(result.getString("attributes"))) getData().getAttributes().load(result.getString("attributes"));
        if (getData().isOnline())
            MMOCore.plugin.attributeManager.getAll().forEach(attribute -> getData().getAttributes().getInstance(attribute).updateStats());
        if (!isEmpty(result.getString("professions")))
            getData().getCollectionSkills().load(result.getString("professions"));
        if (!isEmpty(result.getString("quests"))) getData().getQuestData().load(result.getString("quests"));
        getData().getQuestData().updateBossBar();
        if (!isEmpty(result.getString("waypoints")))
            getData().getWaypoints().addAll(MMOCoreUtils.jsonArrayToList(result.getString("waypoints")));
        if (!isEmpty(result.getString("friends")))
            MMOCoreUtils.jsonArrayToList(result.getString("friends")).forEach(str -> getData().getFriends().add(UUID.fromString(str)));
        if (!isEmpty(result.getString("skills"))) {
            JsonObject object = MythicLib.plugin.getGson().fromJson(result.getString("skills"), JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : object.entrySet())
                getData().setSkillLevel(entry.getKey(), entry.getValue().getAsInt());
        }
        if (!isEmpty(result.getString("bound_skills"))) {
            JsonObject object = MythicLib.plugin.getGson().fromJson(result.getString("bound_skills"), JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                ClassSkill skill = getData().getProfess().getSkill(entry.getValue().getAsString());
                if (skill != null) getData().bindSkill(Integer.parseInt(entry.getKey()), skill);
            }
        }
        if (!isEmpty(result.getString("class_info"))) {
            JsonObject object = MythicLib.plugin.getGson().fromJson(result.getString("class_info"), JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                try {
                    PlayerClass profess = MMOCore.plugin.classManager.get(entry.getKey());
                    Validate.notNull(profess, "Could not find class '" + entry.getKey() + "'");
                    getData().applyClassInfo(profess, new SavedClassInformation(entry.getValue().getAsJsonObject()));
                } catch (IllegalArgumentException exception) {
                    MMOCore.log(Level.WARNING, "Could not load class info '" + entry.getKey() + "': " + exception.getMessage());
                }
            }
        }

        /*
         * These should be loaded after to make sure that the
         * MAX_MANA, MAX_STAMINA & MAX_STELLIUM stats are already loaded.
         */
        getData().setHealth(result.getDouble("health"));
        getData().setMana(result.getDouble("mana"));
        getData().setStamina(result.getDouble("stamina"));
        getData().setStellium(result.getDouble("stellium"));

        UtilityMethods.debug(MMOCore.plugin, "SQL", String.format("{ class: %s, level: %d }", getData().getProfess().getId(), getData().getLevel()));
    }

    private boolean isEmpty(@Nullable String str) {
        return str == null || str.equalsIgnoreCase("null") || str.equalsIgnoreCase("{}") || str.equalsIgnoreCase("[]") || str.equalsIgnoreCase("");
    }

    @Override
    public void loadEmptyData() {
        MMOCore.plugin.playerDataManager.getDefaultData().apply(getData());
        UtilityMethods.debug(MMOCore.plugin, "SQL", "Loaded DEFAULT data for: '" + getData().getUniqueId() + "' as no saved data was found.");
    }
}
