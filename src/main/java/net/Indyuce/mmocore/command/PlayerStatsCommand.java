package net.Indyuce.mmocore.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;

public class PlayerStatsCommand extends BukkitCommand {
	public PlayerStatsCommand(ConfigurationSection config) {
		super(config.getString("main"));
		
		setAliases(config.getStringList("aliases"));
		setDescription("Show player stats.");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is for players only.");
			return true;
		}

		InventoryManager.PLAYER_STATS.newInventory(PlayerData.get((Player) sender)).open();
		return true;
	}
}
