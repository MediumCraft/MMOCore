package net.Indyuce.mmocore.guild.compat;

import io.lumine.mythic.lib.comp.interaction.relation.Relationship;
import me.ulrich.clans.data.ClanData;
import me.ulrich.clans.data.ClanRivalAlly;
import me.ulrich.clans.packets.interfaces.UClans;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.guild.AbstractGuild;
import net.Indyuce.mmocore.guild.GuildModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class UltimateClansGuildModule implements GuildModule {
    private static final UClans API = (UClans) Bukkit.getPluginManager().getPlugin("UltimateCLans");

    @Override
    public AbstractGuild getGuild(PlayerData playerData) {
        return API.getPlayerAPI().hasClan(playerData.getUniqueId()) ? new CustomGuild(API.getClanAPI().getClan(API.getPlayerAPI().getClanID(playerData.getUniqueId()))) : null;
    }

    @Override
    public Relationship getRelationship(Player player, Player target) {
        if (!API.getPlayerAPI().hasClan(player.getUniqueId()) || !API.getPlayerAPI().hasClan(target.getUniqueId()))
            return Relationship.GUILD_NEUTRAL;

        final ClanRivalAlly clan1 = API.getClanAPI().getClan(API.getPlayerAPI().getClanID(player.getUniqueId())).getRivalAlly();
        final UUID clanId2 = API.getPlayerAPI().getClanID(target.getUniqueId());
        return clan1.getAlly().contains(clanId2) ? Relationship.GUILD_ALLY : clan1.getRival().contains(clanId2) ? Relationship.GUILD_ENEMY : Relationship.GUILD_NEUTRAL;
    }

    class CustomGuild implements AbstractGuild {

        @NotNull
        private final ClanData clan;

        CustomGuild(ClanData clan) {
            this.clan = Objects.requireNonNull(clan);
        }

        @Override
        public boolean hasMember(Player player) {
            // List implementation. Pretty bad
            return clan.getMembers().contains(player);
        }
    }
}
