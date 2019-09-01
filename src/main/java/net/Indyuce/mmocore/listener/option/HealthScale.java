package net.Indyuce.mmocore.listener.option;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.Indyuce.mmocore.MMOCore;

public class HealthScale implements Listener {
	private final double scale = MMOCore.plugin.getConfig().getDouble("health-scale.scale");

	@EventHandler
	public void a(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		player.setHealthScaled(true);
		player.setHealthScale(scale);
	}
}
