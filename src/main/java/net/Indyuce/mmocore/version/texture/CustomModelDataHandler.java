package net.Indyuce.mmocore.version.texture;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.item.NBTItem;
import net.Indyuce.mmocore.version.nms.ItemTag;

public class CustomModelDataHandler implements TextureHandler {
	@Override
	public NBTItem copyTexture(NBTItem item) {
		return MMOCore.plugin.nms.getNBTItem(new ItemStack(item.getItem().getType())).add(new ItemTag("CustomModelData", item.getInt("CustomModelData")));
	}

	@Override
	public ItemStack textureItem(Material material, int model) {
		return MMOCore.plugin.nms.getNBTItem(new ItemStack(material)).add(new ItemTag("CustomModelData", model)).toItem();
	}

	@Override
	public NBTItem applyTexture(NBTItem item, int model) {
		return item.add(new ItemTag("CustomModelData", model));
	}
}
