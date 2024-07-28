package net.Indyuce.mmocore.command;

import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.event.MMOCommandEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.api.RegisteredCommand;
import net.Indyuce.mmocore.command.api.ToggleableCommand;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class SkillTreesCommand extends RegisteredCommand {
    public SkillTreesCommand(ConfigurationSection config) {
        super(config, ToggleableCommand.SKILL_TREES);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, String s, String[] args) {
        if (!sender.hasPermission("mmocore.skilltrees"))
            return false;
        if (!(sender instanceof Player))
            return false;
        final Player player = (Player) sender;
        PlayerData data = PlayerData.get(player);
        MMOCommandEvent event = new MMOCommandEvent(data, "skilltrees");
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;

        // Default skilltree command
        if (args.length == 0) {
            if (!MMOCore.plugin.configManager.enableGlobalSkillTreeGUI) {
                sender.sendMessage(ChatColor.RED + "Usage: /skilltrees <skilltree_id>");
                return false;
            }
            if (data.getProfess().getSkillTrees().size() != 0) {
                InventoryManager.TREE_VIEW.newInventory(data).open();
                return true;
            } else {
                ConfigMessage.fromKey("no-skill-tree").send(player);
                return false;
            }
        }
        if (args.length == 1) {
            if (!MMOCore.plugin.configManager.enableSpecificSkillTreeGUI) {
                sender.sendMessage(ChatColor.RED + "Usage: /skilltrees <skilltree-id>");
                return false;
            }

            if (data.getProfess().getSkillTrees()
                    .stream()
                    .filter(skillTree -> UtilityMethods.ymlName(skillTree.getId()).equals(UtilityMethods.ymlName(args[0])))
                    .collect(Collectors.toList())
                    .size() != 0) {
                InventoryManager.SPECIFIC_TREE_VIEW.get(UtilityMethods.ymlName(args[0])).newInventory(data).open();
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Your class does not have a skill tree with id: " + args[0]);
                return false;
            }
        } else {
            if (MMOCore.plugin.configManager.enableSpecificSkillTreeGUI)
                sender.sendMessage(ChatColor.RED + "Usage: /skilltrees <skilltree-id>");
            else
                sender.sendMessage(ChatColor.RED + "Usage: /skilltrees");
            return false;
        }
    }
}
