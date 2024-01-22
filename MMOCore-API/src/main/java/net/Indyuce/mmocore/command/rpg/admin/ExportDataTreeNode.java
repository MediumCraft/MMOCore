package net.Indyuce.mmocore.command.rpg.admin;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.data.sql.SQLDataSource;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.data.sql.SQLDataHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.text.DecimalFormat;
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

    /**
     * Amount of requests generated every batch
     */
    private static final int BATCH_AMOUNT = 50;

    /**
     * Period in ticks
     */
    private static final int BATCH_PERIOD = 20;

    private static final DecimalFormat decFormat = new DecimalFormat("0.#");

    @Override
    public CommandResult execute(CommandSender sender, String[] strings) {

        if (!MMOCore.plugin.dataProvider.getDataManager().getLoaded().isEmpty()) {
            sender.sendMessage("Please make sure no players are logged in when using this command. " +
                    "If you are still seeing this message, restart your server and execute this command before any player logs in.");
            return CommandResult.FAILURE;
        }

        final List<UUID> playerIds = Arrays.stream(new File(MMOCore.plugin.getDataFolder() + "/userdata").listFiles())
                .map(file -> UUID.fromString(file.getName().replace(".yml", ""))).collect(Collectors.toList());

        // Initialize fake SQL data provider
        final SQLDataHandler sqlHandler;
        try {
            sqlHandler = new SQLDataHandler(new SQLDataSource(MMOCore.plugin));
        } catch (RuntimeException exception) {
            sender.sendMessage("Could not initialize SQL provider (see console for stack trace): " + exception.getMessage());
            return CommandResult.FAILURE;
        }

        final double timeEstimation = (double) playerIds.size() / BATCH_AMOUNT * BATCH_PERIOD / 20;
        sender.sendMessage("Exporting " + playerIds.size() + " player data(s).. See console for details");
        sender.sendMessage("Minimum expected time: " + decFormat.format(timeEstimation) + "s");

        // Save player data
        new BukkitRunnable() {
            int errorCount = 0;
            int batchCounter = 0;

            @Override
            public void run() {
                for (int i = 0; i < BATCH_AMOUNT; i++) {
                    final int index = BATCH_AMOUNT * batchCounter + i;

                    /*
                     * Saving is done. Close connection to avoid memory
                     * leaks and ouput the results to the command executor
                     */
                    if (index >= playerIds.size()) {
                        cancel();

                        sqlHandler.close();
                        MMOCore.plugin.getLogger().log(Level.WARNING, "Exported " + playerIds.size() + " player datas to SQL database. Total errors: " + errorCount);
                        return;
                    }

                    final UUID playerId = playerIds.get(index);
                    try {
                        final PlayerData offlinePlayerData = new PlayerData(new MMOPlayerData(playerId));
                        MMOCore.plugin.dataProvider.getDataManager().getDataHandler().loadData(offlinePlayerData);

                        // Player data is loaded, now it gets saved through SQL
                        sqlHandler.saveData(offlinePlayerData, false);
                    } catch (RuntimeException exception) {
                        errorCount++;
                        exception.printStackTrace();
                    }
                }

                batchCounter++;
            }
        }.runTaskTimerAsynchronously(MMOCore.plugin, 0, BATCH_PERIOD);

        return CommandResult.SUCCESS;
    }
}
