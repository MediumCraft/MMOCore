package net.Indyuce.mmocore.skill;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.skill.SkillResult;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.api.util.math.particle.ParabolicProjectile;
import net.mmogroup.mmolib.version.VersionSound;

public class Warp extends Skill {
	public Warp() {
		super();
		setMaterial(Material.ENDER_PEARL);
		setLore("Teleports you to target location.", "Max. Range: &5{range}", "", "&e{cooldown}s Cooldown", "&9Costs {mana} {mana_name}");

		addModifier("cooldown", new LinearValue(15, -.3, 2, 15));
		addModifier("mana", new LinearValue(8, 3));
		addModifier("range", new LinearValue(16, 1, 0, 100));
	}

	@Override
	public SkillResult whenCast(PlayerData data, SkillInfo skill) {
		WarpCast cast = new WarpCast(data, skill);
		if (!cast.isSuccessful())
			return cast;

		data.getPlayer().getWorld().playSound(data.getPlayer().getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 2);

		Location loc = cast.block.getLocation().add(0, 1, 0);
		loc.setYaw(data.getPlayer().getLocation().getYaw());
		loc.setPitch(data.getPlayer().getLocation().getPitch());

		new ParabolicProjectile(data.getPlayer().getLocation().add(0, 1, 0), loc.clone().add(0, 1, 0), () -> {
			if (data.getPlayer().isOnline() && !data.getPlayer().isDead()) {
				data.getPlayer().teleport(loc);
				data.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, data.getPlayer().getLocation().add(0, 1, 0), 0);
				data.getPlayer().getWorld().spawnParticle(Particle.SPELL_INSTANT, data.getPlayer().getLocation().add(0, 1, 0), 32, 0, 0, 0, .1);
				data.getPlayer().getWorld().playSound(data.getPlayer().getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 1, 1);
			}
		}, 2, Particle.SPELL_INSTANT);
		return cast;
	}

	private class WarpCast extends SkillResult {
		private Block block;

		public WarpCast(PlayerData data, SkillInfo skill) {
			super(data, skill);
			if (isSuccessful() && (block = data.getPlayer().getTargetBlock(null, (int) getModifier("range"))) == null)
				abort();
		}
	}
}
