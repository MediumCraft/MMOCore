package net.Indyuce.mmocore.skilltree.tree;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.manager.registry.RegisteredObject;
import net.Indyuce.mmocore.skilltree.*;
import net.Indyuce.mmocore.skilltree.display.DisplayInfo;
import net.Indyuce.mmocore.skilltree.display.NodeDisplayInfo;
import net.Indyuce.mmocore.skilltree.display.PathDisplayInfo;
import net.Indyuce.mmocore.util.Icon;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
 * @author Ka0rX
 * @see SkillTreeNode
 */
public abstract class SkillTree implements RegisteredObject {
    private final String id, name;
    private final List<String> lore = new ArrayList<>();
    private final Material item;
    private final int customModelData;
    protected final Map<String, SkillTreeNode> nodes = new HashMap<>();
    protected final int maxPointSpent;
    protected final List<SkillTreeNode> roots = new ArrayList<>();
    protected final Map<DisplayInfo, Icon> icons = new HashMap<>();

    protected final Map<IntegerCoordinates, SkillTreeNode> coordNodes = new HashMap<>();
    protected final Map<IntegerCoordinates, SkillTreePath> coordPaths = new HashMap<>();

    public SkillTree(@NotNull ConfigurationSection config) {
        this.id = Objects.requireNonNull(config.getString("id"), "Could not find skill tree id");
        this.name = MythicLib.plugin.parseColors(Objects.requireNonNull(config.getString("name"), "Could not find skill tree name"));
        Objects.requireNonNull(config.getStringList("lore"), "Could not find skill tree lore").forEach(str -> lore.add(MythicLib.plugin.parseColors(str)));
        this.item = Material.valueOf(UtilityMethods.enumName(Objects.requireNonNull(config.getString("item"), "Could not find item")));
        this.customModelData = config.getInt("custom-model-data", 0);
        Validate.isTrue(config.isConfigurationSection("nodes"), "Could not find any nodes in the tree");
        this.maxPointSpent = config.getInt("max-point-spent", Integer.MAX_VALUE);

        // Load nodes
        for (String key : config.getConfigurationSection("nodes").getKeys(false))
            try {
                ConfigurationSection section = config.getConfigurationSection("nodes." + key);
                SkillTreeNode node = new SkillTreeNode(this, section);
                nodes.put(node.getId(), node);
                coordNodes.put(node.getCoordinates(), node);

                if (node.isRoot()) roots.add(node);
            } catch (Exception e) {
                MMOCore.log("Couldn't load skill tree node " + id + "." + key + ": " + e.getMessage());
            }

        // Load paths
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
                        IntegerCoordinates coordinates = IntegerCoordinates.from(section.get("paths." + to + "." + pathKey));
                        coordPaths.put(coordinates, new SkillTreePath(this, coordinates, nodes.get(from), node1));
                    }
                }
            }
        }

        // Loads all the pathDisplayInfo
        for (NodeState status : NodeState.values())
            for (PathType pathType : PathType.values())
                try {
                    final String configPath = "display.paths." + MMOCoreUtils.ymlName(status.name()) + "." + MMOCoreUtils.ymlName(pathType.name());
                    icons.put(new PathDisplayInfo(pathType, status), Icon.from(config.get(configPath)));
                } catch (Exception exception) {
                    // Ignore
                }

        // Loads all the nodeDisplayInfo
        for (NodeState status : NodeState.values())
            for (NodeType nodeType : NodeType.values())
                try {
                    final String configPath = "display.nodes." + MMOCoreUtils.ymlName(status.name()) + "." + MMOCoreUtils.ymlName(nodeType.name());
                    icons.put(new NodeDisplayInfo(nodeType, status), Icon.from(config.get(configPath)));
                } catch (Exception exception) {
                    // Ignore
                }

        // Setup children and parents for each node
        for (SkillTreeNode node : nodes.values())
            try {
                if (config.isConfigurationSection("nodes." + node.getId() + ".parents"))
                    for (String key : config.getConfigurationSection("nodes." + node.getId() + ".parents").getKeys(false)) {
                        final ConfigurationSection section = config.getConfigurationSection("nodes." + node.getId() + ".parents." + key);
                        if (section != null) {
                            final ParentType parentType = ParentType.valueOf(UtilityMethods.enumName(key));

                            for (String parentId : section.getKeys(false)) {
                                final SkillTreeNode parent = getNode(parentId);
                                final int level = section.getInt(parentId);
                                node.addParent(parent, parentType, level);
                                parent.addChild(node, parentType, level);
                            }
                        }
                    }
            } catch (RuntimeException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load parents of skill tree node '" + node.getId() + "': " + exception.getMessage());
            }
    }

    public List<String> getLore() {
        return lore;
    }

    public int getMaxPointSpent() {
        return maxPointSpent;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    @Deprecated
    public static SkillTree loadSkillTree(ConfigurationSection config) {
        return MMOCore.plugin.skillTreeManager.loadSkillTree(config);
    }

    public void addRoot(@NotNull SkillTreeNode node) {
        roots.add(node);
    }

    @NotNull
    public List<SkillTreeNode> getRoots() {
        return roots;
    }

    /**
     * TODO Write documentation
     * TODO Use some collection and progressively filter out the nodes to avoid useless iterations
     * <p>
     * Let:
     * - V denote the number of nodes in the skill tree
     * - P the number of parents any node, has at most
     * - C the number of children any node has, at most
     * <p>
     * This algorithm runs in O(V * P * C)
     */
    public void setupNodeStates(@NotNull PlayerData playerData) {

        // Reinitialization
        playerData.clearNodeStates(this);

        // If the player has already spent the maximum amount of points in this skill tree.
        final boolean skillTreeLocked = playerData.getPointsSpent(this) >= this.maxPointSpent;
        final NodeState lockState = skillTreeLocked ? NodeState.FULLY_LOCKED : NodeState.LOCKED;

        // PASS 1
        //
        // Initialization. Mark all nodes either locked or unlocked
        for (SkillTreeNode node : nodes.values())
            playerData.setNodeState(node, playerData.getNodeLevel(node) > 0 ? NodeState.UNLOCKED : lockState);

        if (skillTreeLocked) return;

        // PASS 2
        //
        // Apply basic unreachability rules in O(V * [C + P])
        // It has to differ from pass 1 because it uses results from pass 1.
        final Stack<SkillTreeNode> unreachable = new Stack<>();
        final Set<SkillTreeNode> updated = new HashSet<>();

        for (SkillTreeNode node : nodes.values()) {

            // INCOMPATIBILITY RULES
            //
            // Any node with an unlocked incompatible parent is made unreachable.
            for (ParentInformation parent : node.getParents())
                if (parent.getType() == ParentType.INCOMPATIBLE && playerData.getNodeState(parent.getNode()) == NodeState.UNLOCKED) {
                    unreachable.add(node);
                    break;
                }

            // MAX CHILDREN RULE
            //
            // If a node has N total children and M <= N are already unlocked,
            // the remaining N - M are made unreachable.
            final int maxChildren = node.getMaxChildren();
            if (maxChildren > 0) {

                int unlocked = 0;
                final List<SkillTreeNode> locked = new ArrayList<>();

                for (ParentInformation child : node.getChildren())
                    switch (playerData.getNodeState(child.getNode())) {
                        case LOCKED:
                            locked.add(child.getNode());
                            break;
                        case UNLOCKED:
                            unlocked++;
                            break;
                    }

                if (unlocked >= maxChildren) unreachable.addAll(locked);
            }
        }

        // PASS 3
        //
        // Propagate unreachability in O(V * C * P)
        while (!unreachable.empty()) {
            final SkillTreeNode node = unreachable.pop();

            updated.add(node);
            playerData.setNodeState(node, NodeState.FULLY_LOCKED);
            for (ParentInformation child : node.getChildren()) // Propagate
                if (!updated.contains(child.getNode()) && isUnreachable(child.getNode(), playerData))
                    unreachable.push(child.getNode());
        }

        // PASS 4
        //
        // Mark unlockable nodes, in O(V * P). This rule does not need propagation
        // because the distance between the set of all unlocked nodes and the set
        // of all unlockable nodes is at most 1 (unlockability is not "transitive")
        pass4:
        for (SkillTreeNode node : nodes.values()) {
            if (playerData.getNodeState(node) != NodeState.LOCKED) continue;

            // ROOT NODES
            //
            // Roots are either unlockable or unlocked.
            if (node.isRoot()) {
                playerData.setNodeState(node, NodeState.UNLOCKABLE);
                continue;
            }

            // STRONG & SOFT PARENTS
            //
            // For nodes with no strong/soft parents, the rule is nulled.
            // All strong parents of any node must be unlocked for the node to be unlockable.
            // One soft parent of any node must be unlocked for the node to be unlockable.
            boolean soft = false, hasSoft = false;

            for (ParentInformation parent : node.getParents()) {
                if (parent.getType() == ParentType.STRONG && playerData.getNodeLevel(parent.getNode()) < parent.getLevel())
                    continue pass4; // Keep the node locked
                else if (!soft && parent.getType() == ParentType.SOFT) {
                    hasSoft = true;
                    if (playerData.getNodeLevel(parent.getNode()) >= parent.getLevel())
                        soft = true; // Cannot continue, must check for other strong parents
                }
            }

            // At least one soft parent!
            if (!hasSoft || soft) playerData.setNodeState(node, NodeState.UNLOCKABLE);
        }
    }

    private boolean isUnreachable(@NotNull SkillTreeNode node, @NotNull PlayerData playerData) {

        // UNREACHABILITY RULES
        //
        // If at least one strong parent is unreachable, the node is unreachable too.
        // If all soft parents are unreachable, the node is unreachable.
        // This rule is the logical opposite of the reachability rule.
        boolean soft = false, hasSoft = false;

        for (ParentInformation parent : node.getParents()) {
            if (parent.getType() == ParentType.STRONG && playerData.getNodeState(parent.getNode()) == NodeState.FULLY_LOCKED)
                return true;
            else if (!soft && parent.getType() == ParentType.SOFT) {
                hasSoft = true;
                if (playerData.getNodeState(parent.getNode()) != NodeState.FULLY_LOCKED)
                    soft = true; // Cannot continue, must check for other strong parents
            }
        }

        return hasSoft && !soft;
    }

    public boolean isNode(@NotNull IntegerCoordinates coordinates) {
        return coordNodes.containsKey(coordinates);
    }

    public boolean isPath(@NotNull IntegerCoordinates coordinates) {
        return coordPaths.containsKey(coordinates);
    }

    public boolean isPathOrNode(IntegerCoordinates coordinates) {
        return isNode(coordinates) || isPath(coordinates);
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
    public SkillTreeNode getNode(@NotNull IntegerCoordinates coords) {
        return Objects.requireNonNull(coordNodes.get(coords), "Could not find node in tree '" + id + "' with coordinates '" + coords + "'");
    }

    @Nullable
    public SkillTreeNode getNodeOrNull(@NotNull IntegerCoordinates coords) {
        return coordNodes.get(coords);
    }

    @NotNull
    public SkillTreePath getPath(@NotNull IntegerCoordinates coords) {
        return Objects.requireNonNull(coordPaths.get(coords), "Could not find path in tree '" + id + "' with coordinates '" + coords + "'");
    }

    @NotNull
    public SkillTreeNode getNode(@NotNull String name) {
        return Objects.requireNonNull(nodes.get(name), "Could not find node in tree '" + id + "' with name '" + name + "'");
    }

    public boolean hasIcon(DisplayInfo displayInfo) {
        return icons.containsKey(displayInfo);
    }

    public Icon getIcon(DisplayInfo displayInfo) {
        return icons.get(displayInfo);
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
