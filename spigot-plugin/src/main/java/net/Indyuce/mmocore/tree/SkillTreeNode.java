package net.Indyuce.mmocore.tree;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.util.configobject.ConfigSectionObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.player.Unlockable;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SkillTreeNode implements Unlockable {
    private final SkillTree tree;
    private final String name;
    private final IntegerCoordinates coordinates;
    private final List<String> lore;
    private final Set<PlayerModifier> modifiers = new HashSet<>();

    public SkillTreeNode(SkillTree tree, int x, int y, ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null");
        this.tree = tree;
        name = Objects.requireNonNull(config.getString("name"), "Could not find node name");
        coordinates = new IntegerCoordinates(x, y);
        lore = config.getStringList("lore");
        for (String key : config.getConfigurationSection("modifiers").getKeys(false)) {
            PlayerModifier mod = MythicLib.plugin.getModifiers().loadPlayerModifier(new ConfigSectionObject(config.getConfigurationSection(key)));
            modifiers.add(mod);
        }
    }

    public String getName() {
        return name;
    }

    public IntegerCoordinates getCoordinates() {
        return coordinates;
    }

    public Set<PlayerModifier> getModifiers() {
        return modifiers;
    }

    /**
     * @return Uncolored lore with no placeholders
     */
    public List<String> getLore() {
        return lore;
    }

    @Override
    public String getUnlockNamespacedKey() {
        return "skill_tree:" + tree.getId() + "_" + coordinates.getX() + "_" + coordinates.getY();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillTreeNode that = (SkillTreeNode) o;
        return tree.equals(that.tree) && coordinates.equals(that.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tree, coordinates);
    }

    /**
     * @param namespacedKey Something like "skill_tree:tree_name_1_5"
     * @return The corresponding skill tree node
     * @throws RuntimeException If the string cannot be parsed, if the specified
     *                          skill tree does not exist or if the skill tree has no such node
     */
    @NotNull
    public static SkillTreeNode getFromNamespacedKey(String namespacedKey) {
        String[] split = namespacedKey.substring(11).split("_");
        int n = split.length;

        IntegerCoordinates coords = new IntegerCoordinates(Integer.valueOf(split[n - 2]), Integer.valueOf(split[n - 1]));
        StringBuilder treeIdBuilder = new StringBuilder();
        for (int i = 0; i < n - 2; i++) {
            if (i > 0)
                treeIdBuilder.append("_");
            treeIdBuilder.append(split[i]);
        }
        String treeId = treeIdBuilder.toString();
        return MMOCore.plugin.skillTreeManager.get(treeId).getNode(coords);
    }
}
