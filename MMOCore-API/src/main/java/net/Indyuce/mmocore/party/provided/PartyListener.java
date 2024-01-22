package net.Indyuce.mmocore.party.provided;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.event.social.PartyChatEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.AbstractParty;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PartyListener implements Listener {
    private final MMOCorePartyModule module;

    public PartyListener(MMOCorePartyModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void partyChat(AsyncPlayerChatEvent event) {
        if (!event.getMessage().startsWith(MMOCore.plugin.configManager.partyChatPrefix))
            return;

        PlayerData data = PlayerData.get(event.getPlayer());
        Party party = module.getParty(data);
        if (party == null)
            return;

        event.setCancelled(true);

        // Running it in a delayed task is recommended
        Bukkit.getScheduler().runTask(MMOCore.plugin, () -> {
            ConfigMessage message = ConfigMessage.fromKey("party-chat", "player", data.getPlayer().getName(), "message",
                    event.getMessage().substring(MMOCore.plugin.configManager.partyChatPrefix.length()));
            PartyChatEvent called = new PartyChatEvent(party, data, message.asLine());
            Bukkit.getPluginManager().callEvent(called);
            if (!called.isCancelled()) party.getOnlineMembers().forEach(member -> message.send(member.getPlayer()));
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void leavePartyOnQuit(PlayerQuitEvent event) {
        final PlayerData playerData = PlayerData.get(event.getPlayer());
        final AbstractParty party = playerData.getParty();
        if (party != null)
            ((Party) party).removeMember(playerData);
    }
}
