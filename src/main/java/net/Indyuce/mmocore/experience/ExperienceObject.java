package net.Indyuce.mmocore.experience;

import net.Indyuce.mmocore.experience.droptable.ExperienceTable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Either a profession or a class
 *
 * @author jules
 */
public interface ExperienceObject {

    String getKey();

    /**
     * Indicates the amount of exp required to level up
     *
     * @return Exp curve of that object
     */
    @Nullable
    ExpCurve getExpCurve();

    /**
     * @return Table read when leveling up
     */
    @NotNull
    ExperienceTable getExperienceTable();

    boolean hasExperienceTable();
}
