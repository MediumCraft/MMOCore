package net.Indyuce.mmocore.comp.vault;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class MoneyTrigger extends Trigger {
	private final RandomAmount amount;
	private final Operation operation;

	public MoneyTrigger(MMOLineConfig config) {
		super(config);

		config.validate("amount");
		amount = new RandomAmount(config.getString("amount"));
		operation = config.contains("operation") ? Operation.valueOf(config.getString("operation").toUpperCase()) : Operation.GIVE;
	}

	@Override
	public void apply(PlayerData player) {

		/*
		 * cannot run this safe check on server startup since the mmoloader is
		 * instanced when the plugin loads and the economy has not been loaded
		 * yet.
		 */
		if (!MMOCore.plugin.economy.isValid() || !player.isOnline())
			return;

		if (operation == Operation.GIVE)
			MMOCore.plugin.economy.getEconomy().depositPlayer(player.getPlayer(), amount.calculate());

		else if (operation == Operation.SET)
			throw new IllegalArgumentException("Operation SET is not available for the money trigger.");

		else
			MMOCore.plugin.economy.getEconomy().withdrawPlayer(player.getPlayer(), amount.calculate());
	}

	public enum Operation {
		GIVE,
		SET,
		TAKE;
	}
}
