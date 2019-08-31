package net.Indyuce.mmocore.api.player;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerCombatEvent;

public class CombatRunnable extends BukkitRunnable {
	private final PlayerData player;

	private long lastHit = System.currentTimeMillis();

	public CombatRunnable(PlayerData player) {
		this.player = player;

		player.getPlayer().sendMessage(MMOCore.plugin.configManager.getSimpleMessage("now-in-combat"));
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
			player.getPlayer().sendMessage(MMOCore.plugin.configManager.getSimpleMessage("leave-combat"));
			close();
		}
	}

	private void close() {
		player.combat = null;
		cancel();
	}
}
