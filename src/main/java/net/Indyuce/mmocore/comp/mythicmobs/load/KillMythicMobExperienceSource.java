package net.Indyuce.mmocore.comp.mythicmobs.load;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.profession.ExperienceManager;

public class KillMythicMobExperienceSource extends SpecificExperienceSource<String> {
	private final String internalName;

	public KillMythicMobExperienceSource(Profession profession, MMOLineConfig config) {
		super(profession, config);

		config.validate("type");
		internalName = config.getString("type");
	}

	@Override
	public ExperienceManager<KillMythicMobExperienceSource> newManager() {
		return new ExperienceManager<KillMythicMobExperienceSource>() {
			@EventHandler
			public void a(MythicMobDeathEvent event) {
				Bukkit.getScheduler().runTaskLater(MMOCore.plugin, new Runnable() {
					@Override
					public void run() {
						if (!event.getEntity().isDead()) return;
						if (!(event.getKiller() instanceof Player) && !event.getKiller().hasMetadata("NPC")) return;

						PlayerData data = PlayerData.get((Player) event.getKiller());
						for (KillMythicMobExperienceSource source : getSources())
							if (source.matches(data, event.getMobType().getInternalName()))
								source.giveExperience(data, event.getEntity().getLocation());
					}
				}, 2);
			}
		};
	}

	@Override
	public boolean matches(PlayerData player, String name) {
		return hasRightClass(player) && name.equals(internalName);
	}
}
