package net.Indyuce.mmocore.guild.compat;

import io.lumine.mythic.lib.comp.interaction.relation.Relationship;
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

    @Override
    public Relationship getRelationship(Player player, Player target) {
        final Guild guild1 = Guilds.getApi().getGuild(player);
        if (guild1 == null)
            return Relationship.GUILD_NEUTRAL;

        final Guild guild2 = Guilds.getApi().getGuild(target);
        if (guild2 == null)
            return Relationship.GUILD_NEUTRAL;

        return guild1.getId().equals(guild2.getId()) || guild1.getAllies().contains(guild2.getId()) ? Relationship.GUILD_ALLY : Relationship.GUILD_ENEMY;
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
