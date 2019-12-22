package net.Indyuce.mmocore.skill;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.math.formula.LinearValue;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.skill.SkillResult;
import net.Indyuce.mmocore.api.skill.TargetSkillResult;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Human_Shield extends Skill {
	public Human_Shield() {
		super();
		setMaterial(VersionMaterial.TOTEM_OF_UNDYING.toMaterial());
		setLore("Casts a protection charm onto target ally,", "reducing damage taken by &a{reduction}%&7.", "&a{redirect}% &7of this damage is redirected to you.", "Charm is cancelled when reaching &c{low}%&7 health.", "Lasts &a{duration} &7seconds.", "", "&e{cooldown}s Cooldown", "&9Costs {mana} {mana_name}");

		addModifier("cooldown", new LinearValue(18, -.3, 14, 18));
		addModifier("mana", new LinearValue(15, 1.5));
		addModifier("reduction", new LinearValue(30, 3, 30, 70));
		addModifier("redirect", new LinearValue(30, -2, 20, 30));
		addModifier("duration", new LinearValue(7, 0));
		addModifier("low", new LinearValue(10, 0));
	}

	@Override
	public SkillResult whenCast(PlayerData data, SkillInfo skill) {
		TargetSkillResult cast = new TargetSkillResult(data, skill, 7);
		if (!cast.isSuccessful())
			return cast;

		if (!(cast.getTarget() instanceof Player)) {
			cast.abort();
			return cast;
		}

		data.getPlayer().getWorld().playSound(data.getPlayer().getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1, 1);
		new HumanShield(data, (Player) cast.getTarget(), cast.getModifier("reduction"), cast.getModifier("redirect"), cast.getModifier("duration"), cast.getModifier("low"));
		return cast;
	}

	public class HumanShield extends BukkitRunnable implements Listener {
		private final PlayerData data;
		private final Player target;
		private final double r, rd, d, l;

		private int j;

		public HumanShield(PlayerData data, Player target, double reduction, double redirect, double duration, double low) {
			this.target = target;
			this.data = data;

			r = 1 - Math.min(1, reduction / 100);
			rd = redirect / 100;
			d = duration * 20;
			l = low / 100;

			runTaskTimer(MMOCore.plugin, 0, 1);
			Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
		}

		@EventHandler
		public void a(EntityDamageEvent event) {
			if (event.getEntity().equals(target)) {

				double damage = event.getDamage() * r;
				event.setDamage(damage);

				double health = data.getPlayer().getHealth() - damage * rd;
				if (health > data.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * l)
					data.getPlayer().setHealth(health);
				else {
					data.getPlayer().setHealth(1);
					close();
				}
			}
		}

		@Override
		public void run() {
			if (!data.isOnline() || data.getPlayer().isDead() || !target.isOnline() || target.isDead() || j++ >= d) {
				close();
				return;
			}

			double a = (double) j / 5;
			target.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, target.getLocation().add(Math.cos(a), 1 + Math.sin(a / 3) / 1.3, Math.sin(a)), 0);
		}

		private void close() {
			cancel();
			HandlerList.unregisterAll(this);
		}
	}
}
