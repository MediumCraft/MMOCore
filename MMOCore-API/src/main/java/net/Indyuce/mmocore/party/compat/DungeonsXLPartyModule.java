package net.Indyuce.mmocore.party.compat;

import de.erethon.dungeonsxl.DungeonsXL;
import de.erethon.dungeonsxl.api.event.group.GroupDisbandEvent;
import de.erethon.dungeonsxl.api.event.group.GroupPlayerJoinEvent;
import de.erethon.dungeonsxl.api.event.group.GroupPlayerLeaveEvent;
import de.erethon.dungeonsxl.api.player.PlayerGroup;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.party.PartyModule;
import net.Indyuce.mmocore.party.PartyUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DungeonsXLPartyModule implements PartyModule, Listener {

    public DungeonsXLPartyModule() {
        Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
    }

    @Override
    public AbstractParty getParty(PlayerData playerData) {
        PlayerGroup group = DungeonsXL.getInstance().getPlayerGroup(playerData.getPlayer());
        return group == null ? null : new CustomParty(group);
    }

    @EventHandler
    public void onPlayerJoin(GroupPlayerJoinEvent event) {
        //We add 1 because this returns the members of the group excluding the player that just joined.
        int membersSize = event.getGroup().getMembers().size() + 1;
        PartyUtils.applyStatBonuses(PlayerData.get(event.getPlayer().getPlayer()), membersSize);
        event.getGroup().getMembers().getOnlinePlayers()
                .forEach(p -> PartyUtils.applyStatBonuses(PlayerData.get(p), membersSize));
    }

    @EventHandler
    public void onPlayerLeave(GroupPlayerLeaveEvent event) {
        int membersSize = event.getGroup().getMembers().size();
        PartyUtils.clearStatBonuses(PlayerData.get(event.getPlayer().getPlayer()));
        event.getGroup().getMembers().getOnlinePlayers()
                .forEach(p -> PartyUtils.applyStatBonuses(PlayerData.get(p), membersSize));
    }

    @EventHandler
    public void onGroupDisband(GroupDisbandEvent event) {
        event.getGroup().getMembers().getOnlinePlayers().forEach(p -> PartyUtils.clearStatBonuses(PlayerData.get(p)));
    }

    private static class CustomParty implements AbstractParty {
        private final PlayerGroup group;

        public CustomParty(PlayerGroup group) {
            this.group = group;
        }

        @Override
        public boolean hasMember(Player player) {
            return group.getMembers().contains(player.getUniqueId());
        }

        @Override
        public List<PlayerData> getOnlineMembers() {
            List<PlayerData> list = new ArrayList<>();

            for (UUID playerUid : group.getMembers().getUniqueIds()) {
                PlayerData found = PlayerData.get(playerUid);
                if (found.isOnline())
                    list.add(found);
            }

            return list;
        }

        @Override
        public int countMembers() {
            return group.getMembers().getUniqueIds().size();
        }
    }
}
