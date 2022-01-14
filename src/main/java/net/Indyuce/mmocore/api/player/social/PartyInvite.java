package net.Indyuce.mmocore.api.player.social;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;

public class PartyInvite extends Request {
	private final PlayerData target;
	private final Party party;

	public PartyInvite(Party party, PlayerData creator, PlayerData target) {
		super(creator);

		this.party = party;
		this.target = target;
	}

	public Party getParty() {
		return party;
	}

	public PlayerData getPlayer() {
		return target;
	}

	public void deny() {
		MMOCore.plugin.requestManager.unregisterRequest(getUniqueId());
	}

	public void accept() {
		if(party.getMembers().size() >= Math.max(2, MMOCore.plugin.getConfig().getInt("party.max-players", 8))) {
			MMOCore.plugin.configManager.getSimpleMessage("party-is-full").send(target.getPlayer());
			return;
		}
		if(getCreator().isOnline())
			party.removeLastInvite(getCreator().getPlayer());
		party.getMembers().forEach(member -> {
			if(member.isOnline() && target.isOnline())
				MMOCore.plugin.configManager.getSimpleMessage("party-joined-other", "player", target.getPlayer().getName()).send(member.getPlayer());
			});
		if(party.getOwner().isOnline() && target.isOnline())
			MMOCore.plugin.configManager.getSimpleMessage("party-joined", "owner", party.getOwner().getPlayer().getName()).send(target.getPlayer());
		party.addMember(target);
		InventoryManager.PARTY_VIEW.newInventory(target).open();
		MMOCore.plugin.requestManager.unregisterRequest(getUniqueId());
	}
}