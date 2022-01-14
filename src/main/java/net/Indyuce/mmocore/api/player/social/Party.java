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
    private final List<PlayerData> members = new ArrayList<>();
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

    public List<PlayerData> getMembers() {
        return members;
    }

    public List<PlayerData> getOnlineMembers() {
        List<PlayerData> online = new ArrayList<>();

        for (PlayerData member : members)
            if (member.isOnline())
                online.add(member);

        return online;
    }

    public PlayerData getMember(int index) {
        return members.get(index);
    }

    public long getLastInvite(Player player) {
        return invites.containsKey(player.getUniqueId()) ? invites.get(player.getUniqueId()) : 0;
    }

    public void removeLastInvite(Player player) {
        invites.remove(player.getUniqueId());
    }

    public boolean hasMember(PlayerData playerData) {
        return hasMember(playerData.getUniqueId());
    }

    public boolean hasMember(Player player) {
        return hasMember(player.getUniqueId());
    }

    public boolean hasMember(UUID uuid) {
        for (PlayerData member : members)
            if (member.getUniqueId().equals(uuid))
                return true;
        return false;
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
        clearStatBonuses(data);
        members.forEach(this::applyStatBonuses);
        updateOpenInventories();

        // Disband the party if no member left
        if (members.size() < 1) {
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
        members.forEach(this::applyStatBonuses);

        updateOpenInventories();
    }

    private void updateOpenInventories() {
        for (PlayerData member : members)
            if (member.isOnline() && member.getPlayer().getOpenInventory() != null
                    && member.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof PartyViewInventory)
                ((PluginInventory) member.getPlayer().getOpenInventory().getTopInventory().getHolder()).open();
    }

    @Deprecated
    public void sendPartyInvite(PlayerData inviter, PlayerData target) {
        sendInvite(inviter, target);
    }

    public void sendInvite(PlayerData inviter, PlayerData target) {
        invites.put(target.getUniqueId(), System.currentTimeMillis());
        Request request = new PartyInvite(this, inviter, target);
        if (inviter.isOnline() && target.isOnline())
            new ConfigMessage("party-invite").addPlaceholders("player", inviter.getPlayer().getName(), "uuid", request.getUniqueId().toString())
                    .sendAsJSon(target.getPlayer());
        MMOCore.plugin.requestManager.registerRequest(request);
    }

    /**
     * An issue can happen if the consumer given as parameter
     * modifies the member list during list iteration.
     * <p>
     * To solve this, this method first copies the member list
     * and iterates through it to avoid the exception.
     */
    public void forEachMember(Consumer<PlayerData> action) {
        new ArrayList<>(members).forEach(action);
    }

    private static final String PARTY_BUFF_MODIFIER_KEY = "mmocoreParty";

    /**
     * Applies party stat bonuses to a specific player
     */
    private void applyStatBonuses(PlayerData player) {
        MMOCore.plugin.partyManager.getBonuses().forEach(stat -> player.getStats().getInstance(stat).addModifier(PARTY_BUFF_MODIFIER_KEY,
                MMOCore.plugin.partyManager.getBonus(stat).multiply(members.size() - 1)));
    }

    /**
     * Clear party stat bonuses from a player
     */
    private void clearStatBonuses(PlayerData player) {
        MMOCore.plugin.partyManager.getBonuses().forEach(stat -> player.getStats().getInstance(stat).remove(PARTY_BUFF_MODIFIER_KEY));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Party && ((Party) obj).getUniqueId().equals(getUniqueId());
    }
}
