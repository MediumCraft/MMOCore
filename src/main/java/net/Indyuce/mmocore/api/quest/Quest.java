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
import net.Indyuce.mmocore.api.load.PostLoadObject;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class Quest extends PostLoadObject {
	private final String id;

	private final String name;
	private final List<Quest> parents = new ArrayList<>();
	private final List<Objective> objectives = new ArrayList<>();
	private final List<String> lore;

	private final int mainLevelRestriction;
	private final Map<Profession, Integer> levelRestrictions = new HashMap<>();

	// cooldown in millis
	private final long cooldown;

	public Quest(String id, FileConfiguration config) {
		super(config);

		this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
		cooldown = (long) (config.contains("delay") ? config.getDouble("delay") * 60 * 60 * 1000 : -1);
		name = config.getString("name");
		lore = config.getStringList("lore");

		mainLevelRestriction = config.getInt("level-req.main");

		if (config.contains("level-req"))
			for (String key : config.getConfigurationSection("level-req").getKeys(false))
				if (!key.equals("main"))
					try {
						String id1 = key.toLowerCase().replace("_", "-");
						Validate.isTrue(MMOCore.plugin.professionManager.has(id1), "Could not find profession called '" + id1 + "'");
						levelRestrictions.put(MMOCore.plugin.professionManager.get(id1), config.getInt("level-req." + key));
					} catch (IllegalArgumentException exception) {
						MMOCore.plugin.getLogger().log(Level.WARNING,
								"Could not load level requirement '" + key + "' from quest '" + id + "': " + exception.getMessage());
					}

		for (String key : config.getConfigurationSection("objectives").getKeys(false))
			try {
				ConfigurationSection section = config.getConfigurationSection("objectives." + key);
				Validate.notNull(section, "Could not find config section");

				String format = section.getString("type");
				Validate.notNull(format, "Objective is missing format");

				objectives.add(MMOCore.plugin.loadManager.loadObjective(new MMOLineConfig(format), section));
			} catch (MMOLoadException exception) {
				exception.printConsole("Quests:" + id, "objective");
			}
	}

	@Override
	protected void whenPostLoaded(FileConfiguration config) {
		if (config.contains("parent"))
			for (String parent : config.getStringList("parent"))
				try {
					parents.add(MMOCore.plugin.questManager.get(parent));
				} catch (NullPointerException exception) {
					MMOCore.plugin.getLogger().log(Level.WARNING, "Couldn't find quest ID '" + parent + "'");
				}
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
