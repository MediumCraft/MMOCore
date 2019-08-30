package net.Indyuce.mmocore.gui.api.item;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.gui.api.PluginInventory;

public class NoPlaceholderItem extends InventoryPlaceholderItem {
	public NoPlaceholderItem(ConfigurationSection config) {
		super(config);
	}

	public NoPlaceholderItem(Material material, ConfigurationSection config) {
		super(material, config);
	}

	@Override
	public Placeholders getPlaceholders(PluginInventory inv, int n) {
		return new Placeholders();
	}
}
