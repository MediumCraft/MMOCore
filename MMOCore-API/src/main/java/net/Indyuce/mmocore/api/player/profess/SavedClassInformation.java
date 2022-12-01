package net.Indyuce.mmocore.api.player.profess;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.tree.SkillTreeNode;
import net.Indyuce.mmocore.tree.skilltree.SkillTree;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class SavedClassInformation {
    private final int level, skillPoints, attributePoints, attributeReallocationPoints, skillTreeReallocationPoints, skillReallocationPoints;
    private final double experience;
    private final Map<String, Integer> attributes;
    private final Map<String, Integer> skills;
    private final Map<String, Integer> skillTreePoints;
    private final Map<SkillTreeNode, Integer> nodeLevels;
    /**
     * Stores the tableItemsClaims values but only for skill tree node as it is class based.
     */
    private final Map<String, Integer> nodeTimesClaimed;
    List<String> boundSkills = new ArrayList<>();

    public SavedClassInformation(ConfigurationSection config) {
        level = config.getInt("level");
        experience = config.getDouble("experience");
        skillPoints = config.getInt("skill-points");
        attributePoints = config.getInt("attribute-points");
        attributeReallocationPoints = config.getInt("attribute-realloc-points");
        skillReallocationPoints = config.getInt("skill-reallocation-points");
        skillTreeReallocationPoints = config.getInt("skill-tree-reallocation-points");
        attributes = new HashMap<>();
        if (config.contains("attribute"))
            config.getConfigurationSection("attribute").getKeys(false).forEach(key -> attributes.put(key, config.getInt("attribute." + key)));
        skills = new HashMap<>();
        if (config.contains("skill"))
            config.getConfigurationSection("skill").getKeys(false).forEach(key -> skills.put(key, config.getInt("skill." + key)));
        skillTreePoints = new HashMap<>();
        if (config.contains("skill-tree-points"))
            config.getConfigurationSection("skill-tree-points").getKeys(false).forEach(key -> skillTreePoints.put(key, config.getInt("skill-tree-points." + key)));
        nodeLevels = new HashMap<>();
        if (config.contains("node-levels"))
            config.getConfigurationSection("node-levels").getKeys(false).forEach(key -> nodeLevels.put(MMOCore.plugin.skillTreeManager.getNode(key), config.getInt("node-levels." + key)));
        nodeTimesClaimed = new HashMap<>();
        if (config.contains("node-times-claimed"))
            config.getConfigurationSection("node-times-claimed").getKeys(false).forEach(key -> nodeTimesClaimed.put(key, config.getInt("node-times-claimed." + key)));
        if (config.contains("bound-skills")) {
            config.getStringList("bound-skills").forEach(id -> boundSkills.add(id));

        }
    }

    public SavedClassInformation(JsonObject json) {
        level = json.get("level").getAsInt();
        experience = json.get("experience").getAsDouble();
        skillPoints = json.get("skill-points").getAsInt();
        attributePoints = json.get("attribute-points").getAsInt();
        attributeReallocationPoints = json.get("attribute-realloc-points").getAsInt();
        skillReallocationPoints = json.get("skill-reallocation-points").getAsInt();
        skillTreeReallocationPoints = json.get("skill-tree-reallocation-points").getAsInt();
        attributes = new HashMap<>();
        if (json.has("attribute"))
            for (Entry<String, JsonElement> entry : json.getAsJsonObject("attribute").entrySet())
                attributes.put(entry.getKey(), entry.getValue().getAsInt());
        skills = new HashMap<>();
        if (json.has("skill"))
            for (Entry<String, JsonElement> entry : json.getAsJsonObject("skill").entrySet())
                skills.put(entry.getKey(), entry.getValue().getAsInt());
        skillTreePoints = new HashMap<>();
        if (json.has("skill-tree-points"))
            for (Entry<String, JsonElement> entry : json.getAsJsonObject("skill-tree-points").entrySet())
                skillTreePoints.put(entry.getKey(), entry.getValue().getAsInt());
        nodeLevels = new HashMap<>();
        if (json.has("node-levels"))
            for (Entry<String, JsonElement> entry : json.getAsJsonObject("node-levels").entrySet())
                nodeLevels.put(MMOCore.plugin.skillTreeManager.getNode(entry.getKey()), entry.getValue().getAsInt());
        nodeTimesClaimed = new HashMap<>();
        if (json.has("node-times-claimed"))
            for (Entry<String, JsonElement> entry : json.getAsJsonObject("node-times-claimed").entrySet())
                nodeTimesClaimed.put(entry.getKey(), entry.getValue().getAsInt());
        if (json.has("bound-skills"))
            json.getAsJsonArray("bound-skills").forEach(id -> boundSkills.add(id.getAsString()));

    }

    public SavedClassInformation(PlayerData player) {

        this(player.getLevel(), player.getExperience(), player.getSkillPoints(), player.getAttributePoints(), player.getAttributeReallocationPoints()
                , player.getSkillTreeReallocationPoints(), player.getSkillReallocationPoints(),
                player.getAttributes().mapPoints(), player.mapSkillLevels(), player.getSkillTreePoints(), player.getNodeLevels(), player.getNodeTimesClaimed(), player.getBoundSkills(),player.getBoundPassiveSkills());
    }

    public SavedClassInformation(PlayerDataManager.DefaultPlayerData data) {
        this(data.getLevel(), 0, data.getSkillPoints(), data.getAttributePoints(), data.getAttrReallocPoints(), data.getSkillTreeReallocPoints(), data.getSkillReallocPoints());
    }

    public SavedClassInformation(int level, double experience, int skillPoints, int attributePoints,
                                 int attributeReallocationPoints, int skillTreeReallocationPoints, int skillReallocationPoints) {
        this(level, experience, skillPoints, attributePoints, attributeReallocationPoints, skillTreeReallocationPoints, skillReallocationPoints, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(),new ArrayList<>(),new ArrayList<>());
    }

    public SavedClassInformation(int level, double experience, int skillPoints, int attributePoints,
                                 int attributeReallocationPoints, int skillTreeReallocationPoints, int skillReallocationPoints,
                                 Map<String, Integer> attributes, Map<
            String, Integer> skills, Map<String, Integer> skillTreePoints, Map<SkillTreeNode, Integer> nodeLevels, Map<String, Integer> nodeTimesClaimed, List<ClassSkill> boundActiveSkills, List<PassiveSkill> boundPassiveSkills) {
        List<String> boundSkills = new ArrayList<>();
        boundActiveSkills.forEach(skill -> boundSkills.add(skill.getSkill().getHandler().getId()));
        boundPassiveSkills.forEach(skill -> boundSkills.add(skill.getTriggeredSkill().getHandler().getId()));
        this.level = level;
        this.skillPoints = skillPoints;
        this.attributePoints = attributePoints;
        this.attributeReallocationPoints = attributeReallocationPoints;
        this.skillTreeReallocationPoints = skillTreeReallocationPoints;
        this.skillReallocationPoints = skillReallocationPoints;
        this.experience = experience;
        this.attributes = attributes;
        this.skills = skills;
        this.skillTreePoints = skillTreePoints;
        this.nodeLevels = nodeLevels;
        this.nodeTimesClaimed = nodeTimesClaimed;
        this.boundSkills=boundSkills;
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

    public Set<String> getSkillKeys() {
        return skills.keySet();
    }

    public int getSkillLevel(RegisteredSkill skill) {
        return getSkillLevel(skill.getHandler().getId());
    }

    public int getSkillLevel(String id) {
        return skills.get(id);
    }

    public void registerSkillLevel(RegisteredSkill skill, int level) {
        registerSkillLevel(skill.getHandler().getId(), level);
    }

    public int getSkillTreeReallocationPoints() {
        return skillTreeReallocationPoints;
    }

    public int getSkillReallocationPoints() {
        return skillReallocationPoints;
    }

    public void registerSkillLevel(String attribute, int level) {
        skills.put(attribute, level);
    }


    public Set<SkillTreeNode> getNodeKeys() {
        return nodeLevels.keySet();
    }

    public int getNodeLevel(SkillTreeNode node) {
        return nodeLevels.get(node);
    }


    public Set<String> getSkillTreePointsKeys() {
        return skillTreePoints.keySet();
    }

    public int getSkillTreePoints(String skillTreeId) {
        return skillTreePoints.get(skillTreeId);
    }

    public Set<String> getAttributeKeys() {
        return attributes.keySet();
    }

    public int getAttributeLevel(String id) {
        return attributes.getOrDefault(id, 0);
    }

    public void registerAttributeLevel(PlayerAttribute attribute, int level) {
        registerAttributeLevel(attribute.getId(), level);
    }

    public void registerAttributeLevel(String attribute, int level) {
        attributes.put(attribute, level);
    }

    public void load(PlayerClass profess, PlayerData player) {

        /*
         * saves current class info inside a SavedClassInformation, only if the
         * class is a real class and not the default one.
         */
        if (!player.getProfess().hasOption(ClassOption.DEFAULT) || MMOCore.plugin.configManager.saveDefaultClassInfo)
            player.applyClassInfo(player.getProfess(), new SavedClassInformation(player));

        /*
         * resets information which much be reset after everything is saved.
         */

        player.mapSkillLevels().forEach((skill, level) -> player.resetSkillLevel(skill));
        player.getAttributes().getInstances().forEach(ins -> ins.setBase(0));
        player.clearSkillTreePoints();
        player.clearNodeLevels();
        player.clearNodeStates();
        player.clearPointsSpent();
        // We reset the experience table for each skill tree node to remove the perm stat.
        for (SkillTree skillTree : player.getProfess().getSkillTrees()) {
            for (SkillTreeNode node : skillTree.getNodes())
                node.getExperienceTable().reset(player, node);
        }

        while (player.hasPassiveSkillBound(0))
            player.unbindPassiveSkill(0);

        while (player.hasSkillBound(0))
            player.unbindSkill(0);

        /*
         * reads this class info, applies it to the player. set class after
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
        for (String id : boundSkills) {
            ClassSkill skill = profess.getSkill(id);
            if (skill.getSkill().getTrigger().isPassive())
                player.bindPassiveSkill(-1, skill.toPassive(player));
            else
                player.getBoundSkills().add(skill);
        }

        (skills).forEach(player::setSkillLevel);
        attributes.forEach((id, pts) -> player.getAttributes().setBaseAttribute(id, pts));

        //Careful, the global points must not be forgotten.
        player.setSkillTreePoints("global", skillTreePoints.getOrDefault("global", 0));
        for (SkillTree skillTree : profess.getSkillTrees()) {
            player.setSkillTreePoints(skillTree.getId(), skillTreePoints.getOrDefault(skillTree.getId(), 0));
            for (SkillTreeNode node : skillTree.getNodes()) {
                player.setNodeLevel(node, nodeLevels.getOrDefault(node, 0));
            }

            skillTree.setupNodeState(player);
        }

        //Add the values to the times claimed table and claims the corresponding stat triggers.
        nodeTimesClaimed.forEach((str, val) -> player.setClaims(str, val));
        //We claim back the stats triggers for all the skill tree nodes of the new class.
        for (SkillTree skillTree : profess.getSkillTrees())
            for (SkillTreeNode node : skillTree.getNodes())
                node.getExperienceTable().claimStatTriggers(player, node);

        /*
         * unload current class information and set the new profess once
         * everything is changed
         */
        player.setClass(profess);
        player.unloadClassInfo(profess);

        // Updates level on exp bar
        player.refreshVanillaExp();
    }
}

