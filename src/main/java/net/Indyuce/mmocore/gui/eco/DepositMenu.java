package net.Indyuce.mmocore.gui.eco;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.SmartGive;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.util.item.SimpleItemBuilder;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class DepositMenu extends PluginInventory {
    private ItemStack depositItem;
    private int deposit;

    /**
     * Every time an item is clicked in the inventory, an inventory
     * update is scheduled. If nothing happens for the next 10 ticks
     * then the update is processed. If another item is clicked within
     * this delay the task is cancelled and scheduled for later
     */
    private BukkitRunnable updateRunnable;

    public DepositMenu(Player player) {
        super(player);
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 27, "Deposit");
        updateDeposit(inv);
        return inv;
    }

    @Override
    public void whenClicked(InventoryClickContext event) {
        if (event.getItemStack() == null || event.getItemStack().getType() == Material.AIR)
            return;

        if (event.getItemStack().isSimilar(depositItem)) {
            event.setCancelled(true);

            updateDeposit(event.getInventory());
            if (deposit <= 0)
                return;

            EconomyResponse response = MMOCore.plugin.economy.getEconomy().depositPlayer(player, deposit);
            if (!response.transactionSuccess())
                return;

            event.getInventory().clear();
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
            MMOCore.plugin.configManager.getSimpleMessage("deposit", "worth", String.valueOf(deposit)).send(player);
            return;
        }

        int worth = NBTItem.get(event.getItemStack()).getInteger("RpgWorth");
        if (worth < 1)
            event.setCancelled(true);
        else
            scheduleUpdate(event.getInventory());
    }

    @Override
    public void whenClosed(InventoryCloseEvent event) {

        // Cancel runnable
        if (updateRunnable != null)
            updateRunnable.cancel();

        // Give all items back
        SmartGive smart = new SmartGive(player);
        for (int j = 0; j < 26; j++) {
            ItemStack item = event.getInventory().getItem(j);
            if (item != null)
                smart.give(item);
        }
    }

    private BukkitRunnable newUpdateRunnable(Inventory inv) {
        return new BukkitRunnable() {

            @Override
            public void run() {
                updateDeposit(inv);
            }
        };
    }

    private void scheduleUpdate(Inventory inv) {
        if (updateRunnable != null)
            updateRunnable.cancel();

        updateRunnable = new BukkitRunnable() {

            @Override
            public void run() {
                updateRunnable = null;
                updateDeposit(inv);
            }
        };
        updateRunnable.runTaskLater(MMOCore.plugin, 10);
    }

    private void updateDeposit(Inventory inv) {
        if (updateRunnable != null) {
            updateRunnable.cancel();
            updateRunnable = null;
        }

        deposit = MMOCoreUtils.getWorth(inv.getContents());
        inv.setItem(26, depositItem = new SimpleItemBuilder("DEPOSIT_ITEM").addPlaceholders("worth", String.valueOf(deposit)).build());
    }
}
