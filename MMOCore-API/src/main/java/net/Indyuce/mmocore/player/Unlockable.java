package net.Indyuce.mmocore.player;

import net.Indyuce.mmocore.api.player.PlayerData;

/**
 * Some object that can be unlocked. All unlockables are saved in the
 * same list in the player data, in the form of name-spaced keys. This
 * tool is currently by:
 * - skills
 * - skill slots
 * - waypoints
 * <p>
 * These objects are specific to the player's class and will not be
 * transferred over to the new class if the player switches classes.
 *
 * @see PlayerData#unlock(Unlockable)
 * @see PlayerData#hasUnlocked(Unlockable)
 */
public interface Unlockable {

    /**
     * Format being used is the minecraft's default name-spaced
     * key format, e.g "skill_tree:strength_1_5" for readability
     */
    String getUnlockNamespacedKey();

    boolean isUnlockedByDefault();

    void whenLocked(PlayerData playerData);

    void whenUnlocked(PlayerData playerData);
}
