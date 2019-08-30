package net.Indyuce.mmocore.api.quest.trigger;

import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.math.formula.RandomAmount;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.ManaTrigger.Operation;

public class StelliumTrigger extends Trigger {
	private final RandomAmount amount;
	private final Operation operation;

	public StelliumTrigger(MMOLineConfig config) {
		super(config);

		config.validate("amount");
		amount = new RandomAmount(config.getString("amount"));
		operation = config.contains("operation") ? Operation.valueOf(config.getString("operation").toUpperCase()) : Operation.GIVE;
	}

	@Override
	public void apply(PlayerData player) {

		/*
		 * give mana
		 */
		if (operation == Operation.GIVE)
			player.giveStellium(amount.calculate());

		/*
		 * set mana
		 */
		else if (operation == Operation.SET)
			player.setStellium(amount.calculate());

		/*
		 * take mana
		 */
		else
			player.giveStellium(-amount.calculate());
	}
}
