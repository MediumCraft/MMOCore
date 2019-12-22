package net.Indyuce.mmocore.api.item;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.ItemTag;
import net.mmogroup.mmolib.api.NBTItem;

public class CurrencyItem extends ConfigItem {
	private final int worth, amount;

	public CurrencyItem(String key, int worth) {
		this(key, worth, 1);
	}

	public CurrencyItem(String key, int worth, int amount) {
		super(key);
		addPlaceholders("worth", "" + (this.worth = worth));
		this.amount = amount;
	}

	@Override
	public ItemStack build() {
		ItemStack item = getItem(amount);
		ItemMeta meta = item.getItemMeta();

		meta.addItemFlags(ItemFlag.values());
		meta.setDisplayName(format(getName()));

		List<String> lore = new ArrayList<>();
		getLore().forEach(line -> lore.add(format(line)));
		meta.setLore(lore);

		if(MMOLib.plugin.getVersion().isStrictlyHigher(1, 13))
			meta.setCustomModelData(getModelData());
		
		item.setItemMeta(meta);
		return NBTItem.get(item).addTag(new ItemTag("RpgWorth", worth)).toItem();
	}
}
