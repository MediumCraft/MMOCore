package net.Indyuce.mmocore.guild.provided;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.Request;
import org.bukkit.Bukkit;

public class GuildInvite extends Request {
    private final Guild guild;

    public GuildInvite(Guild guild, PlayerData creator, PlayerData target) {
        super(creator, target);

        this.guild = guild;
    }

    public Guild getGuild() {
        return guild;
    }

    @Override
    public void whenDenied() {
        // Nothing
    }

    @Override
    public void whenAccepted() {
        guild.removeLastInvite(getCreator().getPlayer());
        guild.forEachMember(member -> {
                    if (Bukkit.getPlayer(member) != null) {
                        MMOCore.plugin.configManager.getSimpleMessage("guild-joined-other", "player",
                                getTarget().getPlayer().getName()).send(Bukkit.getPlayer(member));

                        MMOCore.plugin.configManager.getSimpleMessage("guild-joined", "owner",
                                Bukkit.getPlayer(guild.getOwner()).getName()).send(getTarget().getPlayer());
                    }
                }
        );

        guild.addMember(getTarget().getUniqueId());
        InventoryManager.GUILD_VIEW.newInventory(getTarget()).open();
    }
}