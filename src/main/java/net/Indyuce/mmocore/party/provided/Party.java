package net.Indyuce.mmocore.party.provided;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.Request;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.gui.social.party.EditablePartyView.PartyViewInventory;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.party.AbstractParty;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

public class Party implements AbstractParty {
    private final List<PlayerData> members = new ArrayList<>();
    private final Map<UUID, Long> invites = new HashMap<>();

    /**
     * Used for {@link #equals(Object)}
     */
    private final UUID id = UUID.randomUUID();

    /**
     * Owner has to change when previous owner leaves party
     */
    private PlayerData owner;

    private final MMOCorePartyModule module;

    public Party(MMOCorePartyModule module, PlayerData owner) {
        this.owner = owner;
        this.module = module;

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

    @Override
    public List<PlayerData> getOnlineMembers() {
        List<PlayerData> online = new ArrayList<>();

        for (PlayerData member : members)
            if (member.isOnline())
                online.add(member);

        return online;
    }

    @Override
    public int countMembers() {
        return members.size();
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

    @Override
    public boolean hasMember(OfflinePlayer player) {
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

        module.setParty(data, null);
        clearStatBonuses(data);
        members.forEach(this::applyStatBonuses);
        updateOpenInventories();

        // Disband the party if no member left
        if (members.size() < 1) {
            module.unregisterParty(this);
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
        Party party = (Party) data.getParty();
        if (party != null)
            party.removeMember(data);

        module.setParty(data, this);
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

    /**
     * Applies party stat bonuses to a specific player
     */
    private void applyStatBonuses(PlayerData player) {
        MMOCore.plugin.partyManager.getBonuses().forEach(buff -> buff.multiply(members.size() - 1).register(player.getMMOPlayerData()));
    }

    /**
     * Clear party stat bonuses from a player
     */
    private void clearStatBonuses(PlayerData player) {
        MMOCore.plugin.partyManager.getBonuses().forEach(buff -> buff.unregister(player.getMMOPlayerData()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Party party = (Party) o;
        return id.equals(party.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
