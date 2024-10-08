package net.Indyuce.mmocore.gui;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.waypoint.Waypoint;
import net.Indyuce.mmocore.waypoint.WaypointPath;
import net.Indyuce.mmocore.waypoint.WaypointPathCalculation;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
        private final WaypointItemHandler availWaypoint, noStellium, notLinked, currentWayPoint;

        public WaypointItem(ConfigurationSection config) {
            super(Material.BARRIER, config);

            Validate.notNull(config.getConfigurationSection("no-waypoint"), "Could not load 'no-waypoint' config");
            Validate.notNull(config.getConfigurationSection("locked"), "Could not load 'locked' config");
            Validate.notNull(config.getConfigurationSection("not-a-destination"), "Could not load 'not-a-destination' config");
            //Validate.notNull(config.getConfigurationSection("not-dynamic"), "Could not load 'not-dynamic' config");
            Validate.notNull(config.getConfigurationSection("current-waypoint"), "Could not load 'current-waypoint' config");
            Validate.notNull(config.getConfigurationSection("not-enough-stellium"), "Could not load 'not-enough-stellium' config");
            Validate.notNull(config.getConfigurationSection("display"), "Could not load 'display' config");

            noWaypoint = new SimplePlaceholderItem(config.getConfigurationSection("no-waypoint"));
            locked = new SimplePlaceholderItem(config.getConfigurationSection("locked"));
            notLinked = new WaypointItemHandler(config.getConfigurationSection("not-a-destination"), true);
            //notDynamic = new WaypointItemHandler(config.getConfigurationSection("not-dynamic"), true);
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

            final Waypoint waypoint = inv.waypoints.get(index);

            // Current waypoint
            if (inv.current != null && inv.current.equals(waypoint))
                return currentWayPoint.display(inv, n);

            // Locked waypoint
            if (!inv.getPlayerData().hasWaypoint(waypoint))
                return locked.display(inv, n);

            // Waypoints are not linked
            if (!inv.paths.containsKey(waypoint))
                return notLinked.display(inv, n);

            // Normal cost
            if (inv.paths.get(waypoint).getCost() > inv.getPlayerData().getStellium())
                return noStellium.display(inv, n);

            return availWaypoint.display(inv, n);
        }
    }

    public class WaypointItemHandler extends InventoryItem<WaypointViewerInventory> {
        private final boolean onlyName;
        private final String splitter, none;

        public WaypointItemHandler(ConfigurationSection config, boolean onlyName) {
            super(config);

            this.onlyName = onlyName;
            this.splitter = config.getString("format_path.splitter", ", ");
            this.none = config.getString("format_path.none", "None");
        }

        @Override
        public ItemStack display(WaypointViewerInventory inv, int n) {
            // TODO refactor code
            final Placeholders placeholders = getPlaceholders(inv, n);
            final OfflinePlayer effectivePlayer = getEffectivePlayer(inv, n);

            final ItemStack item = new ItemStack(getMaterial());
            final ItemMeta meta = item.getItemMeta();
            meta.setCustomModelData(getModelData());
            // if (texture != null && meta instanceof SkullMeta)
            //    UtilityMethods.setTextureValue((SkullMeta) meta, texture);

            if (hasName()) meta.setDisplayName(placeholders.apply(effectivePlayer, getName()));

            if (hideFlags()) MMOCoreUtils.addAllItemFlags(meta);
            if (hideTooltip()) meta.setHideTooltip(true);
            // If a player can teleport to another waypoint given his location
            Waypoint waypoint = inv.waypoints.get(inv.page * inv.getEditable().getByFunction("waypoint").getSlots().size() + n);

            if (hasLore()) {
                List<String> lore = new ArrayList<>();
                getLore().forEach(line -> {
                    if (line.equals("{lore}")) for (String added : waypoint.getLore())
                        lore.add(ChatColor.GRAY + placeholders.apply(effectivePlayer, added));
                    else lore.add(ChatColor.GRAY + placeholders.apply(effectivePlayer, line));
                });
                meta.setLore(lore);
            }

            item.setItemMeta(meta);

            // Extra code
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(MMOCore.plugin, "waypointId"), PersistentDataType.STRING, waypoint.getId());
            item.setItemMeta(meta);
            return item;
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
                holders.register("intermediary_waypoints", inv.paths.containsKey(waypoint) ? inv.paths.get(waypoint).displayIntermediaryWayPoints(splitter, none) : none);
            }

            return holders;
        }
    }

    public class WaypointViewerInventory extends GeneratedInventory {
        private final List<Waypoint> waypoints = new ArrayList<>(MMOCore.plugin.waypointManager.getAll());
        @Nullable
        private final Waypoint current;

        private Map<Waypoint, WaypointPath> paths;

        private int page;

        public WaypointViewerInventory(PlayerData playerData, EditableInventory editable, Waypoint current) {
            super(playerData, editable);

            this.current = current;
            paths = new WaypointPathCalculation(playerData).run(current).getPaths();
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

                if (tag.isEmpty()) return;

                // Locked waypoint?
                final Waypoint waypoint = MMOCore.plugin.waypointManager.get(tag);
                if (!playerData.hasWaypoint(waypoint)) {
                    ConfigMessage.fromKey("not-unlocked-waypoint").send(player);
                    return;
                }

                // Cannot teleport to current waypoint
                if (waypoint.equals(current)) {
                    ConfigMessage.fromKey("standing-on-waypoint").send(player);
                    return;
                }

                // No access to that waypoint
                if (paths.get(waypoint) == null) {
                    ConfigMessage.fromKey("cannot-teleport-to").send(player);
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
