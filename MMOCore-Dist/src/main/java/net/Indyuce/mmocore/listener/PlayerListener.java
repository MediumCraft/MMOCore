package net.Indyuce.mmocore.listener;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.event.SynchronizedDataLoadEvent;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class PlayerListener implements Listener {

    /**
     * Script ran when the full MMO plugin data is synchronized. Player Health
     * is only updated now otherwise other MMO plugins would not have the time
     * to register their stats beforehand.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void a(SynchronizedDataLoadEvent event) {
        if (event.syncIsFull()) {
            final PlayerData playerData = PlayerData.get(event.getHolder().getUniqueId());
            final Player player = playerData.getPlayer();

            playerData.setupSkillTree();
            playerData.applyTemporaryTriggers();
            playerData.getStats().updateStats(true); // TODO maybe duplicate?

            /*
             * If the player is not dead and the health is 0, this means that the data was
             * missing from the database, and it should give full health to the player. It
             * must account for the edge case where the player is dead.
             */
            if (playerData.isOnline() && !player.isDead()) {
                final double cachedHealth = playerData.getCachedHealth(),
                        maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(),
                        fixedHealth = MMOCoreUtils.fixResource(cachedHealth, maxHealth);
                player.setHealth(fixedHealth);
            }
        }
    }

    /**
     * Register custom inventory clicks
     */
    @EventHandler
    public void registerInventoryClicks(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof PluginInventory)
            ((PluginInventory) event.getInventory().getHolder()).whenClicked(new InventoryClickContext(event.getRawSlot(), event.getCurrentItem(), event.getClick(), event, event.getInventory(), (PluginInventory) event.getInventory().getHolder()));
    }

    /**
     * Register custom inventory close effect
     */
    @EventHandler
    public void registerInventoryCloses(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof PluginInventory)
            ((PluginInventory) event.getInventory().getHolder()).whenClosed(event);
    }

    /**
     * Updates the player's combat log data every time he hits an entity, or
     * gets hit by an entity or a projectile sent by another entity
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateCombat(PlayerAttackEvent event) {
        PlayerData.get(event.getAttacker().getPlayer()).getCombat().update();
    }

    /**
     * Updates the player's combat log everytime he gets hit.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateCombat(EntityDamageEvent event) {
        if (UtilityMethods.isFake(event)) return;
        if (UtilityMethods.isRealPlayer(event.getEntity()) && MMOCore.plugin.configManager.combatLogDamageCauses.contains(event.getCause()))
            PlayerData.get((Player) event.getEntity()).getCombat().update();
    }

    /**
     * Using the Bukkit health update event is not a good way of
     * interacting with MMOCore health regeneration. The
     * PlayerResourceUpdateEvent should be heavily prioritized.
     * <p>
     * This method makes sure that all the plugins which
     * utilize this event can also communicate with MMOCore
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void resourceBukkitInterface(PlayerResourceUpdateEvent event) {
        if (event.getResource() == PlayerResource.HEALTH) {
            final EntityRegainHealthEvent bukkitEvent = new EntityRegainHealthEvent(event.getPlayer(), event.getAmount(), RegainReason.CUSTOM);
            Bukkit.getPluginManager().callEvent(bukkitEvent);

            // Update event values
            event.setAmount(bukkitEvent.getAmount());
            event.setCancelled(bukkitEvent.isCancelled());
        }
    }
}
