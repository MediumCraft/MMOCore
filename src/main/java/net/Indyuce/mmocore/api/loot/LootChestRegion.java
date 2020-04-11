package net.Indyuce.mmocore.api.loot;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.MMOCore;

public class LootChestRegion {
	private final String id;

	private final long chestSpawnPeriod;
	private final RegionBounds bounds;
	private final Set<ChestTier> tiers = new HashSet<>();
	
	/*
	 * last time 
	 */

	public LootChestRegion(ConfigurationSection config) {
		Validate.notNull(config, "Could not load config");
		id = config.getName();

		bounds = new RegionBounds(config.getConfigurationSection("bounds"));
		chestSpawnPeriod = config.getInt("spawn-period");

		Validate.isTrue(config.isConfigurationSection("tiers"), "Could not find chest tiers");
		for (String key : config.getConfigurationSection("tiers").getKeys(false))
			try {
				tiers.add(new ChestTier(config.getConfigurationSection("tiers." + key)));
			} catch (IllegalArgumentException exception) {
				MMOCore.plugin.getLogger().log(Level.WARNING,
						"Could not load tier '" + key + "' from chest region '" + id + "': " + exception.getMessage());
			}
	}

	public String getId() {
		return id;
	}

	public Set<ChestTier> getTiers() {
		return tiers;
	}

	public RegionBounds getBounds() {
		return bounds;
	}

	public long getChestSpawnPeriod() {
		return chestSpawnPeriod;
	}
}
