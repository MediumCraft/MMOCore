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

/**
 * This registers all the KeyPress events
 */
public class PlayerPressKeyListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void registerCrouchKey(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            PlayerKeyPressEvent called = new PlayerKeyPressEvent(PlayerData.get(event.getPlayer()), PlayerKey.CROUCH, event);
            Bukkit.getPluginManager().callEvent(called);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void registerClickKey(PlayerInteractEvent event) {
        if (event.useItemInHand() != Event.Result.DENY && event.getAction().name().contains("CLICK")) {
            boolean rightClick = event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;
            PlayerKeyPressEvent called = new PlayerKeyPressEvent(PlayerData.get(event.getPlayer()), rightClick ? PlayerKey.RIGHT_CLICK : PlayerKey.LEFT_CLICK, event);
            Bukkit.getPluginManager().callEvent(called);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void registerDropKey(PlayerDropItemEvent event) {
        PlayerKeyPressEvent called = new PlayerKeyPressEvent(PlayerData.get(event.getPlayer()), PlayerKey.DROP, event);
        Bukkit.getPluginManager().callEvent(called);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void registerSwapHandsKey(PlayerSwapHandItemsEvent event) {
        PlayerKeyPressEvent called = new PlayerKeyPressEvent(PlayerData.get(event.getPlayer()), PlayerKey.SWAP_HANDS, event);
        Bukkit.getPluginManager().callEvent(called);
    }
}
