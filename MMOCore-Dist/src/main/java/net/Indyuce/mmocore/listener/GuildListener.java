package net.Indyuce.mmocore.listener;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.event.social.GuildChatEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class GuildListener implements Listener {
	@EventHandler(priority = EventPriority.LOW)
	public void a(AsyncPlayerChatEvent event) {
		if (!event.getMessage().startsWith(MMOCore.plugin.dataProvider.getGuildManager().getConfig().getPrefix()))
			return;

		PlayerData data = PlayerData.get(event.getPlayer());
		if (!data.inGuild())
			return;

		event.setCancelled(true);

		// Run it sync
		Bukkit.getScheduler().runTask(MMOCore.plugin, () -> {
			ConfigMessage format = ConfigMessage.fromKey("guild-chat", "player", data.getPlayer().getName(), "tag", data.getGuild().getTag(), "message", event.getMessage().substring(MMOCore.plugin.dataProvider.getGuildManager().getConfig().getPrefix().length()));
			GuildChatEvent called = new GuildChatEvent(data, format.asLine());
			Bukkit.getPluginManager().callEvent(called);
			if (!called.isCancelled())
				data.getGuild().forEachMember(member -> {
					Player p = Bukkit.getPlayer(member);
					if (p != null)
						format.send(p);
				});
		});
	}
}
