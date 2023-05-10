package net.Indyuce.mmocore.api.quest;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import io.lumine.mythic.lib.MythicLib;

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

	public void completeObjective() {
		objective++;
		objectiveProgress.close();


		// end quest
		if (objective >= quest.getObjectives().size())
			player.getQuestData().finishCurrent();
		else
			objectiveProgress = nextObjective().newProgress(this);

		player.getQuestData().updateBossBar();


		// apply triggers at the end so the quest is ended when a trigger quest start is launched.
		objectiveProgress.getObjective().getTriggers().forEach(trigger -> trigger.schedule(getPlayer()));
	}

	public String getFormattedLore() {
		return MythicLib.plugin.parseColors(objectiveProgress.formatLore(objectiveProgress.getObjective().getDefaultLore()));
	}
}