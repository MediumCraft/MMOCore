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

public class AttributesCommand extends RegisteredCommand {
	public AttributesCommand(ConfigurationSection config) {
		super(config, ToggleableCommand.ATTRIBUTES);
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is for players only.");
			return true;
		}

		PlayerData data = PlayerData.get((Player) sender);
		MMOCommandEvent event = new MMOCommandEvent(data, "attributes");
		Bukkit.getServer().getPluginManager().callEvent(event);
		if(!event.isCancelled()) InventoryManager.ATTRIBUTE_VIEW.newInventory(data).open();
		return true;
	}
}
