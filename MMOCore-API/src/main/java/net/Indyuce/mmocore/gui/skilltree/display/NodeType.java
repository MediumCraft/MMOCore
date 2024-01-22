package net.Indyuce.mmocore.gui.skilltree.display;

public enum NodeType implements DisplayType {
    UP_RIGHT_DOWN_LEFT,
    UP_RIGHT_DOWN,
    UP_RIGHT_LEFT,
    UP_DOWN_LEFT,
    DOWN_RIGHT_LEFT,
    UP_RIGHT,
    UP_DOWN,
    UP_LEFT,
    DOWN_RIGHT,
    DOWN_LEFT,
    RIGHT_LEFT,

    RIGHT,
    LEFT,
    DOWN,
    UP,
    NO_PATH;

    public static NodeType getNodeType(boolean hasUpPathOrNode, boolean hasRightPathOrNode, boolean hasDownPathOrNode, boolean hasLeftPathOrNode) {
        if (hasUpPathOrNode && hasRightPathOrNode && hasDownPathOrNode && hasLeftPathOrNode)
            return UP_RIGHT_DOWN_LEFT;
        else if (hasUpPathOrNode && hasRightPathOrNode && hasDownPathOrNode)
            return UP_RIGHT_DOWN;
        else if (hasUpPathOrNode && hasRightPathOrNode && hasLeftPathOrNode)
            return UP_RIGHT_LEFT;
        else if (hasUpPathOrNode && hasDownPathOrNode && hasLeftPathOrNode)
            return UP_DOWN_LEFT;
        else if (hasDownPathOrNode && hasRightPathOrNode && hasLeftPathOrNode)
            return DOWN_RIGHT_LEFT;
        else if (hasUpPathOrNode && hasRightPathOrNode)
            return UP_RIGHT;
        else if (hasUpPathOrNode && hasDownPathOrNode)
            return UP_DOWN;
        else if (hasUpPathOrNode && hasLeftPathOrNode)
            return UP_LEFT;
        else if (hasDownPathOrNode && hasRightPathOrNode)
            return DOWN_RIGHT;
        else if (hasDownPathOrNode && hasLeftPathOrNode)
            return DOWN_LEFT;
        else if (hasRightPathOrNode && hasLeftPathOrNode)
            return RIGHT_LEFT;
        else if (hasUpPathOrNode)
            return UP;
        else if (hasDownPathOrNode)
            return DOWN;
        else if (hasRightPathOrNode)
            return RIGHT;
        else if (hasLeftPathOrNode)
            return LEFT;
        return NO_PATH;
    }
}


