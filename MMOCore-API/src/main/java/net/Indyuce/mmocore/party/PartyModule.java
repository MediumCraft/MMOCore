package net.Indyuce.mmocore.party;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.jetbrains.annotations.Nullable;

public interface PartyModule {

    @Nullable
    public AbstractParty getParty(PlayerData playerData);


    /**
     * Applies party stat bonuses to a specific player
     */
    default void applyStatBonuses(PlayerData player, int memberCount) {
        MMOCore.plugin.partyManager.getBonuses().forEach(buff -> buff.multiply(memberCount - 1).register(player.getMMOPlayerData()));
    }

    /**
     * Clear party stat bonuses from a player
     */
    default void clearStatBonuses(PlayerData player) {
        MMOCore.plugin.partyManager.getBonuses().forEach(buff -> buff.unregister(player.getMMOPlayerData()));
    }
}
