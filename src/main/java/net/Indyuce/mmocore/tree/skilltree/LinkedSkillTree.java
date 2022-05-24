package net.Indyuce.mmocore.tree.skilltree;

import io.netty.handler.codec.http.cookie.CookieDecoder;
import net.Indyuce.mmocore.tree.IntegerCoordinates;
import net.Indyuce.mmocore.tree.SkillTreeNode;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class LinkedSkillTree extends SkillTree{


    public LinkedSkillTree(ConfigurationSection config) {
        super(config);

        //We setup the coordinate map because coordinates are given in the yml for linked skill tree
        setupCoordinatesNodesMap();
    }



    @Override
    protected void whenPostLoaded(@NotNull ConfigurationSection configurationSection) {

        SkillTreeNode root=getNode(new IntegerCoordinates(0,0));
        Validate.notNull(root,"Their must be a node(the root of the tree) at the coordinates (0,0) ");
        //We setup all the children and parent relations between the nodes
        setupChildren(root);
    }

    /**
     * There is no paths on a linked skill tree
     */
    @Override
    public boolean isPath(IntegerCoordinates coordinates) {
        return false;
    }

    /**
     * Recursive algorithm to setup the parents and children of each skillTreeNode
     */
    public void setupChildren(SkillTreeNode node) {
        int x=node.getCoordinates().getX();
        int y=node.getCoordinates().getY();
        List<IntegerCoordinates> checkCoordinates= Arrays.asList(new IntegerCoordinates(x+1,y),
                new IntegerCoordinates(x-1,y),new IntegerCoordinates(x,y+1),new IntegerCoordinates(x,y-1));
        for(IntegerCoordinates coor:checkCoordinates) {
            //We add Parent and child only if the node exists and doesn't have a parent already

            if(isNode(coor)) {
                SkillTreeNode child=getNode(coor);
                if(child.getParents().size()==0) {
                    child.addParent(node);
                    node.addChild(child);
                    //We call recursively the algorithm
                    setupChildren(child);
                }}

        }
    }
}
