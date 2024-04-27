package net.Indyuce.mmocore.guild.provided;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.Request;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.gui.social.guild.EditableGuildView;
import net.Indyuce.mmocore.guild.AbstractGuild;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

public class Guild implements AbstractGuild {
    private final Map<UUID, Long> invites = new HashMap<>();
    private final String guildId, guildName, guildTag;

    private final Set<UUID> members = new HashSet<>();

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

    public long getLastInvite(Player player) {
        return invites.containsKey(player.getUniqueId()) ? invites.get(player.getUniqueId()) : 0;
    }

    public void removeLastInvite(Player player) {
        invites.remove(player.getUniqueId());
    }

    public void removeMember(UUID uuid) {
        removeMember(uuid, false);
    }

    // Disband boolean is to prevent co-modification exception when disbanding a guild
    public void removeMember(UUID uuid, boolean disband) {
        PlayerData data = PlayerData.get(uuid);
        if (data != null && data.isOnline() && data.getPlayer().getOpenInventory() != null && data.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof EditableGuildView.GuildViewInventory)
            InventoryManager.GUILD_CREATION.newInventory(data).open();

        if (!disband)
            members.remove(uuid);
        if (data != null)
            data.setGuild(null);
        reopenInventories();

        // Disband the guild if no member left
        if (members.size() < 1) {
            MMOCore.plugin.nativeGuildManager.unregisterGuild(this);
            return;
        }

        // Transfer ownership
        if (owner.equals(uuid)) {
            owner = members.stream().findAny().get();
            ConfigMessage.fromKey("transfer-guild-ownership").send(Bukkit.getPlayer(owner));
        }
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
        for (UUID uuid : members) {
            PlayerData member = PlayerData.get(uuid);
            if (member != null && member.isOnline() && member.getPlayer().getOpenInventory() != null && member.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof EditableGuildView.GuildViewInventory)
                ((PluginInventory) member.getPlayer().getOpenInventory().getTopInventory().getHolder()).open();
        }
    }

    public void sendGuildInvite(PlayerData inviter, PlayerData target) {
        invites.put(target.getUniqueId(), System.currentTimeMillis());
        Request request = new GuildInvite(this, inviter, target);
        ConfigMessage.fromKey("guild-invite").addPlaceholders("player", inviter.getPlayer().getName(), "uuid", request.getUniqueId().toString()).send(target.getPlayer());
        MMOCore.plugin.requestManager.registerRequest(request);
    }

    @Override
    public boolean hasMember(Player player) {
        return hasMember(player.getUniqueId());
    }

    public boolean hasMember(UUID player) {
        return members.contains(player);
    }

    public List<UUID> listMembers() {
        return new ArrayList<>(members);
    }

    public void forEachMember(Consumer<? super UUID> action) {
        members.forEach(action);
    }

    public int countOnlineMembers() {
        int online = 0;

        for (UUID member : members)
            if (Bukkit.getOfflinePlayer(member).isOnline())
                online++;

        return online;
    }

    public int countMembers() {
        return members.size();
    }
}
