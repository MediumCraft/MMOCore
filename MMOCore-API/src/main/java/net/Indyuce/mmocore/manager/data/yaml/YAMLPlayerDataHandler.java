package net.Indyuce.mmocore.manager.data.yaml;

import io.lumine.mythic.lib.data.yaml.YAMLSynchronizedDataHandler;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.guild.provided.Guild;
import net.Indyuce.mmocore.manager.data.OfflinePlayerData;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import org.apache.commons.lang.Validate;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class YAMLPlayerDataHandler extends YAMLSynchronizedDataHandler<PlayerData, OfflinePlayerData> {
    public YAMLPlayerDataHandler(Plugin owning) {
        super(owning);
    }

    @Override
    public void setup() {

    }

    @Override
    public void close() {

    }

    @Override
    public void loadFromSection(PlayerData data, ConfigurationSection config) {

        // Reset stats linked to triggers.
        data.resetTriggerStats();

        // Load default data
        if (!config.contains("class-points")) {
            MMOCore.plugin.playerDataManager.getDefaultData().apply(data);
            return;
        }

        data.setClassPoints(config.getInt("class-points"));
        data.setSkillPoints(config.getInt("skill-points"));
        data.setSkillReallocationPoints(config.getInt("skill-reallocation-points"));
        data.setSkillTreeReallocationPoints(config.getInt("skill-tree-reallocation-points"));
        data.setAttributePoints(config.getInt("attribute-points"));
        data.setAttributeReallocationPoints(config.getInt("attribute-realloc-points"));
        data.setLevel(config.getInt("level"));
        data.setExperience(config.getInt("experience"));
        if (config.contains("class")) data.setClass(MMOCore.plugin.classManager.get(config.getString("class")));

        if (config.contains("guild")) {
            Guild guild = MMOCore.plugin.nativeGuildManager.getGuild(config.getString("guild"));
            data.setGuild(guild.hasMember(data.getUniqueId()) ? guild : null);
        }
        if (config.contains("attribute"))
            data.getAttributes().load(config.getConfigurationSection("attribute"));
        if (data.isOnline())
            MMOCore.plugin.attributeManager.getAll().forEach(attribute -> data.getAttributes().getInstance(attribute).updateStats());
        if (config.contains("profession"))
            data.getCollectionSkills().load(config.getConfigurationSection("profession"));
        if (config.contains("quest"))
            data.getQuestData().load(config.getConfigurationSection("quest"));
        data.getQuestData().updateBossBar();
        if (config.contains("waypoints"))
            data.getWaypoints().addAll(config.getStringList("waypoints"));
        if (config.contains("friends"))
            config.getStringList("friends").forEach(str -> data.getFriends().add(UUID.fromString(str)));
        if (config.contains("skill"))
            config.getConfigurationSection("skill").getKeys(false).forEach(id -> data.setSkillLevel(id, config.getInt("skill." + id)));
        if (config.isConfigurationSection("bound-skills"))
            for (String key : config.getConfigurationSection("bound-skills").getKeys(false)) {
                ClassSkill skill = data.getProfess().getSkill(config.getString("bound-skills." + key));
                if (skill != null)
                    data.bindSkill(Integer.parseInt(key), skill);
            }

        for (String key : MMOCore.plugin.skillTreeManager.getAll().
                stream().
                map(skillTree -> skillTree.getId()).
                toList()) {
            data.setSkillTreePoints(key, config.getInt("skill-tree-points." + key, 0));
        }
        data.setSkillTreePoints("global", config.getInt("skill-tree-points.global", 0));

        if (config.contains("times-claimed"))
            for (String key : config.getConfigurationSection("times-claimed").getKeys(false)) {
                ConfigurationSection section = config.getConfigurationSection("times-claimed." + key);
                if (section != null)
                    for (String key1 : section.getKeys(false)) {
                        ConfigurationSection section1 = section.getConfigurationSection(key1);
                        if (section1 != null)
                            for (String key2 : config.getConfigurationSection("times-claimed." + key + "." + key1).getKeys(false)) {
                                data.getItemClaims().put(key + "." + key1 + "." + key2, config.getInt("times-claimed." + key + "." + key1 + "." + key2));

                            }
                    }
            }

        data.setUnlockedItems(config.getStringList("unlocked-items").stream().collect(Collectors.toSet()));
        for (SkillTreeNode node : MMOCore.plugin.skillTreeManager.getAllNodes()) {
            data.setNodeLevel(node, config.getInt("skill-tree-level." + node.getFullId(), 0));
        }

        // Load class slots, use try so the player can log in.
        if (config.contains("class-info"))
            for (String key : config.getConfigurationSection("class-info").getKeys(false))
                try {
                    PlayerClass profess = MMOCore.plugin.classManager.get(key);
                    Validate.notNull(profess, "Could not find class '" + key + "'");
                    data.applyClassInfo(profess, new SavedClassInformation(config.getConfigurationSection("class-info." + key)));
                } catch (IllegalArgumentException exception) {
                    MMOCore.log(Level.WARNING, "Could not load class info '" + key + "': " + exception.getMessage());
                }

        /*
         * These should be loaded after to make sure that the
         * MAX_MANA, MAX_STAMINA & MAX_STELLIUM stats are already loaded.
         */
        data.setHealth(config.getDouble("health"));
        data.setMana(config.contains("mana") ? config.getDouble("mana") : data.getStats().getStat("MAX_MANA"));
        data.setStamina(config.contains("stamina") ? config.getDouble("stamina") : data.getStats().getStat("MAX_STAMINA"));
        data.setStellium(config.contains("stellium") ? config.getDouble("stellium") : data.getStats().getStat("MAX_STELLIUM"));
    }

    @Override
    public void saveInSection(PlayerData data, ConfigurationSection config) {

        config.set("class-points", data.getClassPoints());
        config.set("skill-points", data.getSkillPoints());
        config.set("skill-reallocation-points", data.getSkillReallocationPoints());
        config.set("attribute-points", data.getAttributePoints());
        // config.set("skill-realloc-points", skillReallocationPoints);
        config.set("attribute-realloc-points", data.getAttributeReallocationPoints());
        config.set("level", data.getLevel());
        config.set("experience", data.getExperience());
        config.set("class", data.getProfess().getId());
        config.set("waypoints", new ArrayList<>(data.getWaypoints()));
        config.set("friends", data.getFriends().stream().map(UUID::toString).collect(Collectors.toList()));
        config.set("last-login", data.getLastLogin());
        config.set("guild", data.hasGuild() ? data.getGuild().getId() : null);
        data.mapSkillTreePoints().forEach((key1, value) -> config.set("skill-tree-points." + key1, value));
        config.set("skill-tree-reallocation-points", data.getSkillTreeReallocationPoints());
        config.set("skill", null);
        config.set("health", data.getHealth());
        config.set("mana", data.getMana());
        config.set("stellium", data.getStellium());
        config.set("stamina", data.getStamina());
        // Saves the nodes levels
        MMOCore.plugin.skillTreeManager.getAllNodes().forEach(node -> config.set("skill-tree-level." + node.getFullId(), data.getNodeLevel(node)));
        data.mapSkillLevels().forEach((key1, value) -> config.set("skill." + key1, value));
        data.getItemClaims().forEach((key, times) -> config.set("times-claimed." + key, times));

        config.set("bound-skills", null);
        data.mapBoundSkills().forEach((slot, skill) -> config.set("bound-skills." + slot, skill));
        config.set("unlocked-items", data.getUnlockedItems().stream().collect(Collectors.toList()));
        config.set("attribute", null);
        config.createSection("attribute");
        data.getAttributes().save(config.getConfigurationSection("attribute"));

        config.set("profession", null);
        config.createSection("profession");
        data.getCollectionSkills().save(config.getConfigurationSection("profession"));

        config.set("quest", null);
        config.createSection("quest");
        data.getQuestData().save(config.getConfigurationSection("quest"));

        config.set("class-info", null);
        for (String key : data.getSavedClasses()) {
            SavedClassInformation info = data.getClassInfo(key);
            config.set("class-info." + key + ".level", info.getLevel());
            config.set("class-info." + key + ".experience", info.getExperience());
            config.set("class-info." + key + ".skill-points", info.getSkillPoints());
            config.set("class-info." + key + ".attribute-points", info.getAttributePoints());
            config.set("class-info." + key + ".attribute-realloc-points", info.getAttributeReallocationPoints());
            config.set("class-info." + key + ".skill-tree-reallocation-points", info.getSkillTreeReallocationPoints());
            config.set("class-info." + key + ".skill-reallocation-points", info.getSkillReallocationPoints());
            config.set("class-info." + key + ".health", info.getHealth());
            config.set("class-info." + key + ".mana", info.getMana());
            config.set("class-info." + key + ".stamina", info.getStamina());
            config.set("class-info." + key + ".stellium", info.getStellium());
            info.getSkillKeys().forEach(skill -> config.set("class-info." + key + ".skill." + skill, info.getSkillLevel(skill)));
            info.getAttributeKeys().forEach(attribute -> config.set("class-info." + key + ".attribute." + attribute, info.getAttributeLevel(attribute)));
            info.getNodeKeys().forEach(node -> config.set("class-info." + key + ".node-levels." + node, info.getNodeLevel(node)));
            info.getSkillTreePointsKeys().forEach(skillTreeId -> config.set("class-info." + key + ".skill-tree-points." + skillTreeId, info.getAttributeLevel(skillTreeId)));
            info.mapBoundSkills().forEach((slot, skill) -> config.set("class-info." + key + ".bound-skills." + slot, skill));
            config.set("class-info." + key + ".unlocked-items", new ArrayList<>(info.getUnlockedItems()));
        }
    }

    @NotNull
    @Override
    public OfflinePlayerData getOffline(UUID uuid) {
        return new YAMLOfflinePlayerData(uuid);
    }
}


