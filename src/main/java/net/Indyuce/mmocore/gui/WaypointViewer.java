package net.Indyuce.mmocore.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.Waypoint;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.InventoryPlaceholderItem;
import net.Indyuce.mmocore.gui.api.item.NoPlaceholderItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class WaypointViewer extends EditableInventory {
	public WaypointViewer() {
		super("waypoints");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {

		if (function.equals("waypoint"))
			return new WaypointItem(config);

		if (function.equals("previous"))
			return new NoPlaceholderItem(config) {

				@Override
				public boolean canDisplay(GeneratedInventory inv) {
					return ((WaypointViewerInventory) inv).page > 0;
				}
			};

		if (function.equals("next"))
			return new NoPlaceholderItem(config) {

				@Override
				public boolean canDisplay(GeneratedInventory inv) {
					WaypointViewerInventory generated = (WaypointViewerInventory) inv;
					return inv.getEditable().getByFunction("waypoint").getSlots().size() * (generated.page + 1) < generated.waypoints.size();
				}
			};

		return new NoPlaceholderItem(config);
	}

	public GeneratedInventory newInventory(PlayerData data) {
		return newInventory(data, null);
	}

	public GeneratedInventory newInventory(PlayerData data, Waypoint waypoint) {
		return new WaypointViewerInventory(data, this, waypoint);
	}

	public class WaypointDisplayItem extends InventoryPlaceholderItem {
		private final Material notReady;

		public WaypointDisplayItem(ConfigurationSection config) {
			super(config);

			Validate.isTrue(config.contains("not-ready"), "Could not read 'not-ready' material");
			notReady = Material.valueOf(config.getString("not-ready"));
		}

		@Override
		public ItemStack display(GeneratedInventory inv, int n) {
			WaypointViewerInventory generated = (WaypointViewerInventory) inv;
			ItemStack disp = super.display(inv, n);

			Waypoint waypoint = generated.waypoints.get(generated.page * 27 + n);
			if (inv.getPlayerData().getStellium() < waypoint.getStelliumCost() || (generated.current == null && !waypoint.isDynamic()))
				disp.setType(notReady);

			return NBTItem.get(disp).addTag(new ItemTag("waypointId", waypoint.getId())).toItem();
		}

		@Override
		public boolean canDisplay(GeneratedInventory inv) {
			return true;
		}

		@Override
		public Placeholders getPlaceholders(PluginInventory inv, int n) {
			WaypointViewerInventory generated = (WaypointViewerInventory) inv;
			Placeholders holders = new Placeholders();

			Waypoint waypoint = generated.waypoints.get(generated.page * 27 + n);
			holders.register("name", waypoint.getName());
			holders.register("stellium", decimal.format(waypoint.getStelliumCost()));

			return holders;
		}
	}

	public class WaypointItem extends InventoryItem {
		private final InventoryPlaceholderItem noWaypoint, locked;
		private final WaypointDisplayItem availWaypoint;

		public WaypointItem(ConfigurationSection config) {
			super(config);

			Validate.notNull(config.getConfigurationSection("no-waypoint"), "Could not load 'no-waypoint' config");
			Validate.notNull(config.getConfigurationSection("locked"), "Could not load 'locked' config");
			Validate.notNull(config.getConfigurationSection("display"), "Could not load 'display' config");

			noWaypoint = new NoPlaceholderItem(config.getConfigurationSection("no-waypoint"));
			locked = new NoPlaceholderItem(config.getConfigurationSection("locked"));
			availWaypoint = new WaypointDisplayItem(config.getConfigurationSection("display"));
		}

		public boolean hasDifferentDisplay() {
			return true;
		}

		@Override
		public ItemStack display(GeneratedInventory inv, int n) {
			WaypointViewerInventory generated = (WaypointViewerInventory) inv;

			int index = generated.page * 27 + n;
			if (index >= generated.waypoints.size())
				return noWaypoint.display(inv, n);

			Waypoint waypoint = generated.waypoints.get(generated.page * 27 + n);
			return inv.getPlayerData().hasWaypoint(waypoint) ? availWaypoint.display(inv, n) : locked.display(inv);
		}

		@Override
		public boolean canDisplay(GeneratedInventory inv) {
			return true;
		}
	}

	public class WaypointViewerInventory extends GeneratedInventory {
		private final List<Waypoint> waypoints = new ArrayList<>(MMOCore.plugin.waypointManager.getAll());
		private final Waypoint current;

		private int page;

		public WaypointViewerInventory(PlayerData playerData, EditableInventory editable, Waypoint current) {
			super(playerData, editable);

			this.current = current;
		}

		@Override
		public String calculateName() {
			return getName();
		}

		@Override
		public void whenClicked(InventoryClickEvent event, InventoryItem item) {
			if (item.getFunction().equals("next")) {
				page++;
				open();
				return;
			}

			if (item.getFunction().equals("previous")) {
				page--;
				open();
				return;
			}

			if (item.getFunction().equals("waypoint")) {
				String tag = NBTItem.get(event.getCurrentItem()).getString("waypointId");
				if (tag.equals(""))
					return;

				Waypoint waypoint = MMOCore.plugin.waypointManager.get(tag);
				if (!playerData.hasWaypoint(waypoint)) {
					MMOCore.plugin.configManager.getSimpleMessage("not-unlocked-waypoint").send(player);
					return;
				}

				if (waypoint.equals(current)) {
					MMOCore.plugin.configManager.getSimpleMessage("standing-on-waypoint").send(player);
					return;
				}

				if (current == null && !waypoint.isDynamic()) {
					MMOCore.plugin.configManager.getSimpleMessage("not-dynamic-waypoint").send(player);
					return;
				}

				double left = waypoint.getStelliumCost() - playerData.getStellium();
				if (left > 0) {
					MMOCore.plugin.configManager.getSimpleMessage("not-enough-stellium", "more", decimal.format(left)).send(player);
					return;
				}

				double next = (double) playerData.getNextWaypointMillis() / 1000;
				if (next < 0) {
					MMOCore.plugin.configManager.getSimpleMessage("not-enough-stellium", "cooldown", decimal.format(next)).send(player);
					return;
				}

				player.closeInventory();
				playerData.warp(waypoint);
			}
		}
	}
}
