package net.Indyuce.mmocore.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;

public class QuestsCommand extends BukkitCommand {
	public QuestsCommand(ConfigurationSection config) {
		super(config.getString("main"));
		
		setAliases(config.getStringList("aliases"));
		setDescription("Opens the quests menu.");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (sender instanceof Player)
			InventoryManager.QUEST_LIST.newInventory(PlayerData.get((Player) sender)).open();
		return true;
	}
}
