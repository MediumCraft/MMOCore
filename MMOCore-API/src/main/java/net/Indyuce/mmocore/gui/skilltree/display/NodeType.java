package net.Indyuce.mmocore.gui.skilltree.display;

public enum NodeType {
    NORTH_EAST_SOUTH_WEST,
    NORTH_EAST_SOUTH,
    NORTH_EAST_WEST,
    NORTH_SOUTH_WEST,
    SOUTH_EAST_WEST,
    NORTH_EAST,
    NORTH_SOUTH,
    NORTH_WEST,
    SOUTH_EAST,
    SOUTH_WEST,
    EAST_WEST,
    NO_PATH;

    public static NodeType getNodeType(boolean hasNorthPath, boolean hasEastPath, boolean hasSouthPath, boolean hasWestPath) {
        if (hasNorthPath && hasEastPath && hasSouthPath && hasWestPath) {
            return NORTH_EAST_SOUTH_WEST;
        } else if (hasNorthPath && hasEastPath && hasSouthPath) {
            return NORTH_EAST_SOUTH;
        } else if (hasNorthPath && hasEastPath && hasWestPath) {
            return NORTH_EAST_WEST;
        } else if (hasNorthPath && hasSouthPath && hasWestPath) {
            return NORTH_SOUTH_WEST;
        } else if (hasSouthPath && hasEastPath && hasWestPath) {
            return SOUTH_EAST_WEST;
        } else if (hasNorthPath && hasEastPath) {
            return NORTH_EAST;
        } else if (hasNorthPath && hasSouthPath) {
            return NORTH_SOUTH;
        } else if (hasNorthPath && hasWestPath) {
            return NORTH_WEST;
        } else if (hasSouthPath && hasEastPath) {
            return SOUTH_EAST;
        } else if (hasSouthPath && hasWestPath) {
            return SOUTH_WEST;
        } else if (hasEastPath && hasWestPath) {
            return EAST_WEST;
        }
        return NO_PATH;
    }
}


