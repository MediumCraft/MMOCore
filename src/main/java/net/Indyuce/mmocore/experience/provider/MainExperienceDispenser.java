package net.Indyuce.mmocore.experience.provider;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.experience.EXPSource;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public class MainExperienceDispenser implements ExperienceDispenser {
    private final PlayerClass profess;

    public MainExperienceDispenser(PlayerClass profess) {
        this.profess = profess;
    }

    @Override
    public void giveExperience(PlayerData playerData, double experience, @Nullable Location hologramLocation) {
        hologramLocation = !MMOCore.plugin.getConfig().getBoolean("display-main-class-exp-holograms") ? null
                : hologramLocation == null ? getPlayerLocation(playerData) : hologramLocation;
        playerData.giveExperience(experience, EXPSource.SOURCE, hologramLocation);

    }

    @Override
    public boolean shouldHandle(PlayerData playerData) {
        return playerData.getProfess().equals(profess);
    }
}
