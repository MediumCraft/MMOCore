package net.Indyuce.mmocore.api.event;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerEnterCastingModeEvent extends PlayerDataEvent implements Cancellable {
    private boolean cancelled = false;

    private static final HandlerList HANDLERS = new HandlerList();

    @Deprecated
    public PlayerEnterCastingModeEvent(@NotNull Player who) {
        super(PlayerData.get(who));
    }

    public PlayerEnterCastingModeEvent(@NotNull PlayerData playerData) {
        super(playerData);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
