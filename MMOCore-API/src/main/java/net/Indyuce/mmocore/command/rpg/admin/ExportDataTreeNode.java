package net.Indyuce.mmocore.command.rpg.admin;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.data.DataExport;
import io.lumine.mythic.lib.data.sql.SQLDataSource;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.manager.data.sql.SQLDataHandler;
import net.Indyuce.mmocore.manager.data.yaml.YAMLPlayerDataHandler;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * This command allows to transfer data from your actual storage type
 * to the other one which lets the user switch between storage types.
 */
public class ExportDataTreeNode extends CommandTreeNode {
    public ExportDataTreeNode(CommandTreeNode parent) {
        super(parent, "exportdata");
    }

    @Override
    @NotNull
    public CommandResult execute(CommandSender sender, String[] strings) {

        // Export YAML to SQL
        final boolean result = new DataExport<>(MMOCore.plugin.playerDataManager, sender).start(
                () -> new YAMLPlayerDataHandler(MMOCore.plugin),
                () -> new SQLDataHandler(new SQLDataSource(MMOCore.plugin)));

        return result ? CommandResult.SUCCESS : CommandResult.FAILURE;
    }
}
