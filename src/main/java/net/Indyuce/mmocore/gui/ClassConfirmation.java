package net.Indyuce.mmocore.gui;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerChangeClassEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.InventoryPlaceholderItem;
import net.Indyuce.mmocore.gui.api.item.NoPlaceholderItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;

public class ClassConfirmation extends EditableInventory {
	public ClassConfirmation() {
		super("class-confirm");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {
		return function.equalsIgnoreCase("yes") ? new YesItem(config) : new NoPlaceholderItem(config);
	}

	public GeneratedInventory newInventory(PlayerData data, PlayerClass profess, PluginInventory last) {
		return new ClassConfirmationInventory(data, this, profess, last);
	}

	public class UnlockedItem extends InventoryPlaceholderItem {

		public UnlockedItem(ConfigurationSection config) {
			super(config);
		}

		@Override
		public Placeholders getPlaceholders(PluginInventory inv, int n) {
			PlayerClass profess = ((ClassConfirmationInventory) inv).profess;
			SavedClassInformation info = inv.getPlayerData().getClassInfo(profess);
			Placeholders holders = new Placeholders();

			int nextLevelExp = inv.getPlayerData().getLevelUpExperience();
			double ratio = (double) info.getExperience() / (double) nextLevelExp;

			StringBuilder bar = new StringBuilder("" + ChatColor.BOLD);
			int chars = (int) (ratio * 20);
			for (int j = 0; j < 20; j++)
				bar.append(j == chars ? "" + ChatColor.WHITE + ChatColor.BOLD : "").append("|");

			holders.register("percent", decimal.format(ratio * 100));
			holders.register("progress", bar.toString());
			holders.register("class", profess.getName());
			holders.register("unlocked_skills", info.getSkillKeys().size());
			holders.register("class_skills", profess.getSkills().size());
			holders.register("next_level", "" + nextLevelExp);
			holders.register("level", info.getLevel());
			holders.register("exp", info.getExperience());
			holders.register("skill_points", info.getSkillPoints());

			return holders;
		}
	}

	public class YesItem extends InventoryItem {
		private final InventoryPlaceholderItem unlocked, locked;

		public YesItem(ConfigurationSection config) {
			super(config);

			Validate.isTrue(config.contains("unlocked"), "Could not load 'unlocked' config");
			Validate.isTrue(config.contains("locked"), "Could not load 'locked' config");

			unlocked = new UnlockedItem(config.getConfigurationSection("unlocked"));
			locked = new InventoryPlaceholderItem(config.getConfigurationSection("locked")) {

				@Override
				public Placeholders getPlaceholders(PluginInventory inv, int n) {
					Placeholders holders = new Placeholders();
					holders.register("class", ((ClassConfirmationInventory) inv).profess.getName());
					return holders;
				}
			};
		}

		@Override
		public ItemStack display(GeneratedInventory inv, int n) {
			return inv.getPlayerData().hasSavedClass(((ClassConfirmationInventory) inv).profess)
					? unlocked.display(inv, n)
					: locked.display(inv, n);
		}

		@Override
		public boolean canDisplay(GeneratedInventory inv) {
			return true;
		}
	}

	public class ClassConfirmationInventory extends GeneratedInventory {
		private final PlayerClass profess;
		private final PluginInventory last;

		public ClassConfirmationInventory(PlayerData playerData, EditableInventory editable, PlayerClass profess,
				PluginInventory last) {
			super(playerData, editable);

			this.profess = profess;
			this.last = last;
		}

		@Override
		public void whenClicked(InventoryClickEvent event, InventoryItem item) {
			if (event.getInventory() != event.getClickedInventory())
				return;

			if (item.getFunction().equals("back"))
				last.open();

			else if (item.getFunction().equals("yes")) {

				PlayerChangeClassEvent called = new PlayerChangeClassEvent(playerData, profess);
				Bukkit.getPluginManager().callEvent(called);
				if (called.isCancelled())
					return;

				playerData.giveClassPoints(-1);
				(playerData.hasSavedClass(profess) ? playerData.getClassInfo(profess) : new SavedClassInformation(
					MMOCore.plugin.dataProvider.getDataManager().getDefaultData())).load(profess, playerData);
				while (playerData.hasSkillBound(0))
					playerData.unbindSkill(0);
				MMOCore.plugin.configManager.getSimpleMessage("class-select", "class", profess.getName()).send(player);
				player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
				player.closeInventory();
			}
		}

		@Override
		public String calculateName() {
			return getName();
		}
	}
}
