package net.Indyuce.mmocore.command;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.FriendRequest;
import net.Indyuce.mmocore.api.player.social.Request;
import net.Indyuce.mmocore.manager.InventoryManager;

public class FriendsCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is for players only.");
			return true;
		}

		if (args.length > 1) {
			UUID uuid;
			try {
				uuid = UUID.fromString(args[1]);
			} catch (Exception e) {
				return true;
			}

			Request request = MMOCore.plugin.requestManager.getRequest(uuid);
			if (request == null || !(request instanceof FriendRequest))
				return true;

			if (request.isTimedOut()) {
				MMOCore.plugin.requestManager.unregisterRequest(uuid);
				return true;
			}

			if (new OfflinePlayerData(((Player) sender).getUniqueId()).hasFriend(uuid)) {
				MMOCore.plugin.requestManager.unregisterRequest(uuid);
				return true;
			}

			if (args[0].equalsIgnoreCase("accept"))
				((FriendRequest) request).accept();
			if (args[0].equalsIgnoreCase("deny"))
				((FriendRequest) request).deny();
			return true;
		}

		InventoryManager.FRIEND_LIST.newInventory(PlayerData.get((Player) sender)).open();
		return true;
	}
}
