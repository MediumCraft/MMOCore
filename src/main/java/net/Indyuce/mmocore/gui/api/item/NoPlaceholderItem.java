package net.Indyuce.mmocore.gui.api.item;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.gui.api.PluginInventory;

public class NoPlaceholderItem extends InventoryPlaceholderItem {
	public NoPlaceholderItem(ConfigurationSection config) {
		super(config);
	}

	public NoPlaceholderItem(ItemStack stack, ConfigurationSection config) {
		super(stack, config);
	}

	@Override
	public Placeholders getPlaceholders(PluginInventory inv, int n) {
		return new Placeholders();
	}
}
