package net.Indyuce.mmocore.api.player;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerCombatEvent;
import net.Indyuce.mmocore.manager.ConfigManager;

public class CombatRunnable extends BukkitRunnable {
	private final PlayerData player;

	private long lastHit = System.currentTimeMillis();
	private ConfigManager config;

	public CombatRunnable(PlayerData player) {
		this.player = player;

		config = MMOCore.plugin.configManager;
		
		config.getSimpleMessage("now-in-combat").send(player.getPlayer());
		Bukkit.getPluginManager().callEvent(new PlayerCombatEvent(player, true));
		runTaskTimer(MMOCore.plugin, 20, 20);
	}

	public void update() {
		lastHit = System.currentTimeMillis();
	}

	@Override
	public void run() {
		if (lastHit + 10000 < System.currentTimeMillis()) {
			Bukkit.getPluginManager().callEvent(new PlayerCombatEvent(player, false));
			config.getSimpleMessage("leave-combat").send(player.getPlayer());
			close();
		}
	}

	private void close() {
		player.combat = null;
		cancel();
	}
}
