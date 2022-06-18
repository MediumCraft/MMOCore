package net.Indyuce.mmocore.guild.compat;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.guild.AbstractGuild;
import net.Indyuce.mmocore.guild.GuildModule;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GuildsGuildModule implements GuildModule {

    @Override
    public AbstractGuild getGuild(PlayerData playerData) {
        Guild guild = Guilds.getApi().getGuild(playerData.getUniqueId());
        return guild == null ? null : new CustomGuild(guild);
    }

    class CustomGuild implements AbstractGuild {

        @NotNull
        private final Guild guild;

        CustomGuild(Guild guild) {
            this.guild = Objects.requireNonNull(guild);
        }

        @Override
        public boolean hasMember(Player player) {
            return guild.getMember(player.getUniqueId()) != null;
        }
    }
}
