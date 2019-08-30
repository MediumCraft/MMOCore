package net.Indyuce.mmocore.api.player.social;

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
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.gui.social.party.EditablePartyView.PartyViewInventory;
import net.Indyuce.mmocore.manager.InventoryManager;

public class Party {
	private final PartyMembers members = new PartyMembers();
	private final Map<UUID, Long> invites = new HashMap<>();

	/*
	 * owner changes when the old owner leaves party
	 */
	private PlayerData owner;

	// used to check if two parties are the same
	// private UUID uuid = UUID.randomUUID();

	public Party(PlayerData owner) {
		this.owner = owner;
		addMember(owner);
	}

	public PlayerData getOwner() {
		return owner;
	}

	public PartyMembers getMembers() {
		return members;
	}

	public long getLastInvite(Player player) {
		return invites.containsKey(player.getUniqueId()) ? invites.get(player.getUniqueId()) : 0;
	}

	public void removeMember(PlayerData data) {
		if (data.isOnline() && data.getPlayer().getOpenInventory() != null && data.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof PartyViewInventory)
			InventoryManager.PARTY_CREATION.newInventory(data).open();

		members.remove(data);
		data.setParty(null);

		reopenInventories();

		// disband the party if no member left
		if (members.count() < 1) {
			MMOCore.plugin.partyManager.unregisterParty(this);
			return;
		}

		// transfer ownership
		if (owner.equals(data)) {
			owner = members.get(0);
			owner.getPlayer().sendMessage(MMOCore.plugin.configManager.getSimpleMessage("transfer-party-ownership"));
		}
	}

	public void addMember(PlayerData data) {
		if (data.hasParty())
			data.getParty().removeMember(data);

		data.setParty(this);
		members.add(data);

		reopenInventories();
	}

	public void reopenInventories() {
		for (PlayerData member : members.members)
			if (member.isOnline() && member.getPlayer().getOpenInventory() != null && member.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof PartyViewInventory)
				((PluginInventory) member.getPlayer().getOpenInventory().getTopInventory().getHolder()).open();
	}

	public void sendPartyInvite(Player inviter, PlayerData target) {
		invites.put(target.getUniqueId(), System.currentTimeMillis());
		Request request = new PartyInvite(this, target);
		new ConfigMessage("party-invite").addPlaceholders("player", inviter.getName(), "uuid", request.getUniqueId().toString()).sendAsJSon(target.getPlayer());
		MMOCore.plugin.requestManager.registerRequest(request);
	}

	// @Override
	// public boolean equals(Object obj) {
	// return obj instanceof Party && ((Party) obj).uuid.equals(uuid);
	// }

	/*
	 * this class makes controling entries and departures and APPLYING PARTY
	 * STAT ATTRIBUTES much easier
	 */
	public class PartyMembers {
		private final List<PlayerData> members = new ArrayList<>();

		public PlayerData get(int count) {
			return members.get(count);
		}

		public boolean has(PlayerData player) {
			return members.contains(player);
		}

		public void add(PlayerData player) {
			members.add(player);

			refreshAttributes();
		}

		public void remove(PlayerData player) {
			members.remove(player);

			refreshAttributes();
			refreshAttributes(player);
		}
		
		public void forEach(Consumer<? super PlayerData> action) {
			members.forEach(action);
		}

		public int count() {
			return members.size();
		}

		public void refreshAttributes() {
			members.forEach(member -> refreshAttributes(member));
		}

		public void refreshAttributes(PlayerData player) {
			MMOCore.plugin.partyManager.getBonuses().forEach(stat -> player.getStats().getInstance(stat).addModifier("party", MMOCore.plugin.partyManager.getBonus(stat)));
		}
	}
}
