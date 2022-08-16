package net.Indyuce.mmocore.gui.api;


import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryClickContext {
    private final int slot;
    private final ItemStack itemStack;
    private final ClickType clickType;

    private final Cancellable event;

    private Inventory inv;

    public InventoryClickContext(int slot, ItemStack itemStack, ClickType clickType, Cancellable event) {
        this.slot = slot;
        this.itemStack = itemStack;
        this.clickType = clickType;
        this.event = event;
    }

    public void setCancelled(boolean val) {
        event.setCancelled(val);
    }

    public boolean isCancelled() {
        return event.isCancelled();
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Cancellable getEvent() {
        return event;
    }

    public Inventory getInventory() {
        return inv;
    }

    public boolean isClassic() {
        return inv != null;
    }

    public ClickType getClickType() {
        return clickType;
    }
}
