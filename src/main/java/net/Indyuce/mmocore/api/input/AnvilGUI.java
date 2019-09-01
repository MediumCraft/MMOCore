package net.Indyuce.mmocore.api.input;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Consumer;

import net.Indyuce.mmocore.MMOCore;

public class AnvilGUI extends PlayerInput {
	private final int containerId;
	private final Inventory inventory;

	public AnvilGUI(Player player, InputType type, Consumer<String> output) {
		super(player, output);

		ItemStack paper = new ItemStack(Material.PAPER);
		ItemMeta paperMeta = paper.getItemMeta();
		paperMeta.setDisplayName(MMOCore.plugin.configManager.getSimpleMessage("player-input.anvil." + type.getLowerCaseName()));
		paper.setItemMeta(paperMeta);

		MMOCore.plugin.nms.handleInventoryCloseEvent(player);
		MMOCore.plugin.nms.setActiveContainerDefault(player);

		final Object container = MMOCore.plugin.nms.newContainerAnvil(player);

		inventory = MMOCore.plugin.nms.toBukkitInventory(container);
		inventory.setItem(0, paper);

		containerId = MMOCore.plugin.nms.getNextContainerId(player);
		MMOCore.plugin.nms.sendPacketOpenWindow(player, containerId);
		MMOCore.plugin.nms.setActiveContainer(player, container);
		MMOCore.plugin.nms.setActiveContainerId(container, containerId);
		MMOCore.plugin.nms.addActiveContainerSlotListener(container, player);
	}

	public void close() {
		MMOCore.plugin.nms.handleInventoryCloseEvent(getPlayer());
		MMOCore.plugin.nms.setActiveContainerDefault(getPlayer());
		MMOCore.plugin.nms.sendPacketCloseWindow(getPlayer(), containerId);

		InventoryClickEvent.getHandlerList().unregister(this);
		InventoryCloseEvent.getHandlerList().unregister(this);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void a(InventoryClickEvent event) {
		if (event.getInventory().equals(inventory)) {
			event.setCancelled(true);

			if (event.getRawSlot() == 2) {
				ItemStack clicked = inventory.getItem(event.getRawSlot());
				if (clicked != null && clicked.getType() != Material.AIR)
					output(clicked.hasItemMeta() ? clicked.getItemMeta().getDisplayName() : clicked.getType().toString());
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void b(InventoryCloseEvent event) {
		if (event.getInventory().equals(inventory))
			close();
	}
}