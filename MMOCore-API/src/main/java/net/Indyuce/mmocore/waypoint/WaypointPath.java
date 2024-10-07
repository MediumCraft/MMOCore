package net.Indyuce.mmocore.waypoint;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WaypointPath {
    private final List<Waypoint> waypoints = new ArrayList<>();
    private final double cost;

    public static final WaypointPath INFINITE = new WaypointPath(Double.POSITIVE_INFINITY);

    public WaypointPath() {
        this(0);
    }

    public WaypointPath(double cost) {
        this.cost = cost;
    }

    @NotNull
    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public double getCost() {
        return cost;
    }

    @NotNull
    public String displayIntermediaryWayPoints(String splitter, String none) {
        if (waypoints.isEmpty()) return none;

        boolean b = false;
        final StringBuilder result = new StringBuilder();
        for (Waypoint waypoint : waypoints) {
            if (b) result.append(splitter);
            result.append(waypoint.getName());
            if (!b) b = true;
        }

        return result.toString();
    }

    @NotNull
    public WaypointPath push(@NotNull Waypoint waypoint, double extraCost) {
        final WaypointPath clone = new WaypointPath(this.cost + extraCost);
        clone.waypoints.add(waypoint);
        return clone;
    }
}
