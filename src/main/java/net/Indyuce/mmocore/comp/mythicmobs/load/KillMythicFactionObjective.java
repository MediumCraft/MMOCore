package net.Indyuce.mmocore.comp.mythicmobs.load;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import net.Indyuce.mmocore.api.quest.ObjectiveProgress;
import net.Indyuce.mmocore.api.quest.QuestProgress;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class KillMythicFactionObjective extends Objective {
	private final String factionName;
	private final int required;

	public KillMythicFactionObjective(ConfigurationSection section, MMOLineConfig config) {
		super(section);

		config.validate("amount", "name");

		factionName = config.getString("name");
		required = config.getInt("amount");
	}

	@Override
	public ObjectiveProgress newProgress(QuestProgress questProgress) {
		return new KillFactionProgress(questProgress, this);
	}

	public class KillFactionProgress extends ObjectiveProgress implements Listener {
		private int count;

		public KillFactionProgress(QuestProgress questProgress, Objective objective) {
			super(questProgress, objective);
		}

		@EventHandler
		public void a(MythicMobDeathEvent event) {
			if(!getQuestProgress().getPlayer().isOnline()) return;
			if (event.getKiller() instanceof Player
					&& event.getKiller().equals(getQuestProgress().getPlayer().getPlayer())
					&& event.getMob().hasFaction() && event.getMob().getFaction().equals(factionName)) {
				count++;
				getQuestProgress().getPlayer().getQuestData().updateBossBar();
				if (count >= required)
					getQuestProgress().completeObjective();
			}
		}

		@Override
		public String formatLore(String lore) {
			return lore.replace("{left}", "" + (required - count));
		}
	}
}
