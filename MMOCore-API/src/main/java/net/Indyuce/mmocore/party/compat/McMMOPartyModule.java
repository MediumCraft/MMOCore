package net.Indyuce.mmocore.party.compat;

import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
import com.gmail.nossr50.party.PartyManager;
import com.gmail.nossr50.util.player.UserManager;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.party.PartyModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class McMMOPartyModule implements PartyModule, Listener {


    public McMMOPartyModule() {
        Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
    }

    @Nullable
    @Override
    public AbstractParty getParty(PlayerData playerData) {
        McMMOPlayer extPlayerData = UserManager.getPlayer(playerData.getPlayer());
        Party party = extPlayerData == null ? null : extPlayerData.getParty();
        return party == null ? null : new CustomParty(party);
    }


    @EventHandler
    public void onChange(McMMOPartyChangeEvent event) {
        if (event.getNewParty() != null) {
            Party party = PartyManager.getParty(event.getNewParty());
            if (party != null) {
                //This is the size of the party before the player joins=> we increment it by 1.
                int membersSize = party.getMembers().size();
                if(membersSize!=1 || party.getOnlineMembers().get(0)!=event.getPlayer()) {
                    party.getOnlineMembers()
                            .forEach(p -> applyStatBonuses(PlayerData.get(p), membersSize+1));
                    applyStatBonuses(PlayerData.get(event.getPlayer()), membersSize+1);
                }
            }
        }
        if (event.getOldParty() != null) {
            Party party = PartyManager.getParty(event.getOldParty());
            if (party != null) {
                //This is the size of the party before the player leaves=> we decrement it by 1.
                int membersSize = party.getMembers().size() - 1;
                party.getOnlineMembers()
                        .forEach(p -> applyStatBonuses(PlayerData.get(p), membersSize));
                clearStatBonuses(PlayerData.get(event.getPlayer().getPlayer()));
            }
        }
    }

    /**
     * Applies party stat bonuses to a specific player
     */
    private void applyStatBonuses(PlayerData player, int membersSize) {
        MMOCore.plugin.partyManager.getBonuses().forEach(buff -> buff.multiply(membersSize - 1).register(player.getMMOPlayerData()));
    }

    /**
     * Clear party stat bonuses from a player
     */
    private void clearStatBonuses(PlayerData player) {
        MMOCore.plugin.partyManager.getBonuses().forEach(buff -> buff.unregister(player.getMMOPlayerData()));
    }

    class CustomParty implements AbstractParty, Listener {
        private final Party party;

        public CustomParty(Party party) {
            this.party = party;
            Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
        }

        @Override
        public boolean hasMember(Player player) {
            return party.hasMember(player.getUniqueId());
        }

        @Override
        public List<PlayerData> getOnlineMembers() {
            List<PlayerData> list = new ArrayList<>();

            for (Player online : party.getOnlineMembers())
                list.add(PlayerData.get(online.getUniqueId()));

            return list;
        }

        @Override
        public int countMembers() {
            return party.getMembers().size();
        }
    }
}
