package net.Indyuce.mmocore.gui.api.adaptor;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class ClassicAdaptor extends Adaptor {
    private Inventory open;

    public ClassicAdaptor(GeneratedInventory generated) {
        super(generated);
    }

    @Override
    public void open() {
        generated.getPlayer().openInventory(open = getInventory());
    }

    @Override
    public void close() {

    }

    @Override
    public void dynamicallyUpdateItem(InventoryItem<?> item, int n, ItemStack placed, Consumer<ItemStack> update) {
        Bukkit.getScheduler().runTaskAsynchronously(MMOCore.plugin, () -> {
            update.accept(placed);
            open.setItem(item.getSlots().get(n), placed);
        });
    }


    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(generated, generated.getEditable().getSlots(), MythicLib.plugin.getPlaceholderParser().parse(generated.getPlayer(), generated.calculateName()));

        for (InventoryItem item : generated.getEditable().getItems())
            if (item.canDisplay(generated))
                setDisplayed(inv, item);

        return inv;
    }

    private void setDisplayed(Inventory inv, InventoryItem item) {
        generated.addLoaded(item);
        List<Integer> slots = item.getSlots();

        if (!item.hasDifferentDisplay()) {
            ItemStack display = item.display(generated);
            for (int slot : slots)
                inv.setItem(slot, display);
        } else
            for (int j = 0; j < slots.size(); j++)
                inv.setItem(slots.get(j), item.display(generated, j));

    }
}
