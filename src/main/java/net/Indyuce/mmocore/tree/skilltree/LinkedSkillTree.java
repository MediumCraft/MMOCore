package net.Indyuce.mmocore.tree.skilltree;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.tree.IntegerCoordinates;
import net.Indyuce.mmocore.tree.NodeState;
import net.Indyuce.mmocore.tree.ParentType;
import net.Indyuce.mmocore.tree.SkillTreeNode;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * For linked skillTrees there is no notion of children and parents you must have some neighbours unlocked in order to
 * be unlockable. All the relationships in the tree are defined by the coordinates you nodes have.
 */
public class LinkedSkillTree extends SkillTree {


    public LinkedSkillTree(ConfigurationSection config) {
        super(config);
        //We setup the coordinate map because coordinates are given in the yml for linked skill tree
        coordinatesSetup();
    }


    @Override
    protected void whenPostLoaded(@NotNull ConfigurationSection config) {
        //We setup the children and parents if a node precise a required level for upgrade.
        //If it is not filled the algorithm will put the required level to 1
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
    public void setupNodeStateFrom(SkillTreeNode node, PlayerData playerData) {

        int x = node.getCoordinates().getX();
        int y = node.getCoordinates().getY();
        List<IntegerCoordinates> checkCoordinates = Arrays.asList(new IntegerCoordinates(x + 1, y),
                new IntegerCoordinates(x - 1, y), new IntegerCoordinates(x, y + 1), new IntegerCoordinates(x, y - 1));
        if (playerData.getNodeLevel(node) > 0) {
            playerData.setNodeState(node, NodeState.UNLOCKED);
        } else if (playerData.getNodeLevel(node) == 0 && node.isRoot()) {
            playerData.setNodeState(node, NodeState.UNLOCKABLE);
        } else {
            boolean isUnlockable = false;
            for (IntegerCoordinates coordinates : checkCoordinates) {
                if (isNode(coordinates))
                    if (isNode(coordinates) && playerData.getNodeState(getNode(coordinates)) == NodeState.UNLOCKED && numberNeighbours(coordinates, playerData) <= getNode(coordinates).getMaxChildren())
                        isUnlockable = true;
            }
            if (isUnlockable)
                playerData.setNodeState(node, NodeState.UNLOCKABLE);
            else {
                List<SkillTreeNode> parents = new ArrayList<>();
                parents.add(node);
                boolean isFullyLocked = isFullyLockedFrom(node, parents, playerData);

                if (isFullyLocked)
                    playerData.setNodeState(node, NodeState.FULLY_LOCKED);
                else
                    playerData.setNodeState(node, NodeState.LOCKED);
            }

        }

        //We call the recursive algorithm on the rest of the points. Doesn't call the algorithm if already loaded.
        for (IntegerCoordinates coordinates : checkCoordinates) {
            if (isNode(coordinates) && !playerData.hasNodeState(getNode(coordinates)))
                setupNodeStateFrom(getNode(coordinates), playerData);
        }
    }


    //Counts the number of Unlocked Nieghbourgs of a node for a certain playerData
    private int numberNeighbours(IntegerCoordinates coor, PlayerData playerData) {
        int number = 0;
        int x = coor.getX();
        int y = coor.getY();
        List<IntegerCoordinates> checkCoordinates = Arrays.asList(new IntegerCoordinates(x + 1, y),
                new IntegerCoordinates(x - 1, y), new IntegerCoordinates(x, y + 1), new IntegerCoordinates(x, y - 1));
        for (IntegerCoordinates coordinates : checkCoordinates) {
            if (isNode(coordinates) && playerData.getNodeLevel(getNode(coordinates)) > 0)
                number++;
        }
        return number;
    }


    /**
     * A recursive algorithm to see if a node is fully locked or not in a linked skill tree
     */
    public boolean isFullyLockedFrom(SkillTreeNode current, List<SkillTreeNode> parents, PlayerData playerData) {
        if (!parents.contains(current) && (playerData.getNodeState(current) == NodeState.UNLOCKABLE || playerData.getNodeState(current) == NodeState.UNLOCKED)) {
            //If the node is unlocked either we say it is not fully locked if a path can be found either wer return true is not path can be found down this way
            if (numberNeighbours(current.getCoordinates(), playerData) <= getNode(current.getCoordinates()).getMaxChildren()) {
                return false;
            } else
                return true;
        }
        //We verify that the node is unlocked or unlockable and can have links the first node

        int x = current.getCoordinates().getX();
        int y = current.getCoordinates().getY();
        List<IntegerCoordinates> checkCoordinates = Arrays.asList(new IntegerCoordinates(x + 1, y),
                new IntegerCoordinates(x - 1, y), new IntegerCoordinates(x, y + 1), new IntegerCoordinates(x, y - 1));
        //We filter coordinates with only nodes that are not parents and not fully locked
        //We also need to have the number of neighbour <=max-child(max-child=1 -> can have 1 neighbour but if it has 2 it will make the other branches fully blocked
        checkCoordinates = checkCoordinates.stream().filter(coor -> isNode(coor)
                && playerData.getNodeState(getNode(coor)) != NodeState.FULLY_LOCKED
                && !parents.contains(getNode(coor))
        ).collect(Collectors.toList());

        boolean isFullyLocked = true;

        parents.add(current);
        for (IntegerCoordinates coordinates : checkCoordinates) {
            if (!isFullyLockedFrom(getNode(coordinates), parents, playerData)) {
                isFullyLocked = false;
                //Very important to break to stop the recursion algorithm once one unlockable point has been found
                break;
            }
        }
        return isFullyLocked;
    }
}
