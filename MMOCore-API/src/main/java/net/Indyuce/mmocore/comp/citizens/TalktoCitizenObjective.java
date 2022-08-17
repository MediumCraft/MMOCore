package net.Indyuce.mmocore.comp.citizens;

import net.Indyuce.mmocore.api.quest.ObjectiveProgress;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.Indyuce.mmocore.api.quest.QuestProgress;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class TalktoCitizenObjective extends Objective {
	private final int id;

	public TalktoCitizenObjective(ConfigurationSection section, MMOLineConfig config) {
		super(section);

		config.validate("npc");
		
		id = config.getInt("npc");
	}

	@Override
	public ObjectiveProgress newProgress(QuestProgress questProgress) {
		return new TalktoProgress(questProgress, this);
	}

	public class TalktoProgress extends ObjectiveProgress implements Listener {
		public TalktoProgress(QuestProgress questProgress, Objective objective) {
			super(questProgress, objective);
		}

		@EventHandler
		public void a(CitizenInteractEvent event) {
			if(!getQuestProgress().getPlayer().isOnline()) return;
			if (event.getPlayer().equals(getQuestProgress().getPlayer().getPlayer()) && event.getNPC().getId() == id)
				getQuestProgress().completeObjective();
		}

		@Override
		public String formatLore(String lore) {
			return lore;
		}
	}
}
