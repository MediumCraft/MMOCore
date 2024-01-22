package net.Indyuce.mmocore.skilltree;

public enum SkillTreeStatus {

    /**
     * The player does not have access to this skill tree node just yet.
     */
    LOCKED,

    /**
     * The player has bought and unlocked the skill tree node.
     */
    UNLOCKED,

    /**
     * The player has access to but has not unlocked the node yet.
     */
    UNLOCKABLE,

    /**
     * The player had access to this node, but unlocked another
     * node which now prevents him from unlocking this one.
     */
    FULLY_LOCKED;
}
