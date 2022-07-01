package net.Indyuce.mmocore.skill.cast;

public enum PlayerKey {

    /**
     * When a player left clicks
     */
    LEFT_CLICK(false),

    /**
     * When a player right clicks
     */
    RIGHT_CLICK(false),

    /**
     * When a player drops the item they are holding
     */
    DROP(true),

    /**
     * When a player swaps their hand items
     */
    SWAP_HANDS(true),

    /**
     * When a player sneaks (doesn't trigger when unsneaking)
     */
    CROUCH(false);

    private final boolean cancellableEvent;

    private PlayerKey(boolean cancelableEvent) {
        this.cancellableEvent = cancelableEvent;
    }

    /**
     * @return Whether or not the event causing the key press event
     *         should be cancelled when this key is actually being registered
     *         as a key combo action.
     */
    public boolean shouldCancelEvent() {
        return cancellableEvent;
    }
}
