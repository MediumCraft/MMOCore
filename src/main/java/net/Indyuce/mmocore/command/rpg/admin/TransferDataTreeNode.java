package net.Indyuce.mmocore.command.rpg.admin;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.SavingPlayerData;
import net.Indyuce.mmocore.guild.provided.Guild;
import net.Indyuce.mmocore.manager.data.DataProvider;
import net.Indyuce.mmocore.manager.data.mysql.MySQLDataProvider;
import net.Indyuce.mmocore.manager.data.yaml.YAMLDataProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * This command allows to transfer data from your actual storage type
 * to the other one which lets the user switch between storage types.
 */
public class TransferDataTreeNode extends CommandTreeNode {
    public TransferDataTreeNode(CommandTreeNode parent) {
        super(parent, "transferdata");
    }

    @Override
    public CommandResult execute(CommandSender commandSender, String[] strings) {

        final List<UUID> playerUUIDs = new ArrayList<>();
        if (MMOCore.plugin.dataProvider instanceof YAMLDataProvider) {
            File folder = new File(MMOCore.plugin.getDataFolder() + "/userdata");
            playerUUIDs.addAll(Arrays.stream(folder.listFiles())
                    .map(file -> UUID.fromString(file.getName().replace(".yml", "")))
                    .collect(Collectors.toList()));
        } else {
            ((MySQLDataProvider) MMOCore.plugin.dataProvider).getResult(
                    "SELECT uuid from mmocore_playerdata", (result) -> {
                        try {

                            while (result.next()) {
                                playerUUIDs.add(UUID.fromString(result.getString("uuid")));

                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
        }

        List<SavingPlayerData> savingPlayerDataList = new ArrayList<>();
        for (UUID uuid : playerUUIDs) {
            MMOCore.plugin.dataProvider.getDataManager().loadSavingPlayerData(uuid, savingPlayerDataList);
        }


        final DataProvider provider;
        if (MMOCore.plugin.dataProvider instanceof YAMLDataProvider) {
            provider = new MySQLDataProvider(MMOCore.plugin.getConfig());
            ((MySQLDataProvider) provider).load();
        } else {
            provider = new YAMLDataProvider();
        }


        //5 seconds later we put all this data into the other data storage.
        new BukkitRunnable() {

            @Override
            public void run() {
                try {
                    // Save player data
                    for (SavingPlayerData data : savingPlayerDataList)
                        provider.getDataManager().saveData(data);

                    // Save guild info
                    for (Guild guild : provider.getGuildManager().getAll())
                        provider.getGuildManager().save(guild);
                } catch (Exception e) {
                    commandSender.sendMessage("Couldn't transfer properly the data.");
                    e.printStackTrace();
                    if (provider != null && provider instanceof MySQLDataProvider) {
                        ((MySQLDataProvider) provider).close();
                    }
                }
            }
        }.runTaskLater(MMOCore.plugin, 100L);


        //We close the connection 10 s later to avoid memory leaks.
        new BukkitRunnable() {

            @Override
            public void run() {
                if (provider != null && provider instanceof MySQLDataProvider) {
                    ((MySQLDataProvider) provider).close();
                }
            }
        }.runTaskLater(MMOCore.plugin, 200);

        return CommandResult.SUCCESS;
    }
}
