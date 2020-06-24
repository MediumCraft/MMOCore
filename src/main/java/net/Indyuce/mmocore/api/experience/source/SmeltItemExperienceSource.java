package net.Indyuce.mmocore.api.experience.source;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.profession.ExperienceManager;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class SmeltItemExperienceSource extends SpecificExperienceSource<ItemStack> {
	private final Material material;

	public SmeltItemExperienceSource(Profession profession, MMOLineConfig config) {
		super(profession, config);

		config.validate("type");
		material = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
	}

	@Override
	public ExperienceManager<SmeltItemExperienceSource> newManager() {
		return new ExperienceManager<SmeltItemExperienceSource>() {

			@EventHandler(priority = EventPriority.HIGH)
			public void a(BlockCookEvent event) {
				if (!event.isCancelled()) {
					Optional<Player> player = getNearbyPlayer(event.getBlock().getLocation(), 10);
					if (!player.isPresent())
						return;

					ItemStack caught = event.getResult();
					if (caught.hasItemMeta())
						return;

					PlayerData data = PlayerData.get(player.get());
					for (SmeltItemExperienceSource source : getSources())
						if (source.matches(data, caught))
							source.giveExperience(data, event.getBlock().getLocation());
				}
			}
		};
	}

	private Optional<Player> getNearbyPlayer(Location loc, double d) {
		final double d2 = d * d;
		final Player[] nearby = loc.getWorld().getPlayers().stream()
				.filter(player -> player.getLocation().distanceSquared(loc) < d2).toArray(Player[]::new);
		Player selected = null;
		double lastDist = d2;
		for(Player p : nearby) {
			double currDist = p.getLocation().distance(loc);
			if(currDist < lastDist) {
				lastDist = currDist;
				selected = p;
			}
		}
		return Optional.ofNullable(selected);
	}

	@Override
	public boolean matches(PlayerData player, ItemStack obj) {
		return obj.getType() == material && hasRightClass(player);
	}
}
