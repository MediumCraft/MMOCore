package net.Indyuce.mmocore.gui.api;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public abstract class PluginInventory implements InventoryHolder {
    protected final Player player;
    protected final PlayerData playerData;

    public PluginInventory(Player player) {
        this.player = player;
        this.playerData = player.getOpenInventory() != null && player.getOpenInventory().getTopInventory().getHolder() instanceof PluginInventory ? ((PluginInventory) player.getOpenInventory().getTopInventory().getHolder()).playerData : PlayerData.get(player);
    }

    public PluginInventory(PlayerData playerData) {
        this.player = playerData.getPlayer();
        this.playerData = playerData;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public Player getPlayer() {
        return player;
    }

    public void open() {
        getPlayer().openInventory(getInventory());
    }

    public abstract void whenClicked(InventoryClickContext context);

    public void whenClosed(InventoryCloseEvent event) {
    }
}
