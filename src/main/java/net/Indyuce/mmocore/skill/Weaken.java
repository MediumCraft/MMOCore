package net.Indyuce.mmocore.skill;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.skill.SkillResult;
import net.Indyuce.mmocore.api.skill.result.TargetSkillResult;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.api.util.math.particle.ParabolicProjectile;
import net.Indyuce.mmocore.api.util.math.particle.SmallParticleEffect;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Weaken extends Skill {
	public Weaken() {
		super();
		setMaterial(VersionMaterial.MAGENTA_DYE.toMaterial());
		setLore("The target is weakened for", "&8{duration} &7seconds and is dealt", "&7extra &8{ratio}% &7damage.", "", "&e{cooldown}s Cooldown", "&9Costs {mana} {mana_name}");

		addModifier("cooldown", new LinearValue(20, -.1, 5, 20));
		addModifier("mana", new LinearValue(4, 1));
		addModifier("ratio", new LinearValue(30, 3));
		addModifier("duration", new LinearValue(10, -.1, 5, 10));
	}

	@Override
	public SkillResult whenCast(PlayerData data, SkillInfo skill) {
		TargetSkillResult cast = new TargetSkillResult(data, skill, 7);
		if (!cast.isSuccessful())
			return cast;

		LivingEntity target = cast.getTarget();
		new ParabolicProjectile(data.getPlayer().getLocation().add(0, 1, 0), target.getLocation().add(0, target.getHeight() / 2, 0), randomVector(data.getPlayer()), () -> {
			if (!target.isDead())
				new Weakened(target, cast.getModifier("ratio"), cast.getModifier("duration"));
		}, 2, Particle.SPELL_WITCH);
		return cast;
	}

	private Vector randomVector(Player player) {
		double a = Math.toRadians(player.getEyeLocation().getYaw() + 90);
		a += (random.nextBoolean() ? 1 : -1) * (random.nextDouble() + .5) * Math.PI / 6;
		return new Vector(Math.cos(a), .8, Math.sin(a)).normalize().multiply(.4);
	}

	public class Weakened implements Listener {
		private final Entity entity;
		private final double c;

		public Weakened(Entity entity, double ratio, double duration) {
			this.entity = entity;
			this.c = 1 + ratio / 100;

			new SmallParticleEffect(entity, Particle.SPELL_WITCH);

			Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
			Bukkit.getScheduler().scheduleSyncDelayedTask(MMOCore.plugin, () -> EntityDamageByEntityEvent.getHandlerList().unregister(this), (int) duration * 20);
		}

		@EventHandler
		public void a(EntityDamageByEntityEvent event) {
			if (event.getEntity().equals(entity)) {
				event.getEntity().getWorld().spawnParticle(Particle.SPELL_WITCH, entity.getLocation().add(0, entity.getHeight() / 2, 0), 16, .5, .5, .5, 0);
				event.setDamage(event.getDamage() * c);
			}
		}
	}
}
