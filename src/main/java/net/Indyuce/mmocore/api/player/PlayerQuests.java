package net.Indyuce.mmocore.api.player;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.quest.Quest;
import net.Indyuce.mmocore.api.quest.QuestProgress;

public class PlayerQuests {
	private final PlayerData playerData;
	private final BossBar bossbar;
	private final Map<String, Long> finished = new HashMap<>();

	private QuestProgress current;

	public PlayerQuests(PlayerData playerData) {
		this.playerData = playerData;
		bossbar = MMOCore.plugin.version.getVersionWrapper().createBossBar(new NamespacedKey(MMOCore.plugin, "quest_bar_" + playerData.getUniqueId().toString()), "", BarColor.PURPLE, BarStyle.SEGMENTED_20, new BarFlag[0]);
		bossbar.addPlayer(playerData.getPlayer());
	}

	public PlayerQuests load(ConfigurationSection config) {
		if (config.contains("current"))
			try {
				current = MMOCore.plugin.questManager.get(config.getString("current.id")).generateNewProgress(playerData, config.getInt("current.objective"));
			} catch (Exception e) {
				playerData.log(Level.WARNING, "Couldn't load current quest progress (ID '" + config.getString("current.id") + "'");
			}

		if (config.contains("finished"))
			for (String key : config.getConfigurationSection("finished").getKeys(false))
				finished.put(key, config.getLong("finished." + key));

		/*
		 * must update the boss bar once the instance is loaded, otherwise it
		 * won't detect the current quest. THE BOSS BAR UPDATE is in the player
		 * data class, this way it is still set invisible even if the player has
		 * no quest data
		 */

		return this;
	}

	public void save(ConfigurationSection config) {
		if (current != null) {
			config.set("current.id", current.getQuest().getId());
			config.set("current.objective", current.getObjectiveNumber());
		} else
			config.set("current", null);

		for (String key : finished.keySet())
			config.set("finished." + key, finished.get(key));
	}

	public QuestProgress getCurrent() {
		return current;
	}

	public boolean hasCurrent() {
		return current != null;
	}

	public Set<String> getFinishedQuests() {
		return finished.keySet();
	}

	public boolean hasCurrent(Quest quest) {
		return hasCurrent() && current.getQuest().equals(quest);
	}

	public boolean hasFinished(Quest quest) {
		return finished.containsKey(quest.getId());
	}

	public void finishCurrent() {
		finished.put(current.getQuest().getId(), System.currentTimeMillis());
		start(null);
	}

	public void resetFinishedQuests() {
		finished.clear();
	}

	public Date getFinishDate(Quest quest) {
		return new Date(finished.get(quest.getId()));
	}

	public void start(Quest quest) {

		// close current objective progress if quest is active
		if (hasCurrent())
			current.closeObjectiveProgress();

		// apply newer quest
		current = quest == null ? null : quest.generateNewProgress(playerData);
		updateBossBar();
	}

	public boolean checkCooldownAvailability(Quest quest) {
		return (finished.get(quest.getId()) + quest.getDelayMillis()) < System.currentTimeMillis();
	}

	public long getDelayFeft(Quest quest) {
		return Math.max(finished.get(quest.getId()) + quest.getDelayMillis() - System.currentTimeMillis(), 0);
	}

	public boolean checkParentAvailability(Quest quest) {
		for (Quest parent : quest.getParents())
			if (!hasFinished(parent))
				return false;
		return true;
	}

	public void updateBossBar() {
		if (!hasCurrent()) {
			bossbar.setVisible(false);
			return;
		}

		bossbar.setVisible(true);
		bossbar.setColor(current.getProgress().getObjective().getBarColor());
		bossbar.setTitle(current.getFormattedLore());
		bossbar.setProgress((double) current.getObjectiveNumber() / current.getQuest().getObjectives().size());
	}

	public void resetBossBar() {
		bossbar.removePlayer(playerData.getPlayer());
	}
}
