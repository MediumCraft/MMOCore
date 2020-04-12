package net.Indyuce.mmocore.api.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.load.MMOLoadException;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.Indyuce.mmocore.manager.QuestManager;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class Quest {
	private final String id;

	private String name;
	private List<Quest> parents = new ArrayList<>();
	private List<Objective> objectives = new ArrayList<>();
	private List<String> lore;

	private int mainLevelRestriction;
	private Map<Profession, Integer> levelRestrictions = new HashMap<>();

	// cooldown in millis
	private long cooldown;

	/*
	 * cached to load other info to enable parent quests.
	 */
	private FileConfiguration loaded;

	public Quest(String id, FileConfiguration config) {
		this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
		loaded = config;
	}

	/*
	 * forced to request a Questmanager instance because the one in the main
	 * MMOCore class has not been initialized yet, so it can't be accessed using
	 * MMOCore.plugin.questManager
	 */
	public void load(QuestManager manager) {
		cooldown = (long) (loaded.contains("delay") ? loaded.getDouble("delay") * 60 * 60 * 1000 : -1);

		if (loaded.contains("parent"))
			for (String parent : loaded.getStringList("parent"))
				try {
					parents.add(manager.get(parent));
				} catch (NullPointerException exception) {
					MMOCore.plugin.getLogger().log(Level.WARNING, "Couldn't find quest ID '" + parent + "'");
				}

		name = loaded.getString("name");
		lore = loaded.getStringList("lore");

		mainLevelRestriction = loaded.getInt("level-req.main");

		if (loaded.contains("level-req"))
			for (String key : loaded.getConfigurationSection("level-req").getKeys(false))
				if (!key.equals("main"))
					try {
						String id = key.toLowerCase().replace("_", "-");
						Validate.isTrue(MMOCore.plugin.professionManager.has(id));
						levelRestrictions.put(MMOCore.plugin.professionManager.get(id), loaded.getInt("level-req." + key));
					} catch (IllegalArgumentException exception) {
						MMOCore.plugin.getLogger().log(Level.WARNING, "[Quests:" + id + "] Couldn't find profession '" + key + "'");
					}

		for (String key : loaded.getConfigurationSection("objectives").getKeys(false))
			try {
				ConfigurationSection section = loaded.getConfigurationSection("objectives." + key);
				Validate.notNull(section, "Could not find config section");

				String format = section.getString("type");
				Validate.notNull(format, "Objective is missing format");

				objectives.add(MMOCore.plugin.loadManager.loadObjective(new MMOLineConfig(format), section));
			} catch (MMOLoadException exception) {
				exception.printConsole("Quests:" + id, "objective");
			}

		loaded = null;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<String> getLore() {
		return lore;
	}

	public boolean hasParent() {
		return parents.size() > 0;
	}

	public List<Quest> getParents() {
		return parents;
	}

	public boolean isRedoable() {
		return cooldown >= 0;
	}

	public long getDelayMillis() {
		return cooldown;
	}

	public List<Objective> getObjectives() {
		return objectives;
	}

	public Set<Profession> getLevelRestrictions() {
		return levelRestrictions.keySet();
	}

	public int countLevelRestrictions() {
		return levelRestrictions.size() + (mainLevelRestriction > 0 ? 1 : 0);
	}

	public int getLevelRestriction(Profession profession) {
		return profession == null ? mainLevelRestriction : levelRestrictions.containsKey(profession) ? levelRestrictions.get(profession) : 0;
	}

	public QuestProgress generateNewProgress(PlayerData player) {
		return generateNewProgress(player, 0);
	}

	public QuestProgress generateNewProgress(PlayerData player, int objective) {
		return new QuestProgress(this, player, objective);
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof Quest && ((Quest) obj).id.equals(id);
	}
}
