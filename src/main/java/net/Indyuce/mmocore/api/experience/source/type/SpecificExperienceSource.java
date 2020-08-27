package net.Indyuce.mmocore.api.experience.source.type;

import org.bukkit.Location;

import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;
import net.mmogroup.mmolib.api.MMOLineConfig;

public abstract class SpecificExperienceSource<T> extends ExperienceSource<T> {
	private final RandomAmount amount;

	/**
	 * Used to register experience sources with SPECIFIC experience outputs.
	 * Other experience sources like ENCHANT have their exp output depend on the
	 * enchanted item. ALCHEMY exp outputs depend on the potion crafted
	 */
	public SpecificExperienceSource(Profession profession, MMOLineConfig config) {
		super(profession);

		config.validate("amount");
		amount = new RandomAmount(config.getString("amount"));
	}

	public RandomAmount getAmount() {
		return amount;
	}

	public int rollAmount() {
		return amount.calculateInt();
	}

	/**
	 * Used when a player needs to gain experience after performing the action
	 * corresponding to this exp source
	 * 
	 * @param multiplier
	 *            Used by the CraftItem experience source, multiplies the exp
	 *            earned by a certain factor. When crafting an item, the
	 *            multiplier is equal to the amount of items crafted
	 * @param loc
	 *            Location used to display the exp hologram
	 */
	public void giveExperience(PlayerData player, int multiplier, Location loc) {
		giveExperience(player, rollAmount() * multiplier, loc);
	}
}
