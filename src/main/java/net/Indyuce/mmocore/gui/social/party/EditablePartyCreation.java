package net.Indyuce.mmocore.gui.social.party;

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

public class EditablePartyCreation extends EditableInventory {
	public EditablePartyCreation() {
		super("party-creation");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {
		return new NoPlaceholderItem(config);
	}

	public GeneratedInventory newInventory(PlayerData data) {
		return new ClassConfirmationInventory(data, this);
	}

	public class ClassConfirmationInventory extends GeneratedInventory {
		public ClassConfirmationInventory(PlayerData playerData, EditableInventory editable) {
			super(playerData, editable);
		}

		@Override
		public void whenClicked(InventoryClickEvent event, InventoryItem item) {
			if (event.getInventory() != event.getClickedInventory())
				return;

			if (item.getFunction().equals("create")) {
				MMOCore.plugin.partyManager.newRegisteredParty(playerData);
				InventoryManager.PARTY_VIEW.newInventory(playerData).open();
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
