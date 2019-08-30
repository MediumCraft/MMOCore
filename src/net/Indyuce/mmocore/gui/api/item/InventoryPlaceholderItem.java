package net.Indyuce.mmocore.gui.api.item;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.PluginInventory;

public abstract class InventoryPlaceholderItem extends InventoryItem {
	private final Material material;
	private final String name, texture;
	private final List<String> lore;
	private final boolean placeholders, hideFlags;

	public InventoryPlaceholderItem(ConfigurationSection config) {
		this(Material.valueOf(config.getString("item")), config);
	}

	public InventoryPlaceholderItem(Material material, ConfigurationSection config) {
		super(config);

		this.material = material;
		this.name = config.getString("name");
		this.lore = config.getStringList("lore");
		this.hideFlags = config.getBoolean("hide-flags");
		this.texture = config.getString("texture");
		this.placeholders = config.getBoolean("placeholders");
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
		ItemStack item = new ItemStack(getMaterial());
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
