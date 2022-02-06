package net.Indyuce.mmocore.guild.compat;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.guild.AbstractGuild;
import net.Indyuce.mmocore.guild.GuildModule;
import org.jetbrains.annotations.NotNull;

public class FactionsGuildModule implements GuildModule {

    @Override
    public AbstractGuild getGuild(PlayerData playerData) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(playerData.getPlayer());
        return fPlayer.hasFaction() ? new CustomGuild(fPlayer.getFaction()) : null;
    }

    class CustomGuild implements AbstractGuild {

        @NotNull
        private final Faction faction;

        CustomGuild(Faction faction) {
            this.faction = faction;
        }
    }
}
