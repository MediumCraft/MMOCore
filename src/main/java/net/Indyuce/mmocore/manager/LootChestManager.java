package net.Indyuce.mmocore.manager;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.loot.chest.LootChest;
import net.Indyuce.mmocore.loot.chest.LootChestRegion;
import net.Indyuce.mmocore.util.HashableLocation;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class LootChestManager implements MMOCoreManager {

    /**
     * Active loot chests in the server
     */
    private final Map<HashableLocation, LootChest> active = new HashMap<>();

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

    public Collection<LootChest> getActive() {
        return active.values();
    }

    public void register(LootChest chest) {
        active.put(chest.getBlock().getLocation(), chest);
    }

    public void unregister(LootChest chest) {
        active.remove(chest.getBlock().getLocation());
    }

    @Nullable
    public LootChest getChest(Location loc) {
        return active.get(new HashableLocation(loc));
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
