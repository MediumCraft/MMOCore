package net.Indyuce.mmocore.listener.profession;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.droptable.dropitem.fishing.FishingDropItem;
import net.Indyuce.mmocore.api.event.CustomPlayerFishEvent;
import net.Indyuce.mmocore.api.experience.EXPSource;
import net.Indyuce.mmocore.api.loot.LootBuilder;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.manager.profession.FishingManager.FishingDropTable;
import net.mmogroup.mmolib.version.VersionSound;

public class FishingListener implements Listener {
	private Set<UUID> fishing = new HashSet<>();

	private static final Random random = new Random();

	@EventHandler(priority = EventPriority.LOW)
	public void a(PlayerFishEvent event) {
		Player player = event.getPlayer();
		FishHook hook = event.getHook();

		if (event.getState() == State.BITE && !fishing.contains(player.getUniqueId()) && !player.hasMetadata("NPC")) {

			/*
			 * checks for drop tables. if no drop table, just plain vanilla
			 * fishing OTHERWISE initialize fishing, register other listener.
			 */

			FishingDropTable table = MMOCore.plugin.fishingManager.calculateDropTable(player);
			if (table == null)
				return;

			new FishingData(player, hook, table);
			if (MMOCore.plugin.hasHolograms())
				MMOCore.plugin.hologramSupport.displayIndicator(hook.getLocation(),
						MMOCore.plugin.configManager.getSimpleMessage("caught-fish").message());
		}
	}

	public class FishingData extends BukkitRunnable implements Listener {
		private final Location location;
		private final FishingDropItem caught;
		private final PlayerData playerData;
		private final Player player;
		private final FishHook hook;

		private final int total, exp;

		private int pulls;
		private long last = System.currentTimeMillis();

		public FishingData(Player player, FishHook hook, FishingDropTable table) {
			this.location = hook.getLocation();
			this.caught = table.getRandomItem();
			this.playerData = PlayerData.get(this.player = player);
			this.hook = hook;

			this.total = (int) (caught.rollTugs() * (1 - PlayerData.get(player).getStats().getStat(StatType.FISHING_STRENGTH) / 100));
			this.exp = caught.rollExperience();

			fishing.add(player.getUniqueId());
			runTaskTimer(MMOCore.plugin, 0, 2);
			Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
		}

		public void criticalFish() {
			pulls = total + 2;
		}

		public boolean isTimedOut() {
			return last + 1000 < System.currentTimeMillis();
		}

		public boolean pull() {
			last = System.currentTimeMillis();
			return pulls++ > total;
		}

		public boolean isCrit() {
			return pulls > total + 1;
		}

		public void close() {
			fishing.remove(player.getUniqueId());
			hook.remove();

			HandlerList.unregisterAll(this);
			cancel();
		}

		@Override
		public void run() {
			if (isTimedOut())
				close();

			location.getWorld().spawnParticle(Particle.CRIT, location, 0, 2 * (random.nextDouble() - .5), 3, 2 * (random.nextDouble() - .5), .6);
		}

		@EventHandler
		public void a(PlayerFishEvent event) {
			if (event.getPlayer().equals(player) && !player.hasMetadata("NPC")
					&& (event.getState() == State.CAUGHT_FISH || event.getState() == State.FAILED_ATTEMPT || event.getState() == State.REEL_IN)) {

				/*
				 * lose the catch if the current fish is gone!
				 */
				// TODO: Cancelling the event also cancels Rod damage (so it's
				// technically unbreakable)
				event.setCancelled(true);
				if (isTimedOut()) {
					close();
					hook.getWorld().spawnParticle(Particle.SMOKE_NORMAL, hook.getLocation(), 16, 0, 0, 0, .1);
					return;
				}

				if (pulls == 0 && random.nextDouble() < PlayerData.get(player).getStats().getStat(StatType.CRITICAL_FISHING_CHANCE) / 100)
					criticalFish();

				/*
				 * checks for enough pulls. if not, return and wait for next
				 * fish event.
				 */
				if (!pull())
					return;

				/*
				 * successfully pulls the fish
				 */
				close();

				if (!isCrit() && random.nextDouble() < PlayerData.get(player).getStats().getStat(StatType.CRITICAL_FISHING_FAILURE_CHANCE) / 100) {
					player.setVelocity(hook.getLocation().subtract(player.getLocation()).toVector().setY(0).multiply(3).setY(.5));
					hook.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 24, 0, 0, 0, .08);
					return;
				}

				CustomPlayerFishEvent called = new CustomPlayerFishEvent(playerData, caught.getDropItem());
				Bukkit.getPluginManager().callEvent(called);
				if (called.isCancelled())
					return;

				ItemStack collect = caught.collect(new LootBuilder(playerData, 0));
				if (collect == null) {
					hook.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 24, 0, 0, 0, .08);
					return;
				}

				// calculate velocity
				Item item = hook.getWorld().dropItemNaturally(hook.getLocation(), collect);
				if (MMOCore.plugin.hasHolograms())
					MMOCore.plugin.hologramSupport.displayIndicator(location,
							MMOCore.plugin.configManager.getSimpleMessage("fish-out-water" + (isCrit() ? "-crit" : "")).message());
				Vector vec = player.getLocation().subtract(hook.getLocation()).toVector();
				vec.setY(vec.getY() * .031 + vec.length() * .05);
				vec.setX(vec.getX() * .08);
				vec.setZ(vec.getZ() * .08);
				item.setVelocity(vec);
				player.getWorld().playSound(player.getLocation(), VersionSound.BLOCK_NOTE_BLOCK_HAT.toSound(), 1, 0);
				for (int j = 0; j < 16; j++)
					location.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location, 0, 4 * (random.nextDouble() - .5), 2,
							4 * (random.nextDouble() - .5), .05);

				if (MMOCore.plugin.professionManager.has("fishing"))
					playerData.getCollectionSkills().giveExperience(MMOCore.plugin.professionManager.get("fishing"), exp, location,
							EXPSource.FISHING);
			}
		}
	}
}
