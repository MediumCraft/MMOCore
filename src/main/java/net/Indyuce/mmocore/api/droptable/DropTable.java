package net.Indyuce.mmocore.api.droptable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.droptable.dropitem.DropItem;
import net.Indyuce.mmocore.api.load.PostLoadObject;
import net.Indyuce.mmocore.api.loot.LootBuilder;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class DropTable extends PostLoadObject {
	private final String id;
	private final Set<DropItem> drops = new LinkedHashSet<>();

	public DropTable(ConfigurationSection config) {
		super(config);

		id = config.getName();
	}

	public DropTable(String id) {
		super(null);

		this.id = id;
	}

	/*
	 * must be loaded after since drop tables must be initialized first
	 * otherwise no reference for drop table drop items.
	 */
	@Override
	protected void whenPostLoaded(ConfigurationSection config) {
		List<String> list = config.getStringList("items");
		Validate.notNull(list, "Could not find drop item list");

		for (String key : list)
			try {
				drops.add(MMOCore.plugin.loadManager.loadDropItem(new MMOLineConfig(key)));
			} catch (IllegalArgumentException exception) {
				MMOCore.plugin.getLogger().log(Level.WARNING,
						"Could not load drop item '" + key + "' from table '" + id + "': " + exception.getMessage());
			}
	}

	public String getId() {
		return id;
	}

	public void registerDropItem(DropItem item) {
		Validate.notNull(item);

		drops.add(item);
	}

	public List<ItemStack> collect(LootBuilder builder) {

		for (DropItem item : drops)
			if (item.rollChance() && builder.getCapacity() >= item.getWeight()) {
				item.collect(builder);
				builder.reduceCapacity(item.getWeight());
			}

		return builder.getLoot();
	}
}