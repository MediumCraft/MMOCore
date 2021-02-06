package net.Indyuce.mmocore.api.event;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.event.HandlerList;

public class PlayerAttributeUseEvent extends PlayerDataEvent{
    private static final HandlerList handlers = new HandlerList();

    public PlayerAttributeUseEvent(PlayerData playerData) {
        super(playerData);
    }


    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
