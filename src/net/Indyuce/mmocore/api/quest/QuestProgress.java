package net.Indyuce.mmocore.api.quest;

import org.bukkit.ChatColor;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.objective.Objective;

public class QuestProgress {
	private final Quest quest;
	private final PlayerData player;

	private int objective;
	private ObjectiveProgress objectiveProgress;

	public QuestProgress(Quest quest, PlayerData player) {
		this(quest, player, 0);
	}

	public QuestProgress(Quest quest, PlayerData player, int objective) {
		this.quest = quest;
		this.player = player;
		
		this.objective = objective;
		objectiveProgress = nextObjective().newProgress(this);
	}

	public Quest getQuest() {
		return quest;
	}

	public PlayerData getPlayer() {
		return player;
	}

	public int getObjectiveNumber() {
		return objective;
	}

	public ObjectiveProgress getProgress() {
		return objectiveProgress;
	}

	private Objective nextObjective() {
		return quest.getObjectives().get(objective);
	}

	public void closeObjectiveProgress() {
		objectiveProgress.close();
	}

	public void completeObjective() {
		objective++;
		closeObjectiveProgress();

		// apply triggers
		objectiveProgress.getObjective().getTriggers().forEach(trigger -> trigger.apply(getPlayer()));

		// end quest
		if (objective >= quest.getObjectives().size())
			player.getQuestData().finishCurrent();
		else
			objectiveProgress = nextObjective().newProgress(this);

		player.getQuestData().updateBossBar();
	}

	public String getFormattedLore() {
		return ChatColor.translateAlternateColorCodes('&', objectiveProgress.formatLore(objectiveProgress.getObjective().getDefaultLore()));
	}
}