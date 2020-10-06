package net.Indyuce.mmocore.skill;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.skill.SkillResult;
import net.Indyuce.mmocore.api.skill.result.TargetSkillResult;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.api.util.math.particle.ParabolicProjectile;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.version.VersionSound;

public class Fire_Storm extends Skill {
	public Fire_Storm() {
		super();
		setMaterial(Material.BLAZE_POWDER);
		setLore("Casts a flurry of 6 fire projectiles onto", "nearby enemies, proritizing the initial", "target. Each projectile deals &c{damage} &7damage", "and ignite the target for &c{ignite} &7seconds.", "", "&e{cooldown}s Cooldown", "&9Costs {mana} {mana_name}");

		addModifier("mana", new LinearValue(15, 2));
		addModifier("damage", new LinearValue(5, 3));
		addModifier("ignite", new LinearValue(2, .1));
		addModifier("cooldown", new LinearValue(5, -.1, 1, 5));
	}

	@Override
	public SkillResult whenCast(PlayerData data, SkillInfo skill) {
		TargetSkillResult cast = new TargetSkillResult(data, skill, 20);
		if (!cast.isSuccessful() || !data.isOnline())
			return cast;

		LivingEntity target = cast.getTarget();

		double damage = cast.getModifier("damage");
		int ignite = (int) (20 * cast.getModifier("ignite"));

		data.getPlayer().getWorld().playSound(data.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 1);
		new BukkitRunnable() {
			int j = 0;

			@Override
			public void run() {
				if (j++ > 5 || data.getPlayer().isDead() || !data.getPlayer().isOnline() || target.isDead() || !data.getPlayer().getWorld().equals(target.getWorld())) {
					cancel();
					return;
				}

				// TODO dynamic target location

				data.getPlayer().getWorld().playSound(data.getPlayer().getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1, 1);
				new ParabolicProjectile(data.getPlayer().getLocation().add(0, 1, 0), target.getLocation().add(0, target.getHeight() / 2, 0), randomVector(data.getPlayer()), () -> {
					target.getWorld().playSound(target.getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_TWINKLE.toSound(), 1, 2);
					target.getWorld().spawnParticle(Particle.SMOKE_NORMAL, target.getLocation().add(0, target.getHeight() / 2, 0), 8, 0, 0, 0, .15);
					MMOLib.plugin.getDamage().damage(data.getPlayer(), target, new AttackResult(damage, DamageType.SKILL, DamageType.PROJECTILE, DamageType.MAGIC));
					target.setFireTicks(ignite);

				}, 2, Particle.FLAME);
			}
		}.runTaskTimer(MMOCore.plugin, 0, 4);
		return cast;
	}

	private Vector randomVector(Player player) {
		double a = Math.toRadians(player.getEyeLocation().getYaw() + 90);
		a += (random.nextBoolean() ? 1 : -1) * (random.nextDouble() * 2 + 1) * Math.PI / 6;
		return new Vector(Math.cos(a), .8, Math.sin(a)).normalize().multiply(.4);
	}
}
