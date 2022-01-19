package net.Indyuce.mmocore.party.compat;

import de.erethon.dungeonsxl.DungeonsXL;
import de.erethon.dungeonsxl.api.player.PlayerGroup;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.party.PartyModule;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DungeonsPartyModule implements PartyModule {

    @Override
    public AbstractParty getParty(PlayerData playerData) {
        PlayerGroup group = DungeonsXL.getInstance().getPlayerGroup(playerData.getPlayer());
        return group == null ? null : new CustomParty(group);
    }

    class CustomParty implements AbstractParty {
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
