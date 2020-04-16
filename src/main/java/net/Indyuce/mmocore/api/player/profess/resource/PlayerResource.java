package net.Indyuce.mmocore.api.player.profess.resource;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.bukkit.attribute.Attribute;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.ClassOption;
import net.Indyuce.mmocore.api.player.stats.StatType;

public enum PlayerResource {

	HEALTH(StatType.HEALTH_REGENERATION, ClassOption.OFF_COMBAT_HEALTH_REGEN, (data) -> data.getPlayer().getHealth(), (data) -> data.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), (data, d) -> data.heal(d)),
	MANA(StatType.MANA_REGENERATION, ClassOption.OFF_COMBAT_MANA_REGEN, (data) -> data.getMana(), (data) -> data.getStats().getStat(StatType.MAX_MANA), (data, d) -> data.giveMana(d)),
	STAMINA(StatType.STAMINA_REGENERATION, ClassOption.OFF_COMBAT_STAMINA_REGEN, (data) -> data.getStamina(), (data) -> data.getStats().getStat(StatType.MAX_STAMINA), (data, d) -> data.giveStamina(d)),
	STELLIUM(StatType.STELLIUM_REGENERATION, ClassOption.OFF_COMBAT_STELLIUM_REGEN, (data) -> data.getStellium(), (data) -> data.getStats().getStat(StatType.MAX_STELLIUM), (data, d) -> data.giveStellium(d));

	private final StatType regenStat;
	private final ClassOption offCombatRegen;
	private final Function<PlayerData, Double> current, max;
	private final BiConsumer<PlayerData, Double> regen;

	private PlayerResource(StatType regenStat, ClassOption offCombatRegen, Function<PlayerData, Double> current, Function<PlayerData, Double> max, BiConsumer<PlayerData, Double> regen) {
		this.regenStat = regenStat;
		this.offCombatRegen = offCombatRegen;
		this.current = current;
		this.max = max;
		this.regen = regen;
	}

	/*
	 * stat which correspondons to resource regeneration
	 */
	public StatType getRegenStat() {
		return regenStat;
	}

	/*
	 * class option which determines whether or not resource should be
	 * regenerated off combat only
	 */
	public ClassOption getOffCombatRegen() {
		return offCombatRegen;
	}

	/*
	 * get current resource of player
	 */
	public double getCurrent(PlayerData player) {
		return current.apply(player);
	}

	/*
	 * get max resource of player
	 */
	public double getMax(PlayerData player) {
		return max.apply(player);
	}

	/*
	 * regenerate resource of player (TRIGGERS A BUKKIT/CUSTOM EVENT)
	 */
	public void regen(PlayerData player, double amount) {
		regen.accept(player, amount);
	}
}
