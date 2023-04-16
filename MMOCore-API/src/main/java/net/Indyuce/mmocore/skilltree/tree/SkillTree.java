package net.Indyuce.mmocore.skilltree.tree;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.PostLoadObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.registry.RegisteredObject;
import net.Indyuce.mmocore.skilltree.tree.display.DisplayInfo;
import net.Indyuce.mmocore.skilltree.tree.display.Icon;
import net.Indyuce.mmocore.skilltree.IntegerCoordinates;
import net.Indyuce.mmocore.skilltree.NodeStatus;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
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
 * @see {@link SkillTreeNode}
 */
public abstract class SkillTree extends PostLoadObject implements RegisteredObject {
    private final String id, name;
    private final List<String> lore = new ArrayList<>();
    private final Material item;
    private final int customModelData;

    //2 different maps to get the nodes

    //Represents all the coordinates that will be displayed as a path (between 2 nodes of the tree)
    protected final ArrayList<IntegerCoordinates> paths = new ArrayList<>();
    //Represents all the nodes
    protected final Map<IntegerCoordinates, SkillTreeNode> coordinatesNodes = new HashMap<>();
    protected final Map<String, SkillTreeNode> nodes = new HashMap<>();
    protected final int maxPointSpent;
    //Caches the height of the skill tree
    protected int minX, minY, maxX, maxY;
    protected final HashMap<DisplayInfo, Icon> icons = new HashMap<>();
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

                SkillTreeNode node = new SkillTreeNode(this, config.getConfigurationSection("nodes." + key));
                nodes.put(node.getId(), node);

            } catch (Exception e) {
                MMOCore.log("Couldn't load skill tree node " + id + "." + key + ": " + e.getMessage());
            }
        }
        try {
            if (config.contains("paths")) {
                ConfigurationSection section = config.getConfigurationSection("paths");
                for (String key : section.getKeys(false)) {
                    if (section.contains(key + ".x") && section.contains(key + ".y")) {
                        paths.add(new IntegerCoordinates(section.getInt(key + ".x"), section.getInt(key + ".y")));
                    }

                }
            }
        } catch (Exception e) {
            MMOCore.log(Level.WARNING, "Couldn't load paths for skill tree: " + id);
        }

        try {
            //Load the icons of the skill tree.
            for (String key : config.getConfigurationSection("icons").getKeys(false)) {
                if (key.equalsIgnoreCase("path")) {
                    icons.put(DisplayInfo.pathInfo, new Icon(config.getConfigurationSection("icons." + key)));
                    continue;
                }
                for (String size : config.getConfigurationSection("icons." + key).getKeys(false)) {
                    DisplayInfo displayInfo = new DisplayInfo(NodeStatus.valueOf(UtilityMethods.enumName(key)), Integer.parseInt(size));
                    Icon icon = new Icon(config.getConfigurationSection("icons." + key + "." + size));
                    icons.put(displayInfo, icon);

                }
            }
        } catch (Exception e) {
            MMOCore.log("Couldn't load icons for the skill tree " + id);
            e.printStackTrace();
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

    public Icon getIcon(DisplayInfo info) {
        Validate.isTrue(icons.containsKey(info), "The icon corresponding to " + info + " doesn't exist for the skill tree " + id + ".");
        return icons.get(info);
    }

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
            boolean isUnlockableFromStrongParent = node.getStrongParents().size() == 0 ? true : true;
            boolean isUnlockableFromSoftParent = node.getSoftParents().size() == 0 ? true : false;
            boolean isFullyLockedFromStrongParent = node.getStrongParents().size() == 0 ? false : false;
            boolean isFullyLockedFromSoftParent = node.getSoftParents().size() == 0 ? false : true;

            for (SkillTreeNode strongParent : node.getStrongParents()) {
                if (playerData.getNodeLevel(strongParent) < node.getParentNeededLevel(strongParent)) {
                    isUnlockableFromStrongParent = false;
                }
                //We count the number of children the parent
                int numberChildren = 0;
                for (SkillTreeNode child : strongParent.getChildren())
                    if (playerData.getNodeLevel(child) > 0)
                        numberChildren++;

                //We must check if the parent is Fully Locked or not and if it can unlock a new node(with its max children constraint)
                if (numberChildren >= strongParent.getMaxChildren() || playerData.getNodeState(strongParent) == NodeStatus.FULLY_LOCKED)
                    isFullyLockedFromStrongParent = true;
            }


            for (SkillTreeNode softParent : node.getSoftParents()) {
                if (playerData.getNodeLevel(softParent) >= node.getParentNeededLevel(softParent)) {
                    isUnlockableFromSoftParent = true;
                }
                //We count the number of children the parent
                int numberChildren = 0;
                for (SkillTreeNode child : softParent.getChildren())
                    if (playerData.getNodeLevel(child) > 0)
                        numberChildren++;
                if (numberChildren < softParent.getMaxChildren() && playerData.getNodeState(softParent) != NodeStatus.FULLY_LOCKED)
                    isFullyLockedFromSoftParent = false;
            }

            boolean isFullyLocked = isFullyLockedFromSoftParent || isFullyLockedFromStrongParent;
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
        return paths.contains(coordinates);
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
