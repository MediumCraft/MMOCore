package net.Indyuce.mmocore.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.block.BlockType;
import net.Indyuce.mmoitems.api.util.MMOLineConfig;

public class RestrictionManager {
	// private Set<String> breakBlackList = new HashSet<>();
	private Map<Material, BlockPermissions> map = new HashMap<>();

	public RestrictionManager(FileConfiguration config) {

		for (String key : config.getKeys(false))
			try {
				register(new BlockPermissions(config.getConfigurationSection(key)));
			} catch (IllegalArgumentException exception) {
				MMOCore.log(Level.WARNING, "Could not load block perms " + key + ": " + exception.getMessage());
			}

		for (BlockPermissions perms : map.values())
			try {
				perms.load();
			} catch (IllegalArgumentException exception) {
				MMOCore.log(Level.WARNING, "Could not load block perms " + perms.getTool().name() + ": " + exception.getMessage());
			}
	}

	public void register(BlockPermissions perms) {
		if (perms.isValid()) {
			map.put(perms.getTool(), perms);
			// perms.getMinable().forEach(material ->
			// breakBlackList.add(material));
		}
	}

	// public boolean isBlackListed(String s) {
	// return breakBlackList.contains(s);
	// }

	public BlockPermissions getPermissions(Material tool) {
		return map.containsKey(tool) ? map.get(tool) : null;
	}

	public class BlockPermissions {
		private final Set<BlockType> mineable = new HashSet<>();
		private final Material tool;

		private BlockPermissions parent;

		/*
		 * cache configuration section for easier laod
		 */
		private ConfigurationSection loaded;

		/*
		 * these instances must be initialized before loading data about them
		 * because in order to load PARENT permissions, every instance needs to
		 * be added to the map first
		 */
		public BlockPermissions(ConfigurationSection section) {
			Validate.notNull(section, "Could not load config");

			this.loaded = section;
			tool = Material.valueOf(section.getName());
		}

		public void load() {
			if (loaded.contains("parent"))
				parent = map.get(Material.valueOf(loaded.getString("parent", "???").toUpperCase().replace("-", "_").replace(" ", "_")));
			for (String key : loaded.getStringList("can-mine"))
				mineable.add(MMOCore.plugin.loadManager.loadBlockType(new MMOLineConfig(key)));

			loaded = null;
		}

		public void addPermission(BlockType block) {
			mineable.add(block);
		}

		// recursive function to check for parent permissions
		public boolean canMine(BlockType type) {
			return mineable.contains(type) || (parent != null && parent.canMine(type));
		}

		public Material getTool() {
			return tool;
		}

		public boolean isValid() {
			return tool != null;
		}
	}
}
