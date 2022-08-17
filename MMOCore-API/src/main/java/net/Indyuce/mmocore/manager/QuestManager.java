package net.Indyuce.mmocore.manager;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.quest.Quest;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.YamlConfiguration;

public class QuestManager implements MMOCoreManager {
	private final Map<String, Quest> quests = new LinkedHashMap<>();
	
	public void load(File file) {
		if (file.isDirectory())
			Arrays.stream(file.listFiles()).sorted().forEach(this::load);
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

	public Quest getOrThrow(String id) {
		Validate.isTrue(quests.containsKey(id), "Could not find quest with ID '" + id + "'");
		return get(id);
	}

	public Collection<Quest> getAll() {
		return quests.values();
	}

	@Override
	public void initialize(boolean clearBefore) {
		if (clearBefore)
			quests.clear();

		load(new File(MMOCore.plugin.getDataFolder() + "/quests"));
		for (Quest quest : quests.values())
			try {
				quest.postLoad();
			} catch (IllegalArgumentException exception) {
				MMOCore.plugin.getLogger().log(Level.WARNING, "Could not post-load quest '" + quest.getId() + "': " + exception.getMessage());
			}
	}
}
