package net.Indyuce.mmocore.command;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.api.RegisteredCommand;
import net.Indyuce.mmocore.command.api.ToggleableCommand;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.api.event.MMOCommandEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ClassCommand extends RegisteredCommand {
	public ClassCommand(ConfigurationSection config) {
		super(config, ToggleableCommand.CLASS);
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
		MMOCommandEvent event = new MMOCommandEvent(data, "class");
		Bukkit.getServer().getPluginManager().callEvent(event);
		if(event.isCancelled()) return true;
		if (data.getProfess().getSubclasses().stream().anyMatch(sub -> sub.getLevel() <= data.getLevel()))
			InventoryManager.SUBCLASS_SELECT.newInventory(data).open();
		else
			InventoryManager.CLASS_SELECT.newInventory(data).open();
		return true;
	}
}
