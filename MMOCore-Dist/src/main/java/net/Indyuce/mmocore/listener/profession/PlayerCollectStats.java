package net.Indyuce.mmocore.listener.profession;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import net.Indyuce.mmocore.api.event.CustomBlockMineEvent;
import net.Indyuce.mmocore.loot.chest.particle.SmallParticleEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class PlayerCollectStats implements Listener {
	private static final Random RANDOM = new Random();

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void a(CustomBlockMineEvent event) {
		Player player = event.getPlayer();

		// Give haste if right enchant
		double h = event.getData().getStats().getStat("GATHERING_HASTE");
		if (h > 0 && RANDOM.nextDouble() < h * .045) {
			new SmallParticleEffect(player, VParticle.INSTANT_EFFECT.get());
			UtilityMethods.forcePotionEffect(player, VPotionEffectType.HASTE.get(), h / 2, (int) (1 + h / 7));
		}

		// Drop more items if fortune enchant
		double f = event.getData().getStats().getStat("FORTUNE");
		if (f > 0 && RANDOM.nextDouble() < f * .045) {
			int a = (int) (1.5 * Math.sqrt(f / 1.1));
			for (ItemStack item : event.getDrops())
				item.setAmount(item.getAmount() + a);
		}

		if (MythicLib.plugin.getVersion().getWrapper().isCropFullyGrown(event.getBlock())) {

			// Drop more CROP items
			double l = event.getData().getStats().getStat("LUCK_OF_THE_FIELD");
			if (l > 0 && RANDOM.nextDouble() < l * .045) {
				int a = (int) (1.5 * Math.sqrt(l / 1.1));
				Location loc = event.getBlock().getLocation().add(.5, .1, .5);
				loc.getWorld().spawnParticle(VParticle.HAPPY_VILLAGER.get(), loc.clone().add(0, .2, 0), 10, .3, .2, .3, 0);
				for (ItemStack item : event.getDrops())
					item.setAmount(item.getAmount() + a);
			}
		}
	}
}
