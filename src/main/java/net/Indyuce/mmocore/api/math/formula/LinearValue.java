package net.Indyuce.mmocore.api.math.formula;

import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.MMOCore;

public class LinearValue {
	private final double base, perLevel, min, max;
	private final boolean hasmin, hasmax;


	/*
	 * a number which depends on the player level. it can be used as a skill
	 * modifier to make the ability better depending on the player level or as
	 * an attrribute value to make attributes increase with the player level
	 */
	public LinearValue(double base, double perLevel) {
		this.base = base;
		this.perLevel = perLevel;
		hasmin = false;
		hasmax = false;
		min = 0;
		max = 0;
	}

	public LinearValue(double base, double perLevel, double min, double max) {
		this.base = base;
		this.perLevel = perLevel;
		hasmin = true;
		hasmax = true;
		this.min = min;
		this.max = max;
	}

	public LinearValue(LinearValue value) {
		base = value.base;
		perLevel = value.perLevel;
		hasmin = value.hasmin;
		hasmax = value.hasmax;
		min = value.min;
		max = value.max;
	}

	public LinearValue(ConfigurationSection config) {
		base = config.getDouble("base");
		perLevel = config.getDouble("per-level");
		hasmin = config.contains("min");
		hasmax = config.contains("max");
		min = hasmin ? config.getDouble("min") : 0;
		max = hasmax ? config.getDouble("max") : 0;
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
		return hasmax;
	}

	public boolean hasMin() {
		return hasmin;
	}

	public String getDisplay(int level) {
		return MMOCore.plugin.configManager.decimals.format(calculate(level));
	}

	public double calculate(int level) {
		double value = base + perLevel * (level - 1);

		if (hasmin)
			value = Math.max(min, value);

		if (hasmax)
			value = Math.min(max, value);

		return value;
	}
}
