package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.Subclass;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SubclassSelect extends EditableInventory {
	public SubclassSelect() {
		super("subclass-select");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {
		return function.equals("class") ? new ClassItem(config) : new SimplePlaceholderItem(config);
	}

	public GeneratedInventory newInventory(PlayerData data) {
		return new SubclassSelectionInventory(data, this);
	}

	public class ClassItem extends SimplePlaceholderItem<SubclassSelectionInventory> {
		private final String name;
		private final List<String> lore;

		public ClassItem(ConfigurationSection config) {
			super(Material.BARRIER, config);

			this.name = config.getString("name");
			this.lore = config.getStringList("lore");
		}

		@Override
		public boolean hasDifferentDisplay() {
			return true;
		}

		@Override
		public ItemStack display(SubclassSelectionInventory inv, int n) {
			if (n >= inv.subclasses.size())
				return null;

			PlayerClass profess = inv.subclasses.get(n).getProfess();

			ItemStack item = profess.getIcon();
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(MythicLib.plugin.parseColors(name).replace("{name}", profess.getName()));
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
		public boolean canDisplay(SubclassSelectionInventory inv) {
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
					MMOCore.plugin.soundManager.getSound(SoundEvent.CANT_SELECT_CLASS).playTo(getPlayer());
					new ConfigMessage("cant-choose-new-class").send(player);
					return;
				}

				InventoryManager.SUBCLASS_CONFIRM.newInventory(playerData, MMOCore.plugin.classManager.get(tag), this).open();
			}
		}
	}
}
