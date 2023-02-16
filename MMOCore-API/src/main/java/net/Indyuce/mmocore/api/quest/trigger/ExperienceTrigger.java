package net.Indyuce.mmocore.api.quest.trigger;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.SimpleExperienceObject;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import org.apache.commons.lang.Validate;

import io.lumine.mythic.lib.api.MMOLineConfig;
import org.jetbrains.annotations.NotNull;

public class ExperienceTrigger extends Trigger {
	@NotNull
	private final RandomAmount amount;
	@NotNull
	private final EXPSource source;
	@NotNull
	private final ExperienceDispenser dispenser;

	public ExperienceTrigger(MMOLineConfig config) {
		super(config);

		config.validate("amount");

		if (config.contains("profession")) {
			String id = config.getString("profession").toLowerCase().replace("_", "-");
			Validate.isTrue(MMOCore.plugin.professionManager.has(id), "Could not find profession");
			dispenser = MMOCore.plugin.professionManager.get(id);
		} else
			dispenser = new SimpleExperienceObject();
		amount = new RandomAmount(config.getString("amount"));
		source = config.contains("source") ? EXPSource.valueOf(config.getString("source").toUpperCase()) : EXPSource.QUEST;
	}

	@Override
	public void apply(PlayerData player) {
		dispenser.giveExperience(player, amount.calculate(), null, source);
	}
}
