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
		party.removeLastInvite(getCreator().getPlayer());
		party.getMembers().forEach(member -> member.getPlayer().sendMessage(MMOCore.plugin.configManager.getSimpleMessage("party-joined-other", "player", target.getPlayer().getName())));
		target.getPlayer().sendMessage(MMOCore.plugin.configManager.getSimpleMessage("party-joined", "owner", party.getOwner().getPlayer().getName()));
		party.addMember(target);
		InventoryManager.PARTY_VIEW.newInventory(target).open();
		MMOCore.plugin.requestManager.unregisterRequest(getUniqueId());
	}
}