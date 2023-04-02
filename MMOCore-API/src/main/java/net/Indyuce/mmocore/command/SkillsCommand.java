package net.Indyuce.mmocore.command;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.api.RegisteredCommand;
import net.Indyuce.mmocore.command.api.ToggleableCommand;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.api.event.MMOCommandEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class SkillsCommand extends RegisteredCommand {
    public SkillsCommand(ConfigurationSection config) {
        super(config, ToggleableCommand.SKILLS);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player) {
            PlayerData data = PlayerData.get((Player) sender);
            MMOCommandEvent event = new MMOCommandEvent(data, "skills");
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) return true;

            if (data.getProfess().getSkills()
                    .stream()
                    .filter((classSkill) -> data.hasUnlocked(classSkill.getSkill()))
                    .sorted((classSkill1,classSkill2)->classSkill1.getUnlockLevel()-classSkill1.getUnlockLevel())
                    .collect(Collectors.toList())
                    .size() < 1) {
                MMOCore.plugin.configManager.getSimpleMessage("no-class-skill").send((Player) sender);
                return true;
            }

            InventoryManager.SKILL_LIST.newInventory(data).open();
        }
        return true;
    }
}
