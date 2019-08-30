package net.Indyuce.mmocore.manager;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.quest.Quest;

public class QuestManager extends MMOManager {
	private Map<String, Quest> quests = new LinkedHashMap<>();

	public void load(File file) {
		if (file.isDirectory())
			for (File subfile : file.listFiles())
				load(subfile);
		else
			try {
				register(new Quest(file.getName().substring(0, file.getName().length() - 4), YamlConfiguration.loadConfiguration(file)));
			} catch (IllegalArgumentException exception) {
				MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load quest '" + file.getName() + "': " + exception.getMessage());
			}
	}

	public void register(Quest quest) {
		quests.put(quest.getId(), quest);
	}

	public Quest get(String id) {
		return quests.get(id);
	}

	public Collection<Quest> getAll() {
		return quests.values();
	}

	@Override
	public void reload() {
		load(new File(MMOCore.plugin.getDataFolder() + "/quests"));
		quests.values().forEach(quest -> quest.load(this));
	}

	@Override
	public void clear() {
		quests.clear();
	}
}
