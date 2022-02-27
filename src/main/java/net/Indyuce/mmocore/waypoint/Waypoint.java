package net.Indyuce.mmocore.waypoint;

import net.Indyuce.mmocore.player.Unlockable;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class Waypoint implements Unlockable {
    private final String id, name;
    private final Location loc;
    private final double radiusSquared;

    /**
     * Set that saves all the waypoints accessible when in this waypoint.
     * This turns the waypoints system into a giant network.
     * <p>
     * If it's empty it can access any waypoint.
     */
    private final Set<String> destinations = new HashSet<>();

    /**
     * Waypoint options saved here.
     */
    private final Map<WaypointOption, Boolean> options = new HashMap<>();

    /**
     * Stellium cost for each action (0 being the default cost)
     */
    private final Map<CostType, Double> costs = new HashMap<>();

    public Waypoint(ConfigurationSection config) {
        id = Objects.requireNonNull(config, "Could not load config section").getName();
        name = Objects.requireNonNull(config.getString("name"), "Could not load waypoint name");

        loc = readLocation(Objects.requireNonNull(config.getString("location"), "Could not read location"));
        radiusSquared = Math.pow(config.getDouble("radius"), 2);

        for (CostType costType : CostType.values())
            costs.put(costType, config.getDouble("cost." + costType.getPath()));

        for (WaypointOption option : WaypointOption.values())
            options.put(option, config.getBoolean("option." + option.getPath(), option.getDefaultValue()));

        destinations.addAll(config.getStringList("linked"));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return loc;
    }

    /**
     * @param other Another waypoint
     * @return If any player standing on that waypoint can teleport to given waypoint
     */
    public boolean hasDestination(Waypoint other) {
        return destinations.isEmpty() || destinations.contains(other.getId());
    }

    public double getCost(CostType type) {
        return costs.getOrDefault(type, 0d);
    }

    public boolean hasOption(WaypointOption option) {
        return options.get(option);
    }

    public boolean isOnWaypoint(Player player) {
        return player.getWorld().equals(loc.getWorld()) && player.getLocation().distanceSquared(loc) < radiusSquared;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public String getUnlockNamespacedKey() {
        return "waypoint:" + getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Waypoint waypoint = (Waypoint) o;
        return id.equals(waypoint.id);
    }

    private Location readLocation(String string) {
        String[] split = string.split(" ");

        World world = Bukkit.getWorld(split[0]);
        Validate.notNull(world, "Could not find world with name '" + split[0] + "'");

        double x = Double.parseDouble(split[1]);
        double y = Double.parseDouble(split[2]);
        double z = Double.parseDouble(split[3]);
        float yaw = split.length > 4 ? (float) Double.parseDouble(split[4]) : 0;
        float pitch = split.length > 5 ? (float) Double.parseDouble(split[5]) : 0;

        return new Location(world, x, y, z, yaw, pitch);
    }
}