package net.Indyuce.mmocore.comp.rpg.damage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.Indyuce.mmocore.api.player.stats.StatType;

public class DamageInfo {
	private final Set<DamageType> types;
	private final double value;

	public DamageInfo(DamageType... type) {
		this(0, type);
	}

	public DamageInfo(double value, DamageType... types) {
		this(value, Arrays.asList(types));
	}

	public DamageInfo(double value, List<DamageType> types) {
		this(value, new HashSet<>(types));
	}

	public DamageInfo(double value, Set<DamageType> types) {
		// Validate.notEmpty(types, "Damage must have at least one damage
		// type!");

		this.types = types;
		this.value = value;
	}

	public DamageInfo merge(DamageInfo info) {
		types.addAll(info.getTypes());
		return this;
	}

	public Set<DamageType> getTypes() {
		return types;
	}

	public boolean hasType(DamageType type) {
		return types.contains(type);
	}

	public double getValue() {
		return value;
	}

	public enum DamageType {

		/*
		 * skills or abilities dealing magic damage
		 */
		MAGICAL(StatType.MAGICAL_DAMAGE),

		/*
		 * skills or abilities dealing physical damage
		 */
		PHYSICAL(StatType.PHYSICAL_DAMAGE),

		/*
		 * weapons dealing damage
		 */
		WEAPON(StatType.WEAPON_DAMAGE),

		/*
		 * skill damage
		 */
		SKILL(StatType.SKILL_DAMAGE),

		/*
		 * projectile based weapons or skills
		 */
		PROJECTILE(StatType.PROJECTILE_DAMAGE);

		private final StatType stat;

		private DamageType(StatType stat) {
			this.stat = stat;
		}

		public StatType getStat() {
			return stat;
		}

		public String getPath() {
			return name().toLowerCase().replace("_", "-");
		}
	}
}
