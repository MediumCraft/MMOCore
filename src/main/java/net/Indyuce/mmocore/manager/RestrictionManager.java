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

public class RestrictionManager {
	private Set<String> breakBlackList = new HashSet<>();
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
			perms.getMinable().forEach(material -> breakBlackList.add(material));
		}
	}

	public boolean isBlackListed(String s) {
		return breakBlackList.contains(s);
	}

	public BlockPermissions getPermissions(Material tool) {
		return map.containsKey(tool) ? map.get(tool) : null;
	}

	public class BlockPermissions {
		private final Set<String> canMine = new HashSet<>();
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
			if (loaded.contains("parent")) {
				String str = loaded.getString("parent").toUpperCase().replace("-", "_").replace(" ", "_");
				Validate.notNull(str, "Could not load parent");
				parent = map.get(Material.valueOf(str));
			}

			for (String key : loaded.getStringList("can-mine"))
				canMine.add(key.toUpperCase().replace("-", "_"));

			loaded = null;
		}

		public void setParent(BlockPermissions parent) {
			this.parent = parent;
		}

		public void addPermission(String s) {
			canMine.add(s);
		}

		// recursive function to check for parent permissions
		public boolean canMine(String material) {
			return canMine.contains(material) || (parent != null && parent.canMine(material));
		}

		public Set<String> getMinable() {
			Set<String> total = new HashSet<>(canMine);
			if (parent != null)
				total.addAll(parent.getMinable());
			return total;
		}

		public Material getTool() {
			return tool;
		}

		public boolean isValid() {
			return tool != null;
		}
	}
}
