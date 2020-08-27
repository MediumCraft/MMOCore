package net.Indyuce.mmocore.api.experience.source;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.profession.ExperienceManager;
import net.mmogroup.mmolib.api.MMOLineConfig;
import net.mmogroup.mmolib.api.event.EntityKillEntityEvent;

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
			@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
			public void a(EntityKillEntityEvent event) {
				Bukkit.getScheduler().runTaskLater(MMOCore.plugin, new Runnable() {
					@Override
					public void run() {
						if (event.getTarget().isDead() && event.getEntity() instanceof Player && !event.getEntity().hasMetadata("NPC")
								&& !event.getTarget().hasMetadata("spawner_spawned")) {
							PlayerData data = PlayerData.get((Player) event.getEntity());

							for (KillMobExperienceSource source : getSources())
								if (source.matches(data, event.getTarget()))
									source.giveExperience(data, 1, event.getTarget().getLocation());
						}
					}
				}, 2);
			}
		};
	}

	@Override
	public boolean matches(PlayerData player, Entity obj) {
		return hasRightClass(player) && obj.getType() == type;
	}
}
