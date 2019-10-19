package net.Indyuce.mmocore.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;

public class SkillsCommand extends BukkitCommand {
	public SkillsCommand(ConfigurationSection config) {
		super(config.getString("main"));
		
		setAliases(config.getStringList("aliases"));
		setDescription("Opens the skills menu.");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (sender instanceof Player) {
			PlayerData data = PlayerData.get((Player) sender);
			if (data.getProfess().getSkills().size() < 1) {
				MMOCore.plugin.configManager.getSimpleMessage("no-class-skill").send((Player) sender);
				return true;
			}

			InventoryManager.SKILL_LIST.newInventory(data).open();
		}
		return true;
	}
}
