package net.Indyuce.mmocore.guild.compat;

import io.lumine.mythic.lib.comp.interaction.relation.Relationship;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.guild.AbstractGuild;
import net.Indyuce.mmocore.guild.GuildModule;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kingdoms.constants.kingdom.Kingdom;
import org.kingdoms.constants.kingdom.model.KingdomRelation;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.main.Kingdoms;

import java.util.Objects;

public class KingdomsXGuildModule implements GuildModule {

    @Override
    public AbstractGuild getGuild(PlayerData playerData) {
        KingdomPlayer kPlayer = Kingdoms.get().getDataHandlers().getKingdomPlayerManager().getData(playerData.getUniqueId());
        if (kPlayer == null)
            return null;

        Kingdom kingdom = kPlayer.getKingdom();
        return kingdom == null ? null : new CustomGuild(kingdom);
    }

    @Override
    public Relationship getRelationship(Player player, Player target) {

        final KingdomPlayer player1 = Kingdoms.get().getDataHandlers().getKingdomPlayerManager().getData(player.getUniqueId());
        if (player1 == null)
            return null;

        final Kingdom kingdom1 = player1.getKingdom();
        if (kingdom1 == null)
            return Relationship.GUILD_NEUTRAL;

        final KingdomPlayer player2 = Kingdoms.get().getDataHandlers().getKingdomPlayerManager().getData(target.getUniqueId());
        if (player2 == null)
            return null;

        final Kingdom kingdom2 = player2.getKingdom();
        if (kingdom2 == null)
            return Relationship.GUILD_NEUTRAL;

        return adapt(kingdom1.getRelationWith(kingdom2));
    }

    private Relationship adapt(KingdomRelation rel) {
        switch (rel) {
            case ALLY:
            case SELF:
                return Relationship.GUILD_ALLY;
            case ENEMY:
                return Relationship.GUILD_ENEMY;
            case NEUTRAL:
            case TRUCE:
            case NATION:
            default:
                return Relationship.GUILD_NEUTRAL;

        }
    }

    class CustomGuild implements AbstractGuild {

        @NotNull
        private final Kingdom kingdom;

        CustomGuild(Kingdom kingdom) {
            this.kingdom = Objects.requireNonNull(kingdom);
        }

        @Override
        public boolean hasMember(Player player) {
            return kingdom.isMember(player);
        }
    }
}
