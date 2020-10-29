package net.Indyuce.mmocore.skill;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.skill.SkillResult;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.event.PlayerAttackEvent;

public class Backstab extends Skill implements Listener {
	public Backstab() {
		super();
		setMaterial(Material.FLINT);
		setLore("Backstabs deal &c{extra}%&7 damage.", "", "&9Costs {mana} {mana_name}");
		setPassive();

		addModifier("cooldown", new LinearValue(0, 0));
		addModifier("mana", new LinearValue(8, 1));
		addModifier("extra", new LinearValue(50, 20));
		
		Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
	}

	@EventHandler
	public void a(PlayerAttackEvent event) {
		PlayerData data = event.getData().getMMOCore();
		LivingEntity target = (LivingEntity) event.getEntity();
		if (data.isInCombat() || !event.getAttack().hasType(DamageType.WEAPON)
				|| event.getPlayer().getEyeLocation().getDirection().angle(target.getEyeLocation().getDirection()) > Math.PI / 6
				|| !data.getProfess().hasSkill(this))
			return;

		SkillResult cast = data.cast(this);
		if (!cast.isSuccessful())
			return;

		data.cast(cast.getInfo());
		event.getAttack().multiplyDamage(1 + cast.getModifier("extra") / 100);
		target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, target.getHeight() / 2, 0), 32, 0, 0, 0, .05);
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_HURT, 1, 1.5f);
	}
}
