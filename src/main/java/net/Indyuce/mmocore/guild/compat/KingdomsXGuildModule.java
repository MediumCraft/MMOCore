package net.Indyuce.mmocore.guild.compat;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.guild.AbstractGuild;
import net.Indyuce.mmocore.guild.GuildModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kingdoms.constants.kingdom.Kingdom;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.main.Kingdoms;

public class KingdomsXGuildModule implements GuildModule {

    @Override
    public AbstractGuild getGuild(PlayerData playerData) {
        KingdomPlayer kPlayer = Kingdoms.get().getDataHandlers().getKingdomPlayerManager().getData(playerData.getUniqueId());
        Kingdom kingdom = kPlayer == null ? null : kPlayer.getKingdom();
        return kingdom == null ? null : new CustomGuild(kingdom);
    }

    class CustomGuild implements AbstractGuild {

        @NotNull
        private final Kingdom kingdom;

        CustomGuild(Kingdom kingdom) {
            this.kingdom = kingdom;
        }
    }
}
