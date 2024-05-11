package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.skill.binding.SkillSlot;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SkillList extends EditableInventory {
    public SkillList() {
        super("skill-list");
    }

    @Override
    public InventoryItem load(String function, ConfigurationSection config) {

        if (function.equals("skill"))
            return new SkillItem(config);

        if (function.equals("level"))
            return new LevelItem(config);

        if (function.equals("upgrade"))
            return new UpgradeItem(config);

        if (function.equals("reallocation")) {

            return new InventoryItem(config) {

                @Override
                public Placeholders getPlaceholders(GeneratedInventory inv, int n) {
                    Placeholders holders = new Placeholders();
                    holders.register("skill_points", inv.getPlayerData().getSkillPoints());
                    holders.register("points", inv.getPlayerData().getSkillReallocationPoints());
                    holders.register("total", inv.getPlayerData().countSkillPointsSpent());
                    return holders;
                }
            };
        }

        if (function.equals("slot"))
            return new SlotItem(config);

        if (function.equals("previous"))
            return new SimplePlaceholderItem<SkillViewerInventory>(config) {

                @Override
                public boolean canDisplay(SkillViewerInventory inv) {
                    return inv.page > 0;
                }
            };

        if (function.equals("next")) {
            return new SimplePlaceholderItem<SkillViewerInventory>(config) {

                @Override
                public boolean canDisplay(SkillViewerInventory inv) {
                    final int perPage = inv.skillSlots.size();
                    return inv.page < (inv.skills.size() - 1) / perPage;
                }
            };
        }
        if (function.equals("selected"))
            return new SelectedItem(config);

        return new SimplePlaceholderItem(config);
    }

    public GeneratedInventory newInventory(PlayerData data) {
        return new SkillViewerInventory(data, this);
    }

    public class SelectedItem extends InventoryItem<SkillViewerInventory> {
        public SelectedItem(ConfigurationSection config) {
            //We must use this constructor to show that there are not specified material
            super(Material.BARRIER, config);
        }

        @Override
        public ItemStack display(SkillViewerInventory inv, int n) {
            if (inv.selected == null)
                return new ItemStack(Material.AIR);
            Placeholders holders = getPlaceholders(inv, n);
            ItemStack item = new ItemStack(inv.selected.getSkill().getIcon());
            ItemMeta meta = item.getItemMeta();
            int skillLevel = inv.getPlayerData().getSkillLevel(inv.selected.getSkill());
            List<String> lore = new ArrayList<>();
            boolean unlocked = inv.selected.getUnlockLevel() <= inv.getPlayerData().getLevel();
            for (String str : getLore()) {
                if ((str.startsWith("{unlocked}") && !unlocked) || (str.startsWith("{locked}") && unlocked) || (str.startsWith("{max_level}") && (!inv.selected.hasMaxLevel() || inv.selected.getMaxLevel() > inv.getPlayerData().getSkillLevel(inv.selected.getSkill()))))
                    continue;
                if (str.contains("{lore}"))
                    for (String loreLine : inv.selected.calculateLore(inv.getPlayerData()))
                        lore.add(ChatColor.GRAY + loreLine);
                else
                    lore.add(holders.apply(inv.getPlayer(), str));
            }

            meta.setDisplayName(MMOCore.plugin.placeholderParser.parse(inv.getPlayer(), getName().replace("{skill}", inv.selected.getSkill().getName())
                    .replace("{roman}", MMOCoreUtils.intToRoman(skillLevel)).replace("{level}", "" + skillLevel)));
            meta.addItemFlags(ItemFlag.values());
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public Placeholders getPlaceholders(SkillViewerInventory inv, int n) {
            Placeholders holders = new Placeholders();
            holders.register("selected", inv.selected.getSkill().getName());
            holders.register("skill", inv.selected.getSkill().getName());
            holders.register("unlock", "" + inv.selected.getUnlockLevel());
            holders.register("level", "" + inv.getPlayerData().getSkillLevel(inv.selected.getSkill()));
            return holders;
        }

    }

    public class LevelItem extends InventoryItem<SkillViewerInventory> {
        private final int offset;

        public LevelItem(ConfigurationSection config) {
            super(config);

            offset = config.getInt("offset");
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack display(SkillViewerInventory inv, int n) {

            ClassSkill skill = inv.selected;
            int skillLevel = inv.getPlayerData().getSkillLevel(skill.getSkill()) + n - offset;
            if (skillLevel < 1)
                return new ItemStack(Material.AIR);

            List<String> lore = new ArrayList<>(getLore());
            int index = lore.indexOf("{lore}");
            lore.remove(index);
            List<String> skillLore = skill.calculateLore(inv.getPlayerData(), skillLevel);
            for (int j = 0; j < skillLore.size(); j++)
                lore.add(index + j, skillLore.get(j));

            for (int j = 0; j < lore.size(); j++)
                lore.set(j, ChatColor.GRAY + MMOCore.plugin.placeholderParser.parse(inv.getPlayer(), lore.get(j)));

            ItemStack item = new ItemStack(getMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(MMOCore.plugin.placeholderParser.parse(inv.getPlayer(), getName().replace("{skill}", skill.getSkill().getName())
                    .replace("{roman}", MMOCoreUtils.intToRoman(skillLevel)).replace("{level}", "" + skillLevel)));
            meta.addItemFlags(ItemFlag.values());
            meta.setLore(lore);
            if (MythicLib.plugin.getVersion().isStrictlyHigher(1, 13))
                meta.setCustomModelData(getModelData());
            item.setItemMeta(meta);

            return NBTItem.get(item).addTag(new ItemTag("skillId", skill.getSkill().getHandler().getId())).toItem();
        }

        @Override
        public Placeholders getPlaceholders(SkillViewerInventory inv, int n) {
            return new Placeholders();
        }
    }

    public class SlotItem extends InventoryItem<SkillViewerInventory> {
        private final String none;
        @Nullable
        private final Material filledItem;
        private final int filledCMD;

        public SlotItem(ConfigurationSection config) {
            super(config);
            none = MythicLib.plugin.parseColors(config.getString("no-skill"));

            filledItem = config.contains("filled-item") ? Material
                    .valueOf(config.getString("filled-item").toUpperCase().replace("-", "_").replace(" ", "_")) : null;
            filledCMD = config.getInt("filled-custom-model-data", getModelData());
        }

        @Override
        public ItemStack display(SkillViewerInventory inv, int n) {
            final @Nullable SkillSlot skillSlot = inv.getPlayerData().getProfess().getSkillSlot(n + 1);
            if (skillSlot == null || !inv.getPlayerData().hasUnlocked(skillSlot))
                return new ItemStack(Material.AIR);

            final @Nullable ClassSkill boundSkill = inv.getPlayerData().getBoundSkill(n + 1);
            ItemStack item;
            if (boundSkill == null)
                item = super.display(inv, n);
            else if (filledItem == null)
                item = boundSkill.getSkill().getIcon();
            else {
                item = new ItemStack(filledItem);
                if (MythicLib.plugin.getVersion().isStrictlyHigher(1, 13)) {
                    ItemMeta meta = item.getItemMeta();
                    meta.setCustomModelData(filledCMD);
                    item.setItemMeta(meta);
                }
            }
            Placeholders holders = getPlaceholders(inv, n);

            final ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(MMOCore.plugin.placeholderParser.parse(inv.getPlayerData().getPlayer(), skillSlot.getName()));

            List<String> lore = new ArrayList<>(getLore());

            int index = lore.indexOf("{slot-lore}");
            if (index != -1) {
                lore.remove(index);
                List<String> slotLore = skillSlot.getLore();
                for (int j = 0; j < slotLore.size(); j++)
                    lore.add(index + j, slotLore.get(j));
            }
            index = lore.indexOf("{skill-lore}");
            if (index != -1) {
                lore.remove(index);
                List<String> skillLore = boundSkill == null ? new ArrayList() : boundSkill.calculateLore(inv.getPlayerData());
                for (int j = 0; j < skillLore.size(); j++)
                    lore.add(index + j, skillLore.get(j));
            }

            for (int j = 0; j < lore.size(); j++)
                lore.set(j, ChatColor.GRAY + holders.apply(inv.getPlayer(), lore.get(j)));
            meta.setLore(lore);

            item.setItemMeta(meta);
            return item;
        }

        @Override
        public Placeholders getPlaceholders(SkillViewerInventory inv, int n) {
            RegisteredSkill selected = inv.selected.getSkill();
            final @NotNull SkillSlot skillSlot = inv.getPlayerData().getProfess().getSkillSlot(n + 1);
            Placeholders holders = new Placeholders();
            holders.register("slot", skillSlot.getName());
            holders.register("selected", selected == null ? none : selected.getName());
            RegisteredSkill skill = inv.getPlayerData().hasSkillBound(n + 1) ? inv.getPlayerData().getBoundSkill(n + 1).getSkill() : null;
            holders.register("skill", skill == null ? none : skill.getName());
            return holders;
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }
    }

    public class SkillItem extends InventoryItem<SkillViewerInventory> {
        public SkillItem(ConfigurationSection config) {
            super(Material.BARRIER, config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack display(SkillViewerInventory inv, int n) {

            // Calculate placeholders
            int index = n + inv.skillSlots.size() * inv.page;
            if (index >= inv.skills.size())
                return new ItemStack(Material.AIR);

            ClassSkill skill = inv.skills.get(index);
            Placeholders holders = getPlaceholders(inv.getPlayerData(), skill);

            List<String> lore = new ArrayList<>(getLore());

            index = lore.indexOf("{lore}");
            lore.remove(index);
            List<String> skillLore = skill.calculateLore(inv.getPlayerData());
            for (int j = 0; j < skillLore.size(); j++)
                lore.add(index + j, skillLore.get(j));

            boolean unlocked = skill.getUnlockLevel() <= inv.getPlayerData().getLevel();

            lore.removeIf(next -> (next.startsWith("{unlocked}") && !unlocked) || (next.startsWith("{locked}") && unlocked) || (next.startsWith("{max_level}") && (!skill.hasMaxLevel() || skill.getMaxLevel() > inv.getPlayerData().getSkillLevel(skill.getSkill()))));

            for (int j = 0; j < lore.size(); j++)
                lore.set(j, ChatColor.GRAY + holders.apply(inv.getPlayer(), lore.get(j)));

            // Generate item
            ItemStack item = skill.getSkill().getIcon();
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(holders.apply(inv.getPlayer(), getName()));
            meta.addItemFlags(ItemFlag.values());
            meta.setLore(lore);
            item.setItemMeta(meta);

            return NBTItem.get(item).addTag(new ItemTag("skillId", skill.getSkill().getHandler().getId())).toItem();
        }

        public Placeholders getPlaceholders(PlayerData player, ClassSkill skill) {
            Placeholders holders = new Placeholders();
            holders.register("skill", skill.getSkill().getName());
            holders.register("unlock", "" + skill.getUnlockLevel());
            holders.register("level", "" + player.getSkillLevel(skill.getSkill()));
            return holders;
        }

        @Override
        public Placeholders getPlaceholders(SkillViewerInventory inv, int n) {
            return new Placeholders();
        }
    }

    public class UpgradeItem extends InventoryItem<SkillViewerInventory> {
        private int shiftCost = 1;

        public UpgradeItem(ConfigurationSection config) {
            super(config);
            if (config.contains("shift-cost")) {
                this.shiftCost = config.getInt("shift-cost");
                if (shiftCost < 1) {
                    MMOCore.log(Level.WARNING, "Upgrade shift-cost cannot be less than 1. Using default value: 1");
                    shiftCost = 1;
                }
            }
        }

        @Override
        public Placeholders getPlaceholders(SkillViewerInventory inv, int n) {
            RegisteredSkill selected = inv.selected == null ? null : inv.selected.getSkill();
            Placeholders holders = new Placeholders();

            holders.register("skill_caps", selected.getName().toUpperCase());
            holders.register("skill", selected.getName());
            holders.register("skill_points", "" + inv.getPlayerData().getSkillPoints());
            holders.register("shift_points", shiftCost);
            return holders;
        }
    }

    public class SkillViewerInventory extends GeneratedInventory {

        // Cached information
        private final List<ClassSkill> skills;
        private final List<Integer> skillSlots;
        private final List<Integer> slotSlots;

        // Skill the player selected
        private ClassSkill selected;
        private int page = 0;

        public SkillViewerInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);
            skills = playerData.getProfess().getSkills()
                    .stream()
                    .filter(skill -> playerData.hasUnlocked(skill))
                    .sorted(Comparator.comparingInt(ClassSkill::getUnlockLevel))
                    .collect(Collectors.toList());
            skillSlots = getEditable().getByFunction("skill").getSlots();

            Validate.notNull(getEditable().getByFunction("slot"), "Your skill GUI config file is out-of-date, please regenerate it.");
            slotSlots = getEditable().getByFunction("slot").getSlots();
            selected = skills.get(page * skillSlots.size());
        }

        @Override
        public String calculateName() {
            return getName().replace("{skill}", selected.getSkill().getName());
        }

        @Override
        public void open() {
            super.open();
        }

        @Override
        public void whenClicked(InventoryClickContext context, InventoryItem item) {

            if (item.getFunction().equals("skill")) {
                int index = skillSlots.size() * page + skillSlots.indexOf(context.getSlot());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
                selected = skills.get(index);
                open();
                return;
            }

            if (item.getFunction().equals("reallocation")) {
                int spent = getPlayerData().countSkillPointsSpent();

                if (spent < 1) {
                    ConfigMessage.fromKey("no-skill-points-spent").send(player);
                    MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
                    return;
                }

                if (playerData.getSkillReallocationPoints() < 1) {
                    ConfigMessage.fromKey("not-skill-reallocation-point").send(player);
                    MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
                    return;
                }

                for (ClassSkill skill : playerData.getProfess().getSkills())
                    playerData.setSkillLevel(skill.getSkill(), 1);

                playerData.giveSkillPoints(spent);
                playerData.setSkillReallocationPoints(playerData.getSkillReallocationPoints() - 1);
                ConfigMessage.fromKey("skill-points-reallocated", "points", "" + playerData.getSkillPoints()).send(player);
                MMOCore.plugin.soundManager.getSound(SoundEvent.RESET_SKILLS).playTo(getPlayer());
                open();
            }

            if (item.getFunction().equals("previous")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
                page--;
                open();
                return;
            }

            if (item.getFunction().equals("next")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
                page++;
                open();
                return;
            }

            // Binding or unbinding skills.
            if (item.getFunction().equals("slot")) {
                int index = slotSlots.indexOf(context.getSlot()) + 1;
                SkillSlot skillSlot = playerData.getProfess().getSkillSlot(index);
                //Select if the player is doing Shift Left Click
                if (context.getClickType() == ClickType.SHIFT_LEFT) {
                    if (playerData.hasSkillBound(index))
                        selected = playerData.getBoundSkill(index);
                    return;
                }

                // unbind if there is a current spell.
                if (context.getClickType() == ClickType.RIGHT) {
                    if (!playerData.hasSkillBound(index)) {
                        ConfigMessage.fromKey("no-skill-bound").send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                        return;
                    }
                    if (!playerData.getProfess().getSkillSlot(index).canManuallyBind()) {
                        ConfigMessage.fromKey("cant-manually-bind").send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                        return;
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
                    playerData.unbindSkill(index);
                    open();
                    return;
                }

                if (selected.isPermanent()) {
                    ConfigMessage.fromKey("skill-cannot-be-bound").send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                    return;
                }

                if (!playerData.hasUnlockedLevel(selected)) {
                    ConfigMessage.fromKey("skill-level-not-met").send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                    return;
                }

                if (!skillSlot.canManuallyBind()) {
                    ConfigMessage.fromKey("cant-manually-bind").send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                    return;
                }

                if (!skillSlot.acceptsSkill(selected)) {
                    ConfigMessage.fromKey("not-compatible-skill").send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                    return;
                }

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
                playerData.bindSkill(index, selected);
                open();
                return;
            }

            // Upgrading a player skill
            if (item.getFunction().equals("upgrade")) {
                int shiftCost = ((UpgradeItem) item).shiftCost;

                if (!playerData.hasUnlockedLevel(selected)) {
                    ConfigMessage.fromKey("skill-level-not-met").send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                    return;
                }

                if (!selected.isUpgradable()) {
                    ConfigMessage.fromKey("cannot-upgrade-skill").send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                    return;
                }

                if (playerData.getSkillPoints() < 1) {
                    ConfigMessage.fromKey("not-enough-skill-points").send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                    return;
                }

                if (selected.hasMaxLevel() && playerData.getSkillLevel(selected.getSkill()) >= selected.getMaxLevel()) {
                    ConfigMessage.fromKey("skill-max-level-hit").send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                    return;
                }

                if (context.getClickType().isShiftClick()) {
                    if (playerData.getSkillPoints() < shiftCost) {
                        ConfigMessage.fromKey("not-enough-skill-points-shift", "shift_points", "" + shiftCost).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                        return;
                    }

                    playerData.giveSkillPoints(-shiftCost);
                    playerData.setSkillLevel(selected.getSkill(), playerData.getSkillLevel(selected.getSkill()) + shiftCost);
                } else {
                    playerData.giveSkillPoints(-1);
                    playerData.setSkillLevel(selected.getSkill(), playerData.getSkillLevel(selected.getSkill()) + 1);
                }

                ConfigMessage.fromKey("upgrade-skill", "skill", selected.getSkill().getName(), "level",
                        "" + playerData.getSkillLevel(selected.getSkill())).send(player);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
                open();
            }
        }
    }
}