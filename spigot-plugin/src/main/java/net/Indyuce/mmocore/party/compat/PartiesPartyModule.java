package net.Indyuce.mmocore.party.compat;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.party.PartyModule;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PartiesPartyModule implements PartyModule {
    private final PartiesAPI api = Parties.getApi();

    @Nullable
    @Override
    public AbstractParty getParty(PlayerData playerData) {
        Party party = api.getParty(playerData.getUniqueId());
        return party == null ? null : new CustomParty(party);
    }

    class CustomParty implements AbstractParty {
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
