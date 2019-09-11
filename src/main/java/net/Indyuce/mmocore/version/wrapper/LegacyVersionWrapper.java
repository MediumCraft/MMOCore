package net.Indyuce.mmocore.version.wrapper;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Crops;
import org.bukkit.material.MaterialData;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import net.Indyuce.mmocore.MMOCoreUtils;
import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.MovingObjectPosition;
import net.minecraft.server.v1_12_R1.Vec3D;

@SuppressWarnings("deprecation")
public class LegacyVersionWrapper implements VersionWrapper {

	@Override
	public void spawnParticle(Particle particle, Location loc, int amount, double x, double y, double z, double speed, float size, Color color) {
		loc.getWorld().spawnParticle(particle, loc, 0, (double) color.getRed() / 255, (double) color.getGreen() / 255, (double) color.getBlue() / 255, 0);
	}

	@Override
	public void spawnParticle(Particle particle, Location loc, int amount, double x, double y, double z, double speed, Material material) {
		loc.getWorld().spawnParticle(particle, loc, amount, x, y, z, 0, new MaterialData(material));
	}

	@Override
	public BossBar createBossBar(NamespacedKey key, String title, BarColor color, BarStyle style, BarFlag... flags) {
		return Bukkit.createBossBar(title, color, style, flags);
	}
	

	@Override
	public Enchantment getEnchantmentFromString(String s) {
		if(s.equals("protection")) s = "PROTECTION_ENVIRONMENTAL";
		if(s.equals("fire_protection")) s = "PROTECTION_FIRE";
		if(s.equals("feather_falling")) s = "PROTECTION_FALL";
		if(s.equals("blast_protection")) s = "PROTECTION_EXPLOSIONS";
		if(s.equals("projectile_protection")) s = "PROTECTION_PROJECTILE";
		if(s.equals("respiration")) s = "OXYGEN";
		if(s.equals("aqua_affinity")) s = "WATER_WORKER";
		if(s.equals("sharpness")) s = "DAMAGE_ALL";
		if(s.equals("smite")) s = "DAMAGE_UNDEAD";
		if(s.equals("bane_of_arthropods")) s = "DAMAGE_ARTHROPODS";
		if(s.equals("looting")) s = "LOOT_BONUS_MOBS";
		if(s.equals("sweeping")) s = "SWEEPING_EDGE";
		if(s.equals("efficiency")) s = "DIG_SPEED";
		if(s.equals("unbreaking")) s = "DURABILITY";
		if(s.equals("fortune")) s = "LOOT_BONUS_BLOCKS";
		if(s.equals("power")) s = "ARROW_DAMAGE";
		if(s.equals("punch")) s = "ARROW_KNOCKBACK";
		if(s.equals("flame")) s = "ARROW_FIRE";
		if(s.equals("infinity")) s = "ARROW_INFINITE";
		if(s.equals("luck_of_the_sea")) s = "LUCK";
		
		return Enchantment.getByName(s.toUpperCase());
	}

	@Override
	public FurnaceRecipe getFurnaceRecipe(NamespacedKey key, ItemStack item, Material material, float exp, int cook) {
		try {
			return (FurnaceRecipe) Class.forName("org.bukkit.inventory.FurnaceRecipe").getConstructor(ItemStack.class, Material.class, Integer.TYPE, Integer.TYPE).newInstance(item, material, 0, (int) exp);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException exception) {
			exception.printStackTrace();
			return null;
		}
	}
	
	//I'm so sorry Indy.
	//I have no fucking clue what I'm doing :(
	@Override
	public RayTraceResult rayTrace(Player player, Vector direction, double range) {
		BlockIterator blocksToAdd = new BlockIterator(player.getWorld(), player.getLocation().toVector(), direction, 0.0d, (int) range);
        Location location = null;
		
		while(blocksToAdd.hasNext()) {
			location = blocksToAdd.next().getLocation();
        }

    	if(location != null) return new RayTraceResult(location.toVector());

		return new RayTraceResult(null);
	}
	

