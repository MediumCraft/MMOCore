package net.Indyuce.mmocore.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.gui.eco.GoldPouch;
import io.lumine.mythic.lib.api.item.NBTItem;

public class GoldPouchesListener implements Listener {
	@EventHandler
	public void a(PlayerInteractEvent event) {
		if (!event.getAction().name().startsWith("RIGHT") || event.getHand() != EquipmentSlot.HAND)
			return;

		NBTItem nbt = NBTItem.get(event.getItem());
		if (!nbt.hasTag("RpgPouchInventory"))
			return;

		// that way ppl can't open a chest when right clicking a backpack
		// when they wanted to open the backpack
		event.setCancelled(true);

		// dupe bug : open 2 stacked backpacks and split them to dupe.
		if (event.getItem().getAmount() > 1)
			return;

		new GoldPouch(event.getPlayer(), nbt).open();
	}

	/*
	 * if a player has a backpack open, he cannot pick up a backpack. bug fix -
	 * he can pick up a backpack, and dupe items when the items are saved in a
	 * amount=2 backpack itemstack TODO register and unregister listener.
	 */
	@EventHandler
	public void b(EntityPickupItemEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;

		Player player = (Player) event.getEntity();
		if (player.getOpenInventory() == null || !(player.getOpenInventory().getTopInventory().getHolder() instanceof GoldPouch))
			return;

		ItemStack item = event.getItem().getItemStack();
		if (NBTItem.get(item).hasTag("RpgPouchInventory"))
			event.setCancelled(true);
	}
}
