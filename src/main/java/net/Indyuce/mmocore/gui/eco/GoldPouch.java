package net.Indyuce.mmocore.gui.eco;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.MMOCoreUtils;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.mmogroup.mmolib.api.ItemTag;
import net.mmogroup.mmolib.api.NBTItem;

public class GoldPouch extends PluginInventory {
	private final boolean mob;
	private final NBTItem nbt;

	public GoldPouch(Player player, NBTItem nbt) {
		super(player);
		this.nbt = nbt;
		this.mob = nbt.getBoolean("RpgPouchMob");
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = (Inventory) Bukkit.createInventory(this, 18, ChatColor.UNDERLINE + "Gold Pouch");
		inv.setContents(MMOCoreUtils.itemStackArrayFromBase64(nbt.getString("RpgPouchInventory")));
		return inv;
	}

	/*
	 * if the player has opened a backpack, he cannot click a backpack. bug fix
	 * - the player can move the backpack and lose the inventory he had opened
	 */
	@Override
	public void whenClicked(InventoryClickEvent event) {

		ItemStack item = event.getCurrentItem();
		NBTItem nbt = NBTItem.get(item);
		if (!nbt.hasTag("RpgWorth")) {
			event.setCancelled(true);
			return;
		}

		if (mob) {
			event.setCancelled(true);

			// in deposit menu
			if (event.getRawSlot() < 18) {
				int empty = player.getInventory().firstEmpty();
				if (empty < 0)
					return;

				player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 1, 2);
				player.getInventory().addItem(event.getCurrentItem());
				event.setCurrentItem(null);
			}

			return;
		}

		if (nbt.hasTag("RpgPouchInventory"))
			event.setCancelled(true);
	}

	@Override
	public void whenClosed(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		if (mob && isEmpty(event.getInventory())) {
			player.getEquipment().setItemInMainHand(null);
			return;
		}

		ItemStack updated = NBTItem.get(player.getEquipment().getItemInMainHand()).addTag(new ItemTag("RpgPouchInventory", MMOCoreUtils.toBase64(event.getInventory().getContents()))).toItem();
		player.getEquipment().setItemInMainHand(updated);
	}

	private boolean isEmpty(Inventory inv) {
		for (ItemStack item : inv.getContents())
			if (item != null && item.getType() != Material.AIR)
				return false;
		return true;
	}
}
