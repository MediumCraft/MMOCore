package net.Indyuce.mmocore.waypoint;

import java.util.ArrayList;
import java.util.List;

public class WaypointPath {
    private final List<Waypoint> waypoints;
    private double cost;

    public WaypointPath(Waypoint waypoint) {
        this.waypoints = new ArrayList<>();
        this.waypoints.add(waypoint);
        cost = 0;
    }

    public WaypointPath(Waypoint waypoint, double cost) {
        this.waypoints = new ArrayList<>();
        this.waypoints.add(waypoint);
        this.cost = cost;
    }

    public WaypointPath(List<Waypoint> waypoints, double cost) {
        this.waypoints = new ArrayList<>(waypoints);
        this.cost = cost;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public double getCost() {
        return cost;
    }

    public WaypointPath addCost(double cost) {
        this.cost += cost;
        return this;
    }

    public List<WaypointPath> addInOrder(List<WaypointPath> pathInfos) {
        int index = 0;
        while (index < pathInfos.size()) {
            if (cost < pathInfos.get(index).cost) {
                pathInfos.set(index, this);
                return pathInfos;
            }
            index++;
        }
        // If index == pathInfos.size() add the waypoint at the end
        pathInfos.add(this);
        return pathInfos;
    }


    /**
     * @param dynamic Display the first waypoint if it is dynamic as it is an intermediary point
     * @return List with all
     */
    public String displayIntermediaryWayPoints(boolean dynamic) {
        int beginIndex = dynamic ? 0 : 1;
        if (waypoints.size() <= beginIndex + 1)
            return "None";

        String result = "";
        for (int i = beginIndex; i < waypoints.size() - 1; i++)
            result += waypoints.get(i).getName() + (i != waypoints.size() - 2 ? ", " : "");
        return result;
    }

    public WaypointPath addWayPoint(Waypoint waypoint) {
        List<Waypoint> newWaypoints = new ArrayList<>();
        newWaypoints.addAll(waypoints);
        newWaypoints.add(waypoint);
        double cost = this.cost + getFinalWaypoint().getDirectCost(waypoint);
        return new WaypointPath(newWaypoints, cost);
    }

    public Waypoint getInitialWaypoint() {
        return waypoints.get(0);
    }

    public Waypoint getFinalWaypoint() {
        return waypoints.get(waypoints.size() - 1);
    }
}
