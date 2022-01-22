package net.Indyuce.mmocore.guild;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.jetbrains.annotations.Nullable;

public interface GuildModule {

    @Nullable
    public AbstractGuild getGuild(PlayerData playerData);
}
