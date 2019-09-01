package net.Indyuce.mmocore.listener.option;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;

public class VanillaExperienceOverride implements Listener {

	@EventHandler
	public void a(PlayerExpChangeEvent event) {
		event.setAmount(0);
	}

	@EventHandler
	public void b(EnchantItemEvent event) {
		Bukkit.getScheduler().runTask(MMOCore.plugin, () -> event.getEnchanter().setLevel(PlayerData.get(event.getEnchanter()).getLevel()));
	}
}
