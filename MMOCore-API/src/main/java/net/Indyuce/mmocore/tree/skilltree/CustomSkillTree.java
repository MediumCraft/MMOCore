package net.Indyuce.mmocore.tree.skilltree;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.tree.ParentType;
import net.Indyuce.mmocore.tree.SkillTreeNode;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class CustomSkillTree extends SkillTree {
    public CustomSkillTree(ConfigurationSection config) {
        super(config);

        //We setup the coordinate map because coordinates are given in the yml for linked skill tree
        super.coordinatesSetup();


    }

    @Override
    protected void whenPostLoaded(@NotNull ConfigurationSection config) {

        //We setup the children and parents for each node.
        for (SkillTreeNode node : nodes.values()) {

            ConfigurationSection section = config.getConfigurationSection("nodes."+node.getId() + ".children.soft");
            if (section != null) {
                for (String child : section.getKeys(false)) {
                    Validate.isTrue(isNode(child),"The node "+child+ "defined in children.soft of"+node.getId()+"doesn't exist.");
                    node.addChild(getNode(child));
                    getNode(child).addParent(node, section.getInt(child), ParentType.SOFT);
                }
            }
            section = config.getConfigurationSection("nodes."+node.getId() + ".children.strong");
            if (section != null) {
                for (String child : section.getKeys(false)) {
                    Validate.isTrue(isNode(child),"The node "+child+ "defined in children.strong of"+node.getId()+"doesn't exist.");

                    node.addChild(getNode(child));
                    getNode(child).addParent(node, section.getInt(child), ParentType.STRONG);
                }
            }
        }
    }


    private void setupRoots() {
        //We find the roots of the tree which don't have any parents
        for (SkillTreeNode node : nodes.values()) {

            if (node.getSoftParents().size()+node.getStrongParents().size() == 0) {
                //We mark the node as a root also
                roots.add(node);
                node.setIsRoot();
            }
        }
    }

}
