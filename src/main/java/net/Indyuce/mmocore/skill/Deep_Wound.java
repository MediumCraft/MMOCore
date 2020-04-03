package net.Indyuce.mmocore.skill;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.skill.SkillResult;
import net.Indyuce.mmocore.api.skill.result.TargetSkillResult;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;

public class Deep_Wound extends Skill {
	public Deep_Wound() {
		super();
		setMaterial(Material.REDSTONE);
		setLore("You puncture your target, dealing &c{damage} &7damage.", "Damage is increased up to &c+{extra}% &7based", "on your target's missing health.", "", "&e{cooldown}s Cooldown", "&9Costs {mana} {mana_name}");

		addModifier("cooldown", new LinearValue(20, -.1, 5, 20));
		addModifier("mana", new LinearValue(8, 3));
		addModifier("damage", new LinearValue(5, 1.5));
		addModifier("extra", new LinearValue(50, 20));
	}

	@Override
	public SkillResult whenCast(PlayerData data, SkillInfo skill) {
		TargetSkillResult cast = new TargetSkillResult(data, skill, 3);
		if (!cast.isSuccessful())
			return cast;

		LivingEntity target = cast.getTarget();
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2, 2);
		target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, target.getHeight() / 2, 0), 32, 0, 0, 0, .7);
		MMOLib.plugin.getVersion().getWrapper().spawnParticle(Particle.BLOCK_CRACK, target.getLocation().add(0, target.getHeight() / 2, 0), 32, 0, 0, 0, 2, Material.REDSTONE_BLOCK);

		double max = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		double ratio = (max - target.getHealth()) / max;

		double damage = cast.getModifier("damage") * (1 + cast.getModifier("extra") * ratio / 100);
		MMOLib.plugin.getDamage().damage(data.getPlayer(), target, new AttackResult(damage, DamageType.SKILL, DamageType.PHYSICAL));
		return cast;
	}
}
