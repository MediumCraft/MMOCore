package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.ClassOption;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.player.profess.Subclass;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SubclassSelect extends EditableInventory {
    public SubclassSelect() {
        super("subclass-select");
    }

    @Override
    public InventoryItem load(String function, ConfigurationSection config) {
        return function.startsWith("sub-class") ? new ClassItem(config) : new SimplePlaceholderItem(config);
    }

    public GeneratedInventory newInventory(PlayerData data) {
        return new SubclassSelectionInventory(data, this);
    }

    public class ClassItem extends SimplePlaceholderItem<SubclassSelectionInventory> {
        private final String name;
        private final List<String> lore;
        private final PlayerClass playerClass;

        public ClassItem(ConfigurationSection config) {
            super(Material.BARRIER, config);
            Validate.isTrue(config.getString("function").length() > 10, "Couldn't find the class associated to: " + config.getString("function"));
            String classId = UtilityMethods.enumName(config.getString("function").substring(10));
            this.playerClass = Objects.requireNonNull(MMOCore.plugin.classManager.get(classId), classId + " does not correspond to any classId.");
            this.name = config.getString("name");
            this.lore = config.getStringList("lore");
        }

        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack display(SubclassSelectionInventory inv, int n) {
            ItemStack item = playerClass.getIcon();
            ItemMeta meta = item.getItemMeta();
            if (hideFlags())
                meta.addItemFlags(ItemFlag.values());
            meta.setDisplayName(MythicLib.plugin.parseColors(name).replace("{name}", playerClass.getName()));
            List<String> lore = new ArrayList<>(this.lore);

            int index = lore.indexOf("{lore}");
            if (index >= 0) {
                lore.remove(index);
                for (int j = 0; j < playerClass.getDescription().size(); j++)
                    lore.add(index + j, playerClass.getDescription().get(j));
            }

            index = lore.indexOf("{attribute-lore}");
            if (index >= 0) {
                lore.remove(index);
                for (int j = 0; j < playerClass.getAttributeDescription().size(); j++)
                    lore.add(index + j, playerClass.getAttributeDescription().get(j));
            }

            meta.getPersistentDataContainer().set(new NamespacedKey(MMOCore.plugin, "class_id"), PersistentDataType.STRING, playerClass.getId());
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public boolean canDisplay(SubclassSelectionInventory inv) {
            return inv.getPlayerData().getProfess().hasSubclass(playerClass);
        }
    }

    public class SubclassSelectionInventory extends GeneratedInventory {
        public SubclassSelectionInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);
        }

        @Override
        public String calculateName() {
            return getName();
        }

        @Override
        public void whenClicked(InventoryClickContext context, InventoryItem item) {
            if (item.getFunction().equals("back"))
                InventoryManager.CLASS_SELECT.newInventory(playerData).open();

            if (item.getFunction().startsWith("sub-class")) {
                String classId = UtilityMethods.ymlName(item.getFunction().substring(10));
                PlayerClass profess = MMOCore.plugin.classManager.get(UtilityMethods.enumName(classId));
                if (playerData.getClassPoints() < 1) {
                    player.closeInventory();
                    MMOCore.plugin.soundManager.getSound(SoundEvent.CANT_SELECT_CLASS).playTo(getPlayer());
                    new ConfigMessage("cant-choose-new-class").send(player);
                    return;
                }
                if (profess.hasOption(ClassOption.NEEDS_PERMISSION) && !player.hasPermission("mmocore.class." + profess.getId().toLowerCase())) {
                    MMOCore.plugin.soundManager.getSound(SoundEvent.CANT_SELECT_CLASS).playTo(player);
                    new ConfigMessage("no-permission-for-class").send(player);
                    return;
                }

                InventoryManager.CLASS_CONFIRM.get(classId).newInventory(playerData, this, true).open();
            }
        }
    }
}
