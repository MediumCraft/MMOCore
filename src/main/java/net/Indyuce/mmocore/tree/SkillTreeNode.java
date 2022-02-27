package net.Indyuce.mmocore.tree;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.util.configobject.ConfigSectionObject;
import net.Indyuce.mmocore.player.Unlockable;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

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

    @Override
    public String getUnlockNamespacedKey() {
        return "skill_tree:" + tree.getId() + "_" + coordinates.getX() + "_" + coordinates.getY();
    }
}
