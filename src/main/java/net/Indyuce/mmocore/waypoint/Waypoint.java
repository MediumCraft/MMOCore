package net.Indyuce.mmocore.waypoint;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.util.PostLoadObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.loot.chest.condition.Condition;
import net.Indyuce.mmocore.loot.chest.condition.ConditionInstance;
import net.Indyuce.mmocore.player.Unlockable;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public class Waypoint extends PostLoadObject implements Unlockable {
    private final String id, name;
    private final Location loc;
    private final List<String> lore;
    private final double radiusSquared;

    /**
     * Set that saves all the waypoints accessible when in this waypoint.
     * This turns the waypoints system into a giant network.
     * <p>
     * If it's empty it can access any waypoint.
     */
    private final Map<Waypoint, Double> destinations = new HashMap<>();

    /**
     * Waypoint options saved here.
     */
    private final Map<WaypointOption, Boolean> options = new HashMap<>();
    private final double dynamicCost, setSpawnCost, normalCost;
    private final List<Condition> dynamicUseConditions = new ArrayList<>();

    public Waypoint(ConfigurationSection config) {
        super(config);

        id = Objects.requireNonNull(config, "Could not load config section").getName();
        name = Objects.requireNonNull(config.getString("name"), "Could not load waypoint name");
        lore = Objects.requireNonNullElse(config.getStringList("lore"), new ArrayList<>());

        loc = readLocation(Objects.requireNonNull(config.getString("location"), "Could not read location"));
        radiusSquared = Math.pow(config.getDouble("radius"), 2);

        dynamicCost = config.getDouble("cost.dynamic-use");
        normalCost = config.getDouble("cost.normal-use");
        setSpawnCost = config.getDouble("cost.set-spawnpoint");

        for (WaypointOption option : WaypointOption.values())
            options.put(option, config.getBoolean("option." + option.getPath(), option.getDefaultValue()));

        if (config.contains("dynamic-conditions")) {
            List<String> conditions = config.getStringList("dynamic-conditions");
            for (String condition : conditions)
                try {
                    dynamicUseConditions.add(MMOCore.plugin.loadManager.loadCondition(new MMOLineConfig(condition)));
                } catch (RuntimeException exception) {
                    MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load condition '" + condition + "' from waypoint '" + id + "': " + exception.getMessage());
                }
        }
    }


    @Override
    protected void whenPostLoaded(@NotNull ConfigurationSection config) {

        // Load waypoint network
        if (config.contains("linked")) {
            ConfigurationSection section = config.getConfigurationSection("linked");
            for (String key : section.getKeys(false))
                destinations.put(MMOCore.plugin.waypointManager.get(key), section.getDouble(key));
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public Location getLocation() {
        return loc;
    }

    public double getDynamicCost() {
        return dynamicCost;
    }

    @Deprecated
    public double getSetSpawnCost() {
        return setSpawnCost;
    }

    public boolean mayBeUsedDynamically(Player player) {
        if (!options.get(WaypointOption.DYNAMIC))
            return false;

        for (Condition condition : dynamicUseConditions)
            if (!condition.isMet(new ConditionInstance(player)))
                return false;

        return true;
    }

    /**
     * @return Integer.POSITIVE_INFINITY if the way point is not linked
     *         If it is, cost of the instant travel between the two waypoints.
     */
    public double getDirectCost(Waypoint waypoint) {
        return destinations.isEmpty() ? normalCost : destinations.getOrDefault(waypoint, Double.POSITIVE_INFINITY);
    }

    public List<WaypointPath> getAllPath() {
        //All the WayPoints that have been registered
        List<Waypoint> checkedPoints = new ArrayList<>();
        //All the path
        List<WaypointPath> paths = new ArrayList();
        List<WaypointPath> pointsToCheck = new ArrayList<>();
        pointsToCheck.add(new WaypointPath(this));

        while (pointsToCheck.size() != 0) {
            WaypointPath checked = pointsToCheck.get(0);
            pointsToCheck.remove(0);
            // If the point has already been checked, pass
            if (checkedPoints.contains(checked.getFinalWaypoint()))
                continue;

            paths.add(checked);
            checkedPoints.add(checked.getFinalWaypoint());

            for (Waypoint toCheck : checked.getFinalWaypoint().destinations.keySet())
                if (!checkedPoints.contains(toCheck)) {
                    WaypointPath toCheckInfo = checked.addWayPoint(toCheck);
                    // We keep pointsToCheck ordered
                    pointsToCheck = toCheckInfo.addInOrder(pointsToCheck);
                }
        }
        return paths;
    }

    @Nullable
    public WaypointPath getPath(Waypoint targetWaypoint) {
        //All the WayPoints that have been registered
        List<Waypoint> checkedPoints = new ArrayList<>();
        //All the path
        List<WaypointPath> paths = new ArrayList();
        List<WaypointPath> pointsToCheck = new ArrayList<>();
        pointsToCheck.add(new WaypointPath(this));

        while (pointsToCheck.size() != 0) {
            WaypointPath checked = pointsToCheck.get(0);
            pointsToCheck.remove(0);
            // If the point has already been checked, pass
            if (checkedPoints.contains(checked.getFinalWaypoint()))
                continue;

            paths.add(checked);
            checkedPoints.add(checked.getFinalWaypoint());

            if (checked.getFinalWaypoint().equals(targetWaypoint))
                return checked;

            for (Waypoint toCheck : checked.getFinalWaypoint().destinations.keySet())
                if (!checkedPoints.contains(toCheck)) {
                    WaypointPath toCheckInfo = checked.addWayPoint(toCheck);
                    // We keep pointsToCheck ordered
                    pointsToCheck = toCheckInfo.addInOrder(pointsToCheck);
                }
        }

        //If no path has been found we return null
        return null;
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