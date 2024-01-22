package net.Indyuce.mmocore.gui.api;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.api.adaptor.Adaptor;
import net.Indyuce.mmocore.gui.api.adaptor.ClassicAdaptor;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.TriggerItem;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class GeneratedInventory extends PluginInventory {
    private final EditableInventory editable;
    private final List<InventoryItem> loaded = new ArrayList<>();
    private final Adaptor adaptor;

    public GeneratedInventory(PlayerData playerData, EditableInventory editable) {
        super(playerData);

        this.editable = editable;
        this.adaptor = editable.getAdaptorType().supply(this);
    }

    public List<InventoryItem> getLoaded() {
        return loaded;
    }

    public EditableInventory getEditable() {

        return editable;
    }

    public InventoryItem getByFunction(String function) {
        for (InventoryItem item : loaded)
            if (item.getFunction().equals(function))
                return item;
        return null;
    }

    public InventoryItem getBySlot(int slot) {
        for (InventoryItem item : loaded)
            if (item.getSlots().contains(slot))
                return item;
        return null;
    }

    /**
     * This method must use an ordered collection because
     * of GUI items overriding possibilities. Hence the use
     * of an array list instead of a set
     */
    public void addLoaded(InventoryItem item) {
        loaded.add(0, item);
    }

    @Override
    public Inventory getInventory() {
        Validate.isTrue(adaptor instanceof ClassicAdaptor);
        return ((ClassicAdaptor) adaptor).getInventory();
    }

    @Override
    public void open() {
        if (!getPlayerData().isOnline()) return;

        /*
         * Very important, in order to prevent ghost items, the loaded items map
         * must be cleared when the inventory is updated or open at least twice
         */
        loaded.clear();

        adaptor.open();
    }

    /**
     * @deprecated Not a fan of that implementation.
     * Better work with {@link InventoryItem#setDisplayed(Inventory, GeneratedInventory)}
     */
    @Deprecated
    public void dynamicallyUpdateItem(InventoryItem<?> item, int n, ItemStack placed, Consumer<ItemStack> update) {
        adaptor.dynamicallyUpdateItem(item, n, placed, update);
    }

    @Override
    public void whenClicked(InventoryClickContext context) {
        context.setCancelled(true);
        InventoryItem item = getBySlot(context.getSlot());
        //Checks that the click corresponds to a GUI Item.
        if (item == null || context.getClickedItem() == null || context.getClickedItem().getType() == Material.AIR)
            return;

        if (item instanceof TriggerItem)
            ((TriggerItem) item).getTrigger().apply(getPlayerData());
        else
            whenClicked(context, item);
    }

    public abstract String calculateName();

    public abstract void whenClicked(@NotNull InventoryClickContext context, @NotNull InventoryItem item);
}
