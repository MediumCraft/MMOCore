package net.Indyuce.mmocore.api.quest;

import io.lumine.mythic.lib.util.Closeable;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class ObjectiveProgress implements Closeable {
	private final Objective objective;
	private final QuestProgress questProgress;

	public ObjectiveProgress(QuestProgress questProgress, Objective objective) {
		this.questProgress = questProgress;
		this.objective = objective;

		if (this instanceof Listener)
			Bukkit.getPluginManager().registerEvents((Listener) this, MMOCore.plugin);
	}

	public PlayerData getPlayer() {
		return questProgress.getPlayer();
	}

	public Objective getObjective() {
		return objective;
	}

	public QuestProgress getQuestProgress() {
		return questProgress;
	}

	@Override
	public void close() {
		if (this instanceof Listener)
			HandlerList.unregisterAll((Listener) this);
	}

	public abstract String formatLore(String lore);
}
