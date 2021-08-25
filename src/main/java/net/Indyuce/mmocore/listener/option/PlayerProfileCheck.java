package net.Indyuce.mmocore.listener.option;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.target.InteractionType;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;

public class PlayerProfileCheck implements Listener {

    @EventHandler
    public void a(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() != EntityType.PLAYER || !MythicLib.plugin.getEntities().canTarget(event.getPlayer(), event.getRightClicked(), InteractionType.SUPPORT_ACTION))
            return;

        /*
         * This works because the PlayerStats class DOES NOT utilize
         * at all the player instance saved in the InventoryClickEvent
         *
         * Opening inventories like that to other players does NOT
         * necessarily works for any other custom inventory.
         * */
        Inventory inv = InventoryManager.PLAYER_STATS.newInventory(PlayerData.get(event.getRightClicked().getUniqueId())).getInventory();
        event.getPlayer().openInventory(inv);
    }
}
