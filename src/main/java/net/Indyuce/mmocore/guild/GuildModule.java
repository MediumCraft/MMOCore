package net.Indyuce.mmocore.guild;

import net.Indyuce.mmocore.api.player.PlayerData;

public interface GuildModule {

    public AbstractGuild getGuild(PlayerData playerData);
}
