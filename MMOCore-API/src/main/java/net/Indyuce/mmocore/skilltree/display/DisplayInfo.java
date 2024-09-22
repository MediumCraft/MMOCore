package net.Indyuce.mmocore.skilltree.display;

import net.Indyuce.mmocore.skilltree.NodeState;

/**
 * The information needed to determine the display of a node/path depending on its context.
 */
public abstract class DisplayInfo {
    protected final NodeState state;

    protected DisplayInfo(NodeState state) {
        this.state = state;
    }

    public NodeState getState() {
        return state;
    }
}
