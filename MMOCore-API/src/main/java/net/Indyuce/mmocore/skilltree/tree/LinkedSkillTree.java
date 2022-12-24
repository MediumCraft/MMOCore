package net.Indyuce.mmocore.skilltree.tree;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skilltree.IntegerCoordinates;
import net.Indyuce.mmocore.skilltree.NodeStatus;
import net.Indyuce.mmocore.skilltree.ParentType;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * For linked skillTrees there is no notion of children and
 * parents you must have some neighbours unlocked in order to
 * be unlockable. All the relationships in the tree are
 * defined by the coordinates you nodes have.
 */
public class LinkedSkillTree extends SkillTree {
    public LinkedSkillTree(ConfigurationSection config) {
        super(config);

        // Setup the coordinate map because coordinates are given in the yml for linked skill tree
        coordinatesSetup();
    }

    @Override
    protected void whenPostLoaded(@NotNull ConfigurationSection config) {
        // Setup the children and parents if a node precise a required level for upgrade.
        // If it is not filled the algorithm will put the required level to 1
        for (SkillTreeNode node : nodes.values()) {
            if (config.contains(node.getId() + ".children")) {
                ConfigurationSection section = config.getConfigurationSection(node.getId() + ".children.soft");
                if (section != null) {
                    for (String child : section.getKeys(false)) {
                        node.addChild(getNode(child));
                        getNode(child).addParent(node, section.getInt(child), ParentType.SOFT);
                    }
                }
                section = config.getConfigurationSection(node.getId() + ".children.strong");
                if (section != null) {
                    for (String child : section.getKeys(false)) {
                        node.addChild(getNode(child));
                        getNode(child).addParent(node, section.getInt(child), ParentType.STRONG);
                    }
                }

            }
        }

        SkillTreeNode root = getNode(new IntegerCoordinates(0, 0));
        Validate.notNull(root, "Their must be a node(the root of the tree) at the coordinates (0,0) ");
    }

    @Override
    public void setupNodeStates(PlayerData playerData) {

        // Values are labelled as unlockable
        for (SkillTreeNode root : roots)
            playerData.setNodeState(root, NodeStatus.UNLOCKABLE);

        // All the nodes with level >0 are unlocked
        for (SkillTreeNode node : nodes.values()) {
            if (playerData.getNodeLevel(node) > 0)
                playerData.setNodeState(node, NodeStatus.UNLOCKED);
        }
        // Setup unlockable nodes
        for (SkillTreeNode node : nodes.values()) {
            if (isUnlockable(node, playerData) && !playerData.hasNodeState(node))
                playerData.setNodeState(node, NodeStatus.UNLOCKABLE);
        }

        labelLockedNodes(playerData);

        // Label all the remaining nodes to FULLY LOCKED
        for (SkillTreeNode node : nodes.values())
            if (!playerData.hasNodeState(node))
                playerData.setNodeState(node, NodeStatus.FULLY_LOCKED);
    }

    /**
     * We recursively label all the locked nodes who are connected to an unlockable node.
     **/
    private void labelLockedNodes(PlayerData playerData) {
        List<SkillTreeNode> unlockableNodes = nodes.values().stream().filter(node -> playerData.getNodeState(node) == NodeStatus.UNLOCKABLE).toList();
        for (SkillTreeNode node : unlockableNodes) {
            labelLockedNodesFrom(playerData, node);
        }
    }

    private void labelLockedNodesFrom(PlayerData data, SkillTreeNode node) {
        for (IntegerCoordinates coor : getCheckCoordinates(node.getCoordinates())) {
            if (isNode(coor) && !data.hasNodeState(getNode(coor))) {
                data.setNodeState(getNode(coor), NodeStatus.LOCKED);
                labelLockedNodesFrom(data, getNode(coor));
            }
        }
    }

    private List<IntegerCoordinates> getCheckCoordinates(IntegerCoordinates coor) {
        return Arrays.asList(new IntegerCoordinates(coor.getX() + 1, coor.getY()),
                new IntegerCoordinates(coor.getX() - 1, coor.getY()), new IntegerCoordinates(coor.getX(), coor.getY() + 1), new IntegerCoordinates(coor.getX(), coor.getY() - 1));
    }

    private boolean isUnlockable(SkillTreeNode node, PlayerData playerData) {

        boolean isUnlockable = false;
        for (IntegerCoordinates coordinates : getCheckCoordinates(node.getCoordinates())) {
            if (isNode(coordinates))
                if (isNode(coordinates) && playerData.getNodeState(getNode(coordinates)) == NodeStatus.UNLOCKED && countUnlockedNeighbours(coordinates, playerData) <= getNode(coordinates).getMaxChildren())
                    isUnlockable = true;
        }
        return isUnlockable;
    }

    /**
     * Counts the number of unlocked neighbours of a node for a certain playerData
     **/
    private int countUnlockedNeighbours(IntegerCoordinates coor, PlayerData playerData) {
        int number = 0;
        for (IntegerCoordinates coordinates : getCheckCoordinates(coor)) {
            if (isNode(coordinates) && playerData.getNodeLevel(getNode(coordinates)) > 0)
                number++;
        }
        return number;
    }
}
