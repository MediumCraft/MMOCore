package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.waypoint.CostType;
import net.Indyuce.mmocore.waypoint.Waypoint;
import net.Indyuce.mmocore.waypoint.WaypointOption;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public class WaypointItem extends SimplePlaceholderItem<WaypointViewerInventory> {
        private final SimplePlaceholderItem noWaypoint, locked;
        private final WaypointItemHandler availWaypoint, notLinked, notDynamic, noStellium;

        public WaypointItem(ConfigurationSection config) {
            super(Material.BARRIER, config);

            noWaypoint = new SimplePlaceholderItem(Objects.requireNonNull(config.getConfigurationSection("no-waypoint"), "Could not load 'no-waypoint' config"));
            locked = new SimplePlaceholderItem(Objects.requireNonNull(config.getConfigurationSection("locked"), "Could not load 'locked' config"));

            notLinked = new WaypointItemHandler(Objects.requireNonNull(config.getConfigurationSection("not-a-destination"), "Could not load 'not-a-destination' config"));
            notDynamic = new WaypointItemHandler(Objects.requireNonNull(config.getConfigurationSection("not-dynamic"), "Could not load 'not-dynamic' config"));
            noStellium = new WaypointItemHandler(Objects.requireNonNull(config.getConfigurationSection("not-enough-stellium"), "Could not load 'not-enough-stellium' config"));
            availWaypoint = new WaypointItemHandler(Objects.requireNonNull(config.getConfigurationSection("display"), "Could not load 'display' config"));
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

            // Locked waypoint?
            Waypoint waypoint = inv.waypoints.get(index);
            if (!inv.getPlayerData().hasWaypoint(waypoint))
                return locked.display(inv, n);

            // Waypoints are not linked
            if (inv.current != null && !inv.current.hasDestination(waypoint))
                return notLinked.display(inv, n);

            // Not dynamic waypoint
            if (inv.current == null && !waypoint.hasOption(WaypointOption.DYNAMIC))
                return notDynamic.display(inv, n);

            // Stellium cost
            if (waypoint.getCost(inv.current == null ? CostType.DYNAMIC_USE : CostType.NORMAL_USE) > inv.getPlayerData().getStellium())
                return noStellium.display(inv, n);

            return availWaypoint.display(inv, n);
        }
    }

    public class WaypointItemHandler extends InventoryItem<WaypointViewerInventory> {
        public WaypointItemHandler(ConfigurationSection config) {
            super(config);
        }

        @Override
        public ItemStack display(WaypointViewerInventory inv, int n) {
            ItemStack disp = super.display(inv, n);

            // If a player can teleport to another waypoint given his location
            Waypoint waypoint = inv.waypoints.get(inv.page * inv.getEditable().getByFunction("waypoint").getSlots().size() + n);
            return NBTItem.get(disp).addTag(new ItemTag("waypointId", waypoint.getId())).toItem();
        }

        @Override
        public Placeholders getPlaceholders(WaypointViewerInventory inv, int n) {
            Placeholders holders = new Placeholders();

            Waypoint waypoint = inv.waypoints.get(inv.page * inv.getByFunction("waypoint").getSlots().size() + n);
            holders.register("name", waypoint.getName());
            holders.register("current_cost", decimal.format(waypoint.getCost(inv.waypointCostType)));
            holders.register("normal_cost", decimal.format(waypoint.getCost(CostType.NORMAL_USE)));
            holders.register("dynamic_cost", decimal.format(waypoint.getCost(CostType.DYNAMIC_USE)));

            return holders;
        }
    }

    public class WaypointViewerInventory extends GeneratedInventory {
        private final List<Waypoint> waypoints = new ArrayList<>(MMOCore.plugin.waypointManager.getAll());
        private final Waypoint current;
        private final CostType waypointCostType;

        private int page;

        public WaypointViewerInventory(PlayerData playerData, EditableInventory editable, Waypoint current) {
            super(playerData, editable);

            this.current = current;
            this.waypointCostType = current == null ? CostType.DYNAMIC_USE : CostType.NORMAL_USE;
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

                // Locked waypoint?
                Waypoint waypoint = MMOCore.plugin.waypointManager.get(tag);
                if (!playerData.hasWaypoint(waypoint)) {
                    MMOCore.plugin.configManager.getSimpleMessage("not-unlocked-waypoint").send(player);
                    return;
                }

                // Cannot teleport to current waypoint
                if (waypoint.equals(current)) {
                    MMOCore.plugin.configManager.getSimpleMessage("standing-on-waypoint").send(player);
                    return;
                }

                // Waypoint does not have target as destination
                if (current != null && !current.hasDestination(waypoint)) {
                    MMOCore.plugin.configManager.getSimpleMessage("cannot-teleport-to").send(player);
                    return;
                }

                // Not dynamic waypoint
                if (current == null && !waypoint.hasOption(WaypointOption.DYNAMIC)) {
                    MMOCore.plugin.configManager.getSimpleMessage("not-dynamic-waypoint").send(player);
                    return;
                }

                // Stellium cost
                CostType costType = current == null ? CostType.DYNAMIC_USE : CostType.NORMAL_USE;
                double left = waypoint.getCost(costType) - playerData.getStellium();
                if (left > 0) {
                    MMOCore.plugin.configManager.getSimpleMessage("not-enough-stellium", "more", decimal.format(left)).send(player);
                    return;
                }

                if (playerData.getActivityTimeOut(PlayerActivity.USE_WAYPOINT) > 0)
                    return;

                player.closeInventory();
                playerData.warp(waypoint, costType);
            }
        }
    }
}
