package net.Indyuce.mmocore.skill;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.MMOCoreUtils;
import net.Indyuce.mmocore.api.math.formula.LinearValue;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.skill.SkillResult;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.version.VersionMaterial;
import net.mmogroup.mmolib.version.VersionSound;

public class Fireball extends Skill {
	public Fireball() {
		super();
		setMaterial(VersionMaterial.FIRE_CHARGE.toMaterial());
		setLore("Casts a deadly fireball onto your", "target, dealing &c{damage} &7damage upon contact", "and igniting it for &c{ignite} &7seconds.", "", "Shatters into 3 blazing hot shards which stick", "to walls and explode 3 seconds later, dealing", "33% of the initial spell damage.", "", "&e{cooldown}s Cooldown", "&9Costs {mana} {mana_name}");

		addModifier("mana", new LinearValue(15, 1));
		addModifier("damage", new LinearValue(5, 3));
		addModifier("ignite", new LinearValue(2, .1));
		addModifier("cooldown", new LinearValue(9, -.1, 1, 5));
	}

	@Override
	public SkillResult whenCast(PlayerData data, SkillInfo skill) {
		SkillResult cast = new SkillResult(data, skill);
		if (!cast.isSuccessful())
			return cast;

		data.getPlayer().getWorld().playSound(data.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 1);
		new BukkitRunnable() {
			int j = 0;
			Vector vec = data.getPlayer().getEyeLocation().getDirection();
			Location loc = data.getPlayer().getLocation().add(0, 1.3, 0);

			public void run() {
				if (j++ > 40) {
					cancel();
					return;
				}

				loc.add(vec);

				loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 2, 1);
				// loc.getWorld().spawnParticle(Particle.FLAME, loc, 5, .12,
				// .12, .12, 0);
				loc.getWorld().spawnParticle(Particle.FLAME, loc, 4, .02, .02, .02, 0);
				loc.getWorld().spawnParticle(Particle.LAVA, loc, 0);
				// if (random.nextDouble() < .3)
				// loc.getWorld().spawnParticle(Particle.LAVA, loc, 0);

				for (Entity target : MMOCoreUtils.getNearbyChunkEntities(loc))
					if (target.getBoundingBox().expand(.2, .2, .2).contains(loc.toVector()) && MMOCoreUtils.canTarget(data.getPlayer(), target)) {
						loc.getWorld().spawnParticle(Particle.LAVA, loc, 8);
						loc.getWorld().spawnParticle(Particle.FLAME, loc, 32, 0, 0, 0, .1);
						loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_HURT, 2, 1);
						target.setFireTicks((int) (target.getFireTicks() + cast.getModifier("ignite") * 20));
						MMOLib.plugin.getDamage().damage(data.getPlayer(), (LivingEntity) target, new AttackResult(cast.getModifier("damage"), DamageType.SKILL, DamageType.PROJECTILE, DamageType.MAGIC));
						cancel();
					}
			}
		}.runTaskTimer(MMOCore.plugin, 0, 1);
		return cast;
	}
}
