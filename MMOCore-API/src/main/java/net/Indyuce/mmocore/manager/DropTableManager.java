package net.Indyuce.mmocore.manager;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.loot.droptable.DropTable;
import net.Indyuce.mmocore.util.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DropTableManager implements MMOCoreManager {
	private final Map<String, DropTable> map = new HashMap<>();

	public void register(DropTable table) {
		map.put(table.getId(), table);
	}

	@Nullable
	public DropTable get(String path) {
		return map.get(path);
	}

	public boolean has(String path) {
		return map.containsKey(path);
	}

	@NotNull
	public Collection<DropTable> getDropTables() {
		return map.values();
	}

	@NotNull
	public Set<String> getKeys() {
		return map.keySet();
	}

	/**
	 * Can only be used once the plugin has been fully loaded (+ enabled). used
	 * to load extra drop tables from different config files e.g blocks, fishing
	 * drop tables
	 */
	@NotNull
	public DropTable loadDropTable(Object obj) throws IllegalArgumentException {

		if (obj instanceof String)
			return get((String) obj);

		if (obj instanceof ConfigurationSection) {
			DropTable table = new DropTable((ConfigurationSection) obj);
			Bukkit.getScheduler().runTask(MMOCore.plugin, table.getPostLoadAction()::performAction);
			return table;
		}

		throw new IllegalArgumentException("Could not parse drop table.");
	}

	@Override
	public void initialize(boolean clearBefore) {
		if (clearBefore) map.clear();

		// Load drop tables
		FileUtils.loadObjectsFromFolder(MMOCore.plugin, "drop-tables", false, (name, config) -> {
            register(new DropTable(config));
        }, "Could not load drop table '%s' from file '%s': %s");

		Bukkit.getScheduler().runTask(MMOCore.plugin, () -> map.values().forEach(table -> table.getPostLoadAction().performAction()));
	}
}
