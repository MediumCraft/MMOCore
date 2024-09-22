package net.Indyuce.mmocore.skilltree.tree;

import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class CustomSkillTree extends SkillTree {
    public CustomSkillTree(@NotNull ConfigurationSection config) {
        super(config);

        // Nodes with no parents are roots
        for (SkillTreeNode node : nodes.values())
            if (node.getParents().isEmpty()) {
                node.setRoot();
                addRoot(node);
            }
    }
}
