package net.Indyuce.mmocore.skilltree;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.skilltree.display.PathStatus;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;

public record SkillTreePath(SkillTree tree, IntegerCoordinates coordinates, SkillTreeNode from, SkillTreeNode to) {

    public PathStatus getStatus(PlayerData playerData) {
        if (playerData.getNodeState(from) == NodeStatus.UNLOCKED && playerData.getNodeState(to) == NodeStatus.UNLOCKED) {
            return PathStatus.UNLOCKED;
        }
        return PathStatus.LOCKED;
    }


}
