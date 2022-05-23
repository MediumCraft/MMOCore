package net.Indyuce.mmocore.experience;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public class SimpleExperienceObject implements ExperienceDispenser {

    @Override
    public void giveExperience(PlayerData playerData, double experience, @Nullable Location hologramLocation, EXPSource source) {
        hologramLocation = !MMOCore.plugin.getConfig().getBoolean("display-main-class-exp-holograms") ? null
                : hologramLocation == null ? getPlayerLocation(playerData) : hologramLocation;
        playerData.giveExperience(experience, source, hologramLocation, true);
    }

    @Override
    public boolean shouldHandle(PlayerData playerData) {
        return true;
    }
}
