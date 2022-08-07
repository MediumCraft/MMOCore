package net.Indyuce.mmocore.tree.skilltree;

import net.Indyuce.mmocore.tree.ParentType;
import net.Indyuce.mmocore.tree.SkillTreeNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class CustomSkillTree extends SkillTree{
    public CustomSkillTree(ConfigurationSection config) {
        super(config);

        //We setup the coordinate map because coordinates are given in the yml for linked skill tree
        super.coordinatesSetup();



    }

    @Override
    protected void whenPostLoaded(@NotNull ConfigurationSection config) {

        //We setup the children and parents for each node.
        for (SkillTreeNode node : nodes.values()) {
            ConfigurationSection section = config.getConfigurationSection(node.getId() + ".children.soft");
            if(section!=null) {
                for (String child : section.getKeys(false)) {
                    node.addChild(getNode(child));
                    getNode(child).addParent(node, section.getInt(child), ParentType.SOFT);
                }
            }
            section = config.getConfigurationSection(node.getId() + ".children.strong");
            if(section!=null) {
                for (String child : section.getKeys(false)) {
                    node.addChild(getNode(child));
                    getNode(child).addParent(node, section.getInt(child), ParentType.STRONG);
                }
            }
        }
    }

}
