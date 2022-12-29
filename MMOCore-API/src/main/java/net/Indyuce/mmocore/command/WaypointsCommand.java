package net.Indyuce.mmocore.command;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.api.RegisteredCommand;
import net.Indyuce.mmocore.command.api.ToggleableCommand;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.api.event.MMOCommandEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class WaypointsCommand extends RegisteredCommand {
	public WaypointsCommand(ConfigurationSection config) {
		super(config, ToggleableCommand.WAYPOINTS);
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (sender instanceof Player && sender.hasPermission("mmocore.waypoints")) {
			PlayerData data = PlayerData.get((Player) sender);
			MMOCommandEvent event = new MMOCommandEvent(data, "waypoints");
			Bukkit.getServer().getPluginManager().callEvent(event);
			if(!event.isCancelled()) InventoryManager.WAYPOINTS.newInventory(data).open();
		}
		return true;
	}
}
