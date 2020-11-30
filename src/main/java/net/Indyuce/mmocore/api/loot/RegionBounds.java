package net.Indyuce.mmocore.api.loot;

import java.util.stream.Stream;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.player.PlayerData;

public class RegionBounds {
	private final World world;
	private final int x1, z1, x2, z2;

	public RegionBounds(ConfigurationSection config) {
		Validate.notNull(config, "Could not load config");

		String name = config.getString("world");
		Validate.notNull(name, "Could not find world name");
		Validate.notNull(world = Bukkit.getWorld(name), "Could not find world " + config.getString("world"));

		x1 = Math.min(config.getInt("x1"), config.getInt("x2"));
		x2 = Math.max(config.getInt("x1"), config.getInt("x2"));

		z1 = Math.min(config.getInt("z1"), config.getInt("z2"));
		z2 = Math.max(config.getInt("z1"), config.getInt("z2"));
	}

	public RegionBounds(Location loc1, Location loc2) {
		Validate.isTrue(loc1.getWorld().equals(loc2.getWorld()), "Locations must be in the same world");
		world = loc1.getWorld();
		x1 = Math.min(loc1.getBlockX(), loc2.getBlockX());
		x2 = Math.max(loc1.getBlockX(), loc2.getBlockX());

		z1 = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
		z2 = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
	}

	public Stream<PlayerData> getPlayers() {
		return world.getPlayers().stream().filter(this::isInRegion).map(PlayerData::get);
	}

	public boolean isInRegion(Player player) {
		int x = player.getLocation().getBlockX();
		int z = player.getLocation().getBlockZ();
		return player.getWorld().equals(world) && x1 <= x && x2 >= x && z1 <= z && z2 >= z;
	}
}
