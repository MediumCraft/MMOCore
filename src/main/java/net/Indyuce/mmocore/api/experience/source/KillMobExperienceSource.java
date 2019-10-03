package net.Indyuce.mmocore.api.experience.source;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import net.Indyuce.mmocore.api.event.EntityKillEntityEvent;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.profession.ExperienceManager;

public class KillMobExperienceSource extends SpecificExperienceSource<Entity> {
	public final EntityType type;

	public KillMobExperienceSource(Profession profession, MMOLineConfig config) {
		super(profession, config);

		config.validate("type");
		type = EntityType.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
	}

	@Override
	public ExperienceManager<KillMobExperienceSource> newManager() {
		return new ExperienceManager<KillMobExperienceSource>() {

			@EventHandler
			public void a(EntityKillEntityEvent event) {
				if (event.getEntity() instanceof Player && !event.getEntity().hasMetadata("NPC")) {
					PlayerData data = PlayerData.get((Player) event.getEntity());

					for (KillMobExperienceSource source : getSources())
						if (source.matches(data, event.getTarget()))
							source.giveExperience(data);
				}
			}
		};
	}

	@Override
	public boolean matches(PlayerData player, Entity obj) {
		return hasRightClass(player) && obj.getType() == type;
	}
}
