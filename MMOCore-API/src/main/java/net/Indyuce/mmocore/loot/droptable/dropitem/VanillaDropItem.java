package net.Indyuce.mmocore.loot.droptable.dropitem;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.loot.LootBuilder;
import io.lumine.mythic.lib.api.MMOLineConfig;

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
	public void collect(LootBuilder builder) {
		builder.addLoot(new ItemStack(material, rollAmount()));
	}
}
