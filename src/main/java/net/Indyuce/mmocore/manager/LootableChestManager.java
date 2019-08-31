package net.Indyuce.mmocore.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.droptable.DropTable;
import net.Indyuce.mmocore.api.math.particle.ChestParticleEffect;

public class LootableChestManager {
	private Set<LootableChest> map = new HashSet<>();

	private static BukkitRunnable runnable;
	private static final Random random = new Random();

	public LootableChestManager(FileConfiguration config) {
		for (String key : config.getKeys(false))
			register(new LootableChest(config.getConfigurationSection(key)));

		if (runnable != null)
			runnable.cancel();

		(runnable = new BukkitRunnable() {
			public void run() {
				map.forEach(chest -> {
					if (chest.hasEffect() && chest.isSpawned() && chest.hasPlayerNearby())
						chest.playEffect();
				});
			}
		}).runTaskTimerAsynchronously(MMOCore.plugin, 100, 4 * 20);
	}

	public void register(LootableChest chest) {
		if (chest.isValid()) {
			map.add(chest);
			chest.whenClosed(false);
		}
	}

	public LootableChest getLootableChest(Location loc) {
		for (LootableChest chest : map)
			if (blockCheck(chest.getLocation(), loc))
				return chest;
		return null;
	}

	private boolean blockCheck(Location loc1, Location loc2) {
		return loc1.getWorld().equals(loc2.getWorld()) && loc1.getBlockX() == loc2.getBlockX() && loc1.getBlockY() == loc2.getBlockY() && loc1.getBlockZ() == loc2.getBlockZ();
	}

	public class LootableChest {
		private Location loc;
		private DropTable table;
		private int regenTime = -1;
		private long lastDisappear;
		private Particle effectParticle;
		private ChestParticleEffect effect;

		public LootableChest(ConfigurationSection config) {
			try {
				loc = readLocation(config.getName());
				regenTime = config.getInt("regen-time");
				table = MMOCore.plugin.dropTableManager.loadDropTable(config.get("drop-table"));

				if (config.contains("effect")) {
					String format = config.getString("effect.particle");
					Validate.notNull(format, "Particle is missing particle");
					effectParticle = Particle.valueOf(format.toUpperCase().replace("-", "_"));

					format = config.getString("effect.type");
					Validate.notNull(format, "Particle is missing effect type");
					effect = ChestParticleEffect.valueOf(format.toUpperCase().replace("-", "_"));
				}
			} catch (IllegalArgumentException | IndexOutOfBoundsException exception) {
				MMOCore.plugin.getLogger().log(Level.WARNING, "Couldn't read the loot chest config '" + config.getName() + "':" + exception.getMessage());
			}
		}

		public boolean isValid() {
			return loc != null && table != null && regenTime > -1;
		}

		public boolean hasEffect() {
			return effectParticle != null && effect != null;
		}

		public boolean isSpawned() {
			return System.currentTimeMillis() > lastDisappear + 50 * regenTime;
		}

		public Location getLocation() {
			return loc;
		}

		public DropTable getDropTable() {
			return table;
		}

		public int getRegenTime() {
			return regenTime;
		}

		public void playEffect() {
			effect.play(loc.clone().add(.5, .5, .5), effectParticle);
		}

		public void whenClosed(boolean sound) {
			if (sound) {
				loc.getWorld().playSound(loc, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
				loc.getWorld().spawnParticle(Particle.CRIT, loc.clone().add(.5, .5, .5), 16, 0, 0, 0, .5);
			}
			loc.getBlock().setType(Material.AIR);
			lastDisappear = System.currentTimeMillis();
			Bukkit.getScheduler().scheduleSyncDelayedTask(MMOCore.plugin, () -> whenSpawn(), regenTime);
		}

		public boolean hasPlayerNearby() {
			for (Player player : loc.getWorld().getPlayers())
				if (player.getLocation().distanceSquared(loc) < 625)
					return true;
			return false;
		}

		public void whenSpawn() {
			List<Integer> slots = new ArrayList<>();
			for (int j = 0; j < 27; j++)
				slots.add(j);

			loc.getBlock().setType(Material.CHEST);
			Chest chest = (Chest) loc.getBlock().getState();
			table.collect().forEach(item -> {
				Integer slot = slots.get(random.nextInt(slots.size()));
				chest.getInventory().setItem(slot, item);
				slots.remove(slot);
			});
		}

		private Location readLocation(String string) {
			String[] split = string.split("\\ ");

			World world = Bukkit.getWorld(split[0]);
			Validate.notNull(world, "Could not find world '" + split[0] + "'");

			double x = Double.parseDouble(split[1]);
			double y = Double.parseDouble(split[2]);
			double z = Double.parseDouble(split[3]);

			return new Location(world, x, y, z);
		}
	}
}
