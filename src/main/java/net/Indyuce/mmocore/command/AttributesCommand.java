package net.Indyuce.mmocore.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;

public class AttributesCommand extends BukkitCommand {
	public AttributesCommand(ConfigurationSection config) {
		super(config.getString("main"));
		
		setAliases(config.getStringList("aliases"));
		setDescription("Opens the attribute menu.");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is for players only.");
			return true;
		}

		InventoryManager.ATTRIBUTE_VIEW.newInventory(PlayerData.get((Player) sender)).open();
		return true;
	}
}
