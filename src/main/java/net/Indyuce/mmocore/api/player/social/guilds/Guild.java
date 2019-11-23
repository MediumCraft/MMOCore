package net.Indyuce.mmocore.api.player.social.guilds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.Request;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.gui.social.guild.EditableGuildView.GuildViewInventory;
import net.Indyuce.mmocore.manager.InventoryManager;

public class Guild {
	private final GuildMembers members = new GuildMembers();
	private final Map<UUID, Long> invites = new HashMap<>();
	private String guildName;

	/*
	 * owner changes when the old owner leaves guild
	 */
	private PlayerData owner;

	// used to check if two parties are the same
	// private UUID uuid = UUID.randomUUID();

	public Guild(PlayerData owner, String name) {
		this.owner = owner;
		this.guildName = name;
		addMember(owner);
	}

	public PlayerData getOwner() {
		return owner;
	}
	
	public String getName() {
		return guildName;
	}

	public GuildMembers getMembers() {
		return members;
	}

	public long getLastInvite(Player player) {
		return invites.containsKey(player.getUniqueId()) ? invites.get(player.getUniqueId()) : 0;
	}

	public void removeLastInvite(Player player) {
		invites.remove(player.getUniqueId());
	}

	public void removeMember(PlayerData data) {
		if (data.isOnline() && data.getPlayer().getOpenInventory() != null && data.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof GuildViewInventory)
			InventoryManager.GUILD_CREATION.newInventory(data).open();

		members.remove(data);
		data.setGuild(null);

		reopenInventories();

		// disband the guild if no member left
		if (members.count() < 1) {
			MMOCore.plugin.guildManager.unregisterGuild(this);
			return;
		}

		// transfer ownership
		if (owner.equals(data)) {
			owner = members.get(0);
			MMOCore.plugin.configManager.getSimpleMessage("transfer-guild-ownership").send(owner.getPlayer());
		}
	}

	public void addMember(PlayerData data) {
		if (data.inGuild())
			data.getGuild().removeMember(data);

		data.setGuild(this);
		members.add(data);

		reopenInventories();
	}

	public void reopenInventories() {
		for (PlayerData member : members.members)
			if (member.isOnline() && member.getPlayer().getOpenInventory() != null && member.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof GuildViewInventory)
				((PluginInventory) member.getPlayer().getOpenInventory().getTopInventory().getHolder()).open();
	}

	public void sendGuildInvite(PlayerData inviter, PlayerData target) {
		invites.put(target.getUniqueId(), System.currentTimeMillis());
		Request request = new GuildInvite(this, inviter, target);
		new ConfigMessage("guild-invite").addPlaceholders("player", inviter.getPlayer().getName(), "uuid", request.getUniqueId().toString()).sendAsJSon(target.getPlayer());
		MMOCore.plugin.requestManager.registerRequest(request);
	}

	public class GuildMembers {
		private final List<PlayerData> members = new ArrayList<>();

		public PlayerData get(int count) {
			return members.get(count);
		}

		public boolean has(PlayerData player) {
			return members.contains(player);
		}

		public void add(PlayerData player) {
			members.add(player);
		}

		public void remove(PlayerData player) {
			members.remove(player);
		}

		public void forEach(Consumer<? super PlayerData> action) {
			members.forEach(action);
		}

		public int count() {
			return members.size();
		}
	}
}
