package net.Indyuce.mmocore.party.provided;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.Request;
import net.Indyuce.mmocore.manager.InventoryManager;

public class PartyInvite extends Request {
    private final Party party;

    public PartyInvite(Party party, PlayerData creator, PlayerData target) {
        super(creator, target);

        this.party = party;
    }

    public Party getParty() {
        return party;
    }

    @Override
    public void whenDenied() {
        // Nothing
    }

    @Override
    public void whenAccepted() {
        if (party.getMembers().size() >= Math.max(2, MMOCore.plugin.getConfig().getInt("party.max-players", 8))) {
            MMOCore.plugin.configManager.getSimpleMessage("party-is-full").send(getTarget().getPlayer());
            return;
        }
        if (getCreator().isOnline())
            party.removeLastInvite(getCreator().getPlayer());
        party.getMembers().forEach(member -> {
            if (member.isOnline())
                MMOCore.plugin.configManager.getSimpleMessage("party-joined-other", "player", getTarget().getPlayer().getName()).send(member.getPlayer());
        });
        if (party.getOwner().isOnline())
            MMOCore.plugin.configManager.getSimpleMessage("party-joined", "owner", party.getOwner().getPlayer().getName()).send(getTarget().getPlayer());
        party.addMember(getTarget());
        InventoryManager.PARTY_VIEW.newInventory(getTarget()).open();
    }
}