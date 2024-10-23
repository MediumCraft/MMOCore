package net.Indyuce.mmocore.command.rpg;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import net.Indyuce.mmocore.command.rpg.cast.FirstCommandTreeNode;
import net.Indyuce.mmocore.command.rpg.cast.SpecificCommandTreeNode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CastCommandTreeNode extends CommandTreeNode {
    public CastCommandTreeNode(CommandTreeNode parent) {
        super(parent, "cast");

        addChild(new FirstCommandTreeNode(this));
        addChild(new SpecificCommandTreeNode(this));
    }

    @Override
    public @NotNull CommandResult execute(CommandSender commandSender, String[] strings) {
        return CommandResult.THROW_USAGE;
    }
}
