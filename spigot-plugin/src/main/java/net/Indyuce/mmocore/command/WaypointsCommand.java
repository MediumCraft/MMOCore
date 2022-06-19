package net.Indyuce.mmocore.command;

import net.Indyuce.mmocore.api.event.MMOCommandEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;

public class WaypointsCommand extends BukkitCommand {
	public WaypointsCommand(ConfigurationSection config) {
		super(config.getString("main"));
		
		setAliases(config.getStringList("aliases"));
		setDescription("Open the waypoints menu.");
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
