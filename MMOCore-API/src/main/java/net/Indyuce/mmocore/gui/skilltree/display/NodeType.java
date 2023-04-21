package net.Indyuce.mmocore.gui.skilltree.display;

public enum NodeType {
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
    NO_PATH;

    public static NodeType getNodeType(boolean hasUpPath, boolean hasRightPath, boolean hasDownPath, boolean hasLeftPath) {
        if (hasUpPath && hasRightPath && hasDownPath && hasLeftPath) {
            return UP_RIGHT_DOWN_LEFT;
        } else if (hasUpPath && hasRightPath && hasDownPath) {
            return UP_RIGHT_DOWN;
        } else if (hasUpPath && hasRightPath && hasLeftPath) {
            return UP_RIGHT_LEFT;
        } else if (hasUpPath && hasDownPath && hasLeftPath) {
            return UP_DOWN_LEFT;
        } else if (hasDownPath && hasRightPath && hasLeftPath) {
            return DOWN_RIGHT_LEFT;
        } else if (hasUpPath && hasRightPath) {
            return UP_RIGHT;
        } else if (hasUpPath && hasDownPath) {
            return UP_DOWN;
        } else if (hasUpPath && hasLeftPath) {
            return UP_LEFT;
        } else if (hasDownPath && hasRightPath) {
            return DOWN_RIGHT;
        } else if (hasDownPath && hasLeftPath) {
            return DOWN_LEFT;
        } else if (hasRightPath && hasLeftPath) {
            return RIGHT_LEFT;
        }
        return NO_PATH;
    }
}


