package net.Indyuce.mmocore.skill.cast;

public enum PlayerKey {

    /**
     * When a player left clicks
     */
    LEFT_CLICK,

    /**
     * When a player right clicks
     */
    RIGHT_CLICK,

    /**
     * When a player drops the item they are holding
     */
    DROP,

    /**
     * When a player swaps their hand items
     */
    SWAP_HANDS,

    /**
     * When a player sneaks (doesn't trigger when unsneaking)
     */
    CROUCH;
}
