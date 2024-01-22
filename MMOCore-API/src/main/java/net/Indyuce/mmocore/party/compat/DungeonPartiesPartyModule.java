package net.Indyuce.mmocore.party.compat;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.party.PartyModule;
import net.playavalon.avnparty.AvNParty;
import net.playavalon.avnparty.party.Party;
import net.playavalon.avnparty.player.AvalonPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DungeonPartiesPartyModule implements PartyModule, Listener {

    @Nullable
    @Override
    public AbstractParty getParty(PlayerData playerData) {
        final @Nullable Party party = AvNParty.plugin.players.get(playerData.getPlayer()).getParty();
        return party == null ? null : new CustomParty(party);
    }

    class CustomParty implements AbstractParty {
        private final Party party;

        public CustomParty(Party party) {
            this.party = party;
        }

        @Override
        public boolean hasMember(Player player) {
            for (AvalonPlayer member : party.getPlayers())
                if (member.getPlayer().getUniqueId().equals(player.getUniqueId())) return true;
            return false;
        }

        @Override
        public List<PlayerData> getOnlineMembers() {
            final List<PlayerData> list = new ArrayList<>();

            for (AvalonPlayer member : party.getPlayers())
                try {
                    list.add(PlayerData.get(member.getPlayer()));
                } catch (Exception ignored) {
                }

            return list;
        }

        @Override
        public int countMembers() {
            return party.getPlayers().size();
        }
    }
}
