package net.Indyuce.mmocore.api.droptable.dropitem;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.util.MMOLineConfig;

public class VanillaDropItem extends DropItem {
	private final Material material;

	public VanillaDropItem(MMOLineConfig config) {
		super(config);

		config.validate("type");
		this.material = Material.valueOf(config.getString("type"));
	}
	
	public Material getMaterial() {
		return material;
	}

	@Override
	public void collect(List<ItemStack> total) {
		total.add(new ItemStack(material, rollAmount()));
	}
}
