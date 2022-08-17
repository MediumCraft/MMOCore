package net.Indyuce.mmocore.command.rpg.admin;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import io.lumine.mythic.utils.functions.TriConsumer;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.CommandVerbose;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class SkillTreePointsCommandTreeNode extends CommandTreeNode {
    BiFunction<PlayerData, String,Integer> get;

    public SkillTreePointsCommandTreeNode(CommandTreeNode parent, TriConsumer<PlayerData, Integer, String> set,
                                          TriConsumer<PlayerData, Integer, String> give, BiFunction<PlayerData, String,Integer> get) {
        super(parent, "skill-tree-points");
        addChild(new ActionCommandTreeNode(this, "give", give));
        addChild(new ActionCommandTreeNode(this, "set", set));
        this.get = get;
    }

    @Override
    public CommandResult execute(CommandSender commandSender, String[] strings) {
        return CommandResult.THROW_USAGE;
    }

    public class ActionCommandTreeNode extends CommandTreeNode {
        private final TriConsumer<PlayerData, Integer, String> action;


        public ActionCommandTreeNode(CommandTreeNode parent, String id, TriConsumer<PlayerData, Integer, String> action) {
            super(parent, id);
            this.action = action;
            addParameter(Parameter.PLAYER);
            addParameter(Parameter.AMOUNT);
            addParameter(new Parameter("<type>", ((explorer, list) -> {
                MMOCore.plugin.skillTreeManager.getAll().forEach(tree -> list.add(tree.getId()));
                list.add("global");
            })));
        }

        @Override
        public CommandResult execute(CommandSender sender, String[] args) {
            if (args.length < 6)
                return CommandResult.THROW_USAGE;
            Player player = Bukkit.getPlayer(args[3]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[3] + ".");
                return CommandResult.FAILURE;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[4]);
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + args[4] + " is not a valid number.");
                return CommandResult.FAILURE;
            }
            List<String> list = new ArrayList();
            MMOCore.plugin.skillTreeManager.getAll().forEach(tree -> list.add(tree.getId()));
            list.add("global");

            String id = args[5];

            if (!list.contains(id)) {
                sender.sendMessage("Could not find the type of points " + id + ".");
                return CommandResult.FAILURE;
            }

            PlayerData data = PlayerData.get(player);
            action.accept(data, amount, args[5]);
            CommandVerbose.verbose(sender, CommandVerbose.CommandType.SKILL_TREE_POINTS, ChatColor.GOLD + player.getName()
                    + ChatColor.YELLOW + " now has " + ChatColor.GOLD + get.apply(data, id) + ChatColor.YELLOW + " " + id + " skill tree points.");

            return CommandResult.SUCCESS;
        }
    }

}
