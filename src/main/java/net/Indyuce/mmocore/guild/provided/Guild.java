package net.Indyuce.mmocore.guild.provided;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import net.Indyuce.mmocore.guild.AbstractGuild;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.Request;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.gui.social.guild.EditableGuildView.GuildViewInventory;
import net.Indyuce.mmocore.manager.InventoryManager;

public class Guild implements AbstractGuild {
	private final GuildMembers members = new GuildMembers();
	private final Map<UUID, Long> invites = new HashMap<>();
	private final String guildId, guildName, guildTag;

	/**
	 * Owner changes when the old owner leaves guild
	 */
	private UUID owner;

	public Guild(UUID owner, String name, String tag) {
		this.owner = owner;
		this.guildId = tag.toLowerCase();
		this.guildName = name;
		this.guildTag = tag;
	}

	public UUID getOwner() {
		return owner;
	}
	
	public String getName() {
		return guildName;
	}

	public String getId() {
		return guildId;
	}
	
	public String getTag() {
		return guildTag;
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
	
	public void removeMember(UUID uuid)
	{ removeMember(uuid, false); }
	
	// Disband boolean is to prevent co-modification exception when disbanding a guild 
	public void removeMember(UUID uuid, boolean disband) {
		PlayerData data = PlayerData.get(uuid);
		if (data != null && data.isOnline() && data.getPlayer().getOpenInventory() != null && data.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof GuildViewInventory)
			InventoryManager.GUILD_CREATION.newInventory(data).open();

		if(!disband) members.remove(uuid);
		if(data != null) data.setGuild(null);
		reopenInventories();

		//if(!disband) {

			// disband the guild if no member left
			if (members.count() < 1) {
				MMOCore.plugin.dataProvider.getGuildManager().unregisterGuild(this);
				return;
			}

			// transfer ownership
			if (owner.equals(uuid)) {
				owner = members.get(0);
				MMOCore.plugin.configManager.getSimpleMessage("transfer-guild-ownership").send(Bukkit.getPlayer(owner));
			}
		//}
	}

	public void addMember(UUID uuid) {
		PlayerData data = PlayerData.get(uuid);
		if (data.inGuild())
			data.getGuild().removeMember(uuid);

		data.setGuild(this);
		members.add(uuid);

		reopenInventories();
	}
	
	public void registerMember(UUID uuid) {
		members.add(uuid);
	}

	public void reopenInventories() {
		for (UUID uuid : members.members) {
			PlayerData member = PlayerData.get(uuid);
			if (member != null && member.isOnline() && member.getPlayer().getOpenInventory() != null && member.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof GuildViewInventory)
				((PluginInventory) member.getPlayer().getOpenInventory().getTopInventory().getHolder()).open();
		}
	}

	public void sendGuildInvite(PlayerData inviter, PlayerData target) {
		invites.put(target.getUniqueId(), System.currentTimeMillis());
		Request request = new GuildInvite(this, inviter, target);
		new ConfigMessage("guild-invite").addPlaceholders("player", inviter.getPlayer().getName(), "uuid", request.getUniqueId().toString()).sendAsJSon(target.getPlayer());
		MMOCore.plugin.requestManager.registerRequest(request);
	}
	
	public static class GuildMembers {
		private final List<UUID> members = new ArrayList<>();

		public UUID get(int count) {
			return members.get(count);
		}

		public boolean has(UUID player) {
			return members.contains(player);
		}

		public void add(UUID player) {
			members.add(player);
		}

		public void remove(UUID player) {
			members.remove(player);
		}

		public void forEach(Consumer<? super UUID> action) {
			members.forEach(action);
		}

		public int countOnline() {
			int online = 0;
			
			for(UUID member : members)
				if(Bukkit.getOfflinePlayer(member).isOnline())
					online += 1;
			
			return online;
		}

		public int count()
		{ return members.size(); }
		public void clear()
		{ members.clear(); }
	}
}
