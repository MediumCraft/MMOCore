package net.Indyuce.mmocore.api.quest.trigger.api;

import net.Indyuce.mmocore.api.player.PlayerData;

/**
 * Cancelable triggers cause problems when letting the player reset
 * their advancement on things they can spend points in/level up.
 * If you give access to some resource to the player via a trigger,
 * you must take it away when resetting their progression.
 */
public interface Removable {
    public void remove(PlayerData playerData);
}
