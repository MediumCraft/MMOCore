package net.Indyuce.mmocore.gui;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerChangeClassEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.InventoryPlaceholderItem;
import net.Indyuce.mmocore.gui.api.item.NoPlaceholderItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;

public class SubclassConfirmation extends EditableInventory {
	public SubclassConfirmation() {
		super("subclass-confirm");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {
		return function.equalsIgnoreCase("yes") ? new InventoryPlaceholderItem(config) {

			@Override
			public Placeholders getPlaceholders(PluginInventory inv, int n) {

				Placeholders holders = new Placeholders();
				holders.register("class", ((ClassConfirmationInventory) inv).profess.getName());
				return holders;
			}
		} : new NoPlaceholderItem(config);
	}

	public GeneratedInventory newInventory(PlayerData data, PlayerClass profess, PluginInventory last) {
		return new ClassConfirmationInventory(data, this, profess, last);
	}

	public class ClassConfirmationInventory extends GeneratedInventory {
		private final PlayerClass profess;
		private final PluginInventory last;

		public ClassConfirmationInventory(PlayerData playerData, EditableInventory editable, PlayerClass profess, PluginInventory last) {
			super(playerData, editable);

			this.profess = profess;
			this.last = last;
		}

		@Override
		public void whenClicked(InventoryClickEvent event, InventoryItem item) {
			if (event.getInventory() != event.getClickedInventory())
				return;

			if (item.getFunction().equals("back"))
				last.open();

			else if (item.getFunction().equals("yes")) {

				PlayerChangeClassEvent called = new PlayerChangeClassEvent(playerData, profess);
				Bukkit.getPluginManager().callEvent(called);
				if (called.isCancelled())
					return;

				playerData.setClass(profess);
				player.sendMessage(MMOCore.plugin.configManager.getSimpleMessage("class-select", "class", profess.getName()));
				player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
				player.closeInventory();
			}
		}

		@Override
		public String calculateName() {
			return getName();
		}
	}
}
