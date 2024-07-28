package net.Indyuce.mmocore.party;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PartyModule {

    @Nullable
    public AbstractParty getParty(@NotNull PlayerData playerData);

    @Deprecated
    default void applyStatBonuses(PlayerData player, int memberCount) {
        PartyUtils.applyStatBonuses(player, memberCount);
    }

    @Deprecated
    default void clearStatBonuses(PlayerData player) {
        PartyUtils.clearStatBonuses(player);
    }
}
