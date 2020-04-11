package net.Indyuce.mmocore.gui;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.experience.Booster;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.InventoryPlaceholderItem;
import net.Indyuce.mmocore.gui.api.item.NoPlaceholderItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.mmogroup.mmolib.version.VersionMaterial;

public class PlayerStats extends EditableInventory {
	public PlayerStats() {
		super("player-stats");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {

		if (function.equals("boost"))
			return new BoostItem(config);

		if (function.equals("boost-next"))
			return new NoPlaceholderItem(config) {

				@Override
				public boolean hasDifferentDisplay() {
					return true;
				}

				@Override
				public boolean canDisplay(GeneratedInventory inv) {
					InventoryItem boost = inv.getByFunction("boost");
					return boost != null && ((PlayerStatsInventory) inv).boostOffset + boost.getSlots().size() < MMOCore.plugin.boosterManager.getBoosters().size();
				}
			};

		if (function.equals("boost-previous"))
			return new NoPlaceholderItem(config) {

				@Override
				public boolean hasDifferentDisplay() {
					return true;
				}

				@Override
				public boolean canDisplay(GeneratedInventory inv) {
					return ((PlayerStatsInventory) inv).boostOffset > 0;
				}
			};

		if (function.equals("party"))
			return new PartyMoraleItem(config);

		if (function.startsWith("profession_")) {
			String id = function.substring("profession_".length()).toLowerCase();
			Validate.isTrue(MMOCore.plugin.professionManager.has(id));
			Profession profession = MMOCore.plugin.professionManager.get(id);

			return new InventoryPlaceholderItem(config) {

				@Override
				public Placeholders getPlaceholders(PluginInventory inv, int n) {

					Placeholders holders = new Placeholders();
					net.Indyuce.mmocore.api.player.stats.PlayerStats stats = inv.getPlayerData().getStats();

					double ratio = (double) inv.getPlayerData().getCollectionSkills().getExperience(profession)
							/ (double) inv.getPlayerData().getCollectionSkills().getLevelUpExperience(profession);

					String bar = "" + ChatColor.BOLD;
					int chars = (int) (ratio * 20);
					for (int j = 0; j < 20; j++)
						bar += (j == chars ? "" + ChatColor.WHITE + ChatColor.BOLD : "") + "|";

					// holders.register("profession", type.getName());
					holders.register("progress", bar);
					holders.register("level", "" + inv.getPlayerData().getCollectionSkills().getLevel(profession));
					holders.register("xp", inv.getPlayerData().getCollectionSkills().getExperience(profession));
					holders.register("percent", decimal.format(ratio * 100));
					for (StatType stat : StatType.values())
						if (stat.matches(profession))
							holders.register(stat.name().toLowerCase(), stat.format(stats.getStat(stat)));

					return holders;
				}
			};
		}

		if (function.equals("profile"))
			return new PlayerProfileItem(config);

		if (function.equals("stats"))
			return new InventoryPlaceholderItem(config) {

				@Override
				public Placeholders getPlaceholders(PluginInventory inv, int n) {

					net.Indyuce.mmocore.api.player.stats.PlayerStats stats = inv.getPlayerData().getStats();
					Placeholders holders = new Placeholders();

					for (StatType stat : StatType.values()) {
						double base = stats.getBase(stat), total = stats.getInstance(stat).getTotal(base), extra = total - base;
						holders.register(stat.name().toLowerCase(), stat.format(extra + base));
						holders.register(stat.name().toLowerCase() + "_base", stat.format(base));
						holders.register(stat.name().toLowerCase() + "_extra", stat.format(extra));
					}

					for (PlayerAttribute attribute : MMOCore.plugin.attributeManager.getAll())
						holders.register("attribute_" + attribute.getId().replace("-", "_"), inv.getPlayerData().getAttributes().getAttribute(attribute));

					return holders;
				}
			};

		return new NoPlaceholderItem(config);
	}

	public PlayerStatsInventory newInventory(PlayerData data) {
		return new PlayerStatsInventory(data, this);
	}

	public class PlayerStatsInventory extends GeneratedInventory {
		private int boostOffset;

		public PlayerStatsInventory(PlayerData playerData, EditableInventory editable) {
			super(playerData, editable);
		}

		@Override
		public String calculateName() {
			return getName();
		}

		@Override
		public void whenClicked(InventoryClickEvent event, InventoryItem item) {
			if (item.hasFunction())
				if (item.getFunction().equals("boost-next")) {
					boostOffset++;
					open();

				} else if (item.getFunction().equals("boost-previous")) {
					boostOffset--;
					open();
				}
		}
	}

	public class BoostItem extends InventoryItem {
		private final InventoryPlaceholderItem noBoost, mainLevel, profession;

		public BoostItem(ConfigurationSection config) {
			super(config);

			ConfigurationSection noBoost = config.getConfigurationSection("no-boost");
			Validate.notNull(noBoost, "Could not load 'no-boost' config");
			this.noBoost = new NoPlaceholderItem(noBoost);

			ConfigurationSection mainLevel = config.getConfigurationSection("main-level");
			Validate.notNull(mainLevel, "Could not load 'main-level' config");
			this.mainLevel = new InventoryPlaceholderItem(mainLevel) {

				@Override
				public Placeholders getPlaceholders(PluginInventory inv, int n) {
					Placeholders holders = new Placeholders();
					Booster boost = MMOCore.plugin.boosterManager.get(((PlayerStatsInventory) inv).boostOffset + n);

					holders.register("author", boost.hasAuthor() ? boost.getAuthor() : "Server");
					holders.register("value", (int) (boost.getExtra() * 100));
					holders.register("left", new DelayFormat(2).format(boost.getLeft()));

					return holders;
				}
			};

			ConfigurationSection profession = config.getConfigurationSection("profession");
			Validate.notNull(profession, "Could not load 'profession' config");
			this.profession = new InventoryPlaceholderItem(profession) {

				@Override
				public Placeholders getPlaceholders(PluginInventory inv, int n) {
					Placeholders holders = new Placeholders();
					Booster boost = MMOCore.plugin.boosterManager.get(((PlayerStatsInventory) inv).boostOffset + n);

					holders.register("author", boost.hasAuthor() ? boost.getAuthor() : "Server");
					holders.register("profession", boost.getProfession().getName());
					holders.register("value", (int) (boost.getExtra() * 100));
					holders.register("left", new DelayFormat(2).format(boost.getLeft()));

					return holders;
				}
			};
		}

		@Override
		public boolean hasDifferentDisplay() {
			return true;
		}

		@Override
		public ItemStack display(GeneratedInventory inv, int n) {
			int offset = ((PlayerStatsInventory) inv).boostOffset;
			if (n + offset >= MMOCore.plugin.boosterManager.getBoosters().size())
				return noBoost.display(inv, n);

			Booster boost = MMOCore.plugin.boosterManager.get(((PlayerStatsInventory) inv).boostOffset + n);
			return amount(boost.hasProfession() ? profession.display(inv, n) : mainLevel.display(inv, n), n + offset + 1);
		}

		@Override
		public boolean canDisplay(GeneratedInventory inv) {
			return true;
		}
	}

	private ItemStack amount(ItemStack item, int amount) {
		item.setAmount(amount);
		return item;
	}

	public class PartyMoraleItem extends InventoryPlaceholderItem {
		public PartyMoraleItem(ConfigurationSection config) {
			super(config);
		}

		@Override
		public Placeholders getPlaceholders(PluginInventory inv, int n) {
			Placeholders holders = new Placeholders();

			int count = inv.getPlayerData().getParty().getMembers().count();
			holders.register("count", "" + count);
			for (StatType stat : MMOCore.plugin.partyManager.getBonuses())
				holders.register("buff_" + stat.name().toLowerCase(), MMOCore.plugin.partyManager.getBonus(stat).multiply(count - 1).toString());

			return holders;
		}

		@Override
		public boolean canDisplay(GeneratedInventory inv) {
			return inv.getPlayerData().hasParty() && inv.getPlayerData().getParty().getMembers().count() > 1;
		}

	}

	public class PlayerProfileItem extends InventoryPlaceholderItem {
		public PlayerProfileItem(ConfigurationSection config) {
			super(config);
		}

		@Override
		public ItemStack display(GeneratedInventory inv, int n) {
			ItemStack item = super.display(inv, n);
			if (item.getType() == VersionMaterial.PLAYER_HEAD.toMaterial()) {
				SkullMeta meta = (SkullMeta) item.getItemMeta();
				meta.setOwningPlayer(inv.getPlayer());
				item.setItemMeta(meta);
			}
			return item;
		}

		@Override
		public Placeholders getPlaceholders(PluginInventory inv, int n) {

			PlayerData data = inv.getPlayerData();
			Placeholders holders = new Placeholders();

			int nextLevelExp = inv.getPlayerData().getLevelUpExperience();
			double ratio = (double) data.getExperience() / (double) nextLevelExp;

			String bar = "" + ChatColor.BOLD;
			int chars = (int) (ratio * 20);
			for (int j = 0; j < 20; j++)
				bar += (j == chars ? "" + ChatColor.WHITE + ChatColor.BOLD : "") + "|";

			holders.register("percent", decimal.format(ratio * 100));
			holders.register("exp", "" + data.getExperience());
			holders.register("level", "" + data.getLevel());
			holders.register("class_points", "" + data.getClassPoints());
			holders.register("skill_points", "" + data.getSkillPoints());
			holders.register("attribute_points", "" + data.getAttributePoints());
			holders.register("progress", bar);
			holders.register("next_level", "" + nextLevelExp);
			holders.register("player", "" + data.getPlayer().getName());
			holders.register("class", "" + data.getProfess().getName());

			return holders;
		}
	}
}
