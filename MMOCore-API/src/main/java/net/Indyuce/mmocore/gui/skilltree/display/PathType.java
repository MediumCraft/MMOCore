package net.Indyuce.mmocore.gui.skilltree.display;

/**
 * The direction of the path.
 */
public enum PathType implements DisplayType {

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


}
