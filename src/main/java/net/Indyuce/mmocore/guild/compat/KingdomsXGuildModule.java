package net.Indyuce.mmocore.guild.compat;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.guild.AbstractGuild;
import net.Indyuce.mmocore.guild.GuildModule;

public class KingdomsXGuildModule implements GuildModule {

    @Override
    public AbstractGuild getGuild(PlayerData playerData) {
        throw new RuntimeException("Not supported");
    }

    class CustomGuild implements AbstractGuild {
        CustomGuild() {
        }
    }
}
