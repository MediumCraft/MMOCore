package net.Indyuce.mmocore.gui.api.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public abstract class InventoryItem<T extends GeneratedInventory> {
    private final String id, function;
    private final List<Integer> slots = new ArrayList<>();

    @Nullable
    private final InventoryItem<?> parent;

    private final Material material;
    private final String name, texture;
    private final List<String> lore;
    private final int modelData;
    private final boolean hideFlags;

    public InventoryItem(ConfigurationSection config) {
        this((InventoryItem) null, config);
    }

    public InventoryItem(@Nullable InventoryItem<?> parent, ConfigurationSection config) {
        this(parent, Material.valueOf(config.getString("item", "").toUpperCase().replace(" ", "_").replace("-", "_")), config);
    }

    public InventoryItem(@NotNull Material material, ConfigurationSection config) {
        this(null, material, config);
    }

    public InventoryItem(InventoryItem parent, Material material, ConfigurationSection config) {
        this.id = config.getName();
        this.function = config.getString("function", "");
        this.parent = parent;

        this.material = material;
        this.name = config.getString("name");
        this.lore = config.getStringList("lore");
        this.hideFlags = config.getBoolean("hide-flags");
        this.texture = config.getString("texture");
        this.modelData = config.getInt("custom-model-data");

        config.getStringList("slots").forEach(str -> slots.add(Integer.parseInt(str)));
    }

    public String getId() {
        return id;
    }

    @NotNull
    public String getFunction() {
        return parent == null ? function : parent.function;
    }

    public boolean hasFunction() {
        return !getFunction().isEmpty();
    }

    @NotNull
    public List<Integer> getSlots() {
        return parent == null ? slots : parent.slots;
    }

    public Material getMaterial() {
        return material;
    }

    public boolean hideFlags() {
        return hideFlags;
    }

    public boolean hasName() {
        return name != null;
    }

    public String getName() {
        return name;
    }

    public boolean hasLore() {
        return lore != null && !lore.isEmpty();
    }

    public List<String> getLore() {
        return lore;
    }

    public int getModelData() {
        return modelData;
    }

    public void setDisplayed(Inventory inv, T generated) {
        generated.addLoaded(this);

        if (!hasDifferentDisplay()) {
            ItemStack display = display(generated);
            for (int slot : getSlots())
                inv.setItem(slot, display);
        } else for (int j = 0; j < slots.size(); j++)
            inv.setItem(slots.get(j), display(generated, j));

    }

    public boolean hasDifferentDisplay() {
        return false;
    }

    public boolean canDisplay(T inv) {
        return true;
    }

    @NotNull
    public ItemStack display(T inv) {
        return display(inv, modelData);
    }

    @NotNull
    public ItemStack display(T inv, int n) {
        return display(inv, n, material);
    }

    @NotNull
    public ItemStack display(T inv, int n, Material specificMaterial) {
        return display(inv, n, specificMaterial, modelData);
    }

    @NotNull
    public ItemStack display(T inv, int n, Material material, int modelData) {

        final Placeholders placeholders = getPlaceholders(inv, n);
        final OfflinePlayer effectivePlayer = getEffectivePlayer(inv, n);

        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(modelData);
        if (texture != null && meta instanceof SkullMeta) applyTexture(texture, (SkullMeta) meta);

        if (hasName()) meta.setDisplayName(placeholders.apply(effectivePlayer, getName()));

        if (hideFlags()) meta.addItemFlags(ItemFlag.values());

        if (hasLore()) {
            List<String> lore = new ArrayList<>();
            getLore().forEach(line -> lore.add(ChatColor.GRAY + placeholders.apply(effectivePlayer, line)));
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * @param inv Inventory being generated
     * @param n   Index of item being generated
     * @return Player relative to which placeholders are computed when the item is
     *         being displayed in the inventory. Most of the time, it's just
     *         the player opening the inventory, but for friends or party members,
     *         being able to parse placeholders based on other players is great too.
     */
    @NotNull
    public OfflinePlayer getEffectivePlayer(T inv, int n) {
        return inv.getPlayer();
    }

    private void applyTexture(String value, SkullMeta meta) {
        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", value));

            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException exception) {
            MMOCore.log(Level.WARNING, "Could not apply item texture value of " + getId());
        }
    }

    public Placeholders getPlaceholders(T inv) {
        return getPlaceholders(inv, 0);
    }

    public abstract Placeholders getPlaceholders(T inv, int n);
}
