package net.Indyuce.mmocore.skill;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.skill.SkillResult;
import net.Indyuce.mmocore.api.skill.result.LocationSkillResult;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.api.util.math.Line3D;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Ice_Spikes extends Skill {
	private static final double radius = 3;

	public Ice_Spikes() {
		super();
		setMaterial(VersionMaterial.SNOWBALL.toMaterial());
		setLore("Ice spikes summon from the ground", "and shatter, each dealing &9{damage} &7damage", "to hit enemies and slowing them down", "for &9{slow} &7seconds.", "", "&e{cooldown}s Cooldown", "&9Costs {mana} {mana_name}");

		addModifier("cooldown", new LinearValue(6, -.1, 2, 6));
		addModifier("mana", new LinearValue(20, 2));
		addModifier("damage", new LinearValue(3, 1));
		addModifier("slow", new LinearValue(4, 0));
	}

	@Override
	public SkillResult whenCast(PlayerData data, SkillInfo skill) {
		LocationSkillResult cast = new LocationSkillResult(data, skill, 20);
		if (!cast.isSuccessful())
			return cast;

		Location loc = cast.getHit();
		double damage = cast.getModifier("damage");
		int slow = (int) (20 * cast.getModifier("slow"));

		new BukkitRunnable() {
			int j = 0;

			@Override
			public void run() {

				if (j++ > 8) {
					cancel();
					return;
				}

				Location loc1 = loc.clone().add(offset() * radius, 0, offset() * radius).add(0, 2, 0);
				loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc1, 32, 0, 2, 0, 0);
				loc.getWorld().spawnParticle(Particle.SNOWBALL, loc1, 32, 0, 2, 0, 0);
				loc.getWorld().playSound(loc1, Sound.BLOCK_GLASS_BREAK, 2, 0);

				Line3D line = new Line3D(loc.toVector(), loc.toVector().add(new Vector(0, 1, 0)));
				for (Entity entity : MMOCoreUtils.getNearbyChunkEntities(loc1))
					if (line.distanceSquared(entity.getLocation().toVector()) < radius && Math.abs(entity.getLocation().getY() - loc1.getY()) < 10 && MMOCoreUtils.canTarget(data, entity)) {
						MMOLib.plugin.getDamage().damage(data.getPlayer(), (LivingEntity) entity, new AttackResult(damage, DamageType.SKILL, DamageType.MAGIC));
						((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slow, 0));
					}
			}
		}.runTaskTimer(MMOCore.plugin, 0, 5);
		return cast;
	}

	private double offset() {
		return random.nextDouble() * (random.nextBoolean() ? 1 : -1);
	}
}
