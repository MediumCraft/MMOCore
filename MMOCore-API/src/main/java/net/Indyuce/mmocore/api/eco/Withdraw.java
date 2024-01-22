package net.Indyuce.mmocore.api.eco;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.util.item.CurrencyItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import io.lumine.mythic.lib.api.util.SmartGive;

public class Withdraw implements Listener {
	private static final Set<UUID> withdrawing = new HashSet<>();

	private final Player player;

	public Withdraw(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	public void open() {
		if (isWithdrawing())
			return;

		withdrawing.add(player.getUniqueId());
		ConfigMessage.fromKey("withdrawing").send(player);
		Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
		Bukkit.getScheduler().runTaskLater(MMOCore.plugin, this::close, 20 * 20);
	}

	public void close() {
		HandlerList.unregisterAll(this);
		withdrawing.remove(player.getUniqueId());
	}

	public boolean isWithdrawing() {
		return withdrawing.contains(player.getUniqueId());
	}

	@EventHandler
	public void a(PlayerMoveEvent event) {
		if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ())
			return;

		if (!event.getPlayer().equals(player))
			return;

		ConfigMessage.fromKey("withdraw-cancel").send(player);
		close();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void b(AsyncPlayerChatEvent event) {
		if (!event.getPlayer().equals(player))
			return;

		event.setCancelled(true);

		final int worth;
		try {
			worth = Integer.parseInt(event.getMessage());
		} catch (Exception e) {
			ConfigMessage.fromKey("wrong-number").addPlaceholders("arg", event.getMessage()).send(player);
			return;
		}

		int left = (int) (MMOCore.plugin.economy.getEconomy().getBalance(player) - worth);
		if (left < 0) {
			ConfigMessage.fromKey("not-enough-money").addPlaceholders("left", -left).send(player);
			return;
		}

		close();

		Bukkit.getScheduler().runTask(MMOCore.plugin, () -> {
			MMOCore.plugin.economy.getEconomy().withdrawPlayer(player, worth);
			withdrawAlgorythm(worth);
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
			ConfigMessage.fromKey("withdrew").addPlaceholders("worth", worth).send(player);
		});
	}

	public void withdrawAlgorythm(int worth) {
		int note = worth / 10 * 10;
		int coins = worth - note;

		SmartGive smart = new SmartGive(player);
		if (note > 0)
			smart.give(new CurrencyItemBuilder("NOTE", note).build());

		ItemStack coinsItem = new CurrencyItemBuilder("GOLD_COIN", 1).build();
		coinsItem.setAmount(coins);
		smart.give(coinsItem);
	}

	/*
	 * extra safety
	 */
	@EventHandler
	public void c(PlayerQuitEvent event) {
		if (event.getPlayer().equals(player))
			close();
	}
}
