package net.Indyuce.mmocore.api.quest.trigger;

import net.Indyuce.mmocore.experience.ExperienceObject;
import org.apache.commons.lang.Validate;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;
import io.lumine.mythic.lib.api.MMOLineConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ExperienceTrigger extends Trigger {
	@NotNull
	private final RandomAmount amount;
	@NotNull
	private final EXPSource source;
	@Nullable
	private final ExperienceObject expObject;

	public ExperienceTrigger(MMOLineConfig config) {
		super(config);

		config.validate("amount");

		if (config.contains("profession")) {
			String id = config.getString("profession").toLowerCase().replace("_", "-");
			Validate.isTrue(MMOCore.plugin.professionManager.has(id), "Could not find profession");
			expObject = MMOCore.plugin.professionManager.get(id);
		} else
			expObject = null;
		amount = new RandomAmount(config.getString("amount"));
		source = config.contains("source") ? EXPSource.valueOf(config.getString("source").toUpperCase()) : EXPSource.QUEST;
	}

	@Override
	public void apply(PlayerData player) {
		Objects.requireNonNullElse(expObject, player.getProfess()).giveExperience(player, amount.calculateInt(), null, source);
	}
}
