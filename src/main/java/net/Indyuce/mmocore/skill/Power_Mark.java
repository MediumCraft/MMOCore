package net.Indyuce.mmocore.skill;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.MMOCoreUtils;
import net.Indyuce.mmocore.api.math.formula.LinearValue;
import net.Indyuce.mmocore.api.math.particle.ParabolicProjectile;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.PlayerStats.CachedStats;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.skill.SkillResult;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.event.PlayerAttackEvent;
import net.mmogroup.mmolib.version.VersionMaterial;
import net.mmogroup.mmolib.version.VersionSound;

public class Power_Mark extends Skill implements Listener {
	public Power_Mark() {
		super();
		setMaterial(VersionMaterial.WITHER_SKELETON_SKULL.toMaterial());
		setLore("Attacking an enemy applies a deadly", "magical mark which spreads accross the", "ground. This mark accumulates &6{ratio}%", "of the damage dealt to the initial", "target over &6{duration} &7seconds.", "", "After this duration, the mark bursts, dealing", "accumulated damage to nearby enemies and", "stunning them for &6{stun}+ &7seconds.", "", "The more damage, the longer the stun.", "", "&e{cooldown}s Cooldown");
		setPassive();

		addModifier("stun", new LinearValue(.4, .03));
		addModifier("ratio", new LinearValue(10, 5));
		addModifier("duration", new LinearValue(10, .1));
		addModifier("cooldown", new LinearValue(30, 0));

		Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
	}

	@EventHandler
	public void a(PlayerAttackEvent event) {
		PlayerData data = event.getData().getMMOCore();
		if (!event.isWeapon() || !data.getProfess().hasSkill(this))
			return;

		SkillResult cast = data.cast(this);
		if (!cast.isSuccessful())
			return;

		new PowerMark(data, cast, event.getEntity().getLocation());
	}

	public class PowerMark extends BukkitRunnable implements Listener {
		private final PlayerData data;
		private final Location loc;
		private final CachedStats stats;

		private double duration, ratio, stun;

		private double accumulate;
		private int j;

		public PowerMark(PlayerData data, SkillResult cast, Location loc) {
			this.data = data;
			this.loc = loc;
			this.stats = MMOCore.plugin.rpgUtilHandler.cachePlayerStats(data);

			loc.getWorld().playSound(loc, Sound.BLOCK_END_PORTAL_FRAME_FILL, 2, 1);

			duration = cast.getModifier("duration");
			ratio = cast.getModifier("ratio") / 100;
			stun = cast.getModifier("stun");

			runTaskTimer(MMOCore.plugin, 0, 1);
			Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
		}

		public void unregister() {
			PlayerAttackEvent.getHandlerList().unregister(this);
			cancel();
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void stackDamage(PlayerAttackEvent event) {
			if (!event.isCancelled() && j < 20 * (duration - 2) && radiusCheck(event.getEntity().getLocation()) && event.getData().equals(data)) {
				accumulate += event.getDamage() * ratio;
				new ParabolicProjectile(event.getEntity().getLocation().add(0, event.getEntity().getHeight() / 2, 0), loc, () -> loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1), Color.PURPLE);
			}
		}

		private boolean radiusCheck(Location loc) {
			return loc.getWorld().equals(this.loc.getWorld()) && loc.distanceSquared(this.loc) < 16;
		}

		@Override
		public void run() {

			if (j++ > duration * 20) {
				unregister();

				for (double a = 0; a < Math.PI * 2; a += Math.PI * 2 / 17)
					new ParabolicProjectile(loc, loc.clone().add(6 * Math.cos(a), 0, 6 * Math.sin(a)), Particle.SPELL_WITCH);

				loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 0);
				loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc.clone().add(0, 1, 0), 16, 2, 2, 2, 0);
				loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc.clone().add(0, 1, 0), 24, 0, 0, 0, .3f);

				stun += Math.log(Math.max(1, accumulate - 10)) / 8;

				for (Entity entity : MMOCoreUtils.getNearbyChunkEntities(loc))
					if (entity.getLocation().distanceSquared(loc) < 25 && MMOCoreUtils.canTarget(data.getPlayer(), entity)) {
						entity.setVelocity(format(entity.getLocation().subtract(loc).toVector().setY(0)).setY(.3));
						((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (stun * 20), 10, false, false));
						stats.damage((LivingEntity) entity, accumulate, DamageType.SKILL, DamageType.MAGICAL);
					}
				return;
			}

			if (j % 2 == 0 && j > 20 * (duration - 2))
				loc.getWorld().playSound(loc, VersionSound.BLOCK_NOTE_BLOCK_PLING.toSound(), 1, (float) (1 + (j - 20 * (duration - 2)) / 40));

			double a = (double) j / 16;
			double r = Math.sqrt(Math.min(duration * 2 - (double) j / 10, 4)) * 2;
			for (double k = 0; k < Math.PI * 2; k += Math.PI * 2 / 5)
				loc.getWorld().spawnParticle(Particle.SPELL_WITCH, loc.clone().add(r * Math.cos(k + a), 0, r * Math.sin(k + a)), 0);
		}
	}

	private Vector format(Vector vec) {
		return vec.length() < .01 ? new Vector(0, 0, 0) : vec.normalize();
	}
}
