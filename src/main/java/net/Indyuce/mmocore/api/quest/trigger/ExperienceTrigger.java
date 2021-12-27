package net.Indyuce.mmocore.api.quest.trigger;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.ExperienceInfo;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class ExperienceTrigger extends Trigger {
	private final RandomAmount amount;
	private final Profession profession;
	private final EXPSource source;

	public ExperienceTrigger(MMOLineConfig config) {
		super(config);

		config.validate("amount");

		if (config.contains("profession")) {
			String id = config.getString("profession").toLowerCase().replace("_", "-");
			Validate.isTrue(MMOCore.plugin.professionManager.has(id), "Could not find profession");
			profession = MMOCore.plugin.professionManager.get(id);
		} else
			profession = null;
		amount = new RandomAmount(config.getString("amount"));
		source = config.contains("source") ? EXPSource.valueOf(config.getString("source").toUpperCase()) : EXPSource.QUEST;
	}

	@Override
	public void apply(PlayerData player) {
		if (profession == null)
			player.giveExperience(amount.calculateInt(), source);
		else
			player.getCollectionSkills().giveExperience(profession, amount.calculateInt(), source);
	}

	/*
	 * experience must be accessible from the custom mine event. this method
	 * allows to generate an experience info instance for easy manipulations
	 */
	public ExperienceInfo newInfo() {
		return new ExperienceInfo(amount.calculateInt(), profession);
	}
}
