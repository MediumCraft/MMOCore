package net.Indyuce.mmocore.command;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.PartyInvite;
import net.Indyuce.mmocore.api.player.social.Request;
import net.Indyuce.mmocore.manager.InventoryManager;

public class PartyCommand extends BukkitCommand {

	public PartyCommand(ConfigurationSection config) {
		super(config.getString("main"));
		
		setAliases(config.getStringList("aliases"));
		setDescription("Opens the party menu.");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
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
			if (request == null || !(request instanceof PartyInvite))
				return true;

			if (request.isTimedOut()) {
				MMOCore.plugin.requestManager.unregisterRequest(uuid);
				return true;
			}

			if (!MMOCore.plugin.partyManager.isRegistered(((PartyInvite) request).getParty())) {
				MMOCore.plugin.requestManager.unregisterRequest(uuid);
				return true;
			}

			if (args[0].equalsIgnoreCase("accept"))
				((PartyInvite) request).accept();
			if (args[0].equalsIgnoreCase("deny"))
				((PartyInvite) request).deny();
			return true;
		}

		PlayerData data = PlayerData.get((OfflinePlayer) sender);
		if (data.hasParty())
			InventoryManager.PARTY_VIEW.newInventory(data).open();
		else
			InventoryManager.PARTY_CREATION.newInventory(data).open();
		return true;
	}
}
