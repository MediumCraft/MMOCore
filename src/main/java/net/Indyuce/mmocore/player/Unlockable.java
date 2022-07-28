package net.Indyuce.mmocore.player;

import net.Indyuce.mmocore.api.player.PlayerData;

/**
 * Some item that can be unlocked. All unlockables are saved in the
 * same list in the player data. This useful list can be used for:
 * - waypoints
 * - skill tree nodes
 * - skills using skill books? TODO
 * - external plugins that implement other unlockable items
 *
 * @see {@link PlayerData#unlock(Unlockable)} and {@link PlayerData#hasUnlocked(Unlockable)}
 */
public interface Unlockable {

    /**
     * Format being used is the minecraft's default namespaced
     * key format, e.g "skill_tree:strength_1_5" for readability
     */
    String getUnlockNamespacedKey();
}
