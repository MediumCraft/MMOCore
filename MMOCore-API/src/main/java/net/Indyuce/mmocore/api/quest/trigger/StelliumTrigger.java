package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
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

		// Give stellium
		if (operation == Operation.GIVE)
			player.giveStellium(amount.calculate(), PlayerResourceUpdateEvent.UpdateReason.TRIGGER);

			// Set stellium
		else if (operation == Operation.SET)
			player.setStellium(amount.calculate());

			// Take stellium
		else
			player.giveStellium(-amount.calculate(), PlayerResourceUpdateEvent.UpdateReason.TRIGGER);
	}
}
