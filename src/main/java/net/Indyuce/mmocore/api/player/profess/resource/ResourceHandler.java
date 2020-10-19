package net.Indyuce.mmocore.api.player.profess.resource;

import java.util.function.BiFunction;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class ResourceHandler {

	/*
	 * resource regeneration only applies when player is off combat
	 */
	private final boolean offCombatOnly;

	/*
	 * percentage of scaling which the player regenerates every second
	 */
	private final LinearValue scalar;

	/*
	 * whether the resource regeneration scales on missing or max resource. if
	 * TYPE is null, then there is no special regeneration.
	 */
	private final HandlerType type;
	private final PlayerResource resource;

	/*
	 * used when there is no special resource regeneration
	 */
	public ResourceHandler(PlayerResource resource) {
		this(resource, null, null, false);
	}

	public ResourceHandler(PlayerResource resource, ConfigurationSection config) {
		this.resource = resource;
		offCombatOnly = config.getBoolean("off-combat");

		if(config.contains("type")) {
			Validate.isTrue(config.contains("type"), "Could not find resource regen scaling type");
			type = HandlerType.valueOf(config.getString("type").toUpperCase());
		} else type = null;

		if(type != null) {
			Validate.notNull(config.getConfigurationSection("value"), "Could not find resource regen value config section");
			scalar = new LinearValue(config.getConfigurationSection("value"));
		} else scalar = null;
	}

	public ResourceHandler(PlayerResource resource, HandlerType type, LinearValue scalar, boolean offCombatOnly) {
		this.resource = resource;
		this.type = type;
		this.scalar = scalar;
		this.offCombatOnly = offCombatOnly;
	}

	/*
	 * REGENERATION FORMULAS HERE.
	 */
	public double getRegen(PlayerData player) {
		double d = 0;

		// base resource regeneration = value of the corresponding regen stat
		if (!player.isInCombat() || !player.getProfess().hasOption(resource.getOffCombatRegen()))
			d += player.getStats().getStat(resource.getRegenStat());
		
		// extra resource regeneration based on CLASS, scales on LEVEL
		if (type != null && (!player.isInCombat() || !offCombatOnly))
			d = this.scalar.calculate(player.getLevel()) / 100 * type.getScaling(player, resource);
		
		return d;
	}

	public enum HandlerType {
		/*
		 * resource regeneration scales on max resource
		 */
		MAX((player, resource) -> resource.getMax(player)),

		/*
		 * resource regeneration scales on missing resource
		 */
		MISSING((player, resource) -> resource.getMax(player) - resource.getCurrent(player));

		private final BiFunction<PlayerData, PlayerResource, Double> calculation;

		private HandlerType(BiFunction<PlayerData, PlayerResource, Double> calculation) {
			this.calculation = calculation;
		}

		public double getScaling(PlayerData player, PlayerResource resource) {
			return calculation.apply(player, resource);
		}
	}
}
