package net.Indyuce.mmocore.player;

import net.Indyuce.mmocore.api.player.PlayerData;

/**
 * Some item that can be unlocked
 *
 * @see {@link PlayerData#unlock(Unlockable)} and {@link PlayerData#hasUnlocked(Unlockable)}
 */
public interface Unlockable {

    /**
     * Format being used is the minecraft's default
     * namespaced key format, e.g skill_tree:strength_1_5
     */
    String getUnlockNamespacedKey();
}
