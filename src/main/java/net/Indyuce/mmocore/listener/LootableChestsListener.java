package net.Indyuce.mmocore.listener;

import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.manager.LootableChestManager.LootableChest;

public class LootableChestsListener implements Listener {
	@EventHandler
	public void a(InventoryCloseEvent event) {
		if (!(event.getInventory().getHolder() instanceof Chest))
			return;

		Chest chest = (Chest) event.getInventory().getHolder();
		LootableChest lootable = MMOCore.plugin.chestManager.getLootableChest(chest.getLocation());
		if (lootable != null)
			lootable.whenClosed(true);
	}
}
