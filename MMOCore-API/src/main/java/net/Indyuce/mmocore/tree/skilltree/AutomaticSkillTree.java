package net.Indyuce.mmocore.tree.skilltree;

import net.Indyuce.mmocore.tree.IntegerCoordinates;
import net.Indyuce.mmocore.tree.ParentType;
import net.Indyuce.mmocore.tree.SkillTreeNode;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Objects;

/**
 * Skill Trees where you only need to fill the strong and soft
 */
public class AutomaticSkillTree extends SkillTree {

    //Hash map to store the number of left and right branches of each node
    private final HashMap<SkillTreeNode, Branches> nodeBranches = new HashMap<>();

    public AutomaticSkillTree(ConfigurationSection config) {
        super(config);
    }

    @Override
    public void whenPostLoaded(ConfigurationSection config) {


        //We setup the children and parents for each node.
        for (SkillTreeNode node : nodes.values()) {
            ConfigurationSection section = config.getConfigurationSection("nodes." + node.getId() + ".children.soft");
            if (section != null) {
                for (String child : section.getKeys(false)) {
                    node.addChild(getNode(child));
                    getNode(child).addParent(node, section.getInt(child), ParentType.SOFT);
                }
            }
            section = config.getConfigurationSection("nodes." + node.getId() + ".children.strong");

            if (section != null) {
                for (String child : section.getKeys(false)) {
                    node.addChild(getNode(child));
                    getNode(child).addParent(node, section.getInt(child), ParentType.STRONG);
                }
            }

        }
        //We find the root of the tree which is
        for (SkillTreeNode node : nodes.values()) {
            if (node.getSoftParents().size() == 0 && node.getStrongParents().size() == 0) {
                Validate.isTrue(roots.size() == 0, "You can't have 2 roots on one automatic skill tree. You have " + (roots.size() != 0 ? roots.get(0).getName() : "") + " and " + node.getName() + ".");
                //We mark the node as a root also
                roots.add(node);
                node.setIsRoot();
            }
        }

        //We setup the width of all the nodes recursively
        setupTreeWidth(roots.get(0));
        //We recursively setup all the coordinates of the tree nodes
        roots.get(0).setCoordinates(new IntegerCoordinates(0, 0));
        setupCoordinates(roots.get(0));

        //We get and cache the values of minX,minY,maxX and maxY
        minX = nodeBranches.get(roots.get(0)).getLeftBranches();
        minY = 0;
        maxX = nodeBranches.get(roots.get(0)).getRightBranches();

        for (SkillTreeNode node : nodes.values()) {
            if (node.getCoordinates().getY() > maxY)
                maxY = node.getCoordinates().getY();
        }

        //Eventually we setup the skill tree info related to coordinates
        super.coordinatesSetup();

    }

    /**
     * Recursive algorithm to automatically calculate the integercoordinates each node should have to have a good display.
     * It also fills the list pathToParents representing all the coordinates corresponding to a path between 2 nodes (for the GUI)
     *
     * @param node the root
     */
    private void setupCoordinates(SkillTreeNode node) {
        if (node.isRoot()) {
            node.setCoordinates(new IntegerCoordinates(0, 2));
        }
        int childrenSize = node.getChildren().size();

        int x = node.getCoordinates().getX();
        int y = node.getCoordinates().getY();

        int leftOffset = 0;
        int rightOffset = 0;
        for (int i = 0; i < childrenSize; i++) {
            SkillTreeNode child = node.getChildren().get(i);

            if (childrenSize % 2 == 1 && i == 0) {
                child.setCoordinates(new IntegerCoordinates(x, y - 1));
                leftOffset += 2 + nodeBranches.get(child).getLeftBranches();
                rightOffset += 2 + nodeBranches.get(child).getRightBranches();
            } else if (i % 2 == 0) {
                child.setCoordinates(new IntegerCoordinates(x - leftOffset - 2 - nodeBranches.get(child).getWidth(), y - 1));
                for (SkillTreeNode skillTree : nodeBranches.keySet())
                    leftOffset += 2 + nodeBranches.get(child).getWidth();
            } else {
                child.setCoordinates(new IntegerCoordinates(x + rightOffset + 2 + nodeBranches.get(child).getWidth(), y - 1));
                rightOffset += 2 + nodeBranches.get(child).getWidth();
            }

            //We setup the path to parent variable (Used for the GUI)
            int childX = child.getCoordinates().getX();
            int childY = child.getCoordinates().getY();

            int parentX=node.getCoordinates().getX();

            paths.add(new IntegerCoordinates(childX, childY + 1));
            int offset = childX > parentX ? -1 : 1;
            while (childX != parentX) {
                paths.add(new IntegerCoordinates(childX, childY + 2));
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
            //If there is an odd number ob branches the first one will be at the center so we add to the left and to the right
            if (childrenSize % 2 == 1 && i == 0) {

                leftBranches += nodeBranches.get(child).getLeftBranches();
                rightBranches += nodeBranches.get(child).getRightBranches();
            } else if (i % 2 == 0) {
                leftBranches += nodeBranches.get(child).getWidth();
            } else {
                rightBranches += nodeBranches.get(child).getWidth();
            }
        }

        nodeBranches.put(node, new Branches(leftBranches, rightBranches));
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Branches branches = (Branches) o;
            return leftBranches == branches.leftBranches && rightBranches == branches.rightBranches;
        }


        @Override
        public String toString() {
            return "Branches{" +
                    "leftBranches=" + leftBranches +
                    ", rightBranches=" + rightBranches +
                    '}';
        }

        @Override
        public int hashCode() {
            return Objects.hash(leftBranches, rightBranches);
        }
    }
}

