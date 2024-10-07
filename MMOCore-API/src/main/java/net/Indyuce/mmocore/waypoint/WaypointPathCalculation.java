package net.Indyuce.mmocore.waypoint;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WaypointPathCalculation {
    private final PlayerData playerData;
    private final Map<Waypoint, WaypointPath> paths = new HashMap<>();

    public WaypointPathCalculation(PlayerData playerData) {
        this.playerData = playerData;
    }

    @NotNull
    public WaypointPathCalculation run(@Nullable Waypoint source) {
        if (MMOCore.plugin.configManager.waypointAutoPathCalculation) runDijkstra(source);
        else runSimple(source);
        return this;
    }

    public void runSimple(@Nullable Waypoint source) {

        // Direct adjacency
        if (source != null) for (Map.Entry<Waypoint, Double> adjacent : source.getDestinations().entrySet())
            replaceIfLower(adjacent.getKey(), adjacent.getValue());

        // Dynamic waypoints
        for (Waypoint waypoint : MMOCore.plugin.waypointManager.getAll())
            if (waypoint.hasOption(WaypointOption.DYNAMIC)) replaceIfLower(waypoint, waypoint.getDynamicCost());
    }

    private void replaceIfLower(@NotNull Waypoint adjacent, double cost) {
        paths.compute(adjacent, (ignored, path) -> path == null || cost < path.getCost() ? new WaypointPath(cost) : path);
    }

    /**
     * Runs the Dijkstra algorithm to compute the shortest paths between source and
     * all available waypoints to a certain player. Paths using locked waypoints
     * are not computed.
     *
     * @param source Source waypoint node
     */
    public void runDijkstra(@Nullable Waypoint source) {

        final PriorityQueue<Pair<Waypoint, Double>> queue = new PriorityQueue<>(Comparator.comparingDouble(Pair::getRight));
        final Set<Waypoint> visited = new HashSet<>();

        // Initialization
        for (Waypoint waypoint : MMOCore.plugin.waypointManager.getAll()) // Dynamic waypoints
            if (waypoint.hasOption(WaypointOption.DYNAMIC) && waypoint.mayBeUsedDynamically(playerData.getPlayer()))
                init(queue, waypoint, waypoint.getDynamicCost());
        if (source != null) init(queue, source, 0); // !! After dynamic waypoints !!

        // Run Dijkstra
        do {
            final Waypoint currentNode = queue.remove().getLeft();

            // Mark as visited
            visited.add(currentNode);

            // Iterate over neighbors
            final WaypointPath nodePath = path(currentNode);
            for (Map.Entry<Waypoint, Double> adjacentData : currentNode.getDestinations().entrySet()) {

                // Waypoint is usable?
                final Waypoint adjacentNode = adjacentData.getKey();
                if (!(adjacentNode.isUnlockedByDefault()
                        || adjacentNode.isOnWaypoint(playerData.getPlayer())
                        || playerData.hasWaypoint(adjacentNode)))
                    return;

                final double edgeWeight = adjacentData.getValue();

                // Update distance if necessary
                final WaypointPath adjacentPath = path(adjacentNode);
                if (nodePath.getCost() + edgeWeight < adjacentPath.getCost())
                    paths.put(adjacentNode, nodePath.push(currentNode, edgeWeight));

                // Push to queue
                if (!visited.contains(adjacentNode)) queue.add(Pair.of(adjacentNode, edgeWeight));
            }

        } while (!queue.isEmpty());
    }

    private void init(@NotNull PriorityQueue<Pair<Waypoint, Double>> queue, @NotNull Waypoint waypoint, double cost) {
        queue.add(Pair.of(waypoint, cost));
        paths.put(waypoint, new WaypointPath(cost));
    }

    private WaypointPath path(Waypoint waypoint) {
        return paths.getOrDefault(waypoint, WaypointPath.INFINITE);
    }

    /**
     * @return Result of the waypoint path calculation
     */
    @NotNull
    public Map<Waypoint, WaypointPath> getPaths() {
        return paths;
    }
}
