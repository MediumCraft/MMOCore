package net.Indyuce.mmocore.gui.api;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public abstract class PluginInventory implements InventoryHolder {
    protected final Player player;
    protected final PlayerData playerData;
    /**
     * If all the clicks sould be cancelled for the inventory
     */
    private boolean shouldCancel = true;

    public PluginInventory(PlayerData playerData) {
        this.playerData = playerData;
        player = playerData.getPlayer();
    }

    public PluginInventory(Player player) {
        this.player = player;
        this.playerData = player.getOpenInventory() != null && player.getOpenInventory().getTopInventory().getHolder() instanceof PluginInventory ? ((PluginInventory) player.getOpenInventory().getTopInventory().getHolder()).playerData : PlayerData.get(player);
    }

    public PluginInventory(Player player, boolean shouldCancel) {
        this(player);
        this.shouldCancel=shouldCancel;
    }
    public PluginInventory(PlayerData playerData, boolean shouldCancel) {
        this(playerData);
        this.shouldCancel=shouldCancel;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean shouldCancel() {
        return shouldCancel;
    }

    /**
     * Opens classic inventory, throws
     */
    public void open() {
        getPlayer().openInventory(getInventory());
    }

    public abstract void whenClicked(InventoryClickContext context);

    public void whenClosed(InventoryCloseEvent event) {
    }
}
