package net.Indyuce.mmocore.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;

public class PlayerStatsCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is for players only.");
			return true;
		}

		InventoryManager.PLAYER_STATS.newInventory(PlayerData.get((Player) sender)).open();
		return true;
	}
}
