package net.Indyuce.mmocore.api.player.profess;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.player.ClassDataContainer;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.Map.Entry;

public class SavedClassInformation {
    private final int level, skillPoints, attributePoints, attributeReallocationPoints, skillTreeReallocationPoints, skillReallocationPoints;
    private final double experience, health, mana, stellium, stamina;
    private final Map<String, Integer> attributeLevels = new HashMap<>();
    private final Map<String, Integer> skillLevels = new HashMap<>();
    private final Map<String, Integer> skillTreePoints = new HashMap<>();
    private final Map<String, Integer> nodeLevels = new HashMap<>();
    private final Map<String, Integer> nodeTimesClaimed = new HashMap<>();
    private final Map<Integer, String> boundSkills = new HashMap<>();
    private final Set<String> unlockedItems= new HashSet<>();
    /**
     * Used by YAML storage
     */
    public SavedClassInformation(ConfigurationSection config) {
        level = config.getInt("level");
        experience = config.getDouble("experience");
        skillPoints = config.getInt("skill-points");
        attributePoints = config.getInt("attribute-points");
        attributeReallocationPoints = config.getInt("attribute-realloc-points");
        skillReallocationPoints = config.getInt("skill-reallocation-points");
        skillTreeReallocationPoints = config.getInt("skill-tree-reallocation-points");
        health = config.getDouble("health", 20);
        mana = config.getDouble("mana", 0);
        stamina = config.getDouble("stamina", 0);
        stellium = config.getDouble("stellium", 0);
        if (config.contains("attribute"))
            config.getConfigurationSection("attribute").getKeys(false)
                    .forEach(key -> attributeLevels.put(key, config.getInt("attribute." + key)));
        if (config.contains("skill"))
            config.getConfigurationSection("skill").getKeys(false)
                    .forEach(key -> skillLevels.put(key, config.getInt("skill." + key)));
        if (config.contains("skill-tree-points"))
            config.getConfigurationSection("skill-tree-points").getKeys(false)
                    .forEach(key -> skillTreePoints.put(key, config.getInt("skill-tree-points." + key)));
        if (config.contains("node-levels"))
            config.getConfigurationSection("node-levels").getKeys(false)
                    .forEach(key -> nodeLevels.put(key, config.getInt("node-levels." + key)));
        if (config.contains("node-times-claimed"))
            config.getConfigurationSection("node-times-claimed").getKeys(false)
                    .forEach(key -> nodeTimesClaimed.put(key, config.getInt("node-times-claimed." + key)));
        //Old system was using a StringList. If it saved with the old system the if condition won't be respected.
        if (config.isConfigurationSection("bound-skills"))
            config.getConfigurationSection("bound-skills").getKeys(false)
                    .forEach(key -> boundSkills.put(Integer.parseInt(key), config.getString("bound-skills." + key)));
        unlockedItems.addAll(config.getStringList("unlocked-items"));
    }

    /**
     * Used by SQL storage
     */
    public SavedClassInformation(JsonObject json) {
        level = json.get("level").getAsInt();
        experience = json.get("experience").getAsDouble();
        skillPoints = json.get("skill-points").getAsInt();
        attributePoints = json.get("attribute-points").getAsInt();
        attributeReallocationPoints = json.get("attribute-realloc-points").getAsInt();
        skillReallocationPoints = json.get("skill-reallocation-points").getAsInt();
        skillTreeReallocationPoints = json.get("skill-tree-reallocation-points").getAsInt();
        health = json.has("health") ? json.get("health").getAsDouble() : 20;
        mana = json.has("mana") ? json.get("mana").getAsDouble() : 0;
        stamina = json.has("stamina") ? json.get("stamina").getAsDouble() : 0;
        stellium = json.has("stellium") ? json.get("stellium").getAsDouble() : 0;

        if (json.has("attribute"))
            for (Entry<String, JsonElement> entry : json.getAsJsonObject("attribute").entrySet())
                attributeLevels.put(entry.getKey(), entry.getValue().getAsInt());
        if (json.has("skill"))
            for (Entry<String, JsonElement> entry : json.getAsJsonObject("skill").entrySet())
                skillLevels.put(entry.getKey(), entry.getValue().getAsInt());
        if (json.has("skill-tree-points"))
            for (Entry<String, JsonElement> entry : json.getAsJsonObject("skill-tree-points").entrySet())
                skillTreePoints.put(entry.getKey(), entry.getValue().getAsInt());
        if (json.has("node-levels"))
            for (Entry<String, JsonElement> entry : json.getAsJsonObject("node-levels").entrySet())
                nodeLevels.put(entry.getKey(), entry.getValue().getAsInt());
        if (json.has("node-times-claimed"))
            for (Entry<String, JsonElement> entry : json.getAsJsonObject("node-times-claimed").entrySet())
                nodeTimesClaimed.put(entry.getKey(), entry.getValue().getAsInt());
        //Old system was using a JsonArray. If it saved with the old system the if condition won't be respected.
        if (json.has("bound-skills") && json.get("bound-skills").isJsonObject())
            for (Entry<String, JsonElement> entry : json.getAsJsonObject("bound-skills").entrySet())
                boundSkills.put(Integer.parseInt(entry.getKey()), entry.getValue().getAsString());
        if(json.has("unlocked-items")){
            for(JsonElement unlockedItem: json.get("unlocked-items").getAsJsonArray()){
                unlockedItems.add(unlockedItem.getAsString());
            }
        }
    }

