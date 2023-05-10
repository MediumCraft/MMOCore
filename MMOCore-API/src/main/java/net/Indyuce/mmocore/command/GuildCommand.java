package net.Indyuce.mmocore.command;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.api.RegisteredCommand;
import net.Indyuce.mmocore.command.api.ToggleableCommand;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.api.event.MMOCommandEvent;
import net.Indyuce.mmocore.api.player.social.Request;
import net.Indyuce.mmocore.guild.provided.GuildInvite;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GuildCommand extends RegisteredCommand {
    public GuildCommand(ConfigurationSection config) {
        super(config, ToggleableCommand.GUILD);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("mmocore.guild"))
            return false;
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is for players only.");
            return true;
        }

        PlayerData data = PlayerData.get((OfflinePlayer) sender);
        MMOCommandEvent event = new MMOCommandEvent(data, "guild");
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return true;

        if (args.length >= 1) {

            final @Nullable GuildInvite invite;
            if (args.length > 1)

                // Search by request ID
                try {
                    final UUID uuid = UUID.fromString(args[1]);
                    final Request req = MMOCore.plugin.requestManager.getRequest(uuid);
                    Validate.isTrue(!req.isTimedOut() && req instanceof GuildInvite);
                    invite = (GuildInvite) req;
                    Validate.isTrue(MMOCore.plugin.dataProvider.getGuildManager().isRegistered(invite.getGuild()));
                } catch (Exception exception) {
                    return true;
                }

                // Search by target player
            else
                invite = MMOCore.plugin.requestManager.findRequest(data, GuildInvite.class);

            // No invite found with given identifier/target player
            if (invite == null)
                return true;

            if (args[0].equalsIgnoreCase("accept"))
                invite.accept();
            if (args[0].equalsIgnoreCase("deny"))
                invite.deny();
            return true;
        }

        if (data.inGuild())
            InventoryManager.GUILD_VIEW.newInventory(data).open();
        else
            InventoryManager.GUILD_CREATION.newInventory(data).open();
        return true;
    }
}
