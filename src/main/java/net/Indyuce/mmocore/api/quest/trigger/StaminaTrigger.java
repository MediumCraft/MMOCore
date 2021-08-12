package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.ManaTrigger.Operation;
import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;

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

		// Give stamina
		if (operation == Operation.GIVE)
			player.giveStamina(amount.calculate(), PlayerResourceUpdateEvent.UpdateReason.TRIGGER);

			// Set stamina
		else if (operation == Operation.SET)
			player.setStamina(amount.calculate());

			// Take stamina
		else
			player.giveStamina(-amount.calculate(), PlayerResourceUpdateEvent.UpdateReason.TRIGGER);
	}
}
