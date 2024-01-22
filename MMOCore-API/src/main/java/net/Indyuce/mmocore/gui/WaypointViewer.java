package net.Indyuce.mmocore.gui;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.waypoint.Waypoint;
import net.Indyuce.mmocore.waypoint.WaypointPath;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        private final WaypointItemHandler availWaypoint, noStellium, notLinked, notDynamic, currentWayPoint;

        public WaypointItem(ConfigurationSection config) {
            super(Material.BARRIER, config);

            Validate.notNull(config.getConfigurationSection("no-waypoint"), "Could not load 'no-waypoint' config");
            Validate.notNull(config.getConfigurationSection("locked"), "Could not load 'locked' config");
            Validate.notNull(config.getConfigurationSection("not-a-destination"), "Could not load 'not-a-destination' config");
            Validate.notNull(config.getConfigurationSection("not-dynamic"), "Could not load 'not-dynamic' config");
            Validate.notNull(config.getConfigurationSection("current-waypoint"), "Could not load 'current-waypoint' config");
            Validate.notNull(config.getConfigurationSection("not-enough-stellium"), "Could not load 'not-enough-stellium' config");
            Validate.notNull(config.getConfigurationSection("display"), "Could not load 'display' config");

            noWaypoint = new SimplePlaceholderItem(config.getConfigurationSection("no-waypoint"));
            locked = new SimplePlaceholderItem(config.getConfigurationSection("locked"));
            notLinked = new WaypointItemHandler(config.getConfigurationSection("not-a-destination"), true);
            notDynamic = new WaypointItemHandler(config.getConfigurationSection("not-dynamic"), true);
            currentWayPoint = new WaypointItemHandler(config.getConfigurationSection("current-waypoint"), true);
            noStellium = new WaypointItemHandler(config.getConfigurationSection("not-enough-stellium"), false);
            availWaypoint = new WaypointItemHandler(config.getConfigurationSection("display"), false);
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
            if (inv.current != null && inv.current.equals(waypoint))
                return currentWayPoint.display(inv, n);

            if (!inv.getPlayerData().hasWaypoint(waypoint))
                return locked.display(inv, n);

            // Waypoints are not linked
            if (inv.current != null && !inv.paths.containsKey(waypoint))
                return notLinked.display(inv, n);

            // Not dynamic waypoint
            if (inv.current == null && !inv.paths.containsKey(waypoint))
                return notDynamic.display(inv, n);

            //Normal cost
            if (inv.paths.get(waypoint).getCost() > inv.getPlayerData().getStellium())
                return noStellium.display(inv, n);

            return availWaypoint.display(inv, n);
        }
    }

    public class WaypointItemHandler extends InventoryItem<WaypointViewerInventory> {
        private final boolean onlyName;

        public WaypointItemHandler(ConfigurationSection config, boolean onlyName) {
            super(config);
            this.onlyName = onlyName;
        }

        @Override
        public ItemStack display(WaypointViewerInventory inv, int n) {
            ItemStack disp = super.display(inv, n);

            // If a player can teleport to another waypoint given his location
            Waypoint waypoint = inv.waypoints.get(inv.page * inv.getEditable().getByFunction("waypoint").getSlots().size() + n);
            ItemMeta meta = disp.getItemMeta();
            List<String> lore = new ArrayList<>();
            meta.getLore().forEach(string -> {
                if (string.contains("{lore}"))
                    lore.addAll(waypoint.getLore());
                else
                    lore.add(string);
            });
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(MMOCore.plugin, "waypointId"), PersistentDataType.STRING, waypoint.getId());
            disp.setItemMeta(meta);
            return disp;
        }

        @Override
        public Placeholders getPlaceholders(WaypointViewerInventory inv, int n) {
            Placeholders holders = new Placeholders();

            Waypoint waypoint = inv.waypoints.get(inv.page * inv.getByFunction("waypoint").getSlots().size() + n);
            holders.register("name", waypoint.getName());

            if (!onlyName) {
                holders.register("current_cost", inv.paths.get(waypoint).getCost());
                holders.register("normal_cost", decimal.format(inv.paths.containsKey(waypoint) ? inv.paths.get(waypoint).getCost() : Double.POSITIVE_INFINITY));
                holders.register("dynamic_cost", decimal.format(waypoint.getDynamicCost()));
                holders.register("intermediary_waypoints", inv.paths.containsKey(waypoint) ? inv.paths.get(waypoint).displayIntermediaryWayPoints(inv.isDynamicUse()) : "None");
            }

            return holders;
        }
    }

    public class WaypointViewerInventory extends GeneratedInventory {
        private final List<Waypoint> waypoints = new ArrayList<>(MMOCore.plugin.waypointManager.getAll());
        @Nullable
        private final Waypoint current;
        private final Map<Waypoint, WaypointPath> paths = new HashMap<>();

        private int page;

        public WaypointViewerInventory(PlayerData playerData, EditableInventory editable, Waypoint current) {
            super(playerData, editable);

            this.current = current;
            if (current != null)
                for (WaypointPath pathInfo : current.getAllPath())
                    paths.put(pathInfo.getFinalWaypoint(), pathInfo);

            if (current == null) {

                //Iterate through all the dynamic points and find all the points it is linked to and the path
                HashMap<Waypoint, Double> dynamicPoints = new HashMap<>();
                //We first check all the dynamic waypoints
                for (Waypoint waypoint : waypoints) {
                    if (waypoint.mayBeUsedDynamically(playerData.getPlayer())) {
                        paths.put(waypoint, new WaypointPath(waypoint, waypoint.getDynamicCost()));
                        dynamicPoints.put(waypoint, waypoint.getDynamicCost());
                    }
                }
                for (Waypoint source : dynamicPoints.keySet()) {
                    for (WaypointPath target : source.getAllPath()) {
                        if (!paths.containsKey(target.getFinalWaypoint()) || paths.get(target.getFinalWaypoint()).getCost() > target.getCost() + dynamicPoints.get(source)) {
                            paths.put(target.getFinalWaypoint(), target.addCost(dynamicPoints.get(source)));
                        }
                    }
                }
            }
        }

        @Override
        public String calculateName() {
            return getName();
        }

        public boolean isDynamicUse() {
            return current == null;
        }

        @Override
        public void whenClicked(InventoryClickContext context, InventoryItem item) {
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
                PersistentDataContainer container = context.getClickedItem().getItemMeta().getPersistentDataContainer();
                String tag = container.has(new NamespacedKey(MMOCore.plugin, "waypointId"), PersistentDataType.STRING) ?
                        container.get(new NamespacedKey(MMOCore.plugin, "waypointId"), PersistentDataType.STRING) : "";

                if (tag.equals(""))
                    return;

                // Locked waypoint?
                Waypoint waypoint = MMOCore.plugin.waypointManager.get(tag);
                if (!playerData.hasWaypoint(waypoint)) {
                    ConfigMessage.fromKey("not-unlocked-waypoint").send(player);
                    return;
                }

                // Cannot teleport to current waypoint
                if (waypoint.equals(current)) {
                    ConfigMessage.fromKey("standing-on-waypoint").send(player);
                    return;
                }

                // Waypoint does not have target as destination
                if (current != null && current.getPath(waypoint) == null) {
                    ConfigMessage.fromKey("cannot-teleport-to").send(player);
                    return;
                }

                // Not dynamic waypoint
                if (current == null && !paths.containsKey(waypoint)) {
                    ConfigMessage.fromKey("not-dynamic-waypoint").send(player);
                    return;
                }

                // Stellium cost
                double withdraw = paths.get(waypoint).getCost();
                double left = withdraw - playerData.getStellium();
                if (left > 0) {
                    ConfigMessage.fromKey("not-enough-stellium", "more", decimal.format(left)).send(player);
                    return;
                }

                if (playerData.getActivityTimeOut(PlayerActivity.USE_WAYPOINT) > 0)
                    return;

                player.closeInventory();
                playerData.warp(waypoint, withdraw);

            }
        }
    }
}
