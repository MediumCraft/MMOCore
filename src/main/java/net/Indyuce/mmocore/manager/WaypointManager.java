package net.Indyuce.mmocore.manager;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.waypoint.Waypoint;

public class WaypointManager {
	private final Map<String, Waypoint> waypoints = new LinkedHashMap<>();

	public WaypointManager(FileConfiguration config) {
		for (String key : config.getKeys(false))
			try {
				register(new Waypoint(config.getConfigurationSection(key)));
			} catch (IllegalArgumentException exception) {
				MMOCore.log(Level.WARNING, "Could not load waypoint '" + key + "': " + exception.getMessage());
			}
	}

	public Collection<Waypoint> getAll() {
		return waypoints.values();
	}

	public boolean has(String id) {
		return waypoints.containsKey(id);
	}

	public Waypoint get(String id) {
		return waypoints.get(id);
	}

	public void register(Waypoint waypoint) {
		waypoints.put(waypoint.getId(), waypoint);
	}

	public Waypoint getCurrentWaypoint(Player player) {
		for (Waypoint waypoint : getAll())
			if (waypoint.isOnWaypoint(player))
				return waypoint;
		return null;
	}
}
