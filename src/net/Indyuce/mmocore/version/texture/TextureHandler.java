package net.Indyuce.mmocore.version.texture;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.api.item.NBTItem;

public interface TextureHandler {
	public NBTItem copyTexture(NBTItem item);
	
	public ItemStack textureItem(Material material, int model);
	
	public NBTItem applyTexture(NBTItem item, int model);
}
