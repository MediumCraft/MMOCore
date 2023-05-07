package net.Indyuce.mmocore.skilltree.tree;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.PostLoadObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.registry.RegisteredObject;
import net.Indyuce.mmocore.skilltree.*;
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
 * @author Ka0rX
 * @see {@link SkillTreeNode}
 */
public abstract class SkillTree extends PostLoadObject implements RegisteredObject {
    private final String id, name;
    private final List<String> lore = new ArrayList<>();
    private final Material item;
    private final int customModelData;

    //2 different maps to get the nodes
    /**
     * Represents all the nodes
     * Key: the coordinates of the node
     * Value: the node
     */
    protected final Map<IntegerCoordinates, SkillTreeNode> coordinatesNodes = new HashMap<>();
    /**
     * Represents all the paths between nodes.
     */
    protected final Map<IntegerCoordinates, SkillTreePath> coordinatesPaths = new HashMap<>();

    protected final Map<String, SkillTreeNode> nodes = new HashMap<>();
    protected final int maxPointSpent;
    //Caches the height of the skill tree
    protected final List<SkillTreeNode> roots = new ArrayList<>();

    public SkillTree(ConfigurationSection config) {
        super(config);

        this.id = Objects.requireNonNull(config.getString("id"), "Could not find skill tree id");
        this.name = MythicLib.plugin.parseColors(Objects.requireNonNull(config.getString("name"), "Could not find skill tree name"));
        Objects.requireNonNull(config.getStringList("lore"), "Could not find skill tree lore").forEach(str -> lore.add(MythicLib.plugin.parseColors(str)));
        this.item = Material.valueOf(UtilityMethods.enumName(Objects.requireNonNull(config.getString("item"))));
        this.customModelData = config.getInt("custom-model-data", 0);
        Validate.isTrue(config.isConfigurationSection("nodes"), "Could not find any nodes in the tree");
        this.maxPointSpent = config.getInt("max-point-spent", Integer.MAX_VALUE);
        for (String key : config.getConfigurationSection("nodes").getKeys(false)) {
            try {
                ConfigurationSection section = config.getConfigurationSection("nodes." + key);
                SkillTreeNode node = new SkillTreeNode(this, section);
                nodes.put(node.getId(), node);
            } catch (Exception e) {
                MMOCore.log("Couldn't load skill tree node " + id + "." + key + ": " + e.getMessage());
            }
        }
        for (String from : config.getConfigurationSection("nodes").getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection("nodes." + from);
            if (section.contains("paths")) {
                for (String to : section.getConfigurationSection("paths").getKeys(false)) {
                    SkillTreeNode node1 = nodes.get(to);
                    if (node1 == null) {
                        MMOCore.log("Couldn't find node " + to + " for path in node " + from + ".");
                        continue;
                    }
                    for (String pathKey : section.getConfigurationSection("paths." + to).getKeys(false)) {
                        IntegerCoordinates coordinates = new IntegerCoordinates(section.getInt("paths." + to + "." + pathKey + ".x"), section.getInt("paths." + to + "." + pathKey + ".y"));
                        coordinatesPaths.put(coordinates, new SkillTreePath(this, coordinates, nodes.get(from), node1));
                    }
                }
            }

        }
    }

    /**
     * Used to setup everything related to coordinates when each node has its coordinates loaded.
     */
    public void coordinatesSetup() {
        for (SkillTreeNode node : nodes.values()) {
            coordinatesNodes.put(node.getCoordinates(), node);
            if (node.isRoot())
                roots.add(node);
        }
    }

    @Override
    protected abstract void whenPostLoaded(@NotNull ConfigurationSection configurationSection);

    public List<String> getLore() {
        return lore;
    }

