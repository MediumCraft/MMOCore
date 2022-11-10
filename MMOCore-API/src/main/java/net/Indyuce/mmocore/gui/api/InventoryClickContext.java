package net.Indyuce.mmocore.gui.api;

import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InventoryClickContext {
    private final int slot;

    @Nullable
    private final ItemStack currentItem;

    @NotNull
    private final ClickType clickType;

    @NotNull
    private final Cancellable event;

    @Nullable
    private final Inventory inv;

    public InventoryClickContext(int slot, ItemStack currentItem, ClickType clickType, Cancellable event) {
        this(slot, currentItem, clickType, event, null);
    }

    public InventoryClickContext(int slot, ItemStack currentItem, ClickType clickType, Cancellable event, Inventory inv) {
        this.slot = slot;
        this.currentItem = currentItem;
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
        return currentItem;
    }

    @NotNull
    public Cancellable getEvent() {
        return event;
    }

    @Nullable
    public Inventory getInventory() {
        return inv;
    }

    public boolean isClassic() {
        return inv != null;
    }

    @NotNull
    public ClickType getClickType() {
        return clickType;
    }
}
