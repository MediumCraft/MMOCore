package net.Indyuce.mmocore.gui;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerChangeClassEvent;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SubclassConfirmation extends EditableInventory {
	public SubclassConfirmation() {
		super("subclass-confirm");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {
        return function.equalsIgnoreCase("yes") ? new InventoryItem<SubclassConfirmationInventory>(config) {

            @Override
            public Placeholders getPlaceholders(SubclassConfirmationInventory inv, int n) {

                Placeholders holders = new Placeholders();
                holders.register("class", inv.profess.getName());
                return holders;
            }
        } : new SimplePlaceholderItem(config);
    }

	public GeneratedInventory newInventory(PlayerData data, PlayerClass profess, PluginInventory last) {
		return new SubclassConfirmationInventory(data, this, profess, last);
	}

	public class SubclassConfirmationInventory extends GeneratedInventory {
		private final PlayerClass profess;
		private final PluginInventory last;

		public SubclassConfirmationInventory(PlayerData playerData, EditableInventory editable, PlayerClass profess, PluginInventory last) {
			super(playerData, editable);

			this.profess = profess;
			this.last = last;
		}

		@Override
		public void whenClicked(InventoryClickContext context, InventoryItem item) {

			if (item.getFunction().equals("back"))
				last.open();

			else if (item.getFunction().equals("yes")) {

				PlayerChangeClassEvent called = new PlayerChangeClassEvent(playerData, profess);
				Bukkit.getPluginManager().callEvent(called);
				if (called.isCancelled())
					return;

				playerData.setClass(profess);
				MMOCore.plugin.configManager.getSimpleMessage("class-select", "class", profess.getName()).send(player);
				MMOCore.plugin.soundManager.getSound(SoundEvent.SELECT_CLASS).playTo(player);
				player.closeInventory();
			}
		}

		@Override
		public String calculateName() {
			return getName();
		}
	}
}
