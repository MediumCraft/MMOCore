package net.Indyuce.mmocore.guild.compat;

import me.ulrich.clans.data.ClanData;
import me.ulrich.clans.packets.interfaces.UClans;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.guild.AbstractGuild;
import net.Indyuce.mmocore.guild.GuildModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UltimateClansGuildModule implements GuildModule {
    private static final UClans API = (UClans) Bukkit.getPluginManager().getPlugin("UltimateCLans");

    @Override
    public AbstractGuild getGuild(PlayerData playerData) {
        return API.getPlayerAPI().hasClan(playerData.getUniqueId()) ? new CustomGuild(API.getClanAPI().getClan(API.getPlayerAPI().getClanID(playerData.getUniqueId()))) : null;
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
