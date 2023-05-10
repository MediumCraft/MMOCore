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

    @EventHandler(priority = EventPriority.LOWEST)
    public void registerClickKey(PlayerInteractEvent event) {
        if (event.useItemInHand() != Event.Result.DENY && event.getAction().name().contains("CLICK")&&event.getHand().equals(EquipmentSlot.HAND)) {
            boolean rightClick = event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;
            PlayerKeyPressEvent called = new PlayerKeyPressEvent(PlayerData.get(event.getPlayer()), rightClick ? PlayerKey.RIGHT_CLICK : PlayerKey.LEFT_CLICK, event);
            Bukkit.getPluginManager().callEvent(called);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void registerDropKey(PlayerDropItemEvent event) {
        PlayerKeyPressEvent called = new PlayerKeyPressEvent(PlayerData.get(event.getPlayer()), PlayerKey.DROP, event);
        Bukkit.getPluginManager().callEvent(called);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void registerSwapHandsKey(PlayerSwapHandItemsEvent event) {
        PlayerKeyPressEvent called = new PlayerKeyPressEvent(PlayerData.get(event.getPlayer()), PlayerKey.SWAP_HANDS, event);
        Bukkit.getPluginManager().callEvent(called);
    }
}
