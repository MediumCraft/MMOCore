package net.Indyuce.mmocore.gui.social.friend;

import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.InventoryPlaceholderItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;

public class EditableFriendRemoval extends EditableInventory {
	public EditableFriendRemoval() {
		super("friend-removal");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {

		return new InventoryPlaceholderItem(config) {

			@Override
			public Placeholders getPlaceholders(PluginInventory inv, int n) {
				Placeholders holders = new Placeholders();
				holders.register("name", ((ClassConfirmationInventory) inv).friend.getName());
				return holders;
			}
		};
	}

	public GeneratedInventory newInventory(PlayerData data, OfflinePlayer friend, GeneratedInventory last) {
		return new ClassConfirmationInventory(data, this, friend, last);
	}

	public class ClassConfirmationInventory extends GeneratedInventory {
		private final OfflinePlayer friend;
		private final GeneratedInventory last;

		public ClassConfirmationInventory(PlayerData playerData, EditableInventory editable, OfflinePlayer friend, GeneratedInventory last) {
			super(playerData, editable);

			this.friend = friend;
			this.last = last;
		}

		@Override
		public void whenClicked(InventoryClickEvent event, InventoryItem item) {
			if (item.getFunction().equals("yes")) {
				playerData.removeFriend(friend.getUniqueId());
				new OfflinePlayerData(friend.getUniqueId()).removeFriend(playerData.getUniqueId());
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				player.sendMessage(MMOCore.plugin.configManager.getSimpleMessage("no-longer-friends", "unfriend", friend.getName()));
				last.open();
			}

			if (item.getFunction().equals("back"))
				last.open();
		}

		@Override
		public String calculateName() {
			return getName();
		}
	}
}