    public SavedClassInformation(ClassDataContainer data) {
        this.level = data.getLevel();
        this.skillPoints = data.getSkillPoints();
        this.attributePoints = data.getAttributePoints();
        this.attributeReallocationPoints = data.getAttributeReallocationPoints();
        this.skillTreeReallocationPoints = data.getSkillTreeReallocationPoints();
        this.skillReallocationPoints = data.getSkillReallocationPoints();
        this.experience = data.getExperience();
        this.health = data.getHealth();
        this.mana = data.getMana();
        this.stellium = data.getStellium();
        this.stamina = data.getStamina();

        data.mapAttributeLevels().forEach((key, val) -> this.attributeLevels.put(key, val));
        data.mapSkillLevels().forEach((key, val) -> skillLevels.put(key, val));
        data.mapSkillTreePoints().forEach((key, val) -> skillTreePoints.put(key, val));
        data.getNodeLevels().forEach((node, level) -> nodeLevels.put(node.getFullId(), level));
        data.getNodeTimesClaimed().forEach((key, val) -> nodeTimesClaimed.put(key, val));
        data.mapBoundSkills().forEach((slot, skill) -> boundSkills.put(slot, skill));
        data.getUnlockedItems().forEach(item->unlockedItems.add(item));
    }

    public int getLevel() {
        return level;
    }

