package net.Indyuce.mmocore.manager.data.mysql;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.guild.provided.Guild;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import org.apache.commons.lang.Validate;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
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
                //To prevent infinite loops
                if (System.currentTimeMillis() - startTime > 6000) {
                    cancel();
                    return;
                }

                provider.getResult("SELECT * FROM mmocore_playerdata WHERE uuid = '" + data.getUniqueId() + "';", (result) -> {
                    try {
                        if (result.next()) {

                            //If the data couldn't be loaded for more than 2 seconds its probably due to a server crash and we load the old data
                            //If it the status is is_saved we load the data
                            if (System.currentTimeMillis() - startTime > 4000 || result.getInt("is_saved") == 1) {
                                MMOCore.sqlDebug("Time waited: " + (System.currentTimeMillis() - startTime));
                                MMOCore.sqlDebug("Loading data for: '" + data.getUniqueId() + "'...");

                                // Initialize custom resources
                                data.setMana(result.getFloat("mana"));
                                data.setStellium(result.getFloat("stellium"));
                                data.setStamina(result.getFloat("stamina"));

                                data.setClassPoints(result.getInt("class_points"));
                                data.setSkillPoints(result.getInt("skill_points"));
                                data.setSkillReallocationPoints(result.getInt("skill_reallocation_points"));
                                data.setSkillTreeReallocationPoints(result.getInt("skill_tree_reallocation_points"));
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
                                if (!isEmpty(result.getString("skill_tree_points"))) {
                                    JsonObject json = new JsonParser().parse(result.getString("skill_tree_points")).getAsJsonObject();
                                    for (SkillTree skillTree : MMOCore.plugin.skillTreeManager.getAll()) {
                                        data.setSkillTreePoints(skillTree.getId(), json.has(skillTree.getId()) ? json.get(skillTree.getId()).getAsInt() : 0);
                                    }
                                    data.setSkillTreePoints("global", json.has("global") ? json.get("global").getAsInt() : 0);

                                }
                                if (!isEmpty(result.getString("skill_tree_levels"))) {
                                    JsonObject json = new JsonParser().parse(result.getString("skill_tree_levels")).getAsJsonObject();
                                    for (SkillTreeNode skillTreeNode : MMOCore.plugin.skillTreeManager.getAllNodes()) {
                                        data.setNodeLevel(skillTreeNode, json.has(skillTreeNode.getFullId()) ? json.get(skillTreeNode.getFullId()).getAsInt() : 0);
                                    }
                                }
                                data.setupSkillTree();


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
                                    JsonObject object = new Gson().fromJson(result.getString("skills"), JsonObject.class);
                                    for (Entry<String, JsonElement> entry : object.entrySet())
                                        data.setSkillLevel(entry.getKey(), entry.getValue().getAsInt());
                                }
                                if (!isEmpty(result.getString("bound_skills")))
                                    for (String id : MMOCoreUtils.jsonArrayToList(result.getString("bound_skills")))
                                        if (data.getProfess().hasSkill(id)) {
                                            ClassSkill skill = data.getProfess().getSkill(id);
                                            if (skill.getSkill().getTrigger().isPassive())
                                                data.bindPassiveSkill(-1, skill.toPassive(data));
                                            else
                                                data.getBoundSkills().add(skill);
                                        }
                                if (!isEmpty(result.getString("class_info"))) {
                                    JsonObject object = new Gson().fromJson(result.getString("class_info"), JsonObject.class);
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
                                new MySQLTableEditor(MySQLTableEditor.Table.PLAYERDATA, data.getUniqueId(), provider).updateData("is_saved", 0);

                                data.setFullyLoaded();
                                this.cancel();
                                MMOCore.sqlDebug("Loaded saved data for: '" + data.getUniqueId() + "'!");
                                MMOCore.sqlDebug(String.format("{ class: %s, level: %d }", data.getProfess().getId(), data.getLevel()));
                                return;
                            } else {
                                MMOCore.sqlDebug("Failed to load data because is_saved is false.");
                            }

                        } else {
                            data.setLevel(getDefaultData().getLevel());
                            data.setClassPoints(getDefaultData().getClassPoints());
                            data.setSkillPoints(getDefaultData().getSkillPoints());
                            data.setSkillReallocationPoints(getDefaultData().getSkillReallocationPoints());
                            data.setAttributePoints(getDefaultData().getAttributePoints());
                            data.setAttributeReallocationPoints(getDefaultData().getAttributeReallocationPoints());
                            data.setExperience(0);
                            data.getQuestData().updateBossBar();

                            //We now change the saved status to false because the data on SQL won't be the same as in the RAM
                            new MySQLTableEditor(MySQLTableEditor.Table.PLAYERDATA, data.getUniqueId(), provider).updateData("is_saved", 0);

                            data.setFullyLoaded();
                            this.cancel();
                            MMOCore.sqlDebug("Loaded DEFAULT data for: '" + data.getUniqueId() + "' as no saved data was found.");
                            return;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        cancel();
                    }
                });
            }
        };

        runnable.runTaskTimerAsynchronously(MMOCore.plugin, 0L, 20L);
    }

    @Override
    public void saveData(PlayerData data, boolean logout) {

        MySQLTableEditor sql = new MySQLTableEditor(MySQLTableEditor.Table.PLAYERDATA, data.getUniqueId(), provider);
        MMOCore.sqlDebug("Saving data for: '" + data.getUniqueId() + "'...");
        MySQLRequest request = new MySQLRequest(data.getUniqueId());
        request.addData("class_points", data.getClassPoints());
        request.addData("skill_points", data.getSkillPoints());
        request.addData("skill_reallocation_points", data.getSkillReallocationPoints());
        request.addData("attribute_points", data.getAttributePoints());
        request.addData("attribute_realloc_points", data.getAttributeReallocationPoints());
        request.addJSONArray("waypoints", data.getWaypoints());
        request.addData("skill_tree_reallocation_points", data.getSkillTreeReallocationPoints());
        request.addData("mana", data.getMana());
        request.addData("stellium", data.getStellium());
        request.addData("stamina", data.getStamina());
        request.addData("level", data.getLevel());
        request.addData("experience", data.getExperience());
        request.addData("class", data.getProfess().getId());
        request.addData("last_login", data.getLastLogin());
        request.addData("guild", data.hasGuild() ? data.getGuild().getId() : null);
        request.addJSONArray("waypoints", data.getWaypoints());
        request.addJSONArray("friends", data.getFriends().stream().map(UUID::toString).collect(Collectors.toList()));
        List<String> boundSkills = new ArrayList<>();
        data.getBoundSkills().forEach(skill -> boundSkills.add(skill.getSkill().getHandler().getId()));
        data.getBoundPassiveSkills().forEach(skill -> boundSkills.add(skill.getTriggeredSkill().getHandler().getId()));
        request.addJSONArray("bound_skills", boundSkills);
        request.addJSONObject("skills", data.mapSkillLevels().entrySet());
        request.addJSONObject("times_claimed", data.getItemClaims().entrySet());
        request.addJSONObject("skill_tree_points", data.mapSkillTreePoints().entrySet());
        request.addJSONObject("skill_tree_levels", data.getNodeLevelsEntrySet());
        request.addData("attributes", data.getAttributes().toJsonString());
        request.addData("professions", data.getCollectionSkills().toJsonString());
        request.addData("quests", data.getQuestData().toJsonString());
        request.addData("class_info", createClassInfoData(data).toString());
        if (logout)
            request.addData("is_saved", 1);
        sql.updateData(request);
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



