package net.Indyuce.mmocore.party.compat;

import de.simonsator.partyandfriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.partyandfriends.api.pafplayers.PAFPlayerManager;
import de.simonsator.partyandfriends.api.party.PartyManager;
import de.simonsator.partyandfriends.api.party.PlayerParty;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.party.PartyModule;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PAFPartyModule implements PartyModule {

    @Nullable
    @Override
    public AbstractParty getParty(PlayerData playerData) {
        PlayerParty party = PartyManager.getInstance().getParty(playerData.getUniqueId());
        return party == null ? null : new CustomParty(party);
    }

    class CustomParty implements AbstractParty {
        private final PlayerParty party;

        public CustomParty(PlayerParty party) {
            this.party = party;
        }

        @Override
        public boolean hasMember(Player player) {
            return party.isInParty(PAFPlayerManager.getInstance().getPlayer(player));
        }

        @Override
        public List<PlayerData> getOnlineMembers() {
            List<PlayerData> list = new ArrayList<>();

            for (OnlinePAFPlayer online : party.getAllPlayers())
                list.add(PlayerData.get(online.getUniqueId()));

            return list;
        }

        @Override
        public int countMembers() {
            return party.getAllPlayers().size();
        }
    }
}
