package net.Indyuce.mmocore.party;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;

import java.util.List;

public interface AbstractParty {

    /**
     * @return If given player is in that party
     */
    boolean hasMember(Player player);

    /**
     * @return List of online members
     */
    List<PlayerData> getOnlineMembers();

    /**
     * @deprecated Prefer using {@link #getOnlineMembers()}
     */
    @Deprecated
    default PlayerData getMember(int n) {
        return getOnlineMembers().get(n);
    }

    /**
     * @return Number of online/offline players in the party
     */
    int countMembers();
}
