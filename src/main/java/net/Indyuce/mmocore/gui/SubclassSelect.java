package net.Indyuce.mmocore.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.Indyuce.mmocore.manager.SoundManager;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.Subclass;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.NoPlaceholderItem;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class SubclassSelect extends EditableInventory {
	public SubclassSelect() {
		super("subclass-select");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {
		return function.equals("class") ? new ClassItem(config) : new NoPlaceholderItem(config);
	}

	public GeneratedInventory newInventory(PlayerData data) {
		return new SubclassSelectionInventory(data, this);
	}

	public class ClassItem extends InventoryItem {
		private final String name;
		private final List<String> lore;

		public ClassItem(ConfigurationSection config) {
			super(config);

			this.name = config.getString("name");
			this.lore = config.getStringList("lore");
		}

		public boolean hasDifferentDisplay() {
			return true;
		}

		@Override
		public ItemStack display(GeneratedInventory inv, int n) {
			SubclassSelectionInventory generated = (SubclassSelectionInventory) inv;

			if (n >= generated.subclasses.size())
				return null;

			PlayerClass profess = generated.subclasses.get(n).getProfess();
			
			
			ItemStack item = profess.getIcon();
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(MMOLib.plugin.parseColors(name).replace("{name}", profess.getName()));
			List<String> lore = new ArrayList<>(this.lore);

			int index = lore.indexOf("{lore}");
			if (index >= 0) {
				lore.remove(index);
				for (int j = 0; j < profess.getDescription().size(); j++)
					lore.add(index + j, profess.getDescription().get(j));
			}

			index = lore.indexOf("{attribute-lore}");
			if (index >= 0) {
				lore.remove(index);
				for (int j = 0; j < profess.getAttributeDescription().size(); j++)
					lore.add(index + j, profess.getAttributeDescription().get(j));
			}

			meta.setLore(lore);
			item.setItemMeta(meta);
			return NBTItem.get(item).addTag(new ItemTag("classId", profess.getId())).toItem();
		}

		@Override
		public boolean canDisplay(GeneratedInventory inv) {
			return true;
		}
	}

	public class SubclassSelectionInventory extends GeneratedInventory {
		private final List<Subclass> subclasses;

		public SubclassSelectionInventory(PlayerData playerData, EditableInventory editable) {
			super(playerData, editable);

			subclasses = playerData.getProfess().getSubclasses().stream().filter(sub -> playerData.getLevel() >= sub.getLevel())
					.collect(Collectors.toList());
		}

		@Override
		public String calculateName() {
			return getName();
		}

		@Override
		public void whenClicked(InventoryClickEvent event, InventoryItem item) {
			if (item.getFunction().equals("back"))
				InventoryManager.CLASS_SELECT.newInventory(playerData).open();

			if (item.getFunction().equals("class")) {
				String tag = NBTItem.get(event.getCurrentItem()).getString("classId");
				if (tag.equals(""))
					return;

				if (playerData.getClassPoints() < 1) {
					player.closeInventory();
					MMOCore.plugin.soundManager.play(getPlayer(), SoundManager.SoundEvent.CANT_SELECT_CLASS);
					new ConfigMessage("cant-choose-new-class").send(player);
					return;
				}

				InventoryManager.SUBCLASS_CONFIRM.newInventory(playerData, MMOCore.plugin.classManager.get(tag), this).open();
			}
		}
	}
}
