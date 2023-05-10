package net.Indyuce.mmocore.api.event;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.cast.PlayerKey;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Basically an event wrapper for multiple bukkit events that
 * correspond to keys the player can use to cast skill combos.
 *
 * @author jules
 */
public class PlayerKeyPressEvent extends PlayerDataEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Cancellable cancellable;
    private final PlayerKey pressed;

    /**
     * Called when a player presses some key. This event is
     * only fired if the user has chosen the 'key combos' casting
     * method
     *
     * @param playerData  Player pressing the key
     * @param pressed     Key being pressed
     * @param cancellable Cancellable event being wrapped
     */
    public PlayerKeyPressEvent(PlayerData playerData, PlayerKey pressed, Cancellable cancellable) {
        super(playerData);

        this.pressed = pressed;
        this.cancellable = cancellable;
    }

    public PlayerKey getPressed() {
        return pressed;
    }

    @Override
    public boolean isCancelled() {
        return cancellable.isCancelled();
    }

    @Override
    public void setCancelled(boolean b) {
        cancellable.setCancelled(b);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
