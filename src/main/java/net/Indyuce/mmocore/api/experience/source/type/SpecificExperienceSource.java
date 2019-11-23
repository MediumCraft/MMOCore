package net.Indyuce.mmocore.api.experience.source.type;

import org.bukkit.Location;

import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.math.formula.RandomAmount;
import net.Indyuce.mmocore.api.player.PlayerData;

public abstract class SpecificExperienceSource<T> extends ExperienceSource<T> {
	private final RandomAmount amount;

	/*
	 * not all experience sources have to specify a random experience amount e.g
	 * ENCHANT and ALCHEMY experience depend on the enchanted item.
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

	public void giveExperience(PlayerData player, Location loc) {
		giveExperience(player, rollAmount(), loc);
	}
}
