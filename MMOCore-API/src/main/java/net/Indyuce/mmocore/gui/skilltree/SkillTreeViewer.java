package net.Indyuce.mmocore.gui.skilltree;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.gui.skilltree.display.*;
import net.Indyuce.mmocore.skilltree.*;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SkillTreeViewer extends EditableInventory {
    protected final Map<DisplayInfo, Icon> icons = new HashMap<>();
    protected final Map<SkillTreeStatus, String> statusNames = new HashMap<>();

    @Nullable
    /**
     * A null skillTree means the global skill tree view is opened.
     * Else this GUI represents a specific skill tree.
     */
    private final SkillTree defaultSkillTree;

    public SkillTreeViewer() {
        super("skill-tree");
        this.defaultSkillTree = null;
    }

    public SkillTreeViewer(SkillTree initialSkillTree, boolean isDefault) {
        super("specific-skill-tree-" + (isDefault ? "default" : UtilityMethods.ymlName(initialSkillTree.getId())));
        this.defaultSkillTree = initialSkillTree;
    }

    @Override
    public void reload(FileConfiguration config) {
        super.reload(config);
        if (config.contains("status-names"))
            for (SkillTreeStatus skillTreeStatus : SkillTreeStatus.values())
                statusNames.put(skillTreeStatus, config.getString("status-names." + UtilityMethods.ymlName(skillTreeStatus.name()), skillTreeStatus.name()));

        //Loads all the pathDisplayInfo
        for (PathStatus status : PathStatus.values())
            for (PathType pathType : PathType.values()) {
                if (!config.contains("display.paths." + MMOCoreUtils.ymlName(status.name()) + "." + MMOCoreUtils.ymlName(pathType.name()))) {
                    MMOCore.log(Level.WARNING, "An error occurred while loading skill tree GUI: Missing path type: " + MMOCoreUtils.ymlName(pathType.name()) + " for status: " + MMOCoreUtils.ymlName(status.name()));
                    continue;
                }
                icons.put(new PathDisplayInfo(pathType, status), new Icon(config.getConfigurationSection("display.paths." + MMOCoreUtils.ymlName(status.name()) + "." + MMOCoreUtils.ymlName(pathType.name()))));
            }
        //Loads all the nodeDisplayInfo
        for (SkillTreeStatus status : SkillTreeStatus.values())
            for (NodeType nodeType : NodeType.values()) {
                if (!config.contains("display.nodes." + MMOCoreUtils.ymlName(status.name()) + "." + MMOCoreUtils.ymlName(nodeType.name()))) {
                    MMOCore.log(Level.WARNING, "An error occurred while loading skill tree GUI: Missing node type: " + MMOCoreUtils.ymlName(nodeType.name()) + " for status: " + MMOCoreUtils.ymlName(status.name()));
                    continue;
                }
                icons.put(new NodeDisplayInfo(nodeType, status), new Icon(config.getConfigurationSection("display.nodes." + MMOCoreUtils.ymlName(status.name()) + "." + MMOCoreUtils.ymlName(nodeType.name()))));
            }
    }

    @Override
    public InventoryItem load(String function, ConfigurationSection config) {
        if (function.equals("skill-tree")) {
            return new SkillTreeItem(config);
        }
        if (function.equals("up"))
            return new SimplePlaceholderItem(config);
        if (function.equals("left"))
            return new SimplePlaceholderItem(config);
        if (function.equals("down"))
            return new SimplePlaceholderItem(config);
        if (function.equals("right"))
            return new SimplePlaceholderItem(config);

        if (function.equals("reallocation"))
            return new InventoryItem<SkillTreeInventory>(config) {

                @Override
                public Placeholders getPlaceholders(SkillTreeInventory inv, int n) {
                    Placeholders holders = new Placeholders();
                    holders.register("skill-tree-points", inv.getPlayerData().getSkillTreePoint(inv.getSkillTree().getId()));
                    holders.register("global-points", inv.getPlayerData().getSkillTreePoint("global"));
                    holders.register("realloc-points", inv.getPlayerData().getSkillTreeReallocationPoints());
                    int maxPointSpent = inv.getSkillTree().getMaxPointSpent();
                    holders.register("max-point-spent", maxPointSpent == Integer.MAX_VALUE ? "∞" : maxPointSpent);
                    holders.register("point-spent", inv.getPlayerData().getPointSpent(inv.getSkillTree()));

                    return holders;
                }
            };

        if (function.equals("skill-tree-node"))
            return new SkillTreeNodeItem(config);
        if (function.equals("next-tree-list-page")) {
            return new NextTreeListPageItem(config);
        }
        if (function.equals("previous-tree-list-page")) {
            return new PreviousTreeListPageItem(config);
        }
        return null;
    }


    public SkillTreeInventory newInventory(PlayerData playerData) {
        return new SkillTreeInventory(playerData, this, defaultSkillTree);
    }


    public class SkillTreeItem extends InventoryItem<SkillTreeInventory> {

        public SkillTreeItem(ConfigurationSection config) {
            //We must use this constructor to show that there are not specified material
            super(Material.BARRIER, config);

        }


        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack display(SkillTreeInventory inv, int n) {
            int index = inv.getEditable().getByFunction("skill-tree").getSlots().size() * inv.treeListPage + n;
            if (inv.skillTrees.size() <= index) {
                return new ItemStack(Material.AIR);
            }
            SkillTree skillTree = inv.skillTrees.get(index);
            //We display with the material corresponding to the skillTree
            ItemStack item = super.display(inv, n, skillTree.getItem());

            ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.setDisplayName(skillTree.getName());
            Placeholders holders = getPlaceholders(inv, n);
            List<String> lore = new ArrayList<>();
            getLore().forEach(string -> {
                if (string.contains("{tree-lore}")) {
                    lore.addAll(skillTree.getLore());
                } else
                    lore.add(holders.apply(inv.getPlayer(), string));
            });
            meta.setLore(lore);
            if (MythicLib.plugin.getVersion().isStrictlyHigher(1, 13))
                meta.setCustomModelData(skillTree.getCustomModelData());
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(MMOCore.plugin, "skill-tree-id"), PersistentDataType.STRING, skillTree.getId());
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public Placeholders getPlaceholders(SkillTreeInventory inv, int n) {
            int index = inv.getEditable().getByFunction("skill-tree").getSlots().size() * inv.treeListPage + n;
            SkillTree skillTree = inv.skillTrees.get(index);
            Placeholders holders = new Placeholders();
            holders.register("name", skillTree.getName());
            holders.register("id", skillTree.getId());
            int maxPointSpent = inv.getSkillTree().getMaxPointSpent();
            holders.register("max-point-spent", maxPointSpent == Integer.MAX_VALUE ? "∞" : maxPointSpent);
            holders.register("point-spent", inv.getPlayerData().getPointSpent(inv.getSkillTree()));
            holders.register("skill-tree-points", inv.getPlayerData().getSkillTreePoint(inv.getSkillTree().getId()));
            holders.register("global-points", inv.getPlayerData().getSkillTreePoint("global"));
            return holders;
        }
    }

    public class NextTreeListPageItem extends SimplePlaceholderItem<SkillTreeInventory> {

        public NextTreeListPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean canDisplay(SkillTreeInventory inv) {
            return inv.getTreeListPage() < inv.getMaxTreeListPage();
        }
    }

    public class PreviousTreeListPageItem extends SimplePlaceholderItem<SkillTreeInventory> {

        public PreviousTreeListPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean canDisplay(SkillTreeInventory inv) {
            return inv.getTreeListPage() > 0;
        }
    }


    public class SkillTreeNodeItem extends InventoryItem<SkillTreeInventory> {

        private final List<String> pathLore = new ArrayList<>();


        public SkillTreeNodeItem(ConfigurationSection config) {
            super(Material.AIR, config);
            if (config.isList("path-lore"))
                pathLore.addAll(config.getStringList("path-lore"));
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        /**
         * Display the node/path with the lore and name filled in the yml of the skill tree node with the right material
         * and model-data.
         * You don't need to give any name or lore in the gui/skilltree.yml all the information are filled in
         * the yml of the skill tree.
         */
        @Override
        public ItemStack display(SkillTreeInventory inv, int n) {
            IntegerCoordinates coordinates = inv.getCoordinates(n);
            if (inv.getSkillTree().isPathOrNode(coordinates)) {
                Icon icon = inv.getIcon(coordinates);
                ItemStack item = super.display(inv, n, icon.getMaterial(), icon.getCustomModelData());
                ItemMeta meta = item.getItemMeta();
                Placeholders holders = getPlaceholders(inv, n);
                if (inv.getSkillTree().isNode(coordinates)) {
                    SkillTreeNode node = inv.getSkillTree().getNode(coordinates);
                    List<String> lore = new ArrayList<>();
                    getLore().forEach(str -> {
                        if (str.contains("{node-lore}")) {
                            node.getLore(inv.getPlayerData()).forEach(s -> lore.add(holders.apply(inv.getPlayer(), s)));
                        } else if (str.contains("{strong-parents}")) {
                            lore.addAll(getParentsLore(inv, node, node.getParents(ParentType.STRONG)));
                        } else if (str.contains("{soft-parents}")) {
                            lore.addAll(getParentsLore(inv, node, node.getParents(ParentType.SOFT)));
                        } else if (str.contains("{incompatible-parents}")) {
                            lore.addAll(getParentsLore(inv, node, node.getParents(ParentType.INCOMPATIBLE)));
                        } else
                            lore.add(holders.apply(inv.getPlayer(), str));
                    });
                    meta.setLore(lore);
                    final String name = meta.getDisplayName();
                    meta.setDisplayName(name == null || name.isEmpty() ? node.getName() : name);
                }
                //If it is path we remove the display name and the lore.
                else {
                    meta.setLore(pathLore.stream().map(str -> holders.apply(inv.getPlayer(), str)).collect(Collectors.toList()));
                    meta.setDisplayName(" ");
                }
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                PersistentDataContainer container = meta.getPersistentDataContainer();
                container.set(new NamespacedKey(MMOCore.plugin, "coordinates.x"), PersistentDataType.INTEGER, coordinates.getX());
                container.set(new NamespacedKey(MMOCore.plugin, "coordinates.y"), PersistentDataType.INTEGER, coordinates.getY());
                item.setItemMeta(meta);
                return item;
            }
            return new ItemStack(Material.AIR);
        }


        /**
         * Soft&Strong children lore for the node
         */
        public List<String> getParentsLore(SkillTreeInventory inv, SkillTreeNode node, Collection<SkillTreeNode> parents) {
            List<String> lore = new ArrayList<>();
            for (SkillTreeNode parent : parents) {
                int level = inv.getPlayerData().getNodeLevel(parent);
                ChatColor color = level >= node.getParentNeededLevel(parent) ? ChatColor.GREEN : ChatColor.RED;
                lore.add(ChatColor.GRAY + "◆" + parent.getName() + ": " + color + node.getParentNeededLevel(parent));
            }
            return lore;
        }


        @Override
        public Placeholders getPlaceholders(SkillTreeInventory inv, int n) {
            Placeholders holders = new Placeholders();
            holders.register("skill-tree", inv.getSkillTree().getName());
            boolean isNode = inv.getSkillTree().isNode(inv.getCoordinates(n));
            if (isNode) {
                SkillTreeNode node = inv.getNode(n);
                holders.register("current-level", inv.getPlayerData().getNodeLevel(node));
                SkillTreeStatus status = inv.getPlayerData().getNodeStatus(node);
                holders.register("current-state", statusNames.getOrDefault(status, status.name()));
                holders.register("max-level", node.getMaxLevel());
                holders.register("name", node.getName());
                holders.register("max-children", node.getMaxChildren());
                holders.register("point-consumed", node.getSkillTreePointsConsumed());
                holders.register("display-type", node.getNodeType());
            } else {
                holders.register("display-type", inv.skillTree.getPath(inv.getCoordinates(n)).getPathType());
            }
            int maxPointSpent = inv.getSkillTree().getMaxPointSpent();
            holders.register("max-point-spent", maxPointSpent == Integer.MAX_VALUE ? "∞" : maxPointSpent);
            holders.register("point-spent", inv.getPlayerData().getPointSpent(inv.getSkillTree()));
            holders.register("skill-tree-points", inv.getPlayerData().getSkillTreePoint(inv.getSkillTree().getId()));
            holders.register("global-points", inv.getPlayerData().getSkillTreePoint("global"));

            return holders;
        }
    }


    public class SkillTreeInventory extends GeneratedInventory {
        private int x, y;
        //width and height correspond to the the size of the 'board' representing the skill tree
        private int minSlot, maxSlot;
        private final int width, height;
        private int treeListPage;
        private final int maxTreeListPage;
        private final List<SkillTree> skillTrees;
        @NotNull
        private SkillTree skillTree;
        private final List<Integer> slots;

        public SkillTreeInventory(PlayerData playerData, EditableInventory editable, SkillTree skillTree) {
            super(playerData, editable);

            skillTrees = playerData.getProfess().getSkillTrees();
            this.skillTree = skillTree == null ? skillTrees.get(0) : skillTree;
            if (skillTree == null)
                maxTreeListPage = (skillTrees.size() - 1) / editable.getByFunction("skill-tree").getSlots().size();
            else
                maxTreeListPage = 0;
            //We get the width and height of the GUI(corresponding to the slots given)
            slots = editable.getByFunction("skill-tree-node").getSlots();
            minSlot = 64;
            maxSlot = 0;
            for (int slot : slots) {
                if (slot < minSlot)
                    minSlot = slot;
                if (slot > maxSlot)
                    maxSlot = slot;
            }
            width = (maxSlot - minSlot) % 9;
            height = (maxSlot - minSlot) / 9;
            x -= width / 2;
            y -= height / 2;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getTreeListPage() {
            return treeListPage;
        }

        public int getMaxTreeListPage() {
            return maxTreeListPage;
        }


        public Icon getIcon(IntegerCoordinates coordinates) {
            if (skillTree.isNode(coordinates)) {
                SkillTreeNode node = skillTree.getNode(coordinates);
                NodeType nodeType = node.getNodeType();
                SkillTreeStatus skillTreeStatus = playerData.getNodeStatus(node);
                //If the node has its own display, it will be shown.
                if (node.hasIcon(skillTreeStatus))
                    return node.getIcon(skillTreeStatus);
                DisplayInfo displayInfo = new NodeDisplayInfo(nodeType, skillTreeStatus);
                //Takes the display defined in the skill tree config if it exists.
                if (skillTree.hasIcon(displayInfo))
                    return skillTree.getIcon(displayInfo);

                Icon icon = icons.get(displayInfo);
                Validate.notNull(icon, "The node " + node.getFullId() + " has no icon for the type " + nodeType + " and the status " + skillTreeStatus);
                return icon;
            } else {
                SkillTreePath path = skillTree.getPath(coordinates);
                PathType pathType = path.getPathType();
                PathStatus pathStatus = path.getStatus(playerData);
                DisplayInfo displayInfo = new PathDisplayInfo(pathType, pathStatus);
                //Takes the display defined in the skill tree config if it exists.
                if (skillTree.hasIcon(displayInfo))
                    return skillTree.getIcon(displayInfo);
                Icon icon = icons.get(displayInfo);
                Validate.notNull(icon, "There is no icon for the path type " + pathType + " and the status " + pathStatus);
                return icon;
            }
        }

        @Override
        public String calculateName() {
            return getEditable().getName().replace("{skill-tree-name}", skillTree.getName()).replace("{skill-tree-id}", skillTree.getId());
        }

        public IntegerCoordinates getCoordinates(int n) {
            int slot = slots.get(n);
            int deltaX = (slot - getMinSlot()) % 9;
            int deltaY = (slot - getMinSlot()) / 9;
            IntegerCoordinates coordinates = new IntegerCoordinates(getX() + deltaX, getY() + deltaY);
            return coordinates;
        }

        public SkillTreeNode getNode(int n) {
            return getSkillTree().getNode(getCoordinates(n));
        }


        public SkillTree getSkillTree() {
            return skillTree;
        }

        public int getMinSlot() {
            return minSlot;
        }

        @Override
        public void whenClicked(InventoryClickContext event, InventoryItem item) {


            if (item.getFunction().equals("next-tree-list-page")) {
                treeListPage++;
                open();
            }
            if (item.getFunction().equals("up")) {
                y -= MMOCore.plugin.configManager.skillTreeScrollStepY;
                open();

            }
            if (item.getFunction().equals("right")) {
                x += MMOCore.plugin.configManager.skillTreeScrollStepX;
                open();
            }
            if (item.getFunction().equals("down")) {
                y += MMOCore.plugin.configManager.skillTreeScrollStepY;
                open();
            }
            if (item.getFunction().equals("left")) {
                x -= MMOCore.plugin.configManager.skillTreeScrollStepX;
                open();
            }


            if (item.getFunction().equals("previous-tree-list-page")) {
                treeListPage--;
                open();
            }
            if (item.getFunction().equals("reallocation")) {
                int spent = playerData.getPointSpent(skillTree);
                if (spent < 1) {
                    ConfigMessage.fromKey("no-skill-tree-points-spent").send(player);
                    MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
                    return;
                }

                if (getPlayerData().getSkillTreeReallocationPoints() <= 0) {
                    ConfigMessage.fromKey("not-skill-tree-reallocation-point").send(player);
                    MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
                    return;
                } else {
                    int reallocated = playerData.getPointSpent(skillTree);
                    //We remove all the nodeStates progress
                    playerData.giveSkillTreePoints(skillTree.getId(), reallocated);
                    playerData.giveSkillTreeReallocationPoints(-1);
                    playerData.resetSkillTree(skillTree);
                    skillTree.setupNodeStates(playerData);
                    ConfigMessage.fromKey("reallocated-points", "points", "" + playerData.getSkillTreePoint(skillTree.getId()), "skill-tree", skillTree.getName()).send(player);
                    MMOCore.plugin.soundManager.getSound(SoundEvent.RESET_SKILL_TREE).playTo(player);
                    open();
                    return;

                }
            }

            if (item.getFunction().equals("skill-tree")) {
                String id = event.getClickedItem().getItemMeta().getPersistentDataContainer().get(
                        new NamespacedKey(MMOCore.plugin, "skill-tree-id"), PersistentDataType.STRING);
                MMOCore.plugin.soundManager.getSound(SoundEvent.CHANGE_SKILL_TREE).playTo(player);
                skillTree = MMOCore.plugin.skillTreeManager.get(id);
                open();
                return;
            }

            if (item.getFunction().equals("skill-tree-node")) {
                if (event.getClickType() == ClickType.LEFT) {
                    PersistentDataContainer container = event.getClickedItem().getItemMeta().getPersistentDataContainer();
                    int x = container.get(new NamespacedKey(MMOCore.plugin, "coordinates.x"), PersistentDataType.INTEGER);
                    int y = container.get(new NamespacedKey(MMOCore.plugin, "coordinates.y"), PersistentDataType.INTEGER);
                    if (!skillTree.isNode(new IntegerCoordinates(x, y))) {
                        return;
                    }
                    SkillTreeNode node = skillTree.getNode(new IntegerCoordinates(x, y));
                    if (playerData.getPointSpent(skillTree) >= skillTree.getMaxPointSpent()) {
                        ConfigMessage.fromKey("max-points-reached").send(player);
                        MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
                        return;
                    }

                    if (playerData.canIncrementNodeLevel(node)) {
                        playerData.incrementNodeLevel(node);
                        ConfigMessage.fromKey("upgrade-skill-node", "skill-node", node.getName(), "level", "" + playerData.getNodeLevel(node)).send(player);
                        MMOCore.plugin.soundManager.getSound(SoundEvent.LEVEL_UP).playTo(getPlayer());
                        open();
                    } else if (playerData.getNodeStatus(node) == SkillTreeStatus.LOCKED || playerData.getNodeStatus(node) == SkillTreeStatus.FULLY_LOCKED) {
                        ConfigMessage.fromKey("locked-node").send(player);
                        MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());

                    } else if (playerData.getNodeLevel(node) >= node.getMaxLevel()) {
                        ConfigMessage.fromKey("skill-node-max-level-hit").send(player);
                        MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
                    } else if (!node.hasPermissionRequirement(playerData)) {
                        ConfigMessage.fromKey("missing-skill-node-permission").send(player);
                        MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
                    }

                    //Else the player doesn't doesn't have the skill tree points
                    else {
                        ConfigMessage.fromKey("not-enough-skill-tree-points", "point", "" + node.getSkillTreePointsConsumed()).send(player);
                        MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
                    }

                }
            }

        }
    }
}
