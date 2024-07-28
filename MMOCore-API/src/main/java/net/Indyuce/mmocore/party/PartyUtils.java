package net.Indyuce.mmocore.party;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.jetbrains.annotations.NotNull;

public class PartyUtils {

    /**
     * Applies party stat bonuses to a specific player
     */
    public static void applyStatBonuses(@NotNull PlayerData player, int memberCount) {
        MMOCore.plugin.partyManager.getBonuses().forEach(buff -> buff.multiply(memberCount - 1).register(player.getMMOPlayerData()));
    }

    /**
     * Clear party stat bonuses from a player
     */
    public static void clearStatBonuses(@NotNull PlayerData player) {
        MMOCore.plugin.partyManager.getBonuses().forEach(buff -> buff.unregister(player.getMMOPlayerData()));
    }
}
