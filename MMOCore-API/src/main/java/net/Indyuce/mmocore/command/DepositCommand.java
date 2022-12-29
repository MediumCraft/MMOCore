package net.Indyuce.mmocore.command;

import net.Indyuce.mmocore.command.api.RegisteredCommand;
import net.Indyuce.mmocore.command.api.ToggleableCommand;
import net.Indyuce.mmocore.gui.eco.DepositMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class DepositCommand extends RegisteredCommand {
	public DepositCommand(ConfigurationSection config) {
		super(config, ToggleableCommand.DEPOSIT);
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!sender.hasPermission("mmocore.currency"))
			return false;

		Player player = args.length > 0 && sender.hasPermission("mmocore.admin") ? Bukkit.getPlayer(args[0]) : sender instanceof Player ? (Player) sender : null;
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "Please specify a valid player.");
			return true;
		}

		// if (sender instanceof Player)
		// if (!isNearEnderchest(((Player) sender).getLocation())) {
		// sender.sendMessage(MMOCore.plugin.configManager.getSimpleMessage("stand-near-enderchest"));
		// return true;
		// }

		new DepositMenu(player).open();
		return true;
	}

	// private boolean isNearEnderchest(Location loc) {
	// for (int x = -5; x < 6; x++)
	// for (int y = -5; y < 6; y++)
	// for (int z = -5; z < 6; z++)
	// if (loc.clone().add(x, y, z).getBlock().getType() ==
	// Material.ENDER_CHEST)
	// return true;
	// return false;
	// }
}
