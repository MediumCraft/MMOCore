package net.Indyuce.mmocore.command;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.MMOCommandEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.Request;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.party.provided.MMOCorePartyModule;
import net.Indyuce.mmocore.party.provided.PartyInvite;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PartyCommand extends BukkitCommand {

    public PartyCommand(ConfigurationSection config) {
        super(config.getString("main"));

        setAliases(config.getStringList("aliases"));
        setDescription("Opens the party menu.");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is for players only.");
            return true;
        }

        PlayerData data = PlayerData.get((OfflinePlayer) sender);
        MMOCommandEvent event = new MMOCommandEvent(data, "party");
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return true;

        if (args.length > 0) {

            final @Nullable PartyInvite invite;
            if (args.length > 1)

                // Search by request ID
                try {
                    final Request req = MMOCore.plugin.requestManager.getRequest(UUID.fromString(args[1]));
                    Validate.isTrue(req instanceof PartyInvite && !req.isTimedOut());
                    invite = (PartyInvite) req;
                    Validate.isTrue(((MMOCorePartyModule) MMOCore.plugin.partyModule).isRegistered(invite.getParty()));
                } catch (Exception exception) {
                    return true;
                }

                // Search by target player
            else
                invite = MMOCore.plugin.requestManager.findRequest(data, PartyInvite.class);

            // No invite found with given identifier/target player
            if (invite == null)
                return true;

            if (args[0].equalsIgnoreCase("accept"))
                invite.accept();
            else if (args[0].equalsIgnoreCase("deny"))
                invite.deny();
            return true;
        }

        if (data.getParty() != null)
            InventoryManager.PARTY_VIEW.newInventory(data).open();
        else
            InventoryManager.PARTY_CREATION.newInventory(data).open();
        return true;
    }
}
