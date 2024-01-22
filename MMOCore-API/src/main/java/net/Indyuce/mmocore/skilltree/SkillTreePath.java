package net.Indyuce.mmocore.skilltree;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.skilltree.display.PathStatus;
import net.Indyuce.mmocore.gui.skilltree.display.PathType;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;

public record SkillTreePath(SkillTree tree, IntegerCoordinates coordinates, SkillTreeNode from, SkillTreeNode to) {

    public PathStatus getStatus(PlayerData playerData) {
        SkillTreeStatus fromStatus = playerData.getNodeStatus(from);
        SkillTreeStatus toStatus = playerData.getNodeStatus(to);
        if (fromStatus == SkillTreeStatus.UNLOCKED && toStatus == SkillTreeStatus.UNLOCKED)
            return PathStatus.UNLOCKED;
        if ((fromStatus == SkillTreeStatus.UNLOCKABLE && toStatus == SkillTreeStatus.UNLOCKED) || (fromStatus == SkillTreeStatus.UNLOCKED && toStatus == SkillTreeStatus.UNLOCKABLE))
            return PathStatus.UNLOCKABLE;
        if (fromStatus == SkillTreeStatus.FULLY_LOCKED || toStatus == SkillTreeStatus.FULLY_LOCKED)
            return PathStatus.FULLY_LOCKED;
        return PathStatus.LOCKED;
    }

    public PathType getPathType() {
        IntegerCoordinates upCoor = new IntegerCoordinates(coordinates.getX(), coordinates.getY() - 1);
        IntegerCoordinates downCoor = new IntegerCoordinates(coordinates.getX(), coordinates.getY() + 1);
        IntegerCoordinates rightCoor = new IntegerCoordinates(coordinates.getX() + 1, coordinates.getY());
        IntegerCoordinates leftCoor = new IntegerCoordinates(coordinates.getX() - 1, coordinates.getY());
        boolean hasUp = tree.isPath(upCoor) || upCoor.equals(from.getCoordinates()) || upCoor.equals(to.getCoordinates());
        boolean hasDown = tree.isPath(downCoor) || downCoor.equals(from.getCoordinates()) || downCoor.equals(to.getCoordinates());
        boolean hasRight = tree.isPath(rightCoor) || rightCoor.equals(from.getCoordinates()) || rightCoor.equals(to.getCoordinates());
        boolean hasLeft = tree.isPath(leftCoor) || leftCoor.equals(from.getCoordinates()) || leftCoor.equals(to.getCoordinates());

        if ((hasUp || hasDown) && !hasLeft && !hasRight) {
            return PathType.UP;
        } else if ((hasRight || hasLeft) && !hasUp && !hasDown) {
            return PathType.RIGHT;
        } else if (hasUp && hasRight) {
            return PathType.UP_RIGHT;
        } else if (hasUp && hasLeft) {
            return PathType.UP_LEFT;
        } else if (hasDown && hasRight) {
            return PathType.DOWN_RIGHT;
        } else if (hasDown && hasLeft) {
            return PathType.DOWN_LEFT;
        }
        return PathType.DEFAULT;
    }


}
