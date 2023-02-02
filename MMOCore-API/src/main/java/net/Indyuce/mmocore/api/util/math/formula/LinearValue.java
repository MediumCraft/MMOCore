package net.Indyuce.mmocore.api.util.math.formula;

import io.lumine.mythic.lib.MythicLib;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Bounded linear formula.
 *
 * @author Jules
 */
public class LinearValue {
    private final double base, perLevel, min, max;
    private final boolean hasMin, hasMax;

    public static final LinearValue ZERO = new LinearValue(0, 0, 0, 0);

    /**
     * A number formula which depends on the player level. It can be used
     * to handle skill modifiers so that the ability gets better with the
     * skill level, or as an attribute value to make them scale with the class level.
     *
     * @param base     Base value
     * @param perLevel Every level, final value is increased by X
     */
    public LinearValue(double base, double perLevel) {
        this.base = base;
        this.perLevel = perLevel;
        hasMin = false;
        hasMax = false;
        min = 0;
        max = 0;
    }

    /**
     * A number formula which depends on the player level. It can be used
     * to handle skill modifiers so that the ability gets better with the
     * skill level, or as an attribute value to make them scale with the class level.
     *
     * @param base     Base value
     * @param perLevel Every level, final value is increased by X
     * @param min      Minimum final value
     * @param max      Maximum final value
     */
    public LinearValue(double base, double perLevel, double min, double max) {
        this.base = base;
        this.perLevel = perLevel;
        hasMin = true;
        hasMax = true;
        this.min = min;
        this.max = max;
    }

    /**
     * Copies a linear formula
     *
     * @param value Formula to copy
     */
    public LinearValue(LinearValue value) {
        base = value.base;
        perLevel = value.perLevel;
        hasMin = value.hasMin;
        hasMax = value.hasMax;
        min = value.min;
        max = value.max;
    }

    /**
     * Loads a linear formula from a config section
     *
     * @param config Config to load the formula from
     */
    public LinearValue(ConfigurationSection config) {
        base = config.getDouble("base");
        perLevel = config.getDouble("per-level");
        hasMin = config.contains("min");
        hasMax = config.contains("max");
        min = hasMin ? config.getDouble("min") : 0;
        max = hasMax ? config.getDouble("max") : 0;
    }

    public double getBaseValue() {
        return base;
    }

    public double getPerLevel() {
        return perLevel;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public boolean hasMax() {
        return hasMax;
    }

    public boolean hasMin() {
        return hasMin;
    }

    @NotNull
    public String getDisplay(int level) {
        return MythicLib.plugin.getMMOConfig().decimals.format(calculate(level));
    }

    public double calculate(int level) {
        double value = base + perLevel * (level - 1);

        if (hasMin) value = Math.max(min, value);

        if (hasMax) value = Math.min(max, value);

        return value;
    }

    @Override
    public String toString() {
        return "LinearValue{" +
                "base=" + base +
                ", perLevel=" + perLevel +
                ", min=" + min +
                ", max=" + max +
                ", hasmin=" + hasMin +
                ", hasmax=" + hasMax +
                '}';
    }
}
