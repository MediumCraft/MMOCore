package net.Indyuce.mmocore.loot.chest;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.loot.RandomWeightedRoll;
import net.Indyuce.mmocore.api.event.LootChestSpawnEvent;
import net.Indyuce.mmocore.loot.LootBuilder;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public class LootChestRegion {
    private final String id;

    private final long chestSpawnPeriod;
    private final RegionBounds bounds;
    private final ChestAlgorithmOptions algOptions;
    private final Set<ChestTier> tiers = new LinkedHashSet<>();
    private final BukkitRunnable runnable = new BukkitRunnable() {

        @Override
        public void run() {
            getBounds().getPlayers().filter(player -> player.getActivityTimeOut(PlayerActivity.LOOT_CHEST_SPAWN) == 0)
                    .findAny().ifPresent(player -> spawnChest(player));
        }
    };

    private static final Random RANDOM = new Random();

    public LootChestRegion(ConfigurationSection config) {
        Validate.notNull(config, "Could not load config");
        id = config.getName().toLowerCase().replace("_", "-").replace(" ", "-");

        bounds = new RegionBounds(config.getConfigurationSection("bounds"));
        chestSpawnPeriod = config.getLong("spawn-period", 5 * 60);
        algOptions = config.contains("algorithm-options") ? new ChestAlgorithmOptions(config.getConfigurationSection("algorithm-options"))
                : ChestAlgorithmOptions.DEFAULT;

        Validate.isTrue(config.isConfigurationSection("tiers"), "Could not find chest tiers");
        for (String key : config.getConfigurationSection("tiers").getKeys(false))
            try {
                tiers.add(new ChestTier(config.getConfigurationSection("tiers." + key)));
            } catch (IllegalArgumentException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING,
                        "Could not load tier '" + key + "' from chest region '" + id + "': " + exception.getMessage());
            }

        Validate.isTrue(!tiers.isEmpty(), "Your region must have at least one chest tier");

        // Run timer
        runnable.runTaskTimer(MMOCore.plugin, chestSpawnPeriod * 20, chestSpawnPeriod * 20);
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

    public BukkitRunnable getRunnable() {
        return runnable;
    }

    public void spawnChest(PlayerData player) {

        // Apply chest cooldown
        player.setLastActivity(PlayerActivity.LOOT_CHEST_SPAWN);

        // First randomly determine the chest tier
        ChestTier tier = rollTier(player);

        // Find a random location, 20 trials max
        Location location = getRandomLocation(player.getPlayer().getLocation());
        if (location == null)
            return;

        LootChest lootChest = new LootChest(tier, this, location.getBlock());
        LootBuilder builder = new LootBuilder(player, tier.rollCapacity(player));

        LootChestSpawnEvent event = new LootChestSpawnEvent(player, lootChest, builder);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        List<Integer> slots = new ArrayList<>();
        for (int j = 0; j < 27; j++)
            slots.add(j);

        location.getBlock().setType(Material.CHEST);
        Chest chest = (Chest) location.getBlock().getState();
        tier.getDropTable().collect(builder).forEach(item -> {
            Integer slot = slots.get(RANDOM.nextInt(slots.size()));
            chest.getInventory().setItem(slot, item);
            slots.remove(slot);
        });

        MMOCore.plugin.lootChests.register(lootChest);
    }

    /**
     * @param player Player rolling the tier
     * @return A randomly picked tiers taking into account tier spawn rates
     *         and the player Chance attribute
     */
    @NotNull
    public ChestTier rollTier(PlayerData player) {
        return new RandomWeightedRoll<>(player, tiers, MMOCore.plugin.configManager.lootChestsChanceWeight).rollItem();
    }

    public Location getRandomLocation(Location center) {

        for (int j = 0; j < algOptions.iterations; j++) {
            Location random = tryRandomDirection(center);
            if (random != null)
                return random;
        }

        /*
         * No location has been found after the X iterations, return null and
         * cancel chest spawning. worst case scenario, should not happen too
         * often except if the player is in a really NARROW zone
         */
        return null;
    }

    public Location tryRandomDirection(Location center) {

        /*
         * Chooses a random direction and get the block in
         * that direction which has the same height as the player
         */
        double a = RANDOM.nextDouble() * 2 * Math.PI;
        Vector dir = new Vector(Math.cos(a), 0, Math.sin(a))
                .multiply(algOptions.minRange + RANDOM.nextDouble() * (algOptions.maxRange - algOptions.minRange));
        Location random = center.add(dir);

        /*
         * Go up and down at the same time till it finds
         * a non-solid block with a solid block underneath
         */
        for (int h = 0; h <= algOptions.height * 2; h++) {
            int z = h % 2 == 0 ? h / 2 : -(h + 1) / 2; // bijective from N to Z
            Location checked = random.clone().add(0, z, 0);
            if (isSuitable(checked))
                return checked;
        }

        return null;
    }

    private boolean isSuitable(Location loc) {
        return !loc.getBlock().getType().isSolid() && loc.clone().add(0, -1, 0).getBlock().getType().isSolid();
    }
}