    public double getExperience() {
        return experience;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    public int getAttributePoints() {
        return attributePoints;
    }

    public int getAttributeReallocationPoints() {
        return attributeReallocationPoints;
    }

    public double getHealth() {
        return health;
    }

    public double getMana() {
        return mana;
    }

    public double getStellium() {
        return stellium;
    }

    public double getStamina() {
        return stamina;
    }

    public Set<String> getSkillKeys() {
        return skillLevels.keySet();
    }

    public int getSkillLevel(RegisteredSkill skill) {
        return getSkillLevel(skill.getHandler().getId());
    }

    public int getSkillLevel(String id) {
        return skillLevels.get(id);
    }

    public void registerSkillLevel(RegisteredSkill skill, int level) {
        registerSkillLevel(skill.getHandler().getId(), level);
    }

    public Map<Integer, String> getBoundSkills() {
        return boundSkills;
    }

    public int getSkillTreeReallocationPoints() {
        return skillTreeReallocationPoints;
    }

    public int getSkillReallocationPoints() {
        return skillReallocationPoints;
    }

    public void registerSkillLevel(String attribute, int level) {
        skillLevels.put(attribute, level);
    }

    public Set<String> getNodeKeys() {
        return nodeLevels.keySet();
    }

    public int getNodeLevel(String node) {
        return nodeLevels.get(node);
    }

    public Set<String> getSkillTreePointsKeys() {
        return skillTreePoints.keySet();
    }

    public int getSkillTreePoints(String skillTreeId) {
        return skillTreePoints.get(skillTreeId);
    }

    public Set<String> getAttributeKeys() {
        return attributeLevels.keySet();
    }

    public int getAttributeLevel(String id) {
        return attributeLevels.getOrDefault(id, 0);
    }

    public Set<String> getUnlockedItems() {
        return unlockedItems;
    }

    public void registerAttributeLevel(PlayerAttribute attribute, int level) {
        registerAttributeLevel(attribute.getId(), level);
    }

    public void registerAttributeLevel(String attribute, int level) {
        attributeLevels.put(attribute, level);
    }

    /**
     * @param profess Target player class
     * @param player  Player changing class
     */
    public void load(PlayerClass profess, PlayerData player) {

        /*
         * Saves current class info inside a SavedClassInformation, only
         * if the class is a real class and not the default one.
         */
        if (!player.getProfess().hasOption(ClassOption.DEFAULT) || MMOCore.plugin.configManager.saveDefaultClassInfo)
            player.applyClassInfo(player.getProfess(), new SavedClassInformation(player));

        /*
         * Resets information which much be reset after everything is saved.
         */
        player.mapSkillLevels().forEach((skill, level) -> player.resetSkillLevel(skill));
        player.getAttributes().getInstances().forEach(ins -> ins.setBase(0));
        player.clearSkillTreePoints();
        player.clearNodeLevels();
        player.clearNodeStates();

        // We remove perm stats for nodes and class.
        for (SkillTree skillTree : player.getProfess().getSkillTrees())
            for (SkillTreeNode node : skillTree.getNodes())
                node.getExperienceTable().removePermStats(player, node);
        if (player.getProfess().hasExperienceTable())
            player.getProfess().getExperienceTable().removePermStats(player, player.getProfess());

        while (player.hasSkillBound(0))
            player.unbindSkill(0);
        player.clearNodeTimesClaimed();


        /*
         * Reads this class info, applies it to the player. set class after
         * changing level so the player stats can be calculated based on new
         * level.
         */
        player.setLevel(level);
        player.setExperience(experience);
        player.setSkillPoints(skillPoints);
        player.setAttributePoints(attributePoints);
        player.setAttributeReallocationPoints(attributeReallocationPoints);
        player.setSkillTreeReallocationPoints(skillTreeReallocationPoints);
        player.setSkillReallocationPoints(skillReallocationPoints);
        player.setUnlockedItems(unlockedItems);
        for (int slot : boundSkills.keySet())
            player.bindSkill(slot, profess.getSkill(boundSkills.get(slot)));


        skillLevels.forEach(player::setSkillLevel);
        attributeLevels.forEach((id, pts) -> player.getAttributes().setBaseAttribute(id, pts));

        // Careful, the global points must not be forgotten.
        player.setSkillTreePoints("global", skillTreePoints.getOrDefault("global", 0));
        for (SkillTree skillTree : profess.getSkillTrees()) {
            player.setSkillTreePoints(skillTree.getId(), skillTreePoints.getOrDefault(skillTree.getId(), 0));
            for (SkillTreeNode node : skillTree.getNodes())
                player.setNodeLevel(node, nodeLevels.getOrDefault(node.getFullId(), 0));

            skillTree.setupNodeStates(player);
        }

        // Add the values to the times claimed table and claims the corresponding stat triggers.
        nodeTimesClaimed.forEach((str, val) -> player.setClaims(str, val));

        // We claim back the stats triggers for all the skill tree nodes of the new class.
        for (SkillTree skillTree : profess.getSkillTrees())
            for (SkillTreeNode node : skillTree.getNodes())
                node.getExperienceTable().claimStatTriggers(player, node);
        profess.getExperienceTable().claimStatTriggers(player, profess);

        /*
         * Unload current class information and set
         * the new profess once everything is changed
         */
        player.setClass(profess);
        player.unloadClassInfo(profess);


        //These should be loaded after to make sure that the MAX_MANA, MAX_STAMINA & MAX_STELLIUM stats are already loaded.
        player.setMana(mana);
        player.setStellium(stellium);
        player.setStamina(stamina);
        double health=this.health;
        health = health == 0 ? 20 : health;
        player.getPlayer().setHealth(Math.min(health,player.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
        // Updates level on exp bar
        player.refreshVanillaExp();
    }
}

