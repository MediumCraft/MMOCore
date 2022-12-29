package net.Indyuce.mmocore.command;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.FriendRequest;
import net.Indyuce.mmocore.command.api.RegisteredCommand;
import net.Indyuce.mmocore.command.api.ToggleableCommand;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.api.event.MMOCommandEvent;
import net.Indyuce.mmocore.api.player.social.Request;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FriendsCommand extends RegisteredCommand {
    public FriendsCommand(ConfigurationSection config) {
        super(config, ToggleableCommand.FRIENDS);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is for players only.");
            return true;
        }

        PlayerData data = PlayerData.get((Player) sender);
        MMOCommandEvent event = new MMOCommandEvent(data, "friends");
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return true;

        if (args.length > 1) {

            final @Nullable FriendRequest invite;
            if (args.length > 1)

                // Search by request ID
                try {
                    final UUID uuid = UUID.fromString(args[1]);
                    final Request req = MMOCore.plugin.requestManager.getRequest(uuid);
                    Validate.isTrue(!req.isTimedOut() && req instanceof FriendRequest);
                    Validate.isTrue(!data.hasFriend(req.getCreator().getUniqueId()));
                    invite = (FriendRequest) req;
                } catch (Exception exception) {
                    return true;
                }

                // Search by target player
            else
                invite = MMOCore.plugin.requestManager.findRequest(data, FriendRequest.class);

            // No invite found with given identifier/target player
            if (invite == null)
                return true;

            if (args[0].equalsIgnoreCase("accept"))
                invite.accept();
            if (args[0].equalsIgnoreCase("deny"))
                invite.deny();
            return true;
        }

        InventoryManager.FRIEND_LIST.newInventory(data).open();
        return true;
    }
}
