package net.Indyuce.mmocore.skill;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.math.formula.LinearValue;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.skill.SkillResult;
import net.Indyuce.mmocore.api.skill.TargetSkillResult;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;

public class Combo_Attack extends Skill {
	public Combo_Attack() {
		super();
		setMaterial(Material.IRON_SWORD);
		setLore("Violenty slashes your target &8{count}", "times for a total of &8{damage} &7damage.", "", "&e{cooldown}s Cooldown", "&9Costs {mana} {mana_name}");

		addModifier("cooldown", new LinearValue(20, -.1, 5, 20));
		addModifier("damage", new LinearValue(9, .3));
		addModifier("count", new LinearValue(3, .2));
		addModifier("mana", new LinearValue(10, -.1, 3, 5));
	}

	@Override
	public SkillResult whenCast(PlayerData data, SkillInfo skill) {
		TargetSkillResult cast = new TargetSkillResult(data, skill, 3);
		if (!cast.isSuccessful())
			return cast;

		new BukkitRunnable() {
			final int count = (int) cast.getModifier("count");
			final double damage = cast.getModifier("damage") / count;
			LivingEntity target = cast.getTarget();

			int c;

			@Override
			public void run() {
				if (c++ > count) {
					cancel();
					return;
				}

				target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1, 2);
				target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, target.getHeight() / 2, 0), 24, 0, 0, 0, .7);
				MMOLib.plugin.getDamage().damage(data.getPlayer(), target, new AttackResult(damage, DamageType.SKILL, DamageType.PHYSICAL));
			}
		}.runTaskTimer(MMOCore.plugin, 0, 5);
		return cast;
	}
}
