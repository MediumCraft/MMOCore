package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.itemtype.ItemType;
import io.lumine.mythic.lib.api.util.PostLoadObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.block.BlockType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class RestrictionManager {
	private final Map<ItemType, ToolPermissions> map = new HashMap<>();

	public RestrictionManager(FileConfiguration config) {

		for (String key : config.getKeys(false))
			try {
				register(new ToolPermissions(config.getConfigurationSection(key)));
			} catch (IllegalArgumentException exception) {
				MMOCore.log(Level.WARNING, "Could not load block perms " + key + ": " + exception.getMessage());
			}

		for (ToolPermissions perms : map.values())
			try {
				perms.postLoad();
			} catch (IllegalArgumentException exception) {
				MMOCore.log(Level.WARNING, "Could not postload block perms " + perms.getTool().display() + ": " + exception.getMessage());
			}
	}

	public void register(ToolPermissions perms) {
		if (perms.isValid())
			map.put(perms.getTool(), perms);
	}

	/**
	 * @param item
	 *            The item used to break a block
	 * @return A list of all the blocks an item is allowed to break.
	 */
	public Set<ToolPermissions> getPermissions(ItemStack item) {
		Set<ToolPermissions> set = new HashSet<>();
		for (ItemType type : map.keySet())
			if (type.matches(item))
				set.add(map.get(type));
		return set;
	}

	/**
	 * Performance method so that MMOCore does not have to fill in a set of
	 * toolPermission instances.
	 * 
	 * @param item
	 *            The item used to break a block
	 * @return If the block can be broken by a certain item
	 */
	public boolean checkPermissions(ItemStack item, BlockType block) {
		for (ItemType type : map.keySet())
			if (type.matches(item) && map.get(type).canMine(block))
				return true;
		return false;
	}

	public class ToolPermissions extends PostLoadObject {
		private final Set<BlockType> mineable = new HashSet<>();
		private final ItemType tool;

		private ToolPermissions parent;

		public ToolPermissions(ConfigurationSection config) {
			super(config);

			tool = ItemType.fromString(config.getName());
		}

		@Override
		protected void whenPostLoaded(ConfigurationSection config) {
			if (config.contains("parent"))
				parent = map.get(ItemType.fromString(config.getString("parent")));
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

		public ItemType getTool() {
			return tool;
		}

		public boolean isValid() {
			return tool != null;
		}
	}
}
