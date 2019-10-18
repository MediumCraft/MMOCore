package net.Indyuce.mmocore.gui.social.guild;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.NoPlaceholderItem;
import net.Indyuce.mmocore.manager.InventoryManager;

public class EditableGuildCreation extends EditableInventory {
	public EditableGuildCreation() {
		super("guild-creation");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {
		return new NoPlaceholderItem(config);
	}

	public GeneratedInventory newInventory(PlayerData data) {
		return new GuildCreationInventory(data, this);
	}

	public class GuildCreationInventory extends GeneratedInventory {
		public GuildCreationInventory(PlayerData playerData, EditableInventory editable) {
			super(playerData, editable);
		}

		@Override
		public void whenClicked(InventoryClickEvent event, InventoryItem item) {
			if (event.getInventory() != event.getClickedInventory())
				return;

			if (item.getFunction().equals("create")) {
				MMOCore.plugin.guildManager.newRegisteredGuild(playerData, "test");
				InventoryManager.GUILD_VIEW.newInventory(playerData).open();
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
			}

			if (item.getFunction().equals("back"))
				player.closeInventory();
		}

		@Override
		public String calculateName() {
			return getName();
		}
	}
}
