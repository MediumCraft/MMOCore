package net.Indyuce.mmocore.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;

public class SkillsCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			PlayerData data = PlayerData.get((Player) sender);
			if (data.getProfess().getSkills().size() < 1) {
				sender.sendMessage(MMOCore.plugin.configManager.getSimpleMessage("no-class-skill"));
				return true;
			}

			InventoryManager.SKILL_LIST.newInventory(data).open();
		}
		return true;
	}
}
