package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.itemtype.ItemType;
import io.lumine.mythic.lib.api.util.PostLoadObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.block.BlockType;
import net.Indyuce.mmocore.api.ConfigFile;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public class RestrictionManager implements MMOCoreManager {

    /**
     * Using {@link ItemType#display()} instead of an ItemType as
     * map key to utilize the HashMap O(1) time complexity of the
     * get function instead of iterating through the key set.
     */
    private final Map<String, ToolPermissions> map = new HashMap<>();

    /**
     * If a player breaks a block with an item type that was not
     * registered in the map above, it will use this permission set instead.
     */
    @Nullable
    private ToolPermissions defaultPermissions;

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore) {
            map.clear();
            defaultPermissions = null;
        }

        FileConfiguration config = new ConfigFile("restrictions").getConfig();
        for (String key : config.getKeys(false))
            try {
                register(new ToolPermissions(config.getConfigurationSection(key)));
            } catch (IllegalArgumentException exception) {
                MMOCore.log(Level.WARNING, "Could not load perm set '" + key + "': " + exception.getMessage());
            }

        for (ToolPermissions perms : map.values())
            try {
                perms.postLoad();
            } catch (IllegalArgumentException exception) {
                MMOCore.log(Level.WARNING, "Could not post-load perm set '" + perms.getTool().display() + "': " + exception.getMessage());
            }
    }

    public void register(ToolPermissions perms) {
        map.put(perms.getTool().display(), perms);

        if (perms.isDefault()) {
            Validate.isTrue(defaultPermissions == null, "There is already a default tool permission set");
            defaultPermissions = perms;
        }
    }

    /**
     * Uses a hashMap O(1) check to find permission set of given item
     *
     * @param item The item used to break a block
     * @return A list of all the blocks an item is allowed to break.
     *         If it was not registered earlier, it returns the default permission
     *         set. If there is no default permission set, returns null
     */
    @Nullable
    public ToolPermissions getPermissions(ItemStack item) {
        String mapKey = ItemType.fromItemStack(item).display();
        ToolPermissions found = map.get(mapKey);
        return found == null ? defaultPermissions : found;
    }

    /**
     * @param item The item used to break a block
     * @return If the block can be broken by a certain item
     */
    public boolean checkPermissions(ItemStack item, BlockType block) {
        ToolPermissions perms = getPermissions(item);
        return perms != null && perms.canMine(block);
    }

    public class ToolPermissions extends PostLoadObject {

        /**
         * Now saving string keys using {@link BlockType#generateKey()} instead
         * of iterating through the set to take advantage of the O(1) time
         * complexity of hash sets.
         */
        private final Set<String> mineable = new HashSet<>();

        private final ItemType tool;
        private final boolean defaultSet;

        private ToolPermissions parent;

        public ToolPermissions(ConfigurationSection config) {
            super(config);

            tool = ItemType.fromString(config.getName());
            defaultSet = config.getBoolean("default");
        }

        @Override
        protected void whenPostLoaded(ConfigurationSection config) {
            if (config.contains("parent")) {
                String parentFormat = formatId(config.getString("parent"));
                parent = Objects.requireNonNull(map.get(parentFormat), "Could not find parent with ID '" + parentFormat + "'");
            }
            if (config.contains("can-mine"))
                for (String key : config.getStringList("can-mine"))
                    mineable.add(MMOCore.plugin.loadManager.loadBlockType(new MMOLineConfig(key)).generateKey());
        }

        /**
         * Recursively checks if a player can break the
         * given block by exploring the parent permissions tree.
         * <p>
         * Uses hash sets which provide O(1) time complexity for checkups
         * instead of iterating through the entire set
         *
         * @param type Block being broken
         * @return If the given block can be broken
         */
        public boolean canMine(BlockType type) {
            ToolPermissions parent;
            return mineable.contains(type.generateKey()) || ((parent = getParent()) != null && parent.canMine(type));
        }

        /**
         * @return Either parent if provided or default tool permission set if this is
         *         not the default set already, or null otherwise.
         */
        public ToolPermissions getParent() {
            return parent != null ? parent : defaultSet ? null : defaultPermissions;
        }

        public ItemType getTool() {
            return tool;
        }

        public boolean isDefault() {
            return defaultSet;
        }
    }

    private String formatId(String str) {
        return UtilityMethods.enumName(str.replace("?", ".").replace("%", "."));
    }
}
