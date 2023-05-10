package net.Indyuce.mmocore.gui.social.party;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.party.provided.MMOCorePartyModule;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

public class EditablePartyCreation extends EditableInventory {
	public EditablePartyCreation() {
		super("party-creation");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {
        return new SimplePlaceholderItem(config);
    }

	public GeneratedInventory newInventory(PlayerData data) {
		return new ClassConfirmationInventory(data, this);
	}

	public class ClassConfirmationInventory extends GeneratedInventory {
		public ClassConfirmationInventory(PlayerData playerData, EditableInventory editable) {
			super(playerData, editable);
		}

		@Override
		public void whenClicked(InventoryClickContext context, InventoryItem item) {

			if (item.getFunction().equals("create")) {
                ((MMOCorePartyModule) MMOCore.plugin.partyModule).newRegisteredParty(playerData);
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
