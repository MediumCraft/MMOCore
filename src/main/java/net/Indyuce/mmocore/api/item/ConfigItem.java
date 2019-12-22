package net.Indyuce.mmocore.api.item;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.Indyuce.mmocore.MMOCore;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public class ConfigItem {
	private final String name, id, texture;
	private final ItemStack item;
	private final List<String> lore;
	private final int damage, modeldata;

	private boolean unbreakable;
	private Map<String, String> placeholders = new HashMap<>();

	public ConfigItem(ConfigurationSection config) {
		id = config.getName();
		name = config.getString("name");
		lore = config.getStringList("lore");
		item = new ItemStack(Material.valueOf(config.getString("item")));

		Validate.notNull(name, "Name cannot be null");
		Validate.notNull(lore, "Lore can be empty but not null");

		/*
		 * extra options
		 */
		damage = config.getInt("damage");
		texture = config.getString("texture");
		modeldata = config.getInt("custom-model-data");
	}

	public ConfigItem(String id) {
		this(MMOCore.plugin.configItems.get(id));
	}

	public ConfigItem(ConfigItem cache) {
		this.id = cache.id;
		name = cache.name;
		lore = cache.lore;
		item = cache.item;
		damage = cache.damage;
		texture = cache.texture;
		modeldata = cache.modeldata;
	}

	public ItemStack getItem(int amount) {
		ItemStack item = this.item.clone();
		item.setAmount(amount);
		return item;
	}

	public List<String> getLore() {
		return lore;
	}

	public String getName() {
		return name;
	}
	
	public int getModelData() {
		return modeldata;
	}

	public String getId() {
		return id;
	}

	public ConfigItem setUnbreakable() {
		unbreakable = true;
		return this;
	}

	public ConfigItem addPlaceholders(String... placeholders) {
		for (int j = 0; j < placeholders.length - 1; j += 2)
			this.placeholders.put(placeholders[j], placeholders[j + 1]);
		return this;
	}

	public ItemStack build() {
		return build(1);
	}

	public ItemStack build(int amount) {
		ItemStack item = getItem(amount);
		ItemMeta meta = item.getItemMeta();

		if (meta instanceof Damageable)
			((Damageable) meta).setDamage(damage);

		if(MMOLib.plugin.getVersion().isStrictlyHigher(1, 13))
			meta.setCustomModelData(modeldata);
		
		if (item.getType() == VersionMaterial.PLAYER_HEAD.toMaterial() && texture != null) {
			try {
				Field profileField = meta.getClass().getDeclaredField("profile");
				profileField.setAccessible(true);
				GameProfile profile = new GameProfile(UUID.randomUUID(), null);
				profile.getProperties().put("textures", new Property("textures", texture));
				profileField.set(meta, profile);
			} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException exception) {
				MMOCore.log(Level.WARNING, "Could not load config item texture of " + id);
			}
		}

		meta.addItemFlags(ItemFlag.values());
		meta.setDisplayName(format(name));

		List<String> lore = new ArrayList<>();
		getLore().forEach(line -> lore.add(format(line)));
		meta.setLore(lore);

		item.setItemMeta(meta);
		return unbreakable ? NBTItem.get(item).addTag(new ItemTag("Unbreakable", true)).toItem() : item;
	}

	protected String format(String string) {
		for (String placeholder : placeholders.keySet())
			if (string.contains("{" + placeholder + "}"))
				string = string.replace("{" + placeholder + "}", "" + placeholders.get(placeholder));
		return ChatColor.translateAlternateColorCodes('&', string);
	}
}
