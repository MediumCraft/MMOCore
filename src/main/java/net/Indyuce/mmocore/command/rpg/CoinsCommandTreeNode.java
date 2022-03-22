package net.Indyuce.mmocore.command.rpg;

import io.lumine.mythic.lib.api.util.SmartGive;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmocore.util.item.CurrencyItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class CoinsCommandTreeNode extends CommandTreeNode {
    public CoinsCommandTreeNode(CommandTreeNode parent) {
        super(parent, "coins");

        addParameter(Parameter.PLAYER);
        addParameter(Parameter.AMOUNT);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 3)
            return CommandResult.THROW_USAGE;

        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[1] + ".");
            return CommandResult.FAILURE;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + args[2] + " is not a valid number.");
            return CommandResult.FAILURE;
        }

        ItemStack coins = new CurrencyItemBuilder("GOLD_COIN", 1).build();
        coins.setAmount(amount);
        new SmartGive(player).give(coins);
        return CommandResult.SUCCESS;
    }

}
