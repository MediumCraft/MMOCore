package net.Indyuce.mmocore.listener;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.waypoint.Waypoint;
import net.Indyuce.mmocore.waypoint.WaypointOption;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.loot.chest.particle.SmallParticleEffect;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Objects;

public class WaypointsListener implements Listener {

    @EventHandler
    public void interactWithWaypoint(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking())
            return;

        Waypoint waypoint = MMOCore.plugin.waypointManager.getCurrentWaypoint(player);
        if (waypoint == null)
            return;

        PlayerData data = PlayerData.get(player);
        if (waypoint.hasOption(WaypointOption.UNLOCKABLE) && !data.hasWaypoint(waypoint)) {
            data.unlockWaypoint(waypoint);
            new SmallParticleEffect(player, Particle.SPELL_WITCH);
            MMOCore.plugin.configManager.getSimpleMessage("new-waypoint", "waypoint", waypoint.getName()).send(player);
            MMOCore.plugin.soundManager.getSound(SoundEvent.WARP_UNLOCK).playTo(player);
            return;
        }

        if (waypoint.hasOption(WaypointOption.ENABLE_MENU)) {
            player.setSneaking(false);
            InventoryManager.WAYPOINTS.newInventory(data, waypoint).open();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void waypointBook(PlayerInteractEvent event) {
        if (!event.hasItem() || (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK))
            return;

        NBTItem nbtItem = NBTItem.get(event.getItem());
        if (Objects.equals(nbtItem.getString("MMOCoreItemId"), "WAYPOINT_BOOK")) {
            String waypointId = nbtItem.getString("WaypointBookId");
            Waypoint waypoint = MMOCore.plugin.waypointManager.get(waypointId);
            if (waypoint == null)
                return;

            PlayerData playerData = PlayerData.get(event.getPlayer());
            if (playerData.hasWaypoint(waypoint))
                return;

            playerData.unlockWaypoint(waypoint);
            event.getItem().setAmount(event.getItem().getAmount() - 1); // Consume item
            MMOCore.plugin.configManager.getSimpleMessage("new-waypoint-book", "waypoint", waypoint.getName()).send(event.getPlayer());
        }
    }
}
