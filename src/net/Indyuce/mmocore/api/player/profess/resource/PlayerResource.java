package net.Indyuce.mmocore.api.player.profess.resource;

import java.util.function.Function;

import org.bukkit.attribute.Attribute;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass.ClassOption;
import net.Indyuce.mmocore.api.player.stats.StatType;

public enum PlayerResource {

	/*
	 * used to handle resource regeneration.
	 */
	HEALTH(StatType.HEALTH_REGENERATION, ClassOption.OFF_COMBAT_HEALTH_REGEN, ClassOption.MAX_HEALTH_REGEN, ClassOption.MISSING_HEALTH_REGEN, (data) -> data.getPlayer().getHealth(), (data) -> data.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()),
	MANA(StatType.MANA_REGENERATION, ClassOption.OFF_COMBAT_MANA_REGEN, ClassOption.MAX_MANA_REGEN, ClassOption.MISSING_MANA_REGEN, (data) -> data.getMana(), (data) -> data.getStats().getStat(StatType.MAX_MANA)),
	STAMINA(StatType.STAMINA_REGENERATION, ClassOption.OFF_COMBAT_STAMINA_REGEN, ClassOption.MAX_STAMINA_REGEN, ClassOption.MISSING_STAMINA_REGEN, (data) -> data.getStamina(), (data) -> data.getStats().getStat(StatType.MAX_STAMINA));

	private final StatType regenStat;
	private final ClassOption offCombatRegen, maxRegen, missingRegen;
	private final Function<PlayerData, Double> current, max;

	private PlayerResource(StatType regenStat, ClassOption offCombatRegen, ClassOption maxRegen, ClassOption missingRegen, Function<PlayerData, Double> current, Function<PlayerData, Double> max) {
		this.regenStat = regenStat;
		this.offCombatRegen = offCombatRegen;
		this.maxRegen = maxRegen;
		this.missingRegen = missingRegen;
		this.current = current;
		this.max = max;
	}

	public ClassOption getMaxRegen() {
		return maxRegen;
	}

	public ClassOption getMissingRegen() {
		return missingRegen;
	}

	public ClassOption getOffCombatRegen() {
		return offCombatRegen;
	}

	public StatType getRegenStat() {
		return regenStat;
	}

	public double getCurrent(PlayerData data) {
		return current.apply(data);
	}

	public double getMax(PlayerData data) {
		return max.apply(data);
	}
}
