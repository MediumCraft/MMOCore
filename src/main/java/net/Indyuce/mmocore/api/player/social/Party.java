package net.Indyuce.mmocore.api.player.social;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.gui.social.party.EditablePartyView.PartyViewInventory;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

public class Party {
    private final PartyMembers members = new PartyMembers();
    private final Map<UUID, Long> invites = new HashMap<>();

    // used to check if two parties are the same
    private final UUID id = UUID.randomUUID();

    /*
     * owner changes when the old owner leaves party
     */
    private PlayerData owner;

    public Party(PlayerData owner) {
        this.owner = owner;
        addMember(owner);
    }

    public UUID getUniqueId() {
        return id;
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

    public void removeLastInvite(Player player) {
        invites.remove(player.getUniqueId());
    }

    public void removeMember(PlayerData data) {
        removeMember(data, true);
    }

    public void removeMember(PlayerData data, boolean notify) {
        if (data.isOnline() && data.getPlayer().getOpenInventory() != null
                && data.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof PartyViewInventory)
            InventoryManager.PARTY_CREATION.newInventory(data).open();

        members.remove(data);
        data.setParty(null);

        reopenInventories();

        // Disband the party if no member left
        if (members.count() < 1) {
            MMOCore.plugin.partyManager.unregisterParty(this);
            return;
        }

        // Transfer ownership
        if (owner.equals(data)) {
            owner = members.get(0);
            if (notify && owner.isOnline())
                MMOCore.plugin.configManager.getSimpleMessage("transfer-party-ownership").send(owner.getPlayer());
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
            if (member.isOnline() && member.getPlayer().getOpenInventory() != null
                    && member.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof PartyViewInventory)
                ((PluginInventory) member.getPlayer().getOpenInventory().getTopInventory().getHolder()).open();
    }

    public void sendPartyInvite(PlayerData inviter, PlayerData target) {
        invites.put(target.getUniqueId(), System.currentTimeMillis());
        Request request = new PartyInvite(this, inviter, target);
        if (inviter.isOnline() && target.isOnline())
            new ConfigMessage("party-invite").addPlaceholders("player", inviter.getPlayer().getName(), "uuid", request.getUniqueId().toString())
                    .sendAsJSon(target.getPlayer());
        MMOCore.plugin.requestManager.registerRequest(request);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Party && ((Party) obj).getUniqueId().equals(getUniqueId());
    }

    /*
     * this class makes controling entries and departures and APPLYING PARTY
     * STAT ATTRIBUTES much easier
     */
    public static class PartyMembers {
        private final List<PlayerData> members = new ArrayList<>();

        public PlayerData get(int count) {
            return members.get(count);
        }

        public boolean has(PlayerData player) {
            return members.contains(player);
        }

        public boolean has(Player player) {
            for (PlayerData member : members)
                if (member.getUniqueId().equals(player.getUniqueId()))
                    return true;
            return false;
        }

        public void add(PlayerData player) {
            members.add(player);

            members.forEach(this::applyAttributes);
        }

        public void remove(PlayerData player) {
            members.remove(player);

            members.forEach(this::applyAttributes);
            clearAttributes(player);
        }

        public void forEach(Consumer<? super PlayerData> action) {
            members.forEach(action);
        }

        public int count() {
            return members.size();
        }

        private void applyAttributes(PlayerData player) {
            MMOCore.plugin.partyManager.getBonuses().forEach(stat -> player.getStats().getInstance(stat).addModifier("mmocoreParty",
                    MMOCore.plugin.partyManager.getBonus(stat).multiply(members.size() - 1)));
        }

        private void clearAttributes(PlayerData player) {
            MMOCore.plugin.partyManager.getBonuses().forEach(stat -> player.getStats().getInstance(stat).remove("mmocoreParty"));
        }
    }
}
