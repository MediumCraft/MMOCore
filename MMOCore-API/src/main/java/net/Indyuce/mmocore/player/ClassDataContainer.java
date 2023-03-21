package net.Indyuce.mmocore.player;

import io.lumine.mythic.lib.player.skill.PassiveSkill;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * All the class-specific information i.e information being saved
 * in {@link SavedClassInformation} when a player changes its current
 * class.
 */
public interface ClassDataContainer {

    int getLevel();

    double getExperience();

    int getSkillPoints();

    int getAttributePoints();

    int getAttributeReallocationPoints();

    int getSkillReallocationPoints();

    int getSkillTreeReallocationPoints();

    double getHealth();

    double getMana();

    double getStamina();

    double getStellium();

    Map<String, Integer> mapAttributeLevels();

    Map<String, Integer> mapSkillLevels();

    Map<String, Integer> mapSkillTreePoints();

    Map<Integer,String> mapBoundSkills();

    Map<SkillTreeNode, Integer> getNodeLevels();

    Map<String, Integer> getNodeTimesClaimed();

    Set<String> getUnlockedItems();
}
