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
import net.Indyuce.mmocore.api.util.math.particle.SmallParticleEffect;

public class Minor_Healings extends Skill {
	public Minor_Healings() {
		super();
		setMaterial(Material.GOLDEN_APPLE);
		setLore("Instantly grants &a{heal} &7HP to the", "target. Sneak to cast it on yourself.", "", "&e{cooldown}s Cooldown", "&9Costs {mana} {mana_name}");

		addModifier("mana", new LinearValue(4, 2));
		addModifier("heal", new LinearValue(4, 2));
		addModifier("cooldown", new LinearValue(9, -.1, 1, 5));
	}

	@Override
	public SkillResult whenCast(PlayerData data, SkillInfo skill) {
		SkillResult cast = data.getPlayer().isSneaking() ? new SkillResult(data, skill) : new TargetSkillResult(data, skill, 50);
		if (!cast.isSuccessful())
			return cast;

		LivingEntity target = cast instanceof TargetSkillResult ? ((TargetSkillResult) cast).getTarget() : data.getPlayer();

		double max = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		target.setHealth(Math.min(max, target.getHealth() + cast.getModifier("heal")));

		new SmallParticleEffect(target, Particle.HEART, 1);
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2, 2);
		return cast;
	}
}
