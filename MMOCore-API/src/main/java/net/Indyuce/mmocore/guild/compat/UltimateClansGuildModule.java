package net.Indyuce.mmocore.guild.compat;

import io.lumine.mythic.lib.comp.interaction.relation.Relationship;
import me.ulrich.clans.data.ClanData;
import me.ulrich.clans.data.ClanRivalAlly;
import me.ulrich.clans.interfaces.UClans;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.guild.AbstractGuild;
import net.Indyuce.mmocore.guild.GuildModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class UltimateClansGuildModule implements GuildModule {
    private static final UClans API = (UClans) Bukkit.getPluginManager().getPlugin("UltimateCLans");

    @Override
    public AbstractGuild getGuild(PlayerData playerData) {
        final Optional<ClanData> clan_ = API.getPlayerAPI().getPlayerClan(playerData.getUniqueId());
        return clan_.isEmpty() ? null : new CustomGuild(clan_.get());
    }

    @Override
    public Relationship getRelationship(Player player, Player target) {
        final Optional<ClanData> _clan1 = API.getPlayerAPI().getPlayerClan(player.getUniqueId());
        if (_clan1.isEmpty()) return Relationship.GUILD_NEUTRAL;

        final Optional<ClanData> _clan2 = API.getPlayerAPI().getPlayerClan(target.getUniqueId());
        if (_clan2.isEmpty()) return Relationship.GUILD_NEUTRAL;

        final ClanRivalAlly allies1 = _clan1.get().getRivalAlly();
        final UUID uuid2 = _clan2.get().getId();
        return allies1.getAlly().contains(uuid2) ? Relationship.GUILD_ALLY : allies1.getRival().contains(uuid2) ? Relationship.GUILD_ENEMY : Relationship.GUILD_NEUTRAL;
    }

    class CustomGuild implements AbstractGuild {

        @NotNull
        private final ClanData clan;

        CustomGuild(ClanData clan) {
            this.clan = Objects.requireNonNull(clan);
        }

        @Override
        public boolean hasMember(Player player) {
            return clan.getMembers().contains(player);
        }
    }
}
