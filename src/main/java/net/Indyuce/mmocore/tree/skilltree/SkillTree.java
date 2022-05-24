package net.Indyuce.mmocore.tree.skilltree;

import io.lumine.mythic.lib.api.util.PostLoadObject;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.manager.registry.RegisterObject;
import net.Indyuce.mmocore.tree.IntegerCoordinates;
import net.Indyuce.mmocore.tree.NodeState;
import net.Indyuce.mmocore.tree.SkillTreeNode;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
 * @author Ka0rX
 * @see {@link SkillTreeNode}
 */
public abstract class SkillTree extends PostLoadObject implements RegisterObject {
    private final String id, name;
    private final Material guiMaterial;
    //2 different maps to get the nodes
    protected final Map<IntegerCoordinates, SkillTreeNode> coordinatesNodes = new HashMap<>();
    protected final Map<String, SkillTreeNode> nodes = new HashMap<>();
    //Caches the height of the skill tree
    protected int minX, minY, maxX, maxY;

    public SkillTree(ConfigurationSection config) {
        super(config);
        this.id = Objects.requireNonNull(config.getString("id"), "Could not find skill tree id");
        this.name = Objects.requireNonNull(config.getString("name"), "Could not find skill tree name");
        this.guiMaterial = Material.valueOf(MMOCoreUtils.toEnumName(Objects.requireNonNull(config.getString("material"))));
        Validate.isTrue(config.isConfigurationSection("nodes"), "Could not find any nodes in the tree");
        for (String key : config.getConfigurationSection("nodes").getKeys(false)) {
            SkillTreeNode node = new SkillTreeNode(this, config.getConfigurationSection("nodes." + key));
        }
    }

    public void setupCoordinatesNodesMap() {
        for (SkillTreeNode node : nodes.values()) {
            coordinatesNodes.put(node.getCoordinates(), node);
        }
    }

    @Override
    protected abstract void whenPostLoaded(@NotNull ConfigurationSection configurationSection);

    public int getMaxX() {
        return maxX;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }


    public static SkillTree loadSkillTree(ConfigurationSection config) {
        String string = config.getString("type");

        Validate.isTrue(string.equals("automatic") || string.equals("linked") || string.equals("custom"), "You must precise the type of the skill tree in the yml!" +
                "\nAllowed values: 'automatic','linked','custom'");
        SkillTree skillTree = null;
        if (string.equals("automatic")) {
            skillTree = new AutomaticSkillTree(config);
            skillTree.postLoad();
        }
        if (string.equals("linked")) {
            skillTree = new LinkedSkillTree(config);
            skillTree.postLoad();
        }
        if (string.equals("custom")) {
            skillTree = new CustomSkillTree(config);
            skillTree.postLoad();
        }

        return skillTree;
    }

    @Nullable
    /**
     * Returns null if it is not a node and returns the node type if it a node
     */
    public boolean isNode(IntegerCoordinates coordinates) {
        for (SkillTreeNode node : nodes.values()) {
            if (node.getCoordinates().equals(coordinates))
                return true;
        }
        return false;
    }

    public abstract boolean isPath(IntegerCoordinates coordinates);

    public Material getGuiMaterial() {
        return guiMaterial;
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

    @NotNull
    public SkillTreeNode getNode(String name) {
        return Objects.requireNonNull(nodes.get(name), "Could not find node in tree '" + id + "' with name '" + name + "'");
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
