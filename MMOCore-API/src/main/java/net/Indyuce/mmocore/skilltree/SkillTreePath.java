package net.Indyuce.mmocore.skilltree;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;

public class SkillTreePath {
    private final SkillTree tree;
    private final IntegerCoordinates coordinates;
    private final SkillTreeNode from;
    private final SkillTreeNode to;

    public SkillTreePath(SkillTree tree, IntegerCoordinates coordinates, SkillTreeNode from, SkillTreeNode skillTreeNode) {
        this.tree = tree;
        this.coordinates = coordinates;
        this.from = from;
        to = skillTreeNode;
    }

    public NodeState getStatus(PlayerData playerData) {
        NodeState fromStatus = playerData.getNodeState(from);
        NodeState toStatus = playerData.getNodeState(to);
        if (fromStatus == NodeState.UNLOCKED && toStatus == NodeState.UNLOCKED)
            return NodeState.UNLOCKED;
        if ((fromStatus == NodeState.UNLOCKABLE && toStatus == NodeState.UNLOCKED) || (fromStatus == NodeState.UNLOCKED && toStatus == NodeState.UNLOCKABLE))
            return NodeState.UNLOCKABLE;
        if (fromStatus == NodeState.FULLY_LOCKED || toStatus == NodeState.FULLY_LOCKED)
            return NodeState.FULLY_LOCKED;
        return NodeState.LOCKED;
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
