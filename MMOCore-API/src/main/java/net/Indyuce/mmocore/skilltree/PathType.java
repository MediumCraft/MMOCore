package net.Indyuce.mmocore.skilltree;

/**
 * These are the different textures that a path between
 * two nodes can have, just like a redstone wire which can take
 * turns, go straight, or be a one node path on its own.
 */
public enum PathType {

    /**
     * │ up, down
     */
    UP,

    /**
     * ─ right, left
     */
    RIGHT,

    /**
     * ┌ up right, left down
     */
    UP_RIGHT,

    /**
     * ┐ up left, right down
     */
    UP_LEFT,

    /**
     * └ down right, left up
     */
    DOWN_RIGHT,

    /**
     * ┘ down left, right up
     */
    DOWN_LEFT,

    /**
     *
     */
    DEFAULT;
}
