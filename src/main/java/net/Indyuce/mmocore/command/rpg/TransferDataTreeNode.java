package net.Indyuce.mmocore.command.rpg;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.guild.provided.Guild;
import net.Indyuce.mmocore.manager.data.DataProvider;
import net.Indyuce.mmocore.manager.data.mysql.MySQLDataProvider;
import net.Indyuce.mmocore.manager.data.yaml.YAMLDataProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;


/**
 * This command allows to transfer data from your actual datatype (yaml or sql) to the other one to make a change
 * in the data storage type.
 */
public class TransferDataTreeNode extends CommandTreeNode {


    public TransferDataTreeNode(CommandTreeNode parent) {
        super(parent, "transferdata");
    }

    @Override
    public CommandResult execute(CommandSender commandSender, String[] strings) {
        DataProvider provider=null;

        try {

            if (MMOCore.plugin.dataProvider instanceof YAMLDataProvider) {
                provider = new MySQLDataProvider(MMOCore.plugin.getConfig());
                ((MySQLDataProvider) provider).load();
            } else {
                provider = new YAMLDataProvider();
            }

            // Save player data
            for (PlayerData data : PlayerData.getAll())
                if (data.isFullyLoaded())
                    provider.getDataManager().saveData(data);

            // Save guild info
            for (Guild guild : provider.getGuildManager().getAll())
                provider.getGuildManager().save(guild);
        } catch (Exception e) {
            commandSender.sendMessage("Couldn't transfer properly the data.");
            e.printStackTrace();
            if(provider!=null&&provider instanceof MySQLDataProvider) {
                ((MySQLDataProvider) provider).close();
            }
            return CommandResult.FAILURE;
        }
        DataProvider finalProvider = provider;

        //We close the connection 10 s later to avoid memory leaks.
        new BukkitRunnable() {

            @Override
            public void run() {
                if(finalProvider !=null&& finalProvider instanceof MySQLDataProvider) {
                    ((MySQLDataProvider) finalProvider).close();
                }
            }
        }.runTaskLater(MMOCore.plugin,200);


        return CommandResult.SUCCESS;

    }
}
