package net.Indyuce.mmocore.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.social.PartyChatEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.ConfigManager.SimpleMessage;
import net.mmogroup.mmolib.api.event.PlayerAttackEvent;

public class PartyListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void a(AsyncPlayerChatEvent event) {
		if (!event.getMessage().startsWith(MMOCore.plugin.configManager.partyChatPrefix))
			return;

		PlayerData data = PlayerData.get(event.getPlayer());
		if (!data.hasParty())
			return;

		event.setCancelled(true);

		/*
		 * running it in a delayed task is recommended
		 */
		Bukkit.getScheduler().scheduleSyncDelayedTask(MMOCore.plugin, () -> {
			SimpleMessage format = MMOCore.plugin.configManager.getSimpleMessage("party-chat", "player", data.getPlayer().getName(), "message",
					event.getMessage().substring(MMOCore.plugin.configManager.partyChatPrefix.length()));
			PartyChatEvent called = new PartyChatEvent(data, format.message());
			Bukkit.getPluginManager().callEvent(called);
			if (!called.isCancelled())
				data.getParty().getMembers().forEach(member -> {
					if (member.isOnline())
						format.send(member.getPlayer());
				});
		});
	}

	/*
	 * cancel damage of players from the same party
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void b(PlayerAttackEvent event) {
            LivingEntity entity = event.getEntity();
		if (entity instanceof Player) {
		        if (entity.hasMetadata("NPC"))
		            return;
			PlayerData targetData = PlayerData.get((Player) event.getEntity());
			if (targetData.hasParty() && targetData.getParty().getMembers().has(event.getData().getMMOCore()))
				event.setCancelled(true);
		}
	}
}
