package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.Waypoint;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WaypointViewer extends EditableInventory {
	public WaypointViewer() {
		super("waypoints");
	}

	@Override
	public InventoryItem load(String function, ConfigurationSection config) {

		if (function.equals("waypoint"))
			return new WaypointItem(config);

		if (function.equals("previous"))
			return new SimplePlaceholderItem<WaypointViewerInventory>(config) {

				@Override
				public boolean canDisplay(WaypointViewerInventory inv) {
					return inv.page > 0;
				}
			};

		if (function.equals("next"))
			return new SimplePlaceholderItem<WaypointViewerInventory>(config) {

				@Override
				public boolean canDisplay(WaypointViewerInventory inv) {
					return inv.getEditable().getByFunction("waypoint").getSlots().size() * (inv.page + 1) < inv.waypoints.size();
				}
			};

		return new SimplePlaceholderItem(config);
	}

	public GeneratedInventory newInventory(PlayerData data) {
		return newInventory(data, null);
	}

	public GeneratedInventory newInventory(PlayerData data, Waypoint waypoint) {
		return new WaypointViewerInventory(data, this, waypoint);
	}

	public class WaypointDisplayItem extends InventoryItem<WaypointViewerInventory> {
		private final Material notReady;

		public WaypointDisplayItem(ConfigurationSection config) {
			super(config);

			Validate.isTrue(config.contains("not-ready"), "Could not read 'not-ready' material");
			notReady = Material.valueOf(config.getString("not-ready"));
		}

		@Override
		public ItemStack display(WaypointViewerInventory inv, int n) {
			ItemStack disp = super.display(inv, n);

			Waypoint waypoint = inv.waypoints.get(inv.page * inv.getEditable().getByFunction("waypoint").getSlots().size() + n);
			if (inv.getPlayerData().getStellium() < waypoint.getStelliumCost() || (inv.current == null && !waypoint.isDynamic()))
				disp.setType(notReady);

			return NBTItem.get(disp).addTag(new ItemTag("waypointId", waypoint.getId())).toItem();
		}

		@Override
		public Placeholders getPlaceholders(WaypointViewerInventory inv, int n) {
			Placeholders holders = new Placeholders();

			Waypoint waypoint = inv.waypoints.get(inv.page * inv.getByFunction("waypoint").getSlots().size() + n);
			holders.register("name", waypoint.getName());
			holders.register("stellium", decimal.format(waypoint.getStelliumCost()));

			return holders;
		}
	}

	public class WaypointItem extends SimplePlaceholderItem<WaypointViewerInventory> {
		private final SimplePlaceholderItem noWaypoint, locked;
		private final WaypointDisplayItem availWaypoint;

		public WaypointItem(ConfigurationSection config) {
			super(Material.BARRIER, config);

			Validate.notNull(config.getConfigurationSection("no-waypoint"), "Could not load 'no-waypoint' config");
			Validate.notNull(config.getConfigurationSection("locked"), "Could not load 'locked' config");
			Validate.notNull(config.getConfigurationSection("display"), "Could not load 'display' config");

			noWaypoint = new SimplePlaceholderItem(config.getConfigurationSection("no-waypoint"));
			locked = new SimplePlaceholderItem(config.getConfigurationSection("locked"));
			availWaypoint = new WaypointDisplayItem(config.getConfigurationSection("display"));
		}

		@Override
		public boolean hasDifferentDisplay() {
			return true;
		}

		@Override
		public ItemStack display(WaypointViewerInventory inv, int n) {

			int index = inv.page * inv.getEditable().getByFunction("waypoint").getSlots().size() + n;
			if (index >= inv.waypoints.size())
				return noWaypoint.display(inv, n);

			Waypoint waypoint = inv.waypoints.get(index);
			return inv.getPlayerData().hasWaypoint(waypoint) ? availWaypoint.display(inv, n) : locked.display(inv);
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

				if (playerData.getActivityTimeOut(PlayerActivity.USE_WAYPOINT) > 0)
					return;

				player.closeInventory();
				playerData.warp(waypoint);
			}
		}
	}
}