	@Override
	public RayTraceResult rayTraceEntities(Player player, Vector direction, double range) {

		Location loc = player.getEyeLocation();
		Vec3D vec = new Vec3D(loc.getDirection().getX(), loc.getDirection().getY(), loc.getDirection().getZ());
		MovingObjectPosition block = ((CraftPlayer) player).getHandle().getBoundingBox().b(vec, new Vec3D(vec.x, vec.y, vec.z).add(range * vec.x, range * vec.y, range * vec.z));

		double d = block == null ? range : Math.sqrt(block.pos.distanceSquared(new Vec3D(loc.getX(), loc.getY(), loc.getZ())));
		Ray3D line = new Ray3D(player.getEyeLocation());
		for (Entity entity : player.getNearbyEntities(d, d, d))
			if (line.intersectsRay(((CraftEntity) entity).getHandle().getBoundingBox()) && MMOCoreUtils.canTarget(player, entity))
				return new RayTraceResult(entity.getLocation().toVector(), (LivingEntity) entity);

		return new RayTraceResult(null);
	}

	public class Ray3D extends Vec3D {
		public final Vec3D dir;

		/*
		 * warning, direction is not normalized
		 */
		public Ray3D(Vec3D origin, Vec3D direction) {
			super(origin.x, origin.y, origin.z);
			dir = direction;
		}

		/**
		 * Construct a 3D ray from a location.
		 * 
		 * @param loc
		 *            - the Bukkit location.
		 */
		public Ray3D(Location loc) {
			this(new Vec3D(loc.getX(), loc.getY(), loc.getZ()), new Vec3D(loc.getDirection().getX(), loc.getDirection().getY(), loc.getDirection().getZ()));
		}

		public Vec3D getDirection() {
			return dir;
		}

		public String toString() {
			return "origin: " + super.toString() + " dir: " + dir;
		}

		/**
		 * Calculates intersection with the given ray between a certain distance
		 * interval.
		 * <p>
		 * Ray-box intersection is using IEEE numerical properties to ensure the
		 * test is both robust and efficient, as described in: <br>
		 * <code>Amy Williams, Steve Barrus, R. Keith Morley, and Peter Shirley: "An
		 * Efficient and Robust Ray-Box Intersection Algorithm" Journal of graphics
		 * tools, 10(1):49-54, 2005</code>
		 * 
		 * @param ray
		 *            incident ray
		 * @param minDist
		 * @param maxDist
		 * @return intersection point on the bounding box (only the first is
		 *         returned) or null if no intersection
		 */
		public boolean intersectsRay(AxisAlignedBB box) {
			Vec3D invDir = new Vec3D(1f / dir.x, 1f / dir.y, 1f / dir.z);

			Vec3D min = new Vec3D(box.a, box.b, box.c);
			Vec3D max = new Vec3D(box.d, box.e, box.f);

			boolean signDirX = invDir.x < 0;
			boolean signDirY = invDir.y < 0;
			boolean signDirZ = invDir.z < 0;

			Vec3D bbox = signDirX ? max : min;
			double tmin = (bbox.x - x) * invDir.x;
			bbox = signDirX ? min : max;
			double tmax = (bbox.x - x) * invDir.x;
			bbox = signDirY ? max : min;
			double tymin = (bbox.y - y) * invDir.y;
			bbox = signDirY ? min : max;
			double tymax = (bbox.y - y) * invDir.y;

			if ((tmin > tymax) || (tymin > tmax)) {
				return false;
			}
			if (tymin > tmin) {
				tmin = tymin;
			}
			if (tymax < tmax) {
				tmax = tymax;
			}

			bbox = signDirZ ? max : min;
			double tzmin = (bbox.z - z) * invDir.z;
			bbox = signDirZ ? min : max;
			double tzmax = (bbox.z - z) * invDir.z;

			if ((tmin > tzmax) || (tzmin > tmax)) {
				return false;
			}
			if (tzmin > tmin) {
				tmin = tzmin;
			}
			if (tzmax < tmax) {
				tmax = tzmax;
			}
			return true;
		}
	}

	@Override
	public boolean isCropFullyGrown(Block block) {
		if(block.getState().getData() instanceof Crops) {
			Crops ageable = (Crops) block.getState().getData();
			return ageable.getState().equals(CropState.RIPE);
		} return false;
	}
}
