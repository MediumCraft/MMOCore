package net.Indyuce.mmocore.skilltree.tree;

import net.Indyuce.mmocore.skilltree.IntegerCoordinates;
import net.Indyuce.mmocore.skilltree.ParentType;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import org.bukkit.configuration.ConfigurationSection;

public class ProximitySkillTree extends SkillTree {
    public ProximitySkillTree(ConfigurationSection config) {
        super(config);

        // Neighbors are marked as soft parents
        for (SkillTreeNode node : nodes.values())
            for (IntegerCoordinates relative : RELATIVES) {
                final SkillTreeNode neighbor = this.getNodeOrNull(node.getCoordinates().add(relative));
                if (neighbor != null) {
                    node.addParent(neighbor, ParentType.SOFT, 1);
                    neighbor.addChild(node, ParentType.SOFT, 1);
                }
            }
    }

    private static final IntegerCoordinates[] RELATIVES = {
            new IntegerCoordinates(1, 0),
            new IntegerCoordinates(-1, 0),
            new IntegerCoordinates(0, 1),
            new IntegerCoordinates(0, -1)
    };
}
