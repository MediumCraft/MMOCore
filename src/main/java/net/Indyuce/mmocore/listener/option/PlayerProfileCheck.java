package net.Indyuce.mmocore.listener.option;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerProfileCheck implements Listener {

    @EventHandler
    public void a(PlayerInteractEntityEvent event) {
        /**
        if (event.getRightClicked().getType() != EntityType.PLAYER || !MythicLib.plugin.getEntities().canTarget(event.getPlayer(), event.getRightClicked(), InteractionType.SUPPORT_ACTION))
            return;

        /*
         * This works because the PlayerStats class DOES NOT utilize
         * at all the player instance saved in the InventoryClickEvent
         *
         * Opening inventories like that to other players does NOT
         * necessarily works for any other custom inventory.
         * */
        /**
        Inventory inv = InventoryManager.PLAYER_STATS.newInventory(PlayerData.get(event.getRightClicked().getUniqueId())).getInventory();
        event.getPlayer().openInventory(inv);
        **/
    }

}
