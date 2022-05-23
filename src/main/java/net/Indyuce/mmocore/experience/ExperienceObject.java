package net.Indyuce.mmocore.experience;

import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.droptable.ExperienceTable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * General implementation for professions and classes.
 * <p>
 * An experience object is a type of object that can
 * level up. It has an experience curve and table and
 * can receive EXP
 *
 * @author jules
 */
public interface ExperienceObject extends ExperienceDispenser {

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
