package net.Indyuce.mmocore.api.input;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.util.Consumer;

import net.Indyuce.mmocore.MMOCore;

public class ChatInput extends PlayerInput {
	public ChatInput(Player player, InputType type, Consumer<String> output) {
		super(player, output);

		player.closeInventory();
		player.sendMessage(MMOCore.plugin.configManager.getSimpleMessage("player-input.chat." + type.getLowerCaseName()));
	}

	@Override
	public void close() {
		AsyncPlayerChatEvent.getHandlerList().unregister(this);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void a(AsyncPlayerChatEvent event) {
		if (event.getPlayer().equals(getPlayer())) {
			close();
			event.setCancelled(true);
			Bukkit.getScheduler().scheduleSyncDelayedTask(MMOCore.plugin, () -> output(event.getMessage()));
		}
	}

	@EventHandler
	public void b(InventoryOpenEvent event) {
		if (event.getPlayer().equals(getPlayer()))
			close();
	}
}
