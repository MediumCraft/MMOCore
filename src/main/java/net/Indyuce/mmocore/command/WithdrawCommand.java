package net.Indyuce.mmocore.command;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.eco.Withdraw;

public class WithdrawCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("mmocore.currency"))
			return false;

		String playerArgument = args.length < 2 ? null : args[args.length - 2];
		String amountArgument = args.length == 0 ? "0" : args[args.length - 1];

		Player player = playerArgument != null && sender.hasPermission("mmocore.admin") ? Bukkit.getPlayer(playerArgument) : sender instanceof Player ? (Player) sender : null;
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "Please specify a valid player.");
			return true;
		}

		int amount = 0;
		try {
			amount = Integer.parseInt(amountArgument);
			Validate.isTrue(amount >= 0);
		} catch (IllegalArgumentException exception) {
			sender.sendMessage(MMOCore.plugin.configManager.getSimpleMessage("wrong-number", "arg", "" + args[0]));
			return true;
		}

		Withdraw request = new Withdraw(player);

		if (amount == 0) {
			request.open();
			return true;
		}

		int left = (int) MMOCore.plugin.economy.getEconomy().getBalance(player) - amount;
		if (left < 0) {
			player.sendMessage(MMOCore.plugin.configManager.getSimpleMessage("not-enough-money", "left", "" + -left));
			return true;
		}

		MMOCore.plugin.economy.getEconomy().withdrawPlayer(player, amount);
		request.withdrawAlgorythm(amount);
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
		player.sendMessage(MMOCore.plugin.configManager.getSimpleMessage("withdrew", "worth", "" + amount));
		return true;
	}
}
