package net.Indyuce.mmocore.api.loot;

import java.util.Optional;
import java.util.Random;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class RegionBounds {
	private final World world;
	private final int x1, z1, x2, z2;

	private static final Random random = new Random();

	public RegionBounds(ConfigurationSection config) {
		Validate.notNull(config, "Could not load config");
		Validate.notNull(world = Bukkit.getWorld(config.getString("world")),
				"Could not find world " + config.getString("world"));
		x1 = config.getInt("x1");
		z1 = config.getInt("z1");

		x2 = config.getInt("x2");
		z2 = config.getInt("z2");
	}

	public RegionBounds(Location loc1, Location loc2) {
		Validate.isTrue(loc1.getWorld().equals(loc2.getWorld()), "Locations must be in the same world");
		world = loc1.getWorld();
		x1 = loc1.getBlockX();
		z1 = loc1.getBlockZ();

		x2 = loc2.getBlockX();
		z2 = loc2.getBlockZ();
	}

	public boolean isInRegion(Player player) {
		int x = player.getLocation().getBlockX();
		int z = player.getLocation().getBlockZ();
		return player.getWorld().equals(world) && x1 <= x && x2 >= x && z1 <= z && z2 >= z;
	}

	public Location findChestLocation() {
		
		Optional<Player> player = world.getPlayers().stream().filter(check -> isInRegion(check)).findAny();
		
		// TODO
		return null;
	}
}
