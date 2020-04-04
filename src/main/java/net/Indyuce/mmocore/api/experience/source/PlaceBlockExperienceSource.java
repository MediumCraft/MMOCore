package net.Indyuce.mmocore.api.experience.source;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.profession.ExperienceManager;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class PlaceBlockExperienceSource extends SpecificExperienceSource<Material> {
	public final Material material;

	public PlaceBlockExperienceSource(Profession profession, MMOLineConfig config) {
		super(profession, config);

		config.validate("type");
		material = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
	}

	@Override
	public ExperienceManager<PlaceBlockExperienceSource> newManager() {
		return new ExperienceManager<PlaceBlockExperienceSource>() {

			@EventHandler(priority = EventPriority.HIGHEST)
			public void a(BlockPlaceEvent event) {
				if (event.isCancelled() || event.getPlayer().getGameMode() != GameMode.SURVIVAL)
					return;
				PlayerData data = PlayerData.get(event.getPlayer());
				
				for (PlaceBlockExperienceSource source : getSources()) {
					if (source.matches(data, event.getBlock().getType()))
						source.giveExperience(data, event.getBlock().getLocation());
				}
			}
		};
	}

	@Override
	public boolean matches(PlayerData player, Material obj) {
		return material == obj && hasRightClass(player);
	}
}
