package net.Indyuce.mmocore.guild.provided;

import io.lumine.mythic.lib.comp.interaction.relation.Relationship;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.guild.AbstractGuild;
import net.Indyuce.mmocore.guild.GuildModule;
import org.bukkit.entity.Player;

public class MMOCoreGuildModule implements GuildModule {

    @Override
    public AbstractGuild getGuild(PlayerData playerData) {
        return playerData.getGuild();
    }

    @Override
    public Relationship getRelationship(Player player, Player target) {
        final Guild guild1 = PlayerData.get(player).getGuild();
        if (guild1 == null)
            return Relationship.GUILD_NEUTRAL;

        final Guild guild2 = PlayerData.get(target).getGuild();
        if (guild2 == null)
            return Relationship.GUILD_NEUTRAL;

        return guild1.equals(guild2) ? Relationship.GUILD_ALLY : Relationship.GUILD_ENEMY;
    }
}
