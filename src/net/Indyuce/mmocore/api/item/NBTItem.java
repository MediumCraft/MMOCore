package net.Indyuce.mmocore.api.item;

import java.util.List;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.version.nms.ItemTag;

public abstract class NBTItem {
	protected ItemStack item;

	public NBTItem(ItemStack item) {
		this.item = item;
	}

	public ItemStack getItem() {
		return item;
	}

	public abstract String getString(String path);

	public abstract boolean has(String path);

	public abstract boolean getBoolean(String path);

	public abstract double getDouble(String path);

	public abstract int getInt(String path);

	public abstract NBTItem add(ItemTag... tags);

	public abstract NBTItem remove(String... paths);

	public abstract Set<String> getTags();

	public abstract ItemStack toItem();

	public void add(List<ItemTag> tags) {
		for (ItemTag tag : tags)
			add(tag);
	}

	public static NBTItem get(ItemStack item) {
		return MMOCore.plugin.nms.getNBTItem(item);
	}
}
