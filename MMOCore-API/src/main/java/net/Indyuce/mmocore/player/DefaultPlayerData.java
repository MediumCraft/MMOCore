package net.Indyuce.mmocore.player;

import io.lumine.mythic.lib.player.skill.PassiveSkill;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultPlayerData implements ClassDataContainer {
    private final int level, classPoints, skillPoints, attributePoints, attrReallocPoints, skillReallocPoints, skillTreeReallocPoints;

    public static final DefaultPlayerData DEFAULT = new DefaultPlayerData(1, 0, 0, 0, 0, 0, 0);

    public DefaultPlayerData(ConfigurationSection config) {
        level = config.getInt("level", 1);
        classPoints = config.getInt("class-points");
        skillPoints = config.getInt("skill-points");
        attributePoints = config.getInt("attribute-points");
        attrReallocPoints = config.getInt("attribute-realloc-points");
        skillReallocPoints = config.getInt("skill-realloc-points", 0);
        skillTreeReallocPoints = config.getInt("skill-tree-realloc-points", 0);
    }

    public DefaultPlayerData(int level, int classPoints, int skillPoints, int attributePoints, int attrReallocPoints, int skillReallocPoints, int skillTreeReallocPoints) {
        this.level = level;
        this.classPoints = classPoints;
        this.skillPoints = skillPoints;
        this.attributePoints = attributePoints;
        this.attrReallocPoints = attrReallocPoints;
        this.skillReallocPoints = skillReallocPoints;
        this.skillTreeReallocPoints = skillTreeReallocPoints;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public double getExperience() {
        return 0;
    }

    @Override
    public int getSkillPoints() {
        return skillPoints;
    }

    public int getClassPoints() {
        return classPoints;
    }

    @Override
    public int getAttributePoints() {
        return attributePoints;
    }

    @Override
    public int getAttributeReallocationPoints() {
        return attrReallocPoints;
    }

    @Override
    public int getSkillReallocationPoints() {
        return skillReallocPoints;
    }

    @Override
    public int getSkillTreeReallocationPoints() {
        return skillTreeReallocPoints;
    }

    @Override
    public Map<String, Integer> mapSkillLevels() {
        return new HashMap<>();
    }

    @Override
    public Map<String, Integer> mapSkillTreePoints() {
        return new HashMap<>();
    }

    @Override
    public Map<SkillTreeNode, Integer> getNodeLevels() {
        return new HashMap<>();
    }

    @Override
    public Map<String, Integer> getNodeTimesClaimed() {
        return new HashMap<>();
    }

    @Override
    public Map<String, Integer> mapAttributeLevels() {
        return new HashMap<>();
    }

    @Override
    public List<ClassSkill> getBoundSkills() {
        return new ArrayList<>();
    }

    @Override
    public List<PassiveSkill> getBoundPassiveSkills() {
        return new ArrayList<>();
    }

    public void apply(PlayerData player) {
        player.setLevel(level);
        player.setClassPoints(classPoints);
        player.setSkillPoints(skillPoints);
        player.setAttributePoints(attributePoints);
        player.setAttributeReallocationPoints(attrReallocPoints);
        player.setSkillTreeReallocationPoints(skillTreeReallocPoints);
        player.setSkillReallocationPoints(skillReallocPoints);
    }
}
