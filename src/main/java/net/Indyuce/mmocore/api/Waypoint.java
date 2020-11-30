package net.Indyuce.mmocore.api;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class Waypoint {
	private final String id, name;
	private final Location loc;
	private final double radiusSquared, stellium;
	private final boolean def, sneak, dynamic;

	public Waypoint(ConfigurationSection section) {
		Validate.notNull(section, "Could not load config section");

		id = section.getName();

		name = section.getString("name");
		Validate.notNull(name, "Could not load waypoint name");

		String format = section.getString("location");
		Validate.notNull(format, "Could not read location");
		loc = readLocation(format);

		stellium = section.getDouble("stellium");
		radiusSquared = Math.pow(section.getDouble("radius"), 2);
		def = section.getBoolean("default");
		sneak = !section.contains("sneak") || section.getBoolean("sneak");
		dynamic = section.getBoolean("dynamic");
	}

	public Location getLocation() {
		return loc;
	}

	public String getName() {
		return name;
	}

	public double getStelliumCost() {
		return stellium;
	}

	public boolean hasSneakEnabled() {
		return sneak;
	}

	public String getId() {
		return id;
	}

	public boolean isDefault() {
		return def;
	}

	public boolean isDynamic() {
		return dynamic;
	}

	public boolean isOnWaypoint(Player player) {
		return player.getWorld().equals(loc.getWorld()) && player.getLocation().distanceSquared(loc) < radiusSquared;
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof Waypoint && ((Waypoint) object).id.equals(id);
	}

	private Location readLocation(String string) {
		String[] split = string.split(" ");

		World world = Bukkit.getWorld(split[0]);
		Validate.notNull(world, "Could not find world " + world);

		double x = Double.parseDouble(split[1]);
		double y = Double.parseDouble(split[2]);
		double z = Double.parseDouble(split[3]);
		float yaw = split.length > 4 ? (float) Double.parseDouble(split[4]) : 0;
		float pitch = split.length > 5 ? (float) Double.parseDouble(split[5]) : 0;

		return new Location(world, x, y, z, yaw, pitch);
	}
}