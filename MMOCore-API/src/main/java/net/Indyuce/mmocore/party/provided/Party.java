package net.Indyuce.mmocore.party.provided;

import io.lumine.mythic.lib.version.VInventoryView;
import io.lumine.mythic.lib.version.VersionUtils;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.Request;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.gui.social.party.EditablePartyView;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.party.PartyUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
    @NotNull
    private PlayerData owner;

    private final MMOCorePartyModule module;

    public Party(MMOCorePartyModule module, PlayerData owner) {
        this.module = module;
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

    @Override
    public List<PlayerData> getOnlineMembers() {
        List<PlayerData> online = new ArrayList<>();

        for (PlayerData member : members)
            if (member.isOnline())
                online.add(member);

        return online;
    }

    public int getLevel() {
        return owner.getLevel();
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
        if (data.isOnline() && VersionUtils.getOpen(data.getPlayer()).getTopInventory().getHolder() instanceof EditablePartyView.PartyViewInventory)
            InventoryManager.PARTY_CREATION.newInventory(data).open();

        members.remove(data);

        module.setParty(data, null);
        PartyUtils.clearStatBonuses(data);
        members.forEach(member -> PartyUtils.applyStatBonuses(member, members.size()));
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
                ConfigMessage.fromKey("transfer-party-ownership").send(owner.getPlayer());
        }
    }

    public void addMember(PlayerData data) {
        Party party = (Party) data.getParty();
        if (party != null)
            party.removeMember(data);

        module.setParty(data, this);
        members.add(data);
        members.forEach(member -> PartyUtils.applyStatBonuses(member, members.size()));

        updateOpenInventories();
    }

    private void updateOpenInventories() {
        for (PlayerData member : members) {
            final VInventoryView open = VersionUtils.getOpen(member.getPlayer());
            if (member.isOnline() && open.getTopInventory().getHolder() instanceof EditablePartyView.PartyViewInventory)
                ((PluginInventory) open.getTopInventory().getHolder()).open();
        }
    }

    @Deprecated
    public void sendPartyInvite(PlayerData inviter, PlayerData target) {
        sendInvite(inviter, target);
    }

    public void sendInvite(PlayerData inviter, PlayerData target) {
        invites.put(target.getUniqueId(), System.currentTimeMillis());
        Request request = new PartyInvite(this, inviter, target);
        ConfigMessage.fromKey("party-invite").addPlaceholders("player", inviter.getPlayer().getName(), "uuid", request.getUniqueId().toString())
                .send(target.getPlayer());

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
