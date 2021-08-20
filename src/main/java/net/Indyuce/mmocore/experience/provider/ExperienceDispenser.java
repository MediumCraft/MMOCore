package net.Indyuce.mmocore.experience.provider;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

/**
 * Used to differenciate between the main class experience
 * and experience given in a specific profession
 */
public interface ExperienceDispenser {

    /**
     * Called when experience is gained in main class/profession
     *
     * @param playerData       Player gaining the experience
     * @param experience       Experience gained. Note that it is a double
     *                         because it gets converted to an integer at
     *                         the very last moment in MMOCore
     * @param hologramLocation Location of displayed hologram, nothing
     *                         is displayed if it's null
     */
    void giveExperience(PlayerData playerData, double experience, @Nullable Location hologramLocation);

    boolean matches(PlayerData playerData);

    default Location getPlayerLocation(PlayerData player) {
        return player.isOnline() ? player.getPlayer().getLocation() : null;
    }
}
