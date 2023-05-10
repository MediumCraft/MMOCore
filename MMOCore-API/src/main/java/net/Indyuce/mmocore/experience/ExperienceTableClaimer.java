package net.Indyuce.mmocore.experience;

import net.Indyuce.mmocore.experience.droptable.ExperienceItem;
import net.Indyuce.mmocore.experience.droptable.ExperienceTable;

/**
 * Professions and classes share the same properties because
 * they have both exp curves and tables.
 * <p>
 * A 'claimer' is an object that can claim exp tables and therefore
 * needs to save how many times it has already claimed some item
 * before.
 * <p>
 * Since MMOCore 1.9 it's all centralized in the player class data
 */
public interface ExperienceTableClaimer {

    int getClaims(ExperienceObject object, ExperienceTable table, ExperienceItem item);

    void setClaims(ExperienceObject object, ExperienceTable table, ExperienceItem item, int claims);
}
