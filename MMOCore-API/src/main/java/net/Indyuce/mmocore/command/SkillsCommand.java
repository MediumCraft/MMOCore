package net.Indyuce.mmocore.command;

import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.event.MMOCommandEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.api.RegisteredCommand;
import net.Indyuce.mmocore.command.api.ToggleableCommand;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SkillsCommand extends RegisteredCommand {
	public SkillsCommand(ConfigurationSection config) {
		super(config, ToggleableCommand.SKILLS);
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!sender.hasPermission("mmocore.skills"))
			return false;
		if (sender instanceof Player) {
			PlayerData data = PlayerData.get((Player) sender);
			MMOCommandEvent event = new MMOCommandEvent(data, "skills");
			Bukkit.getServer().getPluginManager().callEvent(event);
			if(event.isCancelled()) return true;
			
			if (data.getUnlockedSkills().isEmpty()) {
				ConfigMessage.fromKey("no-class-skill").send((Player) sender);
				return true;
			}

			InventoryManager.SKILL_LIST.newInventory(data).open();
		}
		return true;
	}
}
