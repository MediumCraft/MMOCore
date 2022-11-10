package net.Indyuce.mmocore.command;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.event.MMOCommandEvent;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SkillTreeCommand extends BukkitCommand {
    public SkillTreeCommand(ConfigurationSection config) {
        super(config.getString("main"));

        setAliases(config.getStringList("aliases"));
        setDescription("Opens the skills menu.");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, String s, String[] args) {
        if (!(sender instanceof Player player))
            return false;
        PlayerData data = PlayerData.get(player);
        MMOCommandEvent event = new MMOCommandEvent(data, "skilltrees");
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled())
            return true;
        if (data.getProfess().getSkillTrees().size() != 0) {
            InventoryManager.TREE_VIEW.newInventory(data).open();
            return false;
        }
        else {
            MMOCore.plugin.configManager.getSimpleMessage("no-skill-tree").send(player);
            return true;
        }

    }


}
