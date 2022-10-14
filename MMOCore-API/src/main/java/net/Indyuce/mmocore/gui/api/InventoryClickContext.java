package net.Indyuce.mmocore.gui.api;

import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

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

    public InventoryClickContext(int slot, ItemStack itemStack, ClickType clickType, Cancellable event, Inventory inv) {
        this.slot = slot;
        this.itemStack = itemStack;
        this.clickType = clickType;
        this.event = event;
        this.inv = inv;
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

    /**
     * @return The Bukkit InventoryClickEvent's current item. This is the item
     *         which the player just clicked on. The item on the player's
     *         cursor is NOT provided by click contexts
     */
    @Nullable
    public ItemStack getClickedItem() {
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
