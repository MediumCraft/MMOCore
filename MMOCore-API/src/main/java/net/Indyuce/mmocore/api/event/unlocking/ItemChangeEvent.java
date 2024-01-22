package net.Indyuce.mmocore.api.event.unlocking;

import net.Indyuce.mmocore.api.event.PlayerDataEvent;
import net.Indyuce.mmocore.api.player.PlayerData;

public abstract class ItemChangeEvent extends PlayerDataEvent {
    private final String itemKey;

    public ItemChangeEvent(PlayerData playerData, String itemKey) {
        super(playerData);
        this.itemKey = itemKey;
    }

    /**
     * @return The full item key in the format <plugin-id>:<item-type-id>:<item-id>.
     */
    public String getItemKey() {
        return itemKey;
    }


    /**
     * @return The item-type-id which is the first parameter in the key format <item-type-id>:<item-id>.
     */
    public String getItemTypeId() {
        return itemKey.split(":")[0];
    }

    /**
     * @return The item--id which is the last parameter in the key format <item-type-id>:<item-id>.
     */
    public String getItemId() {
        return itemKey.split(":")[1];
    }
}

