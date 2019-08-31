package net.Indyuce.mmocore.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.item.ConfigItem;

public class ConfigItemManager {
	private Map<String, ConfigItem> map = new HashMap<>();

	public ConfigItemManager(FileConfiguration config) {
		for (String key : config.getKeys(false))
			try {
				register(new ConfigItem(config.getConfigurationSection(key)));
			} catch (NullPointerException | IllegalArgumentException exception) {
				MMOCore.plugin.getLogger().log(Level.INFO, "Could not load config item " + key);
			}
	}

	public void register(ConfigItem item) {
		map.put(item.getId(), item);
	}

	public ConfigItem get(String id) {
		return map.get(id);
	}
}
