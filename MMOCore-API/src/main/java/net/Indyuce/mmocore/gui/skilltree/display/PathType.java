package net.Indyuce.mmocore.gui.skilltree.display;

/**
 * The direction of the path.
 */
public enum PathType {

    UP,
    /**
     * Goes to up then east/ goes to left then down.
     */
    UP_RIGHT,
    UP_LEFT,
    DOWN_RIGHT,
    DOWN_LEFT,
    RIGHT,

    DEFAULT;

    public static PathType getPathType(boolean hasUp,boolean hasRight,boolean hasDown,boolean hasLeft) {
        if ((hasUp || hasDown) && !hasLeft && hasRight) {
            return UP;
        } else if ((hasRight || hasLeft)&& !hasUp && !hasDown) {
            return RIGHT;
        } else if (hasUp && hasRight) {
            return UP_RIGHT;
        }
        else if (hasUp && hasLeft) {
            return UP_LEFT;
        }
        else if (hasDown && hasRight) {
            return DOWN_RIGHT;
        }
        else if (hasDown && hasLeft) {
            return DOWN_LEFT;
        }
        return DEFAULT;
    }

}
