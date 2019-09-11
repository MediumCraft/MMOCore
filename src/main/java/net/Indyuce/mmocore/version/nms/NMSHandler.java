package net.Indyuce.mmocore.version.nms;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import net.Indyuce.mmocore.api.item.NBTItem;

public interface NMSHandler {
	NBTItem getNBTItem(ItemStack item);

	void sendTitle(Player player, String title, String subtitle, int fadeIn, int ticks, int fadeOut);

	void sendActionBar(Player player, String message);

	void sendJson(Player player, String message);

	int getNextContainerId(Player player);

	void handleInventoryCloseEvent(Player player);

	void sendPacketOpenWindow(Player player, int containerId);

	void sendPacketCloseWindow(Player player, int containerId);

	void setActiveContainerDefault(Player player);

	void setActiveContainer(Player player, Object container);

	void setActiveContainerId(Object container, int containerId);

	void addActiveContainerSlotListener(Object container, Player player);

	Inventory toBukkitInventory(Object container);

	Object newContainerAnvil(Player player);

	BoundingBox getBoundingBox(Entity target);
}
