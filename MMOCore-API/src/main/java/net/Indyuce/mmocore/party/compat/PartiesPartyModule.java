package net.Indyuce.mmocore.party.compat;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.events.bukkit.player.BukkitPartiesPlayerPostJoinEvent;
import com.alessiodp.parties.api.events.bukkit.player.BukkitPartiesPlayerPreLeaveEvent;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.party.PartyModule;
import net.Indyuce.mmocore.party.PartyUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PartiesPartyModule implements PartyModule, Listener {

    public PartiesPartyModule() {
        Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
    }

    @Nullable
    @Override
    public AbstractParty getParty(PlayerData playerData) {
        PartiesAPI api = Parties.getApi();
        PartyPlayer partyPlayer = api.getPartyPlayer(playerData.getUniqueId());
        Party party = api.getParty(partyPlayer.getPartyId());
        return party == null ? null : new CustomParty(party);
    }


    @EventHandler
    public void onPlayerJoin(BukkitPartiesPlayerPostJoinEvent event) {
        int membersSize = event.getParty().getMembers().size();
        event.getParty().getOnlineMembers()
                .forEach(p -> PartyUtils.applyStatBonuses(PlayerData.get(p.getPlayerUUID()), membersSize));
    }

    @EventHandler
    public void onPlayerLeave(BukkitPartiesPlayerPreLeaveEvent event) {
        int membersSize = event.getParty().getMembers().size();
        PartyUtils.clearStatBonuses(PlayerData.get(event.getPartyPlayer().getPlayerUUID()));
        event.getParty().getOnlineMembers()
                .forEach(p -> PartyUtils.applyStatBonuses(PlayerData.get(p.getPlayerUUID()), membersSize));
    }

    private static class CustomParty implements AbstractParty {
        private final Party party;

        public CustomParty(Party party) {
            this.party = party;
        }

        @Override
        public boolean hasMember(Player player) {
            for (PartyPlayer member : party.getOnlineMembers())
                if (member.getPlayerUUID().equals(player.getUniqueId()))
                    return true;

            return false;
        }

        @Override
        public List<PlayerData> getOnlineMembers() {
            List<PlayerData> list = new ArrayList<>();

            for (PartyPlayer member : party.getOnlineMembers())
                list.add(PlayerData.get(member.getPlayerUUID()));

            return list;
        }

        @Override
        public int countMembers() {
            return party.getMembers().size();
        }
    }
}
