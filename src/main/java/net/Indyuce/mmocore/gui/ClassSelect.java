package net.Indyuce.mmocore.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.PlayerClass.ClassOption;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.NoPlaceholderItem;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.mmogroup.mmolib.api.ItemTag;
import net.mmogroup.mmolib.api.NBTItem;

public class ClassSelect extends EditableInventory {
	public ClassSelect() {
		super("class-select");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {
		return function.equals("class") ? new ClassItem(config) : new NoPlaceholderItem(config);
	}

	public GeneratedInventory newInventory(PlayerData data) {
		return new ProfessSelectionInventory(data, this);
	}

	public class ClassItem extends NoPlaceholderItem {
		private final String name;
		private final List<String> lore;

		public ClassItem(ConfigurationSection config) {
			super(new ItemStack(Material.BARRIER), config);

			this.name = config.getString("name");
			this.lore = config.getStringList("lore");
		}

		public boolean hasDifferentDisplay() {
			return true;
		}

		@Override
		public ItemStack display(GeneratedInventory inv, int n) {
			ProfessSelectionInventory generated = (ProfessSelectionInventory) inv;

			if (n >= generated.classes.size())
				return null;

			PlayerClass profess = generated.classes.get(n);
			ItemStack item = profess.getIcon();
			ItemMeta meta = item.getItemMeta();
			if (hideFlags())
				meta.addItemFlags(ItemFlag.values());
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name).replace("{name}", profess.getName()));
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

	public class ProfessSelectionInventory extends GeneratedInventory {
		private final List<PlayerClass> classes = MMOCore.plugin.classManager.getAll().stream().filter(c -> c.hasOption(ClassOption.DISPLAY)).sorted(Comparator.comparingInt(PlayerClass::getDisplayOrder)).collect(Collectors.toList());

		public ProfessSelectionInventory(PlayerData playerData, EditableInventory editable) {
			super(playerData, editable);
		}

		@Override
		public String calculateName() {
			return getName();
		}

		@Override
		public void whenClicked(InventoryClickEvent event, InventoryItem item) {
			if (item.getFunction().equals("class")) {
				String tag = NBTItem.get(event.getCurrentItem()).getString("classId");
				if (tag.equals(""))
					return;

				if (playerData.getClassPoints() < 1) {
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					new ConfigMessage("cant-choose-new-class").send(player);
					return;
				}

				PlayerClass profess = MMOCore.plugin.classManager.get(tag);
				if (profess.equals(playerData.getProfess())) {
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					MMOCore.plugin.configManager.getSimpleMessage("already-on-class", "class", profess.getName()).send(player);
					return;
				}

				InventoryManager.CLASS_CONFIRM.newInventory(playerData, profess, this).open();
			}
		}
	}
}
