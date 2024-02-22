package net.Indyuce.mmocore.api.util.input;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ChatInput extends PlayerInput {
    private final InputType inputType;
    private final PluginInventory lastOpened;

    @Deprecated
    public ChatInput(@NotNull Player player, @NotNull InputType inputType, @NotNull Consumer<String> output) {
        this(player, inputType, null, output);
    }

    /**
     * Have a player input a string in the global chat
     *
     * @param player     Player requesting chat input
     * @param inputType  Type of chat input
     * @param lastOpened Inventory opened again if 'cancel' is input. Set to null to disable
     * @param output     What to do when input is detected
     */
    public ChatInput(@NotNull Player player, @NotNull InputType inputType, @Nullable PluginInventory lastOpened, @NotNull Consumer<String> output) {
        super(player, output);

        this.inputType = inputType;
        this.lastOpened = lastOpened;

        player.closeInventory();
        ConfigMessage.fromKey("player-input.chat." + inputType.getLowerCaseName()).send(player);
    }

    @Override
    public void close() {
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
        InventoryOpenEvent.getHandlerList().unregister(this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void registerInput(AsyncPlayerChatEvent event) {
        if (event.getPlayer().equals(getPlayer())) {
            close();
            event.setCancelled(true);

            if (event.getMessage().equals("cancel")) {
                if (lastOpened != null)
                    Bukkit.getScheduler().runTask(MMOCore.plugin, () -> lastOpened.open());
                ConfigMessage.fromKey("player-input.chat." + inputType.getLowerCaseName() + "-cancel").send(getPlayer());
            } else
                // Run sync
                Bukkit.getScheduler().runTask(MMOCore.plugin, () -> output(event.getMessage()));
        }
    }

    @EventHandler
    public void b(InventoryOpenEvent event) {
        if (event.getPlayer().equals(getPlayer()))
            close();
    }
}
