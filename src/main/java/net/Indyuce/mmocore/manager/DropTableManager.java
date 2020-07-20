package net.Indyuce.mmocore.manager;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.droptable.DropTable;

public class DropTableManager extends MMOManager {
	private final Map<String, DropTable> map = new HashMap<>();

	public void register(DropTable table) {
		map.put(table.getId(), table);
	}

	public DropTable get(String path) {
		return map.get(path);
	}

	public boolean has(String path) {
		return map.containsKey(path);
	}

	public Collection<DropTable> getDropTables() {
		return map.values();
	}

	public Set<String> getKeys() {
		return map.keySet();
	}

	/*
	 * can only be used once the plugin has been fully loaded (+ enabled). used
	 * to load extra drop tables from different config files e.g blocks, fishing
	 * drop tables
	 */
	public DropTable loadDropTable(Object obj) throws IllegalArgumentException {

		if (obj instanceof String)
			return get((String) obj);

		if (obj instanceof ConfigurationSection) {
			DropTable table = new DropTable((ConfigurationSection) obj);
			Bukkit.getScheduler().runTask(MMOCore.plugin, () -> table.postLoad());
			return table;
		}

		throw new IllegalArgumentException("Could not parse drop table.");
	}

	@Override
	public void reload() {
		for (File file : new File(MMOCore.plugin.getDataFolder() + "/drop-tables").listFiles())
			try {

				FileConfiguration config = YamlConfiguration.loadConfiguration(file);
				for (String key : config.getKeys(false))
					try {
						register(new DropTable(config.getConfigurationSection(key)));
					} catch (IllegalArgumentException exception) {
						MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load drop table '" + key + "': " + exception.getMessage());
					}

			} catch (IllegalArgumentException exception) {
				MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load drop table file '" + file.getName() + "': " + exception.getMessage());
			}

		Bukkit.getScheduler().runTask(MMOCore.plugin, () -> map.values().forEach(table -> table.postLoad()));
	}

	@Override
	public void clear() {
		map.clear();
	}
}
