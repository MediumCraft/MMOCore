package net.Indyuce.mmocore.gui.skilltree.display;

/**
 * The direction of the path.
 */
public enum PathType {

    NORTH,
    /**
     * Goes to north then east/ goes to west then south.
     */
    NORTH_EAST,
    NORTH_WEST,
    SOUTH_EAST,
    SOUTH_WEST,
    EAST,

    DEFAULT;

    public static PathType getPathType(boolean hasNorth,boolean hasEast,boolean hasSouth,boolean hasWest) {
        if ((hasNorth || hasSouth) && !hasWest && hasEast) {
            return NORTH;
        } else if ((hasEast || hasWest)&& !hasNorth && !hasSouth) {
            return EAST;
        } else if (hasNorth && hasEast) {
            return NORTH_EAST;
        }
        else if (hasNorth && hasWest) {
            return NORTH_WEST;
        }
        else if (hasSouth && hasEast) {
            return SOUTH_EAST;
        }
        else if (hasSouth && hasWest) {
            return SOUTH_WEST;
        }
        return DEFAULT;
    }

}
