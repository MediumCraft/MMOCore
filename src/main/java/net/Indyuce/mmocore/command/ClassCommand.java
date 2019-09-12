package net.Indyuce.mmocore.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;

public class ClassCommand extends BukkitCommand {
	public ClassCommand(ConfigurationSection config) {
		super(config.getString("main"));
		
		setAliases(config.getStringList("aliases"));
		setDescription("Select a new class.");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!sender.hasPermission("mmocore.class-select"))
			return false;

		Player player = args.length > 0 && sender.hasPermission("mmocore.admin") ? Bukkit.getPlayer(args[0]) : sender instanceof Player ? (Player) sender : null;
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "Please specify a valid player.");
			return true;
		}

		PlayerData data = PlayerData.get(player);
		if (data.getProfess().getSubclasses().stream().filter(sub -> sub.getLevel() <= data.getLevel()).count() > 0)
			InventoryManager.SUBCLASS_SELECT.newInventory(data).open();
		else
			InventoryManager.CLASS_SELECT.newInventory(data).open();
		return true;
	}
}
