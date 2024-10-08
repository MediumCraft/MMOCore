package net.Indyuce.mmocore.gui;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.Quest;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestViewer extends EditableInventory {
    public QuestViewer() {
        super("quest-list");
    }

    @Override
    public InventoryItem load(String function, ConfigurationSection config) {

        if (function.equals("quest"))
            return new QuestItem(config);

        if (function.equals("previous"))
            return new SimplePlaceholderItem<QuestInventory>(config) {

                @Override
                public boolean canDisplay(QuestInventory inv) {
                    return inv.page > 0;
                }
            };

        if (function.equals("next"))
            return new SimplePlaceholderItem<QuestInventory>(config) {

                @Override
                public boolean canDisplay(QuestInventory inv) {
                    return inv.perPage * (inv.page + 1) < inv.quests.size();
                }
            };

        return new SimplePlaceholderItem(config);
    }

    public GeneratedInventory newInventory(PlayerData data) {
        return new QuestInventory(data, this);
    }

    public class QuestItem extends SimplePlaceholderItem {
        private final SimplePlaceholderItem noQuest, locked;

        private final String mainHit, mainNotHit, professionHit, professionNotHit;
        private final SimpleDateFormat dateFormat;

        public QuestItem(ConfigurationSection config) {
            super(config);

            Validate.isTrue(config.contains("no-quest"), "Could not load config 'no-quest'");
            Validate.isTrue(config.contains("locked"), "Could not load config 'locked'");

            locked = new SimplePlaceholderItem(config.getConfigurationSection("locked"));
            noQuest = new SimplePlaceholderItem(config.getConfigurationSection("no-quest"));

            Validate.isTrue(config.contains("date-format"), "Could not find date-format");
            dateFormat = new SimpleDateFormat(config.getString("date-format"));

            Validate.notNull(mainHit = config.getString("level-requirement.main.hit"), "Could not load 'level-requirement.main.hit'");
            Validate.notNull(mainNotHit = config.getString("level-requirement.main.not-hit"), "Could not load 'level-requirement.main.not-hit'");
            Validate.notNull(professionHit = config.getString("level-requirement.profession.hit"),
                    "Could not load 'level-requirement.profession.hit'");
            Validate.notNull(professionNotHit = config.getString("level-requirement.profession.not-hit"),
                    "Could not load 'level-requirement.profession.not-hit'");
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack display(GeneratedInventory inv, int itemIndex) {
            final QuestInventory list = (QuestInventory) inv;
            final int index = list.page * list.perPage + itemIndex;

            if (index >= list.quests.size())
                return noQuest.display(inv, itemIndex);

            Quest quest = list.quests.get(index);
            if (quest.hasParent() && !inv.getPlayerData().getQuestData().checkParentAvailability(quest))
                return locked.display(inv, itemIndex);

            List<String> lore = new ArrayList<>(getLore());

            // Replace quest lore
            int loreIndex = lore.indexOf("{lore}");
            if (loreIndex >= 0) {
                lore.remove(loreIndex);
                for (int j = 0; j < quest.getLore().size(); j++)
                    lore.add(loreIndex + j, quest.getLore().get(j));
            }

            // Calculate lore for later
            int reqCount = quest.countLevelRestrictions();
            boolean started = inv.getPlayerData().getQuestData().hasCurrent(quest), completed = inv.getPlayerData().getQuestData().hasFinished(quest),
                    cooldown = completed && inv.getPlayerData().getQuestData().checkCooldownAvailability(quest);

            lore.removeIf(next -> (next.startsWith("{level_req}") && reqCount < 1)
                    || (next.startsWith("{started}") && !started)
                    || (next.startsWith("{!started}") && started)
                    || (next.startsWith("{completed}") && !completed)
                    || (next.startsWith("{completed_cannot_redo}") && !(completed && !quest.isRedoable()))
                    || (next.startsWith("{completed_can_redo}") && !(cooldown && quest.isRedoable()))
                    || (next.startsWith("{completed_delay}") && !(completed && !cooldown)));

            // Replace level requirements
            loreIndex = lore.indexOf("{level_req}{level_requirements}");
            if (loreIndex >= 0) {
                lore.remove(loreIndex);
                int mainRequired = quest.getLevelRestriction(null);
                if (mainRequired > 0)
                    lore.add(loreIndex, (inv.getPlayerData().getLevel() >= mainRequired ? mainHit : mainNotHit).replace("{level}", "" + mainRequired));

                for (Profession profession : quest.getLevelRestrictions()) {
                    int required = quest.getLevelRestriction(profession);
                    lore.add(loreIndex + (mainRequired > 0 ? 1 : 0),
                            (inv.getPlayerData().getCollectionSkills().getLevel(profession) >= required ? professionHit : professionNotHit)
                                    .replace("{level}", "" + required).replace("{profession}", profession.getName()));
                }
            }

            Placeholders holders = getPlaceholders(inv, itemIndex);
            for (int j = 0; j < lore.size(); j++)
                lore.set(j, ChatColor.GRAY + holders.apply(inv.getPlayer(), lore.get(j)));

            // Generate item
            ItemStack item = super.display(inv, itemIndex);
            ItemMeta meta = item.getItemMeta();
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(new NamespacedKey(MMOCore.plugin, "quest_id"), PersistentDataType.STRING, quest.getId());
            item.setItemMeta(meta);

            return item;
        }

        @Override
        public Placeholders getPlaceholders(GeneratedInventory inv, int itemIndex) {
            final QuestInventory list = (QuestInventory) inv;
            final Quest quest = ((QuestInventory) inv).quests.get(list.page * list.perPage + itemIndex);
            PlayerData data = inv.getPlayerData();

            Placeholders holders = new Placeholders();
            holders.register("name", quest.getName());
            holders.register("total_level_req", quest.getLevelRestrictions().size() + (quest.getLevelRestriction(null) > 0 ? 1 : 0));
            holders.register("current_level_req", (data.getLevel() >= quest.getLevelRestriction(null) ? 1 : 0) + quest.getLevelRestrictions().stream()
                    .filter(type -> data.getCollectionSkills().getLevel(type) >= quest.getLevelRestriction(type)).collect(Collectors.toSet()).size());

            if (data.getQuestData().hasCurrent(quest)) {
                holders.register("objective", data.getQuestData().getCurrent().getFormattedLore());
                holders.register("progress",
                        (int) ((double) data.getQuestData().getCurrent().getObjectiveNumber() / quest.getObjectives().size() * 100.));
            }

            if (data.getQuestData().hasFinished(quest)) {
                holders.register("date", dateFormat.format(data.getQuestData().getFinishDate(quest)));
                holders.register("delay", new DelayFormat(2).format(data.getQuestData().getDelayFeft(quest)));
            }

            return holders;
        }
    }

    public class QuestInventory extends GeneratedInventory {
        private final List<Quest> quests = new ArrayList<>(MMOCore.plugin.questManager.getAll());
        private final int perPage;

        private int page;

        public QuestInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);

            perPage = editable.getByFunction("quest").getSlots().size();
        }

        @Override
        public String calculateName() {
            return getName();
        }

        @Override
        public void whenClicked(InventoryClickContext context, InventoryItem item) {
            if (item.getFunction().equals("previous")) {
                page--;
                open();
                return;
            }

            if (item.getFunction().equals("next")) {
                page++;
                open();
                return;
            }

            if (item.getFunction().equals("quest")) {
                String questId = context.getClickedItem().getItemMeta().getPersistentDataContainer()
                        .get(new NamespacedKey(MMOCore.plugin, "quest_id"), PersistentDataType.STRING);
                if (questId == null || questId.equals(""))
                    return;

                Quest quest = MMOCore.plugin.questManager.get(questId);

                if (playerData.getQuestData().hasCurrent()) {

                    // Check if the player is cancelling his ongoing quest
                    if (playerData.getQuestData().hasCurrent(quest)) {
                        if (context.getClickType() == ClickType.RIGHT) {
                            playerData.getQuestData().start(null);
                            MMOCore.plugin.soundManager.getSound(SoundEvent.CANCEL_QUEST).playTo(player);
                            ConfigMessage.fromKey("cancel-quest").send(player);
                            open();
                        }
                        return;
                    }

                    // The player cannot start a new quest if he is already doing one
                    ConfigMessage.fromKey("already-on-quest").send(player);
                    return;
                }

                // Check for level requirements.
                int level;
                if (playerData.getLevel() < (level = quest.getLevelRestriction(null))) {
                    ConfigMessage.fromKey("quest-level-restriction", "level", "Lvl", "count", "" + level).send(player);
                    return;
                }

                for (Profession profession : quest.getLevelRestrictions())
                    if (playerData.getCollectionSkills().getLevel(profession) < (level = quest.getLevelRestriction(profession))) {
                        ConfigMessage.fromKey("quest-level-restriction", "level", profession.getName() + " Lvl", "count", "" + level)
                                .send(player);
                        return;
                    }

                if (playerData.getQuestData().hasFinished(quest)) {

                    // If the player has already finished this quest, he can't start it again
                    if (!quest.isRedoable()) {
                        ConfigMessage.fromKey("cant-redo-quest").send(player);
                        return;
                    }

                    // Has the player waited long enough
                    if (!playerData.getQuestData().checkCooldownAvailability(quest)) {
                        ConfigMessage.fromKey("quest-cooldown", "delay", new DelayFormat(2).format(playerData.getQuestData().getDelayFeft(quest))).send(player);
                        return;
                    }
                }

                // Eventually start the quest
                ConfigMessage.fromKey("start-quest", "quest", quest.getName()).send(player);
                MMOCore.plugin.soundManager.getSound(SoundEvent.START_QUEST).playTo(player);
                playerData.getQuestData().start(quest);
                open();
            }
        }
    }
}
