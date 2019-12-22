package net.Indyuce.mmocore.api.player.stats.stat.modifier;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmocore.MMOCore;

public class StatModifier {
	private final double d;
	private final boolean relative;

	public StatModifier(double d) {
		this(d, false);
	}

	public StatModifier(double d, boolean relative) {
		this.d = d;
		this.relative = relative;
	}

	public StatModifier(String str) {
		Validate.notNull(str, "String cannot be null");
		Validate.notEmpty(str, "String cannot be empty");

		relative = str.toCharArray()[str.length() - 1] == '%';
		d = Double.parseDouble(relative ? str.substring(0, str.length() - 1) : str);
	}

	public StatModifier multiply(int coef) {
		return new StatModifier(d * coef, relative);
	}

	public boolean isRelative() {
		return relative;
	}
	
	public double getValue() {
		return d;
	}

	public double apply(double in) {
		return relative ? in * (1 + d / 100) : in + d;
	}

	public void close() {
	}

	@Override
	public String toString() {
		return MMOCore.plugin.configManager.decimal.format(d) + (relative ? "%" : "");
	}
}
