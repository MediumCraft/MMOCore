package net.Indyuce.mmocore.skilltree;

public enum NodeIncrementResult {

    SUCCESS,

    /**
     * Node is still locked/not unlockable
     */
    LOCKED_NODE,

    /**
     * Player does not have required permission
     */
    PERMISSION_DENIED,

    /**
     * Maximum level of node is reached
     */
    MAX_LEVEL_REACHED,

    /**
     * Player does not have enough skill tree points
     */
    NOT_ENOUGH_POINTS,
}
