package net.Indyuce.mmocore.gui.api.item;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.MMOCoreUtils;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.PluginInventory;

public abstract class InventoryPlaceholderItem extends InventoryItem {
	private final ItemStack stack;
	private final String name, texture;
	private final List<String> lore;
	private final int modelData;
	private final boolean placeholders, hideFlags;

	public InventoryPlaceholderItem(ConfigurationSection config) {
		this(MMOCoreUtils.readIcon(config.getString("item")), config);
	}

	public InventoryPlaceholderItem(ItemStack stack, ConfigurationSection config) {
		super(config);

		this.stack = stack;
		this.name = config.getString("name");
		this.lore = config.getStringList("lore");
		this.hideFlags = config.getBoolean("hide-flags");
		this.texture = config.getString("texture");
		this.placeholders = config.getBoolean("placeholders");
		this.modelData = config.getInt("custom-model-data");
	}

	public ItemStack getStack() {
		return stack;
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
	
	public boolean supportPlaceholders() {
		return placeholders;
	}

	@Override
	public boolean canDisplay(GeneratedInventory inv) {
		return true;
	}

	public Placeholders getPlaceholders(PluginInventory inv) {
		return getPlaceholders(inv, 0);
	}

	public abstract Placeholders getPlaceholders(PluginInventory inv, int n);

	@Override
	public ItemStack display(GeneratedInventory inv, int n) {

		Placeholders placeholders = getPlaceholders(inv, n);
		ItemStack item = getStack();
		ItemMeta meta = item.getItemMeta();

		if (texture != null && meta instanceof SkullMeta)
			applyTexture(texture, (SkullMeta) meta);

		if (hasName())
			meta.setDisplayName(placeholders.apply(inv.getPlayer(), new String(getName())));

		if (hideFlags())
			meta.addItemFlags(ItemFlag.values());

		if (hasLore()) {
			List<String> lore = new ArrayList<>();
			getLore().forEach(line -> lore.add(ChatColor.GRAY + placeholders.apply(inv.getPlayer(), line)));
			meta.setLore(lore);
		}

		if (MMOCore.plugin.version.isStrictlyHigher(1, 13))
			meta.setCustomModelData(getModelData());
		
		item.setItemMeta(meta);
		return item;
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
}
