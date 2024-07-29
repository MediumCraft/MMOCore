package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.util.FileUtils;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.waypoint.Waypoint;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class WaypointManager implements MMOCoreManager {
    private final Map<String, Waypoint> waypoints = new LinkedHashMap<>();

    public Collection<Waypoint> getAll() {
        return waypoints.values();
    }

    public boolean has(String id) {
        return waypoints.containsKey(id);
    }

    @Nullable
    public Waypoint get(String id) {
        return waypoints.get(id);
    }

    public void register(Waypoint waypoint) {
        Validate.isTrue(!waypoints.containsKey(Objects.requireNonNull(waypoint, "Waypoint cannot be null").getId()), "There is already a waypoint with ID '" + waypoint.getId() + "'");

        waypoints.put(waypoint.getId(), waypoint);
    }

    @Nullable
    public Waypoint getCurrentWaypoint(Player player) {
        for (Waypoint waypoint : getAll())
            if (waypoint.isOnWaypoint(player))
                return waypoint;
        return null;
    }

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore)
            waypoints.clear();


        FileUtils.loadObjectsFromFolder(MMOCore.plugin, "waypoints", false, (key, config) -> {
            register(new Waypoint(config));
        }, "Could not load waypoint '%s' from file '%s': %s");

        for (Waypoint waypoint : waypoints.values())
            try {
                waypoint.getPostLoadAction().performAction();
            } catch (RuntimeException exception) {
                MMOCore.log(Level.WARNING, "Could not post-load waypoint '" + waypoint.getId() + "': " + exception.getMessage());
            }
    }
}
