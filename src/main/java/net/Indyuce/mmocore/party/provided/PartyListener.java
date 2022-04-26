package net.Indyuce.mmocore.party.provided;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.social.PartyChatEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.ConfigManager.SimpleMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PartyListener implements Listener {
	private final MMOCorePartyModule module;

	public PartyListener(MMOCorePartyModule module) {
		this.module = module;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void a(AsyncPlayerChatEvent event) {
		if (!event.getMessage().startsWith(MMOCore.plugin.configManager.partyChatPrefix))
			return;

		PlayerData data = PlayerData.get(event.getPlayer());
		Party party = module.getParty(data);
		if (party == null)
			return;

		event.setCancelled(true);

		// Running it in a delayed task is recommended
		Bukkit.getScheduler().scheduleSyncDelayedTask(MMOCore.plugin, () -> {
			SimpleMessage format = MMOCore.plugin.configManager.getSimpleMessage("party-chat", "player", data.getPlayer().getName(), "message",
					event.getMessage().substring(MMOCore.plugin.configManager.partyChatPrefix.length()));
			PartyChatEvent called = new PartyChatEvent(party, data, format.message());
			Bukkit.getPluginManager().callEvent(called);
			if (!called.isCancelled())
				party.getOnlineMembers().forEach(member -> format.send(member.getPlayer()));
		});
	}
}
