package net.Indyuce.mmocore.listener;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.loot.chest.LootChest;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class LootableChestsListener implements Listener {

    @EventHandler
    public void expireOnClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof Chest))
            return;

        Chest chest = (Chest) event.getInventory().getHolder();
        LootChest lootChest = MMOCore.plugin.lootChests.getChest(chest.getLocation());
        if (lootChest != null)
            lootChest.expire(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void noBreaking(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.CHEST) {
            LootChest lootChest = MMOCore.plugin.lootChests.getChest(block.getLocation());
            if (lootChest != null)
                event.setCancelled(true);
        }
    }
}
