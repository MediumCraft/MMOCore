package net.Indyuce.mmocore.player;

import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;

import java.util.Map;
import java.util.Set;

/**
 * All the class-specific information i.e information being saved
 * in {@link SavedClassInformation} when a player changes its current class.
 *
 * TODO move {@link SavedClassInformation} method to ClassDataContainer
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

    Map<String, Integer> getNodeLevels();

    Map<String, Integer> getNodeTimesClaimed();

    Set<String> getUnlockedItems();
}
