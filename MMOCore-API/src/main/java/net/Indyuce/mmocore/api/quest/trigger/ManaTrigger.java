package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;

public class ManaTrigger extends Trigger {
	private final RandomAmount amount;
	private final Operation operation;

	public ManaTrigger(MMOLineConfig config) {
		super(config);

		config.validate("amount");
		amount = new RandomAmount(config.getString("amount"));
		operation = config.contains("operation") ? Operation.valueOf(config.getString("operation").toUpperCase()) : Operation.GIVE;
	}

	@Override
	public void apply(PlayerData player) {

		// Give mana
		if (operation == Operation.GIVE)
			player.giveMana(amount.calculate(), PlayerResourceUpdateEvent.UpdateReason.TRIGGER);

			// Set mana
		else if (operation == Operation.SET)
			player.setMana(amount.calculate());

			// Take mana
		else
			player.giveMana(-amount.calculate(), PlayerResourceUpdateEvent.UpdateReason.TRIGGER);
	}

	public enum Operation {
		GIVE,
		SET,
		TAKE
	}
}
