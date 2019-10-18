package net.Indyuce.mmocore.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.social.GuildChatEvent;
import net.Indyuce.mmocore.api.player.PlayerData;

public class GuildListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void a(AsyncPlayerChatEvent event) {
		if (!event.getMessage().startsWith(MMOCore.plugin.configManager.guildChatPrefix))
			return;

		PlayerData data = PlayerData.get(event.getPlayer());
		if (!data.hasParty())
			return;

		event.setCancelled(true);

		/*
		 * running it in a delayed task is recommended
		 */
		Bukkit.getScheduler().scheduleSyncDelayedTask(MMOCore.plugin, () -> {
			String format = MMOCore.plugin.configManager.getSimpleMessage("guild-chat", "player", data.getPlayer().getName(), "message", event.getMessage().substring(MMOCore.plugin.configManager.guildChatPrefix.length()));
			GuildChatEvent called = new GuildChatEvent(data, format);
			Bukkit.getPluginManager().callEvent(called);
			if (!called.isCancelled()) 			; //remove
				//data.getGuild().members.forEach(member -> {
					//if (member.isOnline())
					//	member.getPlayer().sendMessage(format);
				//});
		});
	}
}
