package net.Indyuce.mmocore.util.item;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class HashableLocation {
    private final World world;
    private final int x, y, z;

    public HashableLocation(Location loc) {
        this.world = loc.getWorld();
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
    }

    public World getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Location bukkit() {
        return new Location(world, x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashableLocation that = (HashableLocation) o;
        return x == that.x && y == that.y && z == that.z && world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }
}