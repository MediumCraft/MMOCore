package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.manager.StatManager;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.api.event.PlayerAttributeUseEvent;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttributes;
import net.Indyuce.mmocore.api.SoundEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

public class AttributeView extends EditableInventory {
	public AttributeView() {
		super("attribute-view");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {
		if (function.equalsIgnoreCase("reallocation"))
			return new InventoryItem(config) {

				@Override
				public Placeholders getPlaceholders(GeneratedInventory inv, int n) {
					Placeholders holders = new Placeholders();
					holders.register("attribute_points", inv.getPlayerData().getAttributePoints());
					holders.register("points", inv.getPlayerData().getAttributeReallocationPoints());
					holders.register("total", inv.getPlayerData().getAttributes().countPoints());
					return holders;
				}
			};

		return function.startsWith("attribute_") ? new AttributeItem(function, config) : new SimplePlaceholderItem(config);
	}

	public GeneratedInventory newInventory(PlayerData data) {
		return new AttributeViewerInventory(data, this);
	}

	public static class AttributeItem extends InventoryItem {
		private final PlayerAttribute attribute;
		private final int shiftCost;

		public AttributeItem(String function, ConfigurationSection config) {
			super(config);

			attribute = MMOCore.plugin.attributeManager
					.get(function.substring("attribute_".length()).toLowerCase().replace(" ", "-").replace("_", "-"));
			shiftCost = Math.max(config.getInt("shift-cost"), 1);
		}

		@Override
		public Placeholders getPlaceholders(GeneratedInventory inv, int n) {
			int total = inv.getPlayerData().getAttributes().getInstance(attribute).getTotal();

			Placeholders holders = new Placeholders();
			holders.register("name", attribute.getName());
			holders.register("buffs", attribute.getBuffs().size());
			holders.register("spent", inv.getPlayerData().getAttributes().getInstance(attribute).getBase());
			holders.register("max", attribute.getMax());
			holders.register("current", total);
			holders.register("attribute_points", inv.getPlayerData().getAttributePoints());
			holders.register("shift_points", shiftCost);
			attribute.getBuffs().forEach(buff -> {
				final String stat = buff.getStat();
				holders.register("buff_" + buff.getStat().toLowerCase(), StatManager.format(stat, buff.getValue()));
				holders.register("total_" + buff.getStat().toLowerCase(), StatManager.format(stat, buff.multiply(total).getValue()));
			});

			return holders;
		}
	}

	public class AttributeViewerInventory extends GeneratedInventory {

		public AttributeViewerInventory(PlayerData playerData, EditableInventory editable) {
			super(playerData, editable);

		}

		@Override
		public String calculateName() {
			return getName();
		}

		@Override
		public void whenClicked(InventoryClickContext context, InventoryItem item) {

			if (item.getFunction().equalsIgnoreCase("reallocation")) {
				int spent = playerData.getAttributes().countPoints();
				if (spent < 1) {
					MMOCore.plugin.configManager.getSimpleMessage("no-attribute-points-spent").send(player);
					MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
					return;
				}

				if (playerData.getAttributeReallocationPoints() < 1) {
					MMOCore.plugin.configManager.getSimpleMessage("not-attribute-reallocation-point").send(player);
					MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
					return;
				}

				playerData.getAttributes().getInstances().forEach(ins -> ins.setBase(0));
				playerData.giveAttributePoints(spent);
				playerData.giveAttributeReallocationPoints(-1);
				MMOCore.plugin.configManager.getSimpleMessage("attribute-points-reallocated", "points", "" + playerData.getAttributePoints()).send(player);
				MMOCore.plugin.soundManager.getSound(SoundEvent.RESET_ATTRIBUTES).playTo(getPlayer());
				open();
			}

			if (item.getFunction().startsWith("attribute_")) {
				PlayerAttribute attribute = ((AttributeItem) item).attribute;

				if (playerData.getAttributePoints() < 1) {
					MMOCore.plugin.configManager.getSimpleMessage("not-attribute-point").send(player);
					MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
					return;
				}

				PlayerAttributes.AttributeInstance ins = playerData.getAttributes().getInstance(attribute);
				if (attribute.hasMax() && ins.getBase() >= attribute.getMax()) {
					MMOCore.plugin.configManager.getSimpleMessage("attribute-max-points-hit").send(player);
					MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
					return;
				}

				// Amount of points spent
				final boolean shiftClick = context.getClickType().isShiftClick();
				int pointsSpent = shiftClick ? ((AttributeItem) item).shiftCost : 1;
				if (attribute.hasMax())
					pointsSpent = Math.min(pointsSpent, attribute.getMax() - ins.getBase());

				if (shiftClick && playerData.getAttributePoints() < pointsSpent) {
					MMOCore.plugin.configManager.getSimpleMessage("not-attribute-point-shift", "shift_points", String.valueOf(pointsSpent)).send(player);
					MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
					return;
				}

				ins.addBase(pointsSpent);
				playerData.giveAttributePoints(-pointsSpent);

				// Apply exp table as many times as required
				if (attribute.hasExperienceTable())
					while (pointsSpent-- > 0)
						attribute.getExperienceTable().claim(playerData, ins.getBase(), attribute);

				MMOCore.plugin.configManager.getSimpleMessage("attribute-level-up", "attribute", attribute.getName(), "level", String.valueOf(ins.getBase())).send(player);
				MMOCore.plugin.soundManager.getSound(SoundEvent.LEVEL_ATTRIBUTE).playTo(getPlayer());

				PlayerAttributeUseEvent playerAttributeUseEvent = new PlayerAttributeUseEvent(playerData, attribute);
				Bukkit.getServer().getPluginManager().callEvent(playerAttributeUseEvent);

				open();
			}
		}
	}
}