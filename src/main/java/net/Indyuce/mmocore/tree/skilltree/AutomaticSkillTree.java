package net.Indyuce.mmocore.tree.skilltree;

import net.Indyuce.mmocore.tree.IntegerCoordinates;
import net.Indyuce.mmocore.tree.SkillTreeNode;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;

public class AutomaticSkillTree extends SkillTree  {
    private SkillTreeNode root;
    //Represents all the coordinates that will be displayed as a path (between 2 nodes of the tree)
    private final ArrayList<IntegerCoordinates> pathToParents = new ArrayList<>();

    //Hash map to store the left and right branches of each node
    private final HashMap<SkillTreeNode, Branches> nodeBranches = new HashMap<>();

    public AutomaticSkillTree(ConfigurationSection config) {
        super(config);
    }

    @Override
    public void whenPostLoaded(ConfigurationSection config) {


        //We setup the children and parents for each node.
        for (SkillTreeNode node : nodes.values()) {
            ConfigurationSection section = config.getConfigurationSection(node.getId());
            for (String child : section.getStringList("children")) {
                node.addChild(getNode(child));
                getNode(child).addParent(node);
            }
        }


        //We find the root of the tree wich is
        for (SkillTreeNode node : nodes.values()) {
            if (node.getParents().size() == 0) {
                Validate.isTrue(root == null, "Their can't be more than 1 root in the skillTree!");
                root = node;
            }
        }
        //We setup the width of all the nodes recursively
        setupTreeWidth(root);
        //We recursively setup all the coordinates of the tree nodes
        root.setCoordinates(new IntegerCoordinates(0, 0));
        setupCoordinates(root);

        //We get and cache the values of minX,minY,maxX and maxY
        minX = nodeBranches.get(root).getLeftBranches();
        minY = 0;
        maxX = nodeBranches.get(root).getRightBranches();

        for (SkillTreeNode node : nodes.values()) {
            if (node.getCoordinates().getY() > maxY)
                maxY = node.getCoordinates().getY();
        }

        //Eventually we setup the coordinateNodesMap
        super.setupCoordinatesNodesMap();

    }

    /**
     * Recursive algorithm to automatically calculate the integercoordinates each node should have to have a good display.
     * It also fills the list pathToParents representing all the coordinates corresponding to a path between 2 nodes (for the GUI)
     *
     * @param node the root
     */
    private void setupCoordinates(SkillTreeNode node) {
        int childrenSize = node.getChildren().size();
        int x = node.getCoordinates().getX();
        ;
        int y = node.getCoordinates().getY();
        ;
        int leftOffset = 0;
        int rightOffset = 0;
        for (int i = 0; i < childrenSize; i++) {
            SkillTreeNode child = node.getChildren().get(i);

            if (childrenSize % 2 == 0 && i == 0) {
                child.setCoordinates(new IntegerCoordinates(x, y + 2));
                leftOffset += 2 + nodeBranches.get(child).getLeftBranches();
                rightOffset += 2 + nodeBranches.get(child).getRightBranches();
            } else if (i % 2 == 0) {
                child.setCoordinates(new IntegerCoordinates(x - leftOffset - 2 - nodeBranches.get(child).getWidth(), y + 2));
                leftOffset += 2 + nodeBranches.get(child).getWidth();
            } else {
                child.setCoordinates(new IntegerCoordinates(x + rightOffset + 2 + nodeBranches.get(child).getWidth(), y + 2));
                rightOffset += 2 + nodeBranches.get(child).getWidth();
            }

            //We setup the path to parent variable (Used for the GUI)
            int childX = child.getCoordinates().getX();
            int childY = child.getCoordinates().getY();

            int parentX = node.getParents().get(0).getCoordinates().getX();
            pathToParents.add(new IntegerCoordinates(childX, childY - 1));
            int offset = childX > parentX ? -1 : 1;
            while (childX != parentX) {
                pathToParents.add(new IntegerCoordinates(childX, childY - 2));
                childX += offset;
            }


            //We setup the coordinates for the associated child
            setupCoordinates(child);
        }

    }

    public Branches getBranches(SkillTreeNode node) {
        return nodeBranches.get(node);
    }

    /**
     * Recursively sed to setup all the right and left branches of the node to later determine its coordinates for GUI display
     */
    public void setupTreeWidth(SkillTreeNode node) {
        int childrenSize = node.getChildren().size();
        int leftBranches = 0;
        int rightBranches = 0;
        for (int i = 0; i < childrenSize; i++) {
            SkillTreeNode child = node.getChildren().get(i);
            setupTreeWidth(child);
            if (childrenSize % 2 == 0 && i == 0) {

                leftBranches += nodeBranches.get(child).getLeftBranches();
                rightBranches += nodeBranches.get(child).getRightBranches();
            } else if (i % 2 == 0) {
                leftBranches += nodeBranches.get(child).getWidth() + 2;
            } else {
                rightBranches += nodeBranches.get(child).getWidth() + 2;
            }

        }
    }

    @Override
    public boolean isPath(IntegerCoordinates coordinates) {
        return pathToParents.contains(coordinates);
    }


    private class Branches {
        private final int leftBranches, rightBranches;

        public Branches(int leftBranches, int rightBranches) {
            this.leftBranches = leftBranches;
            this.rightBranches = rightBranches;
        }

        public int getLeftBranches() {
            return leftBranches;
        }

        public int getRightBranches() {
            return rightBranches;
        }

        public int getWidth() {
            return leftBranches + rightBranches;
        }
    }
}