    public int getMaxPointSpent() {
        return maxPointSpent;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public static SkillTree loadSkillTree(ConfigurationSection config) {
        SkillTree skillTree = null;

        try {
            String string = config.getString("type");
            Validate.notNull(string, "You must precise a type for the skill tree.");
            Validate.isTrue(string.equals("linked") || string.equals("custom"), "You must precise the type of the skill tree in the yml!" +
                    "\nAllowed values: 'linked','custom'");

            if (string.equals("linked")) {
                skillTree = new LinkedSkillTree(config);
                skillTree.postLoad();
            }
            if (string.equals("custom")) {
                skillTree = new CustomSkillTree(config);
                skillTree.postLoad();
            }
        } catch (Exception e) {
            MMOCore.log("Couldn't load skill tree " + config.getString("id") + ": " + e.getMessage());
        }
        return skillTree;
    }

    public void addRoot(SkillTreeNode node) {
        roots.add(node);
    }

    /**
     * Recursively go through the skill trees to update the the node states
     */
    public void setupNodeStates(PlayerData playerData) {
        for (SkillTreeNode root : roots)
            setupNodeStateFrom(root, playerData);
    }

    public List<SkillTreeNode> getRoots() {
        return roots;
    }

    /**
     * Update recursively the state of all the nodes that are
     * children of this node (Used when we change the state of a node)
     */
    private void setupNodeStateFrom(SkillTreeNode node, PlayerData playerData) {
        if (playerData.getNodeLevel(node) > 0) {
            playerData.setNodeState(node, NodeStatus.UNLOCKED);
        } else if (playerData.getNodeLevel(node) == 0 && node.isRoot()) {
            playerData.setNodeState(node, NodeStatus.UNLOCKABLE);
        } else {
            Set<SkillTreeNode> strongParents = node.getParents(ParentType.STRONG);
            Set<SkillTreeNode> softParents = node.getParents(ParentType.SOFT);
            Set<SkillTreeNode> incompatibleParents = node.getParents(ParentType.INCOMPATIBLE);

            boolean isUnlockableFromStrongParent = true;
            boolean isUnlockableFromSoftParent = softParents.size() == 0;
            boolean isFullyLockedFromStrongParent = false;
            boolean isFullyLockedFromSoftParent = softParents.size() != 0;
            boolean isFullyLockedFromIncompatibleParent = false;

            for (SkillTreeNode strongParent : strongParents) {
                if (playerData.getNodeLevel(strongParent) < node.getParentNeededLevel(strongParent, ParentType.STRONG)) {
                    isUnlockableFromStrongParent = false;
                }
                //We count the number of children the parent
                int numberChildren = 0;
                for (SkillTreeNode child : strongParent.getChildren())
                    if (playerData.getNodeLevel(child) > 0)
                        numberChildren++;

                //We must check if the parent is Fully Locked or not and if it can unlock a new node(with its max children constraint)
                if (numberChildren >= strongParent.getMaxChildren() || playerData.getNodeStatus(strongParent) == NodeStatus.FULLY_LOCKED)
                    isFullyLockedFromStrongParent = true;
            }


            for (SkillTreeNode softParent : node.getParents(ParentType.SOFT)) {
                if (playerData.getNodeLevel(softParent) >= node.getParentNeededLevel(softParent, ParentType.SOFT)) {
                    isUnlockableFromSoftParent = true;
                }
                //We count the number of children the parent has
                int numberChildren = 0;
                for (SkillTreeNode child : softParent.getChildren())
                    if (playerData.getNodeLevel(child) > 0)
                        numberChildren++;
                if (numberChildren < softParent.getMaxChildren() && playerData.getNodeStatus(softParent) != NodeStatus.FULLY_LOCKED)
                    isFullyLockedFromSoftParent = false;
            }
            for (SkillTreeNode incompatibleParent : node.getParents(ParentType.INCOMPATIBLE)) {
                if (playerData.getNodeLevel(incompatibleParent) > 0) {
                    isFullyLockedFromIncompatibleParent = true;
                    break;
                }
            }

            boolean isFullyLocked = isFullyLockedFromSoftParent || isFullyLockedFromStrongParent || isFullyLockedFromIncompatibleParent;
            boolean isUnlockable = isUnlockableFromSoftParent && isUnlockableFromStrongParent;
            if (isFullyLocked)
                playerData.setNodeState(node, NodeStatus.FULLY_LOCKED);
            else if (isUnlockable)
                playerData.setNodeState(node, NodeStatus.UNLOCKABLE);
            else
                playerData.setNodeState(node, NodeStatus.LOCKED);
        }
        //We recursively call the algorithm for all the children of the current node
        for (SkillTreeNode child : node.getChildren())
            setupNodeStateFrom(child, playerData);

    }

    /**
     * Returns null if it is not a node and returns the node type if it a node
     */
    @Nullable
    public boolean isNode(IntegerCoordinates coordinates) {
        for (SkillTreeNode node : nodes.values()) {
            if (node.getCoordinates().equals(coordinates))
                return true;
        }
        return false;
    }

    public boolean isPath(IntegerCoordinates coordinates) {
        return coordinatesPaths.keySet().contains(coordinates);
    }

    public Material getItem() {
        return item;
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
        return Objects.requireNonNull(coordinatesNodes.get(coords), "Could not find node in tree '" + id + "' with coordinates '" + coords.toString() + "'");
    }

    @NotNull
    public SkillTreePath getPath(IntegerCoordinates coords) {
        return Objects.requireNonNull(coordinatesPaths.get(coords), "Could not find path in tree '" + id + "' with coordinates '" + coords.toString() + "'");
    }

    @NotNull
    public SkillTreeNode getNode(String name) {
        return Objects.requireNonNull(nodes.get(name), "Could not find node in tree '" + id + "' with name '" + name + "'");
    }


    public boolean isNode(String name) {
        return nodes.containsKey(name);
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
