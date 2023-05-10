package net.Indyuce.mmocore.skilltree.tree;

import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmocore.skilltree.ParentType;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class CustomSkillTree extends SkillTree {
    public CustomSkillTree(ConfigurationSection config) {
        super(config);

        // Setup the coordinate map because coordinates are given in the yml for linked skill tree
        super.coordinatesSetup();
    }

    @Override
    protected void whenPostLoaded(@NotNull ConfigurationSection config) {

        // Setup the children and parents for each node.
        for (SkillTreeNode node : nodes.values()) {
            if (config.isConfigurationSection("nodes." + node.getId() + ".parents"))
                for (String key : config.getConfigurationSection("nodes." + node.getId() + ".parents").getKeys(false)) {
                    ConfigurationSection section = config.getConfigurationSection("nodes." + node.getId() + ".parents." + key);
                    if (section != null) {
                        for (String parent : section.getKeys(false)) {
                            node.addParent(getNode(parent), section.getInt(parent), ParentType.valueOf(UtilityMethods.enumName(key)));
                            getNode(parent).addChild(node);
                        }
                    }
                }
        }
        setupRoots();
    }

    private void setupRoots() {

        // Find the tree roots which don't have any parents
        for (SkillTreeNode node : nodes.values()) {
            if (node.getParents().size() == 0) {
                // We mark the node as a root also
                roots.add(node);
                node.setIsRoot();
            }
        }
    }
}
