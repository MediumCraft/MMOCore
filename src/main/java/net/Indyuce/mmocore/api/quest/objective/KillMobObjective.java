package net.Indyuce.mmocore.api.quest.objective;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.quest.ObjectiveProgress;
import net.Indyuce.mmocore.api.quest.QuestProgress;
import net.mmogroup.mmolib.api.event.EntityKillEntityEvent;

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
		public void a(EntityKillEntityEvent event) {
			if (event.getTarget().getType() == type && event.getEntity().equals(getPlayer().getPlayer())) {
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
