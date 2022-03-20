package net.Indyuce.mmocore.experience.dispenser;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.experience.EXPSource;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used to differenciate between the main class experience and
 * experience given in a specific profession. Also being used to
 * monitor EXP holograms.
 */
public interface ExperienceDispenser {

    /**
     * Called when experience is gained in main class/profession
     *
     * @param playerData       Player gaining the experience
     * @param experience       Experience gained. Note that it is a double
     *                         because it gets converted to an integer at
     *                         the very last moment in MMOCore
     * @param hologramLocation Location of displayed hologram. When set to null
     *                         and if exp holograms are enabled it will take the
     *                         player's location instead.
     * @param source           Why the EXP was gained
     */
    void giveExperience(PlayerData playerData, double experience, @Nullable Location hologramLocation, @NotNull EXPSource source);

    /**
     * Experience sources handle both CLASS experience sources and PROFESSION
     * experience sources. Professions have no problem because whatever
     * class the player has chosen, he can get exp in that profession.
     * <p>
     * But class experience sources must first make sure that the player has
     * the right class before giving exp to the player
     */
    boolean shouldHandle(PlayerData playerData);

    @Nullable
    default Location getPlayerLocation(PlayerData player) {
        return player.isOnline() ? MMOCoreUtils.getCenterLocation(player.getPlayer()) : null;
    }
}
