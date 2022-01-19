package net.Indyuce.mmocore.party.compat;

import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.util.player.UserManager;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.party.PartyModule;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class McMMOPartyModule implements PartyModule {

    @Nullable
    @Override
    public AbstractParty getParty(PlayerData playerData) {
        McMMOPlayer extPlayerData = UserManager.getPlayer(playerData.getPlayer());
        Party party = extPlayerData == null ? null : extPlayerData.getParty();
        return party == null ? null : new CustomParty(party);
    }

    class CustomParty implements AbstractParty {
        private final Party party;

        public CustomParty(Party party) {
            this.party = party;
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
