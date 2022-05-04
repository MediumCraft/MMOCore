package net.Indyuce.mmocore.waypoint;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.loot.droptable.condition.Condition;
import net.Indyuce.mmocore.loot.droptable.condition.ConditionInstance;
import net.Indyuce.mmocore.player.Unlockable;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;

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
    private final Map<String, Integer> destinations = new HashMap<>();

    /**
     * Waypoint options saved here.
     */
    private final Map<WaypointOption, Boolean> options = new HashMap<>();

    /**
     * Stellium cost for each action (0 being the default cost)
     */
    private final double dynamicCost, setSpawnCost;
    private final ArrayList<Condition> dynamicUseConditions = new ArrayList<>();

    private final Map<CostType, Double> costs = new HashMap<>();

    public double getDynamicCost() {
        return dynamicCost;
    }

    public double getSetSpawnCost() {
        return setSpawnCost;
    }

    public double getCost(Waypoint waypoint) {
        return getPath(waypoint).cost;
    }

    public Waypoint(ConfigurationSection config) {
        id = Objects.requireNonNull(config, "Could not load config section").getName();
        name = Objects.requireNonNull(config.getString("name"), "Could not load waypoint name");

        loc = readLocation(Objects.requireNonNull(config.getString("location"), "Could not read location"));
        radiusSquared = Math.pow(config.getDouble("radius"), 2);

        dynamicCost = config.getDouble("cost.dynamic-use");
        setSpawnCost = config.getDouble("cost.set-spawnpoint");


        for (WaypointOption option : WaypointOption.values())
            options.put(option, config.getBoolean("option." + option.getPath(), option.getDefaultValue()));

        //We load all the linked WayPoints
        if (config.contains("linked")) {
            ConfigurationSection section = config.getConfigurationSection("linked");
            for (String key : section.getKeys(false)) {
                destinations.put(key, section.getInt(key));
            }
        }
        if (config.contains("conditions")) {
            List<String> conditions = config.getStringList("conditions");
            for (String condition : conditions) {
                dynamicUseConditions.add(MMOCore.plugin.loadManager.loadCondition(new MMOLineConfig(condition)));

            }

        }
    }

    public boolean canHaveDynamicUse(Player player) {
        if (!options.get(WaypointOption.DYNAMIC))
            return false;

        for (Condition condition : dynamicUseConditions) {
            if (!condition.isMet(new ConditionInstance(player)))
                return false;
        }
        return true;
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
    @Deprecated
    public boolean hasDestination(Waypoint other) {
        return destinations.isEmpty() || destinations.keySet().contains(other.getId());
    }

    /**
     * Checks directly if the waypoint is directly linked to the current one
     *
     * @return Integer.POSITIVE_INFINITY if the way point is not linked
     */
    private int getSimpleCostDestination(Waypoint waypoint) {
        if (!destinations.keySet().contains(waypoint.getId()))
            return Integer.MAX_VALUE;
        return destinations.get(waypoint.getId());
    }


    public ArrayList<PathInfo> getAllPath() {
        //All the WayPoints that have been registered
        ArrayList<Waypoint> checkedPoints = new ArrayList<>();
        //All the path
        ArrayList<PathInfo> paths = new ArrayList();
        ArrayList<PathInfo> pointsToCheck = new ArrayList<>();
        pointsToCheck.add(new PathInfo(this));

        while (pointsToCheck.size() != 0) {
            PathInfo checked = pointsToCheck.get(0);
            pointsToCheck.remove(0);
            paths.add(checked);
            checkedPoints.add(checked.getFinalWaypoint());

            for (String wayPointId : checked.getFinalWaypoint().destinations.keySet()) {
                Waypoint toCheck = MMOCore.plugin.waypointManager.get(wayPointId);
                if (!checkedPoints.contains(toCheck)) {
                    PathInfo toCheckInfo = checked.addWayPoint(toCheck);
                    //We keep pointsToCheck ordered
                    pointsToCheck = toCheckInfo.addInOrder(pointsToCheck);
                }
            }
        }
        return paths;

    }


    @Nullable
    public PathInfo getPath(Waypoint targetWaypoint) {
        //All the WayPoints that have been registered
        ArrayList<Waypoint> checkedPoints = new ArrayList<>();
        //All the path
        ArrayList<PathInfo> paths = new ArrayList();
        ArrayList<PathInfo> pointsToCheck = new ArrayList<>();
        pointsToCheck.add(new PathInfo(this));

        while (pointsToCheck.size() != 0) {
            PathInfo checked = pointsToCheck.get(0);
            pointsToCheck.remove(0);
            paths.add(checked);
            checkedPoints.add(checked.getFinalWaypoint());

            if (checked.getFinalWaypoint().equals(targetWaypoint))
                return checked;

            for (String wayPointId : checked.getFinalWaypoint().destinations.keySet()) {
                Waypoint toCheck = MMOCore.plugin.waypointManager.get(wayPointId);
                if (!checkedPoints.contains(toCheck)) {
                    PathInfo toCheckInfo = checked.addWayPoint(toCheck);
                    //We keep pointsToCheck ordered
                    pointsToCheck = toCheckInfo.addInOrder(pointsToCheck);
                }
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

    public static class PathInfo {
        private final ArrayList<Waypoint> waypoints;
        private double cost;

        public ArrayList<Waypoint> getWaypoints() {
            return waypoints;
        }

        public double getCost() {
            return cost;
        }

        public PathInfo(Waypoint waypoint) {
            this.waypoints = new ArrayList<>();
            this.waypoints.add(waypoint);
            cost = 0;
        }
        public PathInfo(Waypoint waypoint,double cost) {
            this.waypoints = new ArrayList<>();
            this.waypoints.add(waypoint);
            this.cost = cost;
        }

        public PathInfo addCost(double cost) {
            this.cost+=cost;
            return this;
        }

        public ArrayList<PathInfo> addInOrder(ArrayList<PathInfo> pathInfos) {
            int index = 0;
            while (index < pathInfos.size()) {
                if (cost < pathInfos.get(index).cost) {
                    pathInfos.set(index, this);
                    return pathInfos;
                }
                index++;
            }
            //If index==pathInfos.size() we add the waypoint at the end
            pathInfos.add(this);
            return pathInfos;
        }


        public PathInfo(List<Waypoint> waypoints, double cost) {
            this.waypoints = new ArrayList<>(waypoints);
            this.cost = cost;
        }

        /**
         *
         * @param dynamic We display the first waypoint if it is dynamic as it is an intermediary point
         * @return
         */
        public String displayIntermediaryWayPoints(boolean dynamic) {
            String result = "";
            if(!dynamic) {
                if (waypoints.size() <= 2)
                    return "none";
                for (int i = 1; i < waypoints.size() - 1; i++) {
                    result += waypoints.get(i).name + (i != waypoints.size() - 2 ? "," : "");
                }

            }
            if(dynamic) {
                if (waypoints.size() <= 1)
                    return "none";
                for (int i = 0; i < waypoints.size() - 1; i++) {
                    result += waypoints.get(i).name + (i != waypoints.size() - 2 ? "," : "");
                }
            }
            return result;
            }

        public PathInfo addWayPoint(Waypoint waypoint) {
            Validate.isTrue(!waypoints.contains(waypoint), "You can't create cyclic path");
            ArrayList<Waypoint> newWaypoints = new ArrayList<>();
            newWaypoints.addAll(waypoints);
            newWaypoints.add(waypoint);
            double cost = this.cost + getFinalWaypoint().getSimpleCostDestination(waypoint);
            return new PathInfo(newWaypoints, cost);
        }


        public Waypoint getInitialWaypoint() {
            return waypoints.get(0);
        }

        public Waypoint getFinalWaypoint() {
            return waypoints.get(waypoints.size() - 1);
        }
    }
}