package net.Indyuce.mmocore.party;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.jetbrains.annotations.Nullable;

public interface PartyModule {

    @Nullable
    public AbstractParty getParty(PlayerData playerData);
}
