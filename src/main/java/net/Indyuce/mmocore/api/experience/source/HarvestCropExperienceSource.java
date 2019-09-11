package net.Indyuce.mmocore.api.experience.source;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.profession.ExperienceManager;

public class HarvestCropExperienceSource extends SpecificExperienceSource<Material> {
	private final Material material;
	private final boolean ripe;

	public HarvestCropExperienceSource(Profession profession, MMOLineConfig config) {
		super(profession, config);

		config.validate("type");
		material = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
		ripe = config.getBoolean("ripe", true);
	}

	@Override
	public ExperienceManager<HarvestCropExperienceSource> newManager() {
		return new ExperienceManager<HarvestCropExperienceSource>() {

			@EventHandler(priority = EventPriority.HIGHEST)
			public void a(BlockBreakEvent event) {
				if (event.isCancelled() || event.getPlayer().getGameMode() != GameMode.SURVIVAL)
					return;

				
				
				if (ripe && !MMOCore.plugin.version.getVersionWrapper().isCropFullyGrown(event.getBlock()))
					return;

				Material broken = event.getBlock().getType();

				PlayerData data = PlayerData.get(event.getPlayer());
				for (HarvestCropExperienceSource source : getSources())
					if (source.matches(data, broken))
						source.giveExperience(data);
			}
		};
	}

	@Override
	public boolean matches(PlayerData player, Material obj) {
		return material == obj && hasRightClass(player);
	}
}
