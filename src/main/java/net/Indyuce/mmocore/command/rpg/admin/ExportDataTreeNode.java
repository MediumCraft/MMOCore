package net.Indyuce.mmocore.command.rpg.admin;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.data.mysql.MySQLDataProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;


/**
 * This command allows to transfer data from your actual storage type
 * to the other one which lets the user switch between storage types.
 */
public class ExportDataTreeNode extends CommandTreeNode {
    public ExportDataTreeNode(CommandTreeNode parent) {
        super(parent, "exportdata");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] strings) {

        final List<UUID> playerIds = Arrays.stream(new File(MMOCore.plugin.getDataFolder() + "/userdata").listFiles())
                .map(file -> UUID.fromString(file.getName().replace(".yml", ""))).collect(Collectors.toList());

        // Initialize fake SQL data provider
        final MySQLDataProvider sqlProvider;
        try {
            sqlProvider = new MySQLDataProvider(MMOCore.plugin.getConfig());
        } catch (RuntimeException exception) {
            sender.sendMessage("Could not initialize SQL provider (see console for stack trace): " + exception.getMessage());
            return CommandResult.FAILURE;
        }

        sender.sendMessage("Exporting " + playerIds.size() + " player data(s).. See console for details");

        // Save player data
        Bukkit.getScheduler().runTaskAsynchronously(MMOCore.plugin, () -> {

            // Loop first for garbage collection
            int errorCount = 0;
            for (UUID playerId : playerIds)
                try {
                    final PlayerData offlinePlayerData = new PlayerData(new MMOPlayerData(playerId));
                    MMOCore.plugin.dataProvider.getDataManager().loadData(offlinePlayerData);

                    // Player data is loaded, now it gets saved through SQL
                    sqlProvider.getDataManager().saveData(offlinePlayerData);
                } catch (RuntimeException exception) {
                    errorCount++;
                    exception.printStackTrace();
                }

            MMOCore.plugin.getLogger().log(Level.WARNING, "Exported player data to SQL database. Total errors: " + errorCount);

            // Close connection to avoid memory leaks
            sqlProvider.close();

        });

        return CommandResult.SUCCESS;
    }
}
