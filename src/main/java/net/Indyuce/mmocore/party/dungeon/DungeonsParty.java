package net.Indyuce.mmocore.party.dungeon;

import de.erethon.dungeonsxl.api.player.PlayerGroup;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.AbstractParty;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DungeonsParty implements AbstractParty {
    private final PlayerGroup group;

    public DungeonsParty(PlayerGroup group) {
        this.group = group;
    }

    @Override
    public boolean hasMember(OfflinePlayer player) {
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
