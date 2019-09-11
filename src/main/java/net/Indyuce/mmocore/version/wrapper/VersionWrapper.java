package net.Indyuce.mmocore.version.wrapper;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
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

public interface VersionWrapper {
	default void spawnParticle(Particle particle, Location loc, Color color) {
		spawnParticle(particle, loc, 1, 0, 0, 0, 0, 1, color);
	}

	default void spawnParticle(Particle particle, Location loc, float size, Color color) {
		spawnParticle(particle, loc, 1, 0, 0, 0, 0, size, color);
	}

	default void spawnParticle(Particle particle, Location loc, Material material) {
		spawnParticle(particle, loc, 1, 0, 0, 0, 0, material);
	}

	void spawnParticle(Particle particle, Location loc, int amount, double x, double y, double z, double speed, float size, Color color);

	void spawnParticle(Particle particle, Location loc, int amount, double x, double y, double z, double speed, Material material);

	BossBar createBossBar(NamespacedKey key, String title, BarColor color, BarStyle style, BarFlag... flags);

	Enchantment getEnchantmentFromString(String s);

	FurnaceRecipe getFurnaceRecipe(NamespacedKey key, ItemStack item, Material material, float exp, int cook);

	
	default RayTraceResult rayTrace(Player player, double range) {
		return rayTrace(player, player.getEyeLocation().getDirection(), range);
	}

	RayTraceResult rayTrace(Player player, Vector direction, double range);
	

	default RayTraceResult rayTraceEntities(Player player, double range) {
		return rayTraceEntities(player, player.getEyeLocation().getDirection(), range);
	}

	RayTraceResult rayTraceEntities(Player player, Vector direction, double range);
	
	boolean isCropFullyGrown(Block block);
}
