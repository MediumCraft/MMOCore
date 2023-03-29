package net.Indyuce.mmocore.api.event.unlocking;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ItemUnlockedEvent extends ItemChangeEvent {
    private static final HandlerList handlers = new HandlerList();

    public ItemUnlockedEvent(PlayerData playerData, String itemKey) {
        super(playerData, itemKey);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
