package net.Indyuce.mmocore.party.compat;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.party.PartyModule;
import org.bukkit.entity.Player;

import java.util.List;

@Deprecated
public class DungeonsPartyModule implements PartyModule {

    @Override
    public AbstractParty getParty(PlayerData playerData) {
        throw new RuntimeException("Not supported");
    }

    private static class CustomParty implements AbstractParty {

        public CustomParty() {
        }

        @Override
        public boolean hasMember(Player player) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public List<PlayerData> getOnlineMembers() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public int countMembers() {
            throw new RuntimeException("Not supported");
        }
    }
}
