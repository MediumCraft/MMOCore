package net.Indyuce.mmocore.api.quest.trigger;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class StaminaTrigger extends Trigger {
	private final RandomAmount amount;
	private final Operation operation;

	public StaminaTrigger(MMOLineConfig config) {
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
			player.giveStamina(amount.calculate());

		/*
		 * set mana
		 */
		else if (operation == Operation.SET)
			player.setStamina(amount.calculate());

		/*
		 * take mana
		 */
		else
			player.giveStamina(-amount.calculate());
	}

	public enum Operation {
		GIVE,
		SET,
		TAKE;
	}
}
