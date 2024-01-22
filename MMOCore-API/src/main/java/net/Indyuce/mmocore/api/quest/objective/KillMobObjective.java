package net.Indyuce.mmocore.api.quest.objective;

import io.lumine.mythic.lib.api.event.PlayerKillEntityEvent;
import net.Indyuce.mmocore.api.quest.ObjectiveProgress;
import net.Indyuce.mmocore.api.quest.QuestProgress;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.api.MMOLineConfig;

public class KillMobObjective extends Objective {
	private final EntityType type;
	private final int required;

	public KillMobObjective(ConfigurationSection section, MMOLineConfig config) {
		super(section);

		config.validate("amount", "type");

		type = EntityType.valueOf(config.getString("type"));
		required = config.getInt("amount");
	}

	@Override
	public ObjectiveProgress newProgress(QuestProgress questProgress) {
		return new KillMobProgress(questProgress, this);
	}

	public class KillMobProgress extends ObjectiveProgress implements Listener {
		private int count;

		public KillMobProgress(QuestProgress questProgress, Objective objective) {
			super(questProgress, objective);
		}

		@EventHandler
		public void a(PlayerKillEntityEvent event) {
			if(!getPlayer().isOnline()) return;
			if (event.getTarget().getType() == type && event.getPlayer().equals(getPlayer().getPlayer())) {
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
