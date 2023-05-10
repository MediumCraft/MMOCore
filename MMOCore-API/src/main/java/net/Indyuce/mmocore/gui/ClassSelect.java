package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.ClassOption;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClassSelect extends EditableInventory {
    public ClassSelect() {
        super("class-select");
    }

    @Override
    public InventoryItem load(String function, ConfigurationSection config) {
        return function.startsWith("class") ? new ClassItem(config) : new SimplePlaceholderItem(config);
    }

    public GeneratedInventory newInventory(PlayerData data) {
        return new ProfessSelectionInventory(data, this);
    }

    public class ClassItem extends SimplePlaceholderItem<ProfessSelectionInventory> {
        private final String name;
        private final List<String> lore;
        private final PlayerClass playerClass;

        public ClassItem(ConfigurationSection config) {
            super(Material.BARRIER, config);
            Validate.isTrue(config.getString("function").length()>6,"Couldn't find the class associated to: "+config.getString("function"));
            String classId = UtilityMethods.enumName(config.getString("function").substring(6));
            this.playerClass = Objects.requireNonNull(MMOCore.plugin.classManager.get(classId),classId+" does not correspond to any classId.");
            this.name = config.getString("name");
            this.lore = config.getStringList("lore");
        }

        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack display(ProfessSelectionInventory inv, int n) {
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
    }

    public class ProfessSelectionInventory extends GeneratedInventory {
        public ProfessSelectionInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);
        }

        @Override
        public String calculateName() {
            return getName();
        }

        @Override
        public void whenClicked(InventoryClickContext context, InventoryItem item) {
            if (item instanceof ClassItem) {
                PlayerClass profess = ((ClassItem) item).playerClass;

                if (playerData.getClassPoints() < 1) {
                    MMOCore.plugin.soundManager.getSound(SoundEvent.CANT_SELECT_CLASS).playTo(player);
                    new ConfigMessage("cant-choose-new-class").send(player);
                    return;
                }

                if (profess.hasOption(ClassOption.NEEDS_PERMISSION) && !player.hasPermission("mmocore.class." + profess.getId().toLowerCase())) {
                    MMOCore.plugin.soundManager.getSound(SoundEvent.CANT_SELECT_CLASS).playTo(player);
                    new ConfigMessage("no-permission-for-class").send(player);
                    return;
                }

                if (profess.equals(playerData.getProfess())) {
                    MMOCore.plugin.soundManager.getSound(SoundEvent.CANT_SELECT_CLASS).playTo(player);
                    MMOCore.plugin.configManager.getSimpleMessage("already-on-class", "class", profess.getName()).send(player);
                    return;
                }

                final PlayerClass playerClass = findDeepestSubclass(playerData, profess);
                InventoryManager.CLASS_CONFIRM.get(MMOCoreUtils.ymlName(playerClass.getId())).newInventory(playerData, this, false).open();
            }
        }
    }

    /**
     * When switching from a class where you had progress before,
     * you should be instantly redirected to the highest subclass
     * in the subclass tree that you chose, because your progress
     * is saved there.
     * <p>
     * It's also more RPG style to take the player back to the subclass
     * he chose because that way he can't turn back and chose another path.
     * <p>
     * This does NOT function properly with subclass nets yet.
     *
     * @param root The root class, it's called the root because since the
     *             player was able to choose it in the class GUI, it should
     *             be at the bottom of the class tree.
     */
    private PlayerClass findDeepestSubclass(PlayerData player, PlayerClass root) {
        for (String checkedName : player.getSavedClasses()) {
            PlayerClass checked = MMOCore.plugin.classManager.getOrThrow(checkedName);
            if (root.hasSubclass(checked))
                return checked;
        }

        return root;
    }
}
