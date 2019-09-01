package net.Indyuce.mmocore.listener.option;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;

public class VanillaExperienceOverride implements Listener {

	/*
	 * when picking up exp orbs or any action like that
	 */
	@EventHandler
	public void a(PlayerExpChangeEvent event) {
		event.setAmount(0);
	}

	/*
	 * this event is not supported by the expChangeEvent. since the event is
	 * actually called before applying the enchant and consuming levels, we must
	 * update the player level using a delayed task. setExpLevelCost(level) DOES
	 * NOT WORK
	 */
	@EventHandler
	public void b(EnchantItemEvent event) {
		Bukkit.getScheduler().runTask(MMOCore.plugin, () -> event.getEnchanter().setLevel(PlayerData.get(event.getEnchanter()).getLevel()));
	}
}
