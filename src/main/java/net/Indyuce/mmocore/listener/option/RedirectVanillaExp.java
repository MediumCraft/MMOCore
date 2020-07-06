package net.Indyuce.mmocore.listener.option;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

import net.Indyuce.mmocore.api.experience.EXPSource;
import net.Indyuce.mmocore.api.player.PlayerData;

public class RedirectVanillaExp implements Listener {
	private final double ratio;

	public RedirectVanillaExp(double ratio) {
		this.ratio = ratio;
	}

	@EventHandler
	public void a(PlayerExpChangeEvent event) {
		int a = (int) (event.getAmount() * ratio);
		if (a > 0)
			PlayerData.get(event.getPlayer()).giveExperience(a, EXPSource.VANILLA);
	}
}
