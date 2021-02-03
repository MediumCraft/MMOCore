package net.Indyuce.mmocore.comp.mythicmobs.load;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.profession.ExperienceManager;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class KillMythicFactionExperienceSource extends SpecificExperienceSource<String> {
	private final String factionName;

	public KillMythicFactionExperienceSource(Profession profession, MMOLineConfig config) {
		super(profession, config);

		config.validate("name");
		factionName = config.getString("name");
	}

	@Override
	public ExperienceManager<KillMythicFactionExperienceSource> newManager() {
		return new ExperienceManager<KillMythicFactionExperienceSource>() {
			@EventHandler
			public void a(MythicMobDeathEvent event) {
				Bukkit.getScheduler().runTaskLater(MMOCore.plugin, () -> {
					if (!event.getEntity().isDead()) return;
					if (!event.getMob().hasFaction()) return;
					if (!(event.getKiller() instanceof Player) || event.getKiller().hasMetadata("NPC")) return;

					PlayerData data = PlayerData.get((Player) event.getKiller());
					for (KillMythicFactionExperienceSource source : getSources())
						if (source.matches(data, event.getMob().getFaction()))
							source.giveExperience(data, 1, event.getEntity().getLocation());
				}, 2);
			}
		};
	}

	@Override
	public boolean matches(PlayerData player, String name) {
		return hasRightClass(player) && name.equals(factionName);
	}
}
