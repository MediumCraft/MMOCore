package net.Indyuce.mmocore.listener.profession;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.api.event.CustomBlockMineEvent;
import net.Indyuce.mmocore.loot.chest.particle.SmallParticleEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class PlayerCollectStats implements Listener {
	private static final Random random = new Random();

	@EventHandler
	public void a(CustomBlockMineEvent event) {
		Player player = event.getPlayer();

		// Give haste if right enchant
		double h = event.getData().getStats().getStat("GATHERING_HASTE");
		if (h > 0 && random.nextDouble() < h * .045) {
			new SmallParticleEffect(player, Particle.SPELL_INSTANT);
			player.removePotionEffect(PotionEffectType.FAST_DIGGING);
			player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, (int) (10 * h), (int) (1 + h / 7)));
		}

		// Drop more items if fortune enchant
		double f = event.getData().getStats().getStat("FORTUNE");
		if (f > 0 && random.nextDouble() < f * .045) {
			int a = (int) (1.5 * Math.sqrt(f / 1.1));
			for (ItemStack item : event.getDrops())
				item.setAmount(item.getAmount() + a);
		}

		if (MythicLib.plugin.getVersion().getWrapper().isCropFullyGrown(event.getBlock())) {

			// Drop more CROP items
			double l = event.getData().getStats().getStat("LUCK_OF_THE_FIELD");
			if (l > 0 && random.nextDouble() < l * .045) {
				int a = (int) (1.5 * Math.sqrt(l / 1.1));
				Location loc = event.getBlock().getLocation().add(.5, .1, .5);
				loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0, .2, 0), 10, .3, .2, .3, 0);
				for (ItemStack item : event.getDrops())
					item.setAmount(item.getAmount() + a);
			}
		}
	}
}
