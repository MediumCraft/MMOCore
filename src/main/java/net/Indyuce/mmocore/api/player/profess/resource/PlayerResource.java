package net.Indyuce.mmocore.api.player.profess.resource;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.bukkit.attribute.Attribute;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.ClassOption;
import net.Indyuce.mmocore.api.player.stats.StatType;

public enum PlayerResource {

	HEALTH(StatType.HEALTH_REGENERATION, ClassOption.OFF_COMBAT_HEALTH_REGEN, (data) -> data.getPlayer().getHealth(),
			(data) -> data.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), PlayerData::heal),
	MANA(StatType.MANA_REGENERATION, ClassOption.OFF_COMBAT_MANA_REGEN, PlayerData::getMana,
			(data) -> data.getStats().getStat(StatType.MAX_MANA), PlayerData::giveMana),
	STAMINA(StatType.STAMINA_REGENERATION, ClassOption.OFF_COMBAT_STAMINA_REGEN, PlayerData::getStamina,
			(data) -> data.getStats().getStat(StatType.MAX_STAMINA), PlayerData::giveStamina),
	STELLIUM(StatType.STELLIUM_REGENERATION, ClassOption.OFF_COMBAT_STELLIUM_REGEN, PlayerData::getStellium,
			(data) -> data.getStats().getStat(StatType.MAX_STELLIUM), PlayerData::giveStellium);

	private final StatType regenStat;
	private final ClassOption offCombatRegen;
	private final Function<PlayerData, Double> current, max;
	private final BiConsumer<PlayerData, Double> regen;

	PlayerResource(StatType regenStat, ClassOption offCombatRegen, Function<PlayerData, Double> current, Function<PlayerData, Double> max, BiConsumer<PlayerData, Double> regen) {
		this.regenStat = regenStat;
		this.offCombatRegen = offCombatRegen;
		this.current = current;
		this.max = max;
		this.regen = regen;
	}

	/**
	 * @return Stat which corresponds to resource regeneration
	 */
	public StatType getRegenStat() {
		return regenStat;
	}

	/**
	 * @return Class option which determines whether or not resource should be
	 *         regenerated off combat only
	 */
	public ClassOption getOffCombatRegen() {
		return offCombatRegen;
	}

	/**
	 * @return Current resource of the given player
	 */
	public double getCurrent(PlayerData player) {
		return current.apply(player);
	}

	/**
	 * @return Max amount of that resource of the given player
	 */
	public double getMax(PlayerData player) {
		return max.apply(player);
	}

	/**
	 * Regens a player resource. Whatever resource, a bukkit event is triggered
	 * 
	 * @param player
	 *            Player to regen
	 * @param amount
	 *            Amount to regen
	 */
	public void regen(PlayerData player, double amount) {
		regen.accept(player, amount);
	}
}
