package net.Indyuce.mmocore.command.rpg;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.util.item.CurrencyItem;
import net.Indyuce.mmocore.api.util.item.SmartGive;
import net.Indyuce.mmocore.command.api.CommandEnd;
import net.Indyuce.mmocore.command.api.CommandMap;
import net.Indyuce.mmocore.command.api.Parameter;

public class CoinsCommandMap extends CommandEnd {
	public CoinsCommandMap(CommandMap parent) {
		super(parent, "coins");

		addParameter(Parameter.PLAYER);
		addParameter(Parameter.AMOUNT);
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		Player player = Bukkit.getPlayer(args[1]);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[1] + ".");
			return CommandResult.FAILURE;
		}

		int amount;
		try {
			amount = Integer.parseInt(args[2]);
		} catch (Exception e) {
			sender.sendMessage(ChatColor.RED + args[2] + " is not a valid number.");
			return CommandResult.FAILURE;
		}

		new SmartGive(player).give(new CurrencyItem("GOLD_COIN", 1, amount).build());
		return CommandResult.SUCCESS;
	}

}
