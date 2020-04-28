package net.Indyuce.mmocore.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.block.BlockType;
import net.Indyuce.mmocore.api.load.PostLoadObject;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class RestrictionManager {
	// private Set<String> breakBlackList = new HashSet<>();
	private final Map<Material, BlockPermissions> map = new HashMap<>();

	public RestrictionManager(FileConfiguration config) {

		for (String key : config.getKeys(false))
			try {
				register(new BlockPermissions(config.getConfigurationSection(key)));
			} catch (IllegalArgumentException exception) {
				MMOCore.log(Level.WARNING, "Could not load block perms " + key + ": " + exception.getMessage());
			}

		for (BlockPermissions perms : map.values())
			try {
				perms.postLoad();
			} catch (IllegalArgumentException exception) {
				MMOCore.log(Level.WARNING, "Could not load block perms " + perms.getTool().name() + ": " + exception.getMessage());
			}
	}

	public void register(BlockPermissions perms) {
		if (perms.isValid())
			map.put(perms.getTool(), perms);
		// perms.getMinable().forEach(material ->
		// breakBlackList.add(material));
	}

	// public boolean isBlackListed(String s) {
	// return breakBlackList.contains(s);
	// }

	public BlockPermissions getPermissions(Material tool) {
		return map.getOrDefault(tool, null);
	}

	public class BlockPermissions extends PostLoadObject {
		private final Set<BlockType> mineable = new HashSet<>();
		private final Material tool;

		private BlockPermissions parent;

		public BlockPermissions(ConfigurationSection config) {
			super(config);

			tool = Material.valueOf(config.getName());
		}

		@Override
		protected void whenPostLoaded(ConfigurationSection config) {
			if (config.contains("parent"))
				parent = map.get(Material.valueOf(config.getString("parent", "None").toUpperCase().replace("-", "_").replace(" ", "_")));
			for (String key : config.getStringList("can-mine"))
				mineable.add(MMOCore.plugin.loadManager.loadBlockType(new MMOLineConfig(key)));
		}

		public void addPermission(BlockType block) {
			mineable.add(block);
		}

		// recursive function to check for parent permissions
		public boolean canMine(BlockType type) {
			String key = type.generateKey();
			for (BlockType mineable : this.mineable)
				if (mineable.generateKey().equals(key))
					return true;

			return parent != null && parent.canMine(type);
		}

		public Material getTool() {
			return tool;
		}

		public boolean isValid() {
			return tool != null;
		}
	}
}
