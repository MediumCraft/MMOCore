package net.Indyuce.mmocore.tree.skilltree;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.tree.IntegerCoordinates;
import net.Indyuce.mmocore.tree.ParentType;
import net.Indyuce.mmocore.tree.SkillTreeNode;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import javax.swing.text.html.HTMLDocument;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Skill Trees where you only need to fill the strong and soft
 */
public class AutomaticSkillTree extends SkillTree  {
    private SkillTreeNode root;

    //Hash map to store the number of left and right branches of each node
    private final HashMap<SkillTreeNode, Branches> nodeBranches = new HashMap<>();

    public AutomaticSkillTree(ConfigurationSection config) {
        super(config);
    }

    @Override
    public void whenPostLoaded(ConfigurationSection config) {


        //We setup the children and parents for each node.
        for(SkillTreeNode node:nodes.values()) {
            ConfigurationSection section = config.getConfigurationSection("nodes."+node.getId() + ".children.soft");
            if(section!=null) {
                for (String child : section.getKeys(false)) {
                    node.addChild(getNode(child));
                    getNode(child).addParent(node, section.getInt(child), ParentType.SOFT);
                }
            }
            section = config.getConfigurationSection("nodes."+node.getId() + ".children.strong");

            if(section!=null) {
                for (String child : section.getKeys(false)) {
                    node.addChild(getNode(child));
                    getNode(child).addParent(node, section.getInt(child), ParentType.STRONG);
                }
            }

        }
        //We find the root of the tree wich is
        for (SkillTreeNode node : nodes.values()) {
            if (node.getSoftParents().size() == 0&&node.getStrongParents().size()==0) {
                    Validate.isTrue(root == null, "You can't have 2 roots on one automatic skill tree. You have "+(root!=null?root.getName():"")+" and "+node.getName()+".");
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
                for(SkillTreeNode skillTree : nodeBranches.keySet())
                leftOffset += 2 + nodeBranches.get(child).getWidth();
            } else {
                child.setCoordinates(new IntegerCoordinates(x + rightOffset + 2 + nodeBranches.get(child).getWidth(), y + 2));
                rightOffset += 2 + nodeBranches.get(child).getWidth();
            }

            //We setup the path to parent variable (Used for the GUI)
            int childX = child.getCoordinates().getX();
            int childY = child.getCoordinates().getY();

            int parentX = node.getSoftParents().size()!=0?((SkillTreeNode)node.getSoftParents().toArray()[0]).getCoordinates().getX():((SkillTreeNode)node.getStrongParents().toArray()[0]).getCoordinates().getX();
            paths.add(new IntegerCoordinates(childX, childY - 1));
            int offset = childX > parentX ? -1 : 1;
            while (childX != parentX) {
                paths.add(new IntegerCoordinates(childX, childY - 2));
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
                leftBranches += nodeBranches.get(child).getWidth() + 2;
            } else {
                rightBranches += nodeBranches.get(child).getWidth() + 2;
            }
        }

        nodeBranches.put(node,new Branches(leftBranches,rightBranches));
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

