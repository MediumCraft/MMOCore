package net.Indyuce.mmocore.gui;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttributes.AttributeInstance;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.InventoryPlaceholderItem;
import net.Indyuce.mmocore.gui.api.item.NoPlaceholderItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;

public class AttributeView extends EditableInventory {
	public AttributeView() {
		super("attribute-view");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {
		if (function.equalsIgnoreCase("reallocation"))
			return new InventoryPlaceholderItem(config) {

				@Override
				public Placeholders getPlaceholders(PluginInventory inv, int n) {
					Placeholders holders = new Placeholders();
					holders.register("attribute_points", inv.getPlayerData().getAttributePoints());
					holders.register("points", inv.getPlayerData().getAttributeReallocationPoints());
					holders.register("total", inv.getPlayerData().getAttributes().countSkillPoints());
					return holders;
				}
			};

		return function.startsWith("attribute_") ? new AttributeItem(function, config) : new NoPlaceholderItem(config);
	}

	public GeneratedInventory newInventory(PlayerData data) {
		return new SkillViewerInventory(data, this);
	}

	public static class AttributeItem extends InventoryPlaceholderItem {
		private final PlayerAttribute attribute;

		public AttributeItem(String function, ConfigurationSection config) {
			super(config);

			attribute = MMOCore.plugin.attributeManager
					.get(function.substring("attribute_".length()).toLowerCase().replace(" ", "-").replace("_", "-"));
		}

		@Override
		public Placeholders getPlaceholders(PluginInventory inv, int n) {
			int total = inv.getPlayerData().getAttributes().getInstance(attribute).getTotal();

			Placeholders holders = new Placeholders();
			holders.register("name", attribute.getName());
			holders.register("buffs", attribute.getBuffs().size());
			holders.register("spent", inv.getPlayerData().getAttributes().getInstance(attribute).getBase());
			holders.register("max", attribute.getMax());
			holders.register("current", total);
			holders.register("attribute_points", inv.getPlayerData().getAttributePoints());
			attribute.getBuffs().forEach((key, buff) -> {
				holders.register("buff_" + key.toLowerCase(), buff);
				holders.register("total_" + key.toLowerCase(), buff.multiply(total));
			});
			return holders;
		}
	}

	public class SkillViewerInventory extends GeneratedInventory {
		public SkillViewerInventory(PlayerData playerData, EditableInventory editable) {
			super(playerData, editable);
		}

		@Override
		public String calculateName() {
			return getName();
		}

		@Override
		public void whenClicked(InventoryClickEvent event, InventoryItem item) {

			if (item.getFunction().equalsIgnoreCase("reallocation")) {
				int spent = playerData.getAttributes().countSkillPoints();
				if (spent < 1) {
					MMOCore.plugin.configManager.getSimpleMessage("no-attribute-points-spent").send(player);
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					return;
				}

				if (playerData.getAttributeReallocationPoints() < 1) {
					MMOCore.plugin.configManager.getSimpleMessage("not-attribute-reallocation-point").send(player);
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					return;
				}

				playerData.getAttributes().getInstances().forEach(ins -> ins.setBase(0));
				playerData.giveAttributePoints(spent);
				playerData.giveAttributeReallocationPoints(-1);
				MMOCore.plugin.configManager.getSimpleMessage("attribute-points-reallocated", "points", "" + playerData.getAttributePoints()).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
				open();
			}

			if (item.getFunction().startsWith("attribute_")) {
				PlayerAttribute attribute = ((AttributeItem) item).attribute;

				if (playerData.getAttributePoints() < 1) {
					MMOCore.plugin.configManager.getSimpleMessage("not-attribute-point").send(player);
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					return;
				}

				AttributeInstance ins = playerData.getAttributes().getInstance(attribute);
				if (attribute.hasMax() && ins.getBase() >= attribute.getMax()) {
					MMOCore.plugin.configManager.getSimpleMessage("attribute-max-points-hit").send(player);
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					return;
				}

				ins.addBase(1);
				playerData.giveAttributePoints(-1);
				MMOCore.plugin.configManager.getSimpleMessage("attribute-level-up", "attribute", attribute.getName(), "level", "" + ins.getBase()).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
				open();
			}
		}
	}
}