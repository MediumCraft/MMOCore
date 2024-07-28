package net.Indyuce.mmocore.party;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface AbstractParty {

    /**
     * @return If given player is in that party
     */
    default boolean hasMember(@NotNull Player player) {
        for (PlayerData member : getOnlineMembers())
            if (member.getPlayer().equals(player)) return true;
        return false;
    }

    /**
     * @return List of online members
     */
    List<PlayerData> getOnlineMembers();

    default PlayerData getMember(int n) {
        return getOnlineMembers().get(n);
    }

    /**
     * @return Number of online/offline players in the party
     */
    int countMembers();
}
