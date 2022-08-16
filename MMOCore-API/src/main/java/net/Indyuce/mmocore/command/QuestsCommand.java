package net.Indyuce.mmocore.command;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.api.event.MMOCommandEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class QuestsCommand extends BukkitCommand {
	public QuestsCommand(ConfigurationSection config) {
		super(config.getString("main"));
		
		setAliases(config.getStringList("aliases"));
		setDescription("Opens the quests menu.");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (sender instanceof Player) {
			PlayerData data = PlayerData.get((Player) sender);
			MMOCommandEvent event = new MMOCommandEvent(data, "quests");
			Bukkit.getServer().getPluginManager().callEvent(event);
			if(!event.isCancelled()) InventoryManager.QUEST_LIST.newInventory(data).open();
		}
		return true;
	}
}
