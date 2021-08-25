package net.Indyuce.mmocore.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.loot.chest.LootChest;
import net.Indyuce.mmocore.loot.chest.LootChestRegion;

public class LootChestManager {

	/*
	 * all active loot chests in the server
	 */
	private final Set<LootChest> active = new HashSet<>();

	private final Map<String, LootChestRegion> regions = new HashMap<>();

	public boolean hasRegion(String id) {
		return regions.containsKey(id);
	}

	public LootChestRegion getRegion(String id) {
		return regions.get(id);
	}

	public Collection<LootChestRegion> getRegions() {
		return regions.values();
	}

	public Set<LootChest> getActive() {
		return active;
	}

	public void register(LootChest chest) {
		active.add(chest);
	}

	public void unregister(LootChest chest) {
		active.remove(chest);
	}

	public LootChest getChest(Location loc) {

		for (LootChest chest : active)
			if (chest.getBlock().matches(loc))
				return chest;

		return null;
	}

	public void reload() {
		regions.values().forEach(region -> region.getRunnable().cancel());
		regions.clear();

		FileConfiguration config = new ConfigFile("loot-chests").getConfig();
		for (String key : config.getKeys(false))
			try {
				LootChestRegion region = new LootChestRegion(config.getConfigurationSection(key));
				regions.put(region.getId(), region);
			} catch (IllegalArgumentException exception) {
				MMOCore.plugin.getLogger().log(Level.WARNING,
						"An error occured while trying to load loot chest region '" + key + "': " + exception.getMessage());
			}

	}
}
