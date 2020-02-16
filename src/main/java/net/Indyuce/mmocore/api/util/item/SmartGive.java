package net.Indyuce.mmocore.api.util.item;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SmartGive {
	private final Inventory inv;
	private final Location loc;

	public SmartGive(Player player) {
		inv = player.getInventory();
		loc = player.getLocation();
	}

	/*
	 * either give directly the item to the player or drops it on the ground if
	 * there is not enough space in the player inventory.
	 */
	public void give(ItemStack... item) {
		for (ItemStack drop : inv.addItem(item).values())
			loc.getWorld().dropItem(loc, drop);
	}

	public void give(List<ItemStack> item) {
		for (ItemStack drop : inv.addItem(item.toArray(new ItemStack[item.size()])).values())
			loc.getWorld().dropItem(loc, drop);
	}
}
