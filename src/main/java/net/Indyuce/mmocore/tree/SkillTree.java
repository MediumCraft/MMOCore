package net.Indyuce.mmocore.tree;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.manager.registry.RegisterObject;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

/**
 * A passive skill tree that features nodes, or passive skills.
 * <p>
 * The player can explore the passive skill tree using the right GUI
 * and unlock nodes by spending passive skill points. Unlocking nodes
 * grant permanent player modifiers, including
 * - stats
 * - active or passive MythicLib skills
 * - active or passive MMOCore skills
 * - extra attribute pts
 * - particle or potion effects
 *
 * @author jules
 * @see {@link SkillTreeNode}
 */
public class SkillTree implements RegisterObject {
    private final String id, name;
    private final Map<IntegerCoordinates, SkillTreeNode> nodes = new HashMap<>();

    public SkillTree(ConfigurationSection config) {
        this.id = config.getName();
        this.name = Objects.requireNonNull(config.getString("name"), "Could not find skill tree name");
        Validate.isTrue(config.isConfigurationSection("nodes"), "Could not find tree passive skills");
        for (String xKey : config.getConfigurationSection("nodes").getKeys(false))
            for (String yKey : config.getConfigurationSection("nodes." + xKey).getKeys(false))
                try {
                    int x = Integer.parseInt(xKey), y = Integer.parseInt(yKey);
                    SkillTreeNode node = new SkillTreeNode(this, x, y, config.getConfigurationSection("nodes." + xKey + "." + yKey));
                    nodes.put(node.getCoordinates(), node);
                } catch (RuntimeException exception) {
                    MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load tree node '" + xKey + "." + yKey + "' for skill tree '" + id + "': " + exception.getMessage());
                }
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Collection<SkillTreeNode> getNodes() {
        return nodes.values();
    }

    @NotNull
    public SkillTreeNode getNode(IntegerCoordinates coords) {
        return Objects.requireNonNull(nodes.get(coords), "Could not find node in tree '" + id + "' with coordinates '" + coords.toString() + "'");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillTree skillTree = (SkillTree) o;
        return id.equals(skillTree.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
