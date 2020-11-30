package net.Indyuce.mmocore.gui.eco;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.api.util.item.ConfigItem;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.api.util.SmartGive;

public class DepositMenu extends PluginInventory {
	private ItemStack depositItem;
	private int deposit;

	public DepositMenu(Player player) {
		super(player);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 27, "Deposit");

		inv.setItem(26, depositItem = new ConfigItem("DEPOSIT_ITEM").addPlaceholders("worth", "0").build());

		new BukkitRunnable() {

			@Override
			public void run() {
				if (inv.getViewers().size() < 1) {
					cancel();
					return;
				}

				updateDeposit(inv);
			}
		}.runTaskTimer(MMOCore.plugin, 0, 20);
		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		// event.setCancelled(true);
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
			return;

		if (event.getCurrentItem().isSimilar(depositItem)) {
			event.setCancelled(true);

			updateDeposit(event.getInventory());
			if (deposit <= 0)
				return;

			MMOCore.plugin.economy.getEconomy().depositPlayer(player, deposit);
			event.getInventory().clear();
			player.closeInventory();
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
			MMOCore.plugin.configManager.getSimpleMessage("deposit", "worth", "" + deposit).send(player);
			return;
		}

		int worth = NBTItem.get(event.getCurrentItem()).getInteger("RpgWorth");
		if (worth < 1) {
			event.setCancelled(true);
		}

		// in deposit menu
		// if (event.getRawSlot() < 27) {
		// int empty = player.getInventory().firstEmpty();
		// if (empty < 0)
		// return;
		//
		// player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT,
		// 1, 2);
		// player.getInventory().addItem(event.getCurrentItem());
		// event.setCurrentItem(null);
		// updateDeposit(event.getInventory());
		// return;
		// }

		// in player inventory
		// int empty = event.getInventory().firstEmpty();
		// if (empty < 0)
		// return;
		//
		// player.playSound(player.getLocation(),
		// Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
		// event.getInventory().addItem(event.getCurrentItem());
		// event.setCurrentItem(null);
		// updateDeposit(event.getInventory());
		// return;
	}

	@Override
	public void whenClosed(InventoryCloseEvent event) {
		SmartGive smart = new SmartGive(player);
		for (int j = 0; j < 26; j++) {
			ItemStack item = event.getInventory().getItem(j);
			if (item != null)
				smart.give(item);
		}
	}

	private void updateDeposit(Inventory inv) {
		deposit = MMOCoreUtils.getWorth(inv.getContents());
		inv.setItem(26, depositItem = new ConfigItem("DEPOSIT_ITEM").addPlaceholders("worth", "" + deposit).build());
	}
}
