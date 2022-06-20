package net.Indyuce.mmocore.command;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.MMOCommandEvent;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.FriendRequest;
import net.Indyuce.mmocore.api.player.social.Request;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.UUID;

public class FriendsCommand extends BukkitCommand {
	public FriendsCommand(ConfigurationSection config) {
		super(config.getString("main"));
		
		setAliases(config.getStringList("aliases"));
		setDescription("Opens the friends menu.");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is for players only.");
			return true;
		}

		PlayerData data = PlayerData.get((Player) sender);
		MMOCommandEvent event = new MMOCommandEvent(data, "friends");
		Bukkit.getServer().getPluginManager().callEvent(event);
		if(event.isCancelled()) return true;
		
		if (args.length > 1) {
			UUID uuid;
			try {
				uuid = UUID.fromString(args[1]);
			} catch (Exception e) {
				return true;
			}

			Request request = MMOCore.plugin.requestManager.getRequest(uuid);
			if (!(request instanceof FriendRequest))
				return true;

			if (request.isTimedOut()) {
				MMOCore.plugin.requestManager.unregisterRequest(uuid);
				return true;
			}

			if (OfflinePlayerData.get(((Player) sender).getUniqueId()).hasFriend(uuid)) {
				MMOCore.plugin.requestManager.unregisterRequest(uuid);
				return true;
			}

			if (args[0].equalsIgnoreCase("accept"))
				request.accept();
			if (args[0].equalsIgnoreCase("deny"))
				request.deny();
			return true;
		}

		InventoryManager.FRIEND_LIST.newInventory(data).open();
		return true;
	}
}
