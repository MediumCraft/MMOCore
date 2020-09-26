package net.Indyuce.mmocore.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.skill.Skill.SkillInfo;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.InventoryPlaceholderItem;
import net.Indyuce.mmocore.gui.api.item.NoPlaceholderItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class SkillList extends EditableInventory {
	public SkillList() {
		super("skill-list");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {

		if (function.equals("skill"))
			return new SkillItem(config);

		if (function.equals("switch"))
			return new SwitchItem(config);

		if (function.equals("level"))
			return new LevelItem(config);

		if (function.equals("upgrade"))
			return new InventoryPlaceholderItem(config) {

				@Override
				public Placeholders getPlaceholders(PluginInventory inv, int n) {
					Skill selected = ((SkillViewerInventory) inv).selected.getSkill();
					Placeholders holders = new Placeholders();

					holders.register("skill_caps", selected.getName().toUpperCase());
					holders.register("skill", selected.getName());
					holders.register("skill_points", "" + inv.getPlayerData().getSkillPoints());

					return holders;
				}

				@Override
				public boolean canDisplay(GeneratedInventory inv) {
					return !((SkillViewerInventory) inv).binding;
				}
			};

		if (function.equals("slot"))
			return new InventoryPlaceholderItem(config) {
				private final String none = MMOLib.plugin.parseColors(config.getString("no-skill"));
				private final Material emptyMaterial = Material
						.valueOf(config.getString("empty-item").toUpperCase().replace("-", "_").replace(" ", "_"));

				@Override
				public Placeholders getPlaceholders(PluginInventory inv, int n) {
					Skill selected = ((SkillViewerInventory) inv).selected.getSkill();
					Skill skill = inv.getPlayerData().hasSkillBound(n) ? inv.getPlayerData().getBoundSkill(n).getSkill() : null;

					Placeholders holders = new Placeholders();

					holders.register("skill", skill == null ? none : skill.getName());
					holders.register("index", "" + (n + 1));
					holders.register("slot", MMOCoreUtils.intToRoman(n + 1));
					holders.register("selected", selected.getName());

					return holders;
				}

				@Override
				public ItemStack display(GeneratedInventory inv, int n) {
					ItemStack item = super.display(inv, n);
					if (!inv.getPlayerData().hasSkillBound(n))
						item.setType(emptyMaterial);
					return item;
				}

				@Override
				public boolean canDisplay(GeneratedInventory inv) {
					return ((SkillViewerInventory) inv).binding;
				}

				@Override
				public boolean hasDifferentDisplay() {
					return true;
				}
			};

		return new NoPlaceholderItem(config);
	}

	public GeneratedInventory newInventory(PlayerData data) {
		return new SkillViewerInventory(data, this);
	}

	public class SwitchItem extends NoPlaceholderItem {
		private final InventoryPlaceholderItem binding, upgrading;

		public SwitchItem(ConfigurationSection config) {
			super(config);

			Validate.isTrue(config.contains("binding"), "Config must have 'binding'");
			Validate.isTrue(config.contains("upgrading"), "Config must have 'upgrading'");

			binding = new NoPlaceholderItem(config.getConfigurationSection("binding"));
			upgrading = new NoPlaceholderItem(config.getConfigurationSection("upgrading"));
		}

		@Override
		public ItemStack display(GeneratedInventory inv, int n) {
			return ((SkillViewerInventory) inv).binding ? upgrading.display(inv) : binding.display(inv);
		}

		@Override
		public boolean canDisplay(GeneratedInventory inv) {
			return true;
		}
	}

	public class LevelItem extends InventoryPlaceholderItem {
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
		public ItemStack display(GeneratedInventory inv, int n) {

			SkillInfo skill = ((SkillViewerInventory) inv).selected;
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
				lore.set(j, ChatColor.GRAY + MMOLib.plugin.parseColors(lore.get(j)));

			ItemStack item = new ItemStack(getMaterial());
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(MMOLib.plugin.parseColors(getName().replace("{skill}", skill.getSkill().getName())
					.replace("{roman}", MMOCoreUtils.intToRoman(skillLevel)).replace("{level}", "" + skillLevel)));
			meta.addItemFlags(ItemFlag.values());
			meta.setLore(lore);
			item.setItemMeta(meta);

			return NBTItem.get(item).addTag(new ItemTag("skillId", skill.getSkill().getId())).toItem();
		}

		@Override
		public Placeholders getPlaceholders(PluginInventory inv, int n) {
			return null;
		}

		@Override
		public boolean canDisplay(GeneratedInventory inv) {
			return !((SkillViewerInventory) inv).binding;
		}
	}

	public class SkillItem extends InventoryPlaceholderItem {
		private final int selectedSkillSlot;

		public SkillItem(ConfigurationSection config) {
			super(Material.BARRIER, config);

			selectedSkillSlot = config.getInt("selected-slot");
		}

		@Override
		public boolean hasDifferentDisplay() {
			return true;
		}

		@Override
		public ItemStack display(GeneratedInventory inv, int n) {

			/*
			 * calculate placeholders
			 */
			SkillViewerInventory viewer = (SkillViewerInventory) inv;
			SkillInfo skill = viewer.skills.get(mod(n + inv.getPlayerData().skillGuiDisplayOffset, viewer.skills.size()));
			Placeholders holders = getPlaceholders(inv.getPlayerData(), skill);

			List<String> lore = new ArrayList<>(getLore());

			int index = lore.indexOf("{lore}");
			lore.remove(index);
			List<String> skillLore = skill.calculateLore(inv.getPlayerData());
			for (int j = 0; j < skillLore.size(); j++)
				lore.add(index + j, skillLore.get(j));

			boolean unlocked = skill.getUnlockLevel() <= inv.getPlayerData().getLevel();

			for (Iterator<String> iterator = lore.iterator(); iterator.hasNext();) {
				String next = iterator.next();
				if ((next.startsWith("{unlocked}") && !unlocked) || (next.startsWith("{locked}") && unlocked) || (next.startsWith("{max_level}")
						&& (!skill.hasMaxLevel() || skill.getMaxLevel() > inv.getPlayerData().getSkillLevel(skill.getSkill()))))
					iterator.remove();
			}

			for (int j = 0; j < lore.size(); j++)
				lore.set(j, ChatColor.GRAY + holders.apply(inv.getPlayer(), lore.get(j)));

			/*
			 * generate item
			 */
			ItemStack item = skill.getSkill().getIcon();
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(holders.apply(inv.getPlayer(), new String(getName())));
			meta.addItemFlags(ItemFlag.values());
			meta.setLore(lore);
			item.setItemMeta(meta);

			return NBTItem.get(item).addTag(new ItemTag("skillId", skill.getSkill().getId())).toItem();
		}

		public Placeholders getPlaceholders(PlayerData player, SkillInfo skill) {
			Placeholders holders = new Placeholders();
			holders.register("skill", skill.getSkill().getName());
			holders.register("unlock", "" + skill.getUnlockLevel());
			holders.register("level", "" + player.getSkillLevel(skill.getSkill()));
			return holders;
		}

		@Override
		public Placeholders getPlaceholders(PluginInventory inv, int n) {
			return null;
		}
	}

	public class SkillViewerInventory extends GeneratedInventory {

		/*
		 * cached information
		 */
		private final List<SkillInfo> skills;
		private final List<Integer> skillSlots;
		private final List<Integer> slotSlots;

		private boolean binding;
		private SkillInfo selected;

		public SkillViewerInventory(PlayerData playerData, EditableInventory editable) {
			super(playerData, editable);

			skills = new ArrayList<>(playerData.getProfess().getSkills());
			skillSlots = getEditable().getByFunction("skill").getSlots();
			slotSlots = getEditable().getByFunction("slot").getSlots();
		}

		@Override
		public String calculateName() {
			return getName();
		}

		@Override
		public void open() {
			int selectedSkillSlot = ((SkillItem) getEditable().getByFunction("skill")).selectedSkillSlot;
			selected = skills.get(mod(selectedSkillSlot + playerData.skillGuiDisplayOffset, skills.size()));

			super.open();
		}

		@Override
		public void whenClicked(InventoryClickEvent event, InventoryItem item) {

			if (skillSlots.contains(event.getRawSlot())
					&& event.getRawSlot() != ((SkillItem) getEditable().getByFunction("skill")).selectedSkillSlot) {
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
				playerData.skillGuiDisplayOffset = (playerData.skillGuiDisplayOffset + (event.getRawSlot() - 13)) % skills.size();
				open();
				return;
			}

			if (item.getFunction().equals("previous")) {
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
				playerData.skillGuiDisplayOffset = (playerData.skillGuiDisplayOffset - 1) % skills.size();
				open();
				return;
			}

			if (item.getFunction().equals("next")) {
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
				playerData.skillGuiDisplayOffset = (playerData.skillGuiDisplayOffset + 1) % skills.size();
				open();
				return;
			}

			if (item.getFunction().equals("switch")) {
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
				binding = !binding;
				open();
				return;
			}

			/*
			 * binding or unbinding skills.
			 */
			if (binding) {
				for (int index = 0; index < slotSlots.size(); index++) {
					int slot = slotSlots.get(index);
					if (event.getRawSlot() == slot) {

						// unbind if there is a current spell.
						if (event.getAction() == InventoryAction.PICKUP_HALF) {
							if (!playerData.hasSkillBound(index)) {
								MMOCore.plugin.configManager.getSimpleMessage("no-skill-bound").send(player);
								player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
								return;
							}

							player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
							playerData.unbindSkill(index);
							open();
							return;
						}

						if (selected == null)
							return;

						if (selected.getSkill().isPassive()) {
							MMOCore.plugin.configManager.getSimpleMessage("not-active-skill").send(player);
							player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
							return;
						}

						if (!playerData.hasSkillUnlocked(selected)) {
							MMOCore.plugin.configManager.getSimpleMessage("not-unlocked-skill").send(player);
							player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
							return;
						}

						player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
						playerData.setBoundSkill(index, selected);
						open();
						return;
					}
				}

				/*
				 * upgrading a player skill
				 */
			} else if (item.getFunction().equals("upgrade")) {
				if (!playerData.hasSkillUnlocked(selected)) {
					MMOCore.plugin.configManager.getSimpleMessage("not-unlocked-skill").send(player);
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
					return;
				}

				if (playerData.getSkillPoints() < 1) {
					MMOCore.plugin.configManager.getSimpleMessage("not-enough-skill-points").send(player);
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
					return;
				}

				if (selected.hasMaxLevel() && playerData.getSkillLevel(selected.getSkill()) >= selected.getMaxLevel()) {
					MMOCore.plugin.configManager.getSimpleMessage("skill-max-level-hit").send(player);
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
					return;
				}

				playerData.giveSkillPoints(-1);
				playerData.setSkillLevel(selected.getSkill(), playerData.getSkillLevel(selected.getSkill()) + 1);
				MMOCore.plugin.configManager.getSimpleMessage("upgrade-skill", "skill", selected.getSkill().getName(), "level",
						"" + playerData.getSkillLevel(selected.getSkill())).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
				open();
			}
		}
	}

	private int mod(int x, int n) {
		return x < 0 ? (x + n) : (x % n);
	}
}