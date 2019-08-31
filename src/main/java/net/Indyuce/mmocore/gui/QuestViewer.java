package net.Indyuce.mmocore.gui;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.item.NBTItem;
import net.Indyuce.mmocore.api.math.format.DelayFormat;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.Quest;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.InventoryPlaceholderItem;
import net.Indyuce.mmocore.gui.api.item.NoPlaceholderItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.version.nms.ItemTag;

public class QuestViewer extends EditableInventory {
	public QuestViewer() {
		super("quest-list");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {
		return function.equals("quest") ? new QuestItem(config) : new NoPlaceholderItem(config);
	}

	public GeneratedInventory newInventory(PlayerData data) {
		return new QuestInventory(data, this);
	}

	public class QuestItem extends NoPlaceholderItem {
		private final InventoryPlaceholderItem noQuest, locked;

		private final String mainHit, mainNotHit, professionHit, professionNotHit;
		private final SimpleDateFormat dateFormat;

		public QuestItem(ConfigurationSection config) {
			super(config);

			Validate.isTrue(config.contains("no-quest"), "Could not load config 'no-quest'");
			Validate.isTrue(config.contains("locked"), "Could not load config 'locked'");

			locked = new NoPlaceholderItem(config.getConfigurationSection("locked"));
			noQuest = new NoPlaceholderItem(config.getConfigurationSection("no-quest"));

			Validate.isTrue(config.contains("date-format"), "Could not find date-format");
			dateFormat = new SimpleDateFormat(config.getString("date-format"));

			Validate.notNull(mainHit = config.getString("level-requirement.main.hit"), "Could not load 'level-requirement.main.hit'");
			Validate.notNull(mainNotHit = config.getString("level-requirement.main.not-hit"), "Could not load 'level-requirement.main.not-hit'");
			Validate.notNull(professionHit = config.getString("level-requirement.profession.hit"), "Could not load 'level-requirement.profession.hit'");
			Validate.notNull(professionNotHit = config.getString("level-requirement.profession.not-hit"), "Could not load 'level-requirement.profession.not-hit'");
		}

		@Override
		public boolean hasDifferentDisplay() {
			return true;
		}

		@Override
		public ItemStack display(GeneratedInventory inv, int n) {

			QuestInventory list = (QuestInventory) inv;
			int index = list.page * list.perPage + n;
			if (index >= list.quests.size())
				return noQuest.display(inv, n);

			Quest quest = list.quests.get(index);
			if (quest.hasParent() && !inv.getPlayerData().getQuestData().checkParentAvailability(quest))
				return locked.display(inv, n);

			List<String> lore = new ArrayList<>(getLore());

			/*
			 * replace quest lore
			 */
			index = lore.indexOf("{lore}");
			if (index >= 0) {
				lore.remove(index);
				for (int j = 0; j < quest.getLore().size(); j++)
					lore.add(index + j, quest.getLore().get(j));
			}

			/*
			 * calculate quest info for later.
			 */
			int reqCount = quest.countLevelRestrictions();
			boolean started = inv.getPlayerData().getQuestData().hasCurrent(quest), completed = inv.getPlayerData().getQuestData().hasFinished(quest), cooldown = completed ? inv.getPlayerData().getQuestData().checkCooldownAvailability(quest) : false;

			for (Iterator<String> iterator = lore.iterator(); iterator.hasNext();) {
				String next = iterator.next();

				if ((next.startsWith("{level_req}") && reqCount < 1) || (next.startsWith("{started}") && !started) || (next.startsWith("{!started}") && started) || (next.startsWith("{completed}") && !completed) || (next.startsWith("{completed_cannot_redo}") && !(completed && !quest.isRedoable())) || (next.startsWith("{completed_can_redo}") && !(cooldown && quest.isRedoable())) || (next.startsWith("{completed_delay}") && !(completed && !cooldown)))
					iterator.remove();
			}

			/*
			 * replace level requirements
			 */
			index = lore.indexOf("{level_req}{level_requirements}");
			if (index >= 0) {
				lore.remove(index);
				int mainRequired = quest.getLevelRestriction(null);
				if (mainRequired > 0)
					lore.add(index, (inv.getPlayerData().getLevel() >= mainRequired ? mainHit : mainNotHit).replace("{level}", "" + mainRequired));

				for (Profession profession : quest.getLevelRestrictions()) {
					int required = quest.getLevelRestriction(profession);
					lore.add(index + (mainRequired > 0 ? 1 : 0), (inv.getPlayerData().getCollectionSkills().getLevel(profession) >= required ? professionHit : professionNotHit).replace("{level}", "" + required).replace("{profession}", profession.getName()));
				}
			}

			Placeholders holders = getPlaceholders(inv.getPlayerData(), quest);

			for (int j = 0; j < lore.size(); j++)
				lore.set(j, ChatColor.GRAY + holders.apply(inv.getPlayer(), lore.get(j)));

			/*
			 * generate item
			 */
			ItemStack item = new ItemStack(getMaterial());
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(holders.apply(inv.getPlayer(), getName()));
			meta.addItemFlags(ItemFlag.values());
			meta.setLore(lore);
			item.setItemMeta(meta);

			return NBTItem.get(item).add(new ItemTag("questId", quest.getId())).toItem();
		}

		private Placeholders getPlaceholders(PlayerData data, Quest quest) {
			Placeholders holders = new Placeholders();
			holders.register("name", quest.getName());
			holders.register("total_level_req", quest.getLevelRestrictions().size() + (quest.getLevelRestriction(null) > 0 ? 1 : 0));
			holders.register("current_level_req", (data.getLevel() >= quest.getLevelRestriction(null) ? 1 : 0) + quest.getLevelRestrictions().stream().filter(type -> data.getCollectionSkills().getLevel(type) >= quest.getLevelRestriction(type)).collect(Collectors.toSet()).size());

			if (data.getQuestData().hasCurrent(quest)) {
				holders.register("objective", data.getQuestData().getCurrent().getFormattedLore());
				holders.register("progress", new DecimalFormat("0").format((double) data.getQuestData().getCurrent().getObjectiveNumber() / quest.getObjectives().size() * 100));
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
		public void whenClicked(InventoryClickEvent event, InventoryItem item) {
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
				String tag = NBTItem.get(event.getCurrentItem()).getString("questId");
				if (tag.equals(""))
					return;

				Quest quest = MMOCore.plugin.questManager.get(tag);

				if (playerData.getQuestData().hasCurrent()) {

					/*
					 * check if the player is cancelling his ongoing quest.
					 */
					if (playerData.getQuestData().hasCurrent(quest)) {
						if (event.getAction() == InventoryAction.PICKUP_HALF) {
							playerData.getQuestData().start(null);
							player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
							player.sendMessage(MMOCore.plugin.configManager.getSimpleMessage("cancel-quest"));
							open();
						}
						return;
					}

					/*
					 * the player cannot start a new quest if he is already
					 * doing one.
					 */
					player.sendMessage(MMOCore.plugin.configManager.getSimpleMessage("already-on-quest"));
					return;
				}

				/*
				 * check for level requirements.
				 */
				int level;
				if (playerData.getLevel() < (level = quest.getLevelRestriction(null))) {
					player.sendMessage(MMOCore.plugin.configManager.getSimpleMessage("quest-level-restriction", "level", "Lvl", "count", "" + level));
					return;
				}

				for (Profession profession : quest.getLevelRestrictions())
					if (playerData.getCollectionSkills().getLevel(profession) < (level = quest.getLevelRestriction(profession))) {
						player.sendMessage(MMOCore.plugin.configManager.getSimpleMessage("quest-level-restriction", "level", profession.getName() + " Lvl", "count", "" + level));
						return;
					}

				if (playerData.getQuestData().hasFinished(quest)) {

					/*
					 * if the player has already finished this quest, he can't
					 * start it again.
					 */
					if (!quest.isRedoable()) {
						player.sendMessage(MMOCore.plugin.configManager.getSimpleMessage("cant-redo-quest"));
						return;
					}

					/*
					*
					*/
					if (!playerData.getQuestData().checkCooldownAvailability(quest)) {
						player.sendMessage(MMOCore.plugin.configManager.getSimpleMessage("quest-cooldown", "delay", new DelayFormat(2).format(playerData.getQuestData().getDelayFeft(quest))));
						return;
					}
				}

				/*
				 * eventually start a new quest.
				 */
				player.sendMessage(MMOCore.plugin.configManager.getSimpleMessage("start-quest", "quest", quest.getName()));
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
				playerData.getQuestData().start(quest);
				open();
			}
		}
	}

	//
	// @Override
	// public Inventory getInventory() {
	// Inventory inv = Bukkit.createInventory(this, 54, "Quests");
	//

	// for (int j = (page - 1) * 21; j < Math.min(21 * page, quests.size());
	// j++) {
	// Quest quest = quests.get(j);
	//
	// ItemStack item = new ItemStack(Material.BOOK);
	// ItemMeta meta = item.getItemMeta();
	// meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
	//
	// if (quest.hasParent() &&
	// !playerData.getQuestData().checkParentAvailability(quest)) {
	// item.setType(Material.PAPER);
	// meta.setDisplayName(ChatColor.RED + "Not Available");
	// item.setItemMeta(meta);
	//
	// inv.setItem(slots[j % 21], item);
	// continue;
	// }
	//
	// meta.setDisplayName(ChatColor.GREEN + quest.getName());
	// List<String> lore = new ArrayList<>();
	// for (String line : quest.getLore())
	// lore.add(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&',
	// line));
	//
	// boolean current;
	// if (current = playerData.getQuestData().hasCurrent(quest)) {
	// meta.addEnchant(Enchantment.DURABILITY, 1, true);
	// lore.add("");
	// lore.add(ChatColor.YELLOW + "Quest Started!");
	// lore.add(ChatColor.GRAY + AltChar.listDash + " Progression: " +
	// ChatColor.YELLOW + new DecimalFormat("0").format((double)
	// playerData.getQuestData().getCurrent().getObjectiveNumber() /
	// quest.getObjectives().size() * 100) + "%");
	// lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + AltChar.listDash + " "
	// + playerData.getQuestData().getCurrent().getFormattedLore());
	// }
	//
	// int restrictions = quest.getLevelRestrictions().size() +
	// (quest.getLevelRestriction(null) > 0 ? 1 : 0);
	// int non = (playerData.getLevel() >= quest.getLevelRestriction(null) ? 1 :
	// 0) + quest.getLevelRestrictions().stream().filter(type ->
	// playerData.getCollectionSkills().getLevel(type) >=
	// quest.getLevelRestriction(type)).collect(Collectors.toSet()).size();
	//
	// if (playerData.getQuestData().hasFinished(quest)) {
	// lore.add("");
	// lore.add(ChatColor.DARK_GRAY + "You've completed this quest on the " +
	// new SimpleDateFormat("MMM d
	// yyyy").format(playerData.getQuestData().getFinishDate(quest)));
	// if (!quest.isRedoable())
	// lore.add(ChatColor.DARK_GRAY + "You can't do this quest twice.");
	// else if (!playerData.getQuestData().checkCooldownAvailability(quest))
	// lore.add(ChatColor.DARK_GRAY + "You can start the quest in " + new
	// DelayFormat(2).format(playerData.getQuestData().getDelayFeft(quest)));
	// else
	// lore.add(ChatColor.DARK_GRAY + "You can start this quest.");
	// }
	//
	// if (restrictions > 0) {
	// lore.add("");
	// lore.add(ChatColor.GRAY + "Level Requirements (" + non + "/" +
	// restrictions + "):");
	// if (quest.getLevelRestriction(null) > 0)
	// lore.add(booleanSymbol(playerData.getLevel() >=
	// quest.getLevelRestriction(null)) + " Level: " +
	// quest.getLevelRestriction(null));
	// for (Profession profession : quest.getLevelRestrictions())
	// lore.add(booleanSymbol(playerData.getCollectionSkills().getLevel(profession)
	// >= quest.getLevelRestriction(profession)) + " " + profession.getName() +
	// " Level: " + quest.getLevelRestriction(profession));
	// }
	//
	// if (current) {
	// lore.add("");
	// lore.add(ChatColor.RED + AltChar.listDash + " Right click to cancel.");
	// }
	//
	// meta.setLore(lore);
	// item.setItemMeta(meta);
	//
	// inv.setItem(slots[j % 21], NBTItem.get(item).add(new ItemTag("questId",
	// quest.getId())).toItem());
	// }
	//
	// if (page > 1)
	// inv.setItem(18, prev = new ConfigItem("PREVIOUS_PAGE").build());
	// if (21 * page < quests.size())
	// inv.setItem(26, next = new ConfigItem("NEXT_PAGE").build());
	//
	// return inv;
	// }
	//
	// private String booleanSymbol(boolean bool) {
	// return bool ? ChatColor.GREEN + AltChar.ok : ChatColor.RED + AltChar.no;
	// }
	//
}
