package net.Indyuce.mmocore.skill;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.skill.SkillResult;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.api.util.math.particle.ParabolicProjectile;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.event.PlayerAttackEvent;

public class Ambers extends Skill implements Listener {
	public Ambers() {
		super();
		setMaterial(Material.EMERALD);
		setLore("Dealing magic damage has &630% &7chance", "of dropping an amber on the ground.", "", "When picked up, ambers stack and", "refund &915% &7of your missing mana.", "", "&oAmbers can be used in other damaging skills.", "&oThe more you collect, the more powerful the skills.", "", "&e{cooldown}s Cooldown");
		setPassive();

		addModifier("cooldown", new LinearValue(3, -.1, 1, 3));

		Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
	}

	@EventHandler
	public void a(PlayerAttackEvent event) {
		PlayerData data = event.getData().getMMOCore();
		if (event.isWeapon() || !data.getProfess().hasSkill(this))
			return;

		SkillResult cast = data.cast(this);
		if (!cast.isSuccessful())
			return;

		Location loc = event.getEntity().getLocation();
		double a = random.nextDouble() * 2 * Math.PI;

		new Amber(data, loc.add(0, event.getEntity().getHeight() / 2, 0), loc.clone().add(4 * Math.cos(a), 0, 4 * Math.sin(a)));
	}

	public class Amber extends BukkitRunnable {
		private final Location loc;
		private final PlayerData data;

		private int j;

		private Amber(PlayerData data, Location source, Location loc) {
			this.loc = loc;
			this.data = data;

			final Amber amber = this;
			new ParabolicProjectile(source, loc, Particle.REDSTONE, () -> amber.runTaskTimer(MMOCore.plugin, 0, 3), 1, Color.ORANGE, 1.3f);
		}

		@Override
		public void run() {
			if (j++ > 66 || !data.getPlayer().getWorld().equals(loc.getWorld())) {
				cancel();
				return;
			}

			if (data.getPlayer().getLocation().distanceSquared(loc) < 2) {

				data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);
				data.getSkillData().ambers++;
				data.giveMana((data.getStats().getStat(StatType.MAX_MANA) - data.getMana()) * .15);

				cancel();
				return;
			}

			for (int j = 0; j < 5; j++)
				loc.getWorld().spawnParticle(Particle.SPELL_MOB, loc, 0, 1, 0.647, 0, 1);
			MMOLib.plugin.getVersion().getWrapper().spawnParticle(Particle.REDSTONE, loc, 1.3f, Color.ORANGE);
		}
	}
}
