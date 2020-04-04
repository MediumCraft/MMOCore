package net.Indyuce.mmocore.api.droptable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.droptable.dropitem.DropItem;
import net.Indyuce.mmocore.api.load.MMOLoadException;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class DropTable {
	private final String id;
	private final Set<DropItem> drops = new HashSet<>();

	/*
	 * cached in order to load other items.
	 */
	private ConfigurationSection loaded;

	public DropTable(ConfigurationSection section) {
		id = section.getName();
		loaded = section;
	}

	/*
	 * must be loaded after since drop tables must be initialized first
	 * otherwise no reference for drop table drop items.
	 */
	public DropTable load() {

		List<String> list = loaded.getStringList("items");
		Validate.notNull(list, "Could not find drop item list");

		for (String key : list)
			try {
				drops.add(MMOCore.plugin.loadManager.loadDropItem(new MMOLineConfig(key)));
			} catch (MMOLoadException exception) {
				exception.printConsole("DropTables", "drop item");
			}

		loaded = null;
		return this;
	}

	public String getId() {
		return id;
	}

	public List<ItemStack> collect() {
		List<ItemStack> total = new ArrayList<>();

		for (DropItem item : drops)
			if (item.rollChance())
				item.collect(total);

		return total;
	}
}