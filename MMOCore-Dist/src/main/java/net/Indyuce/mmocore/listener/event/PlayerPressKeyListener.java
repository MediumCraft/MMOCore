package net.Indyuce.mmocore.listener.event;

import net.Indyuce.mmocore.api.event.PlayerKeyPressEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.cast.PlayerKey;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * This registers all the KeyPress events. All events are registered
 * with LOWEST priority so that if the wrapped event happens to be
 * cancelled because of a key press, it is canceled before any plugin
 * can deal with it.
 */
public class PlayerPressKeyListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void registerCrouchKey(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            PlayerKeyPressEvent called = new PlayerKeyPressEvent(PlayerData.get(event.getPlayer()), PlayerKey.CROUCH, event);
            Bukkit.getPluginManager().callEvent(called);
        }
    }

    /**
     * Recent bukkit builds automatically send an interact packet
     * when dropping an item, by pressing Q or from any inventory.
     * <p>
     * Simple implementation of a Xms timeout after drop events
     * where all interact events are nulled. This timeout should
     * be lower slightly lower than one tick, if the TPS gets
     * really close to 20 it could cause a loss of true clicks.
     * Finally, it should only cancel ONE click event at most
     * and get reset by the first cancelled click event.
     */
    private static final long CLICK_EVENT_TIMEOUT = 30;

    @EventHandler(priority = EventPriority.LOWEST)
    public void registerClickKey(PlayerInteractEvent event) {
        if (event.useItemInHand() != Event.Result.DENY && event.getAction().name().contains("CLICK") && event.getHand().equals(EquipmentSlot.HAND)) {
            final PlayerData playerData = PlayerData.get(event.getPlayer());
            if (System.currentTimeMillis() - playerData.lastDropEvent < CLICK_EVENT_TIMEOUT) {
                playerData.lastDropEvent = 0;
                return;
            }
            final boolean rightClick = event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;
            Bukkit.getPluginManager().callEvent(new PlayerKeyPressEvent(playerData, rightClick ? PlayerKey.RIGHT_CLICK : PlayerKey.LEFT_CLICK, event));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void registerDropKey(PlayerDropItemEvent event) {
        final PlayerData playerData = PlayerData.get(event.getPlayer());
        Bukkit.getPluginManager().callEvent(new PlayerKeyPressEvent(playerData, PlayerKey.DROP, event));
        playerData.lastDropEvent = System.currentTimeMillis();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void registerSwapHandsKey(PlayerSwapHandItemsEvent event) {
        PlayerKeyPressEvent called = new PlayerKeyPressEvent(PlayerData.get(event.getPlayer()), PlayerKey.SWAP_HANDS, event);
        Bukkit.getPluginManager().callEvent(called);
    }
}
