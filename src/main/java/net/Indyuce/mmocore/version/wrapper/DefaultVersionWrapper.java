package net.Indyuce.mmocore.version.wrapper;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import net.Indyuce.mmocore.MMOCoreUtils;

public class DefaultVersionWrapper implements VersionWrapper {

	@Override
	public void spawnParticle(Particle particle, Location loc, int amount, double x, double y, double z, double speed, float size, Color color) {
		loc.getWorld().spawnParticle(particle, loc, amount, x, y, z, speed, new Particle.DustOptions(color, size));
	}

	@Override
	public void spawnParticle(Particle particle, Location loc, int amount, double x, double y, double z, double speed, Material material) {
		loc.getWorld().spawnParticle(particle, loc, amount, x, y, z, 0, material.createBlockData());
	}

	@Override
	public Enchantment getEnchantmentFromString(String s) {
		return Enchantment.getByKey(NamespacedKey.minecraft(s));
	}

	@Override
	public FurnaceRecipe getFurnaceRecipe(NamespacedKey key, ItemStack item, Material material, float exp, int cook) {
		return new FurnaceRecipe(key, item, material, exp, cook);
	}
	
	@Override
	public RayTraceResult rayTraceEntities(Player player, Vector direction, double range) {
		return player.getWorld().rayTraceEntities(player.getEyeLocation(), direction, range, (entity) -> MMOCoreUtils.canTarget(player, entity));
	}
	
	@Override
	public RayTraceResult rayTrace(Player player, Vector direction, double range) {
		return player.rayTraceBlocks(range);
	}

	@Override
	public BossBar createBossBar(NamespacedKey key, String title, BarColor color, BarStyle style, BarFlag... flags) {
		return Bukkit.createBossBar(key, title, color, style, flags);
	}

	@Override
	public boolean isCropFullyGrown(Block block) {
		if (block.getBlockData() instanceof Ageable) {
			Ageable ageable = (Ageable) block.getBlockData();
			return ageable.getAge() == ageable.getMaximumAge();
		} return false;
	}
}
