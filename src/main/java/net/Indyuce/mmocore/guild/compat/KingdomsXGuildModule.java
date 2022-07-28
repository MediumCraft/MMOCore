package net.Indyuce.mmocore.guild.compat;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.guild.AbstractGuild;
import net.Indyuce.mmocore.guild.GuildModule;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kingdoms.constants.kingdom.Kingdom;
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
