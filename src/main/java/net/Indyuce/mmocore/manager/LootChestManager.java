package net.Indyuce.mmocore.manager;

import java.util.*;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.loot.chest.LootChest;
import net.Indyuce.mmocore.loot.chest.LootChestRegion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LootChestManager implements MMOCoreManager {

	/**
	 * Active loot chests in the server
	 */
	private final Set<LootChest> active = new HashSet<>();

	/**
	 * Registered loot chest regions
	 */
	private final Map<String, LootChestRegion> regions = new HashMap<>();

	public boolean hasRegion(String id) {
		return regions.containsKey(id);
	}

    /**
     * @return Region with specific identifier
     * @throws NullPointerException if not found
     */
    @NotNull
    public LootChestRegion getRegion(String id) {
        return Objects.requireNonNull(regions.get(id), "Could not find region with ID '" + id + "'");
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

	@Nullable
	public LootChest getChest(Location loc) {

		for (LootChest chest : active)
			if (chest.getBlock().matches(loc))
				return chest;

		return null;
	}

	@Override
	public void initialize(boolean clearBefore) {
		if (clearBefore) {
			regions.values().forEach(region -> region.getRunnable().cancel());
			regions.clear();
		}

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
