package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.tree.IntegerCoordinates;
import net.Indyuce.mmocore.tree.NodeState;
import net.Indyuce.mmocore.tree.skilltree.SkillTree;
import net.Indyuce.mmocore.tree.SkillTreeNode;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class SkillTreeViewer extends EditableInventory {


    public SkillTreeViewer() {
        super("skill-tree");
    }

    @Override
    public InventoryItem load(String function, ConfigurationSection config) {
        if (function.equals("skill-tree")) {
            return new SkillTreeItem(config);
        }
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
        return new SkillTreeInventory(playerData, this);
    }


    public class SkillTreeItem extends InventoryItem<SkillTreeInventory> {

        public SkillTreeItem(ConfigurationSection config) {
            super(config);

        }

        @Override
        public ItemStack display(SkillTreeInventory inv, int n) {
            int index = 4 * inv.treeListPage + n;
            SkillTree skillTree = MMOCore.plugin.skillTreeManager.get(index);
            //We display with the material corresponding to the skillTree
            ItemStack item = super.display(inv, n, skillTree.getGuiMaterial());
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(MMOCore.plugin, "skill-tree-id"), PersistentDataType.STRING, skillTree.getId());
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public Placeholders getPlaceholders(SkillTreeInventory inv, int n) {
            int index = 4 * inv.treeListPage + n;
            SkillTree skillTree = MMOCore.plugin.skillTreeManager.get(index);
            Placeholders holders = new Placeholders();
            holders.register("name", skillTree.getName());
            holders.register("id", skillTree.getId());
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
        private final LockedSkillNodeItem lockedSkillNode;
        private final UnlockedSkillNodeItem unlockedSkillNode;
        private final PathTreeNodeItem pathTreeNode;


        public SkillTreeNodeItem(ConfigurationSection config) {
            super(config);
            Validate.isTrue(config.contains("locked-skill-node"));
            Validate.isTrue(config.contains("unlocked-skill-node"));
            Validate.isTrue(config.contains("path-tree-node"));
            lockedSkillNode = new LockedSkillNodeItem(config.getConfigurationSection("locked-skill-node"));
            unlockedSkillNode = new UnlockedSkillNodeItem(config.getConfigurationSection("unlocked-skill-node"));
            pathTreeNode = new PathTreeNodeItem(config.getConfigurationSection("path-tree-node"));
        }


        @Override
        public ItemStack display(SkillTreeInventory inv, int n) {
            int slot = getSlots().get(n);
            int deltaX = (slot - inv.getMinSlot()) % 9;
            int deltaY = (slot - inv.getMinSlot()) / 9;
            IntegerCoordinates coordinates = new IntegerCoordinates(inv.getX() + deltaX, inv.getY() + deltaY);
            ItemStack item=null;
            if (inv.getSkillTree().isNode(coordinates)) {
                SkillTreeNode node = inv.getSkillTree().getNode(coordinates);
                if (inv.getPlayerData().getNodeState(node).equals(NodeState.UNLOCKED))
                    item = unlockedSkillNode.display(inv, n, coordinates);
                else if (inv.getPlayerData().getNodeState(node).equals(NodeState.LOCKED))
                    item = lockedSkillNode.display(inv, n, coordinates);

            }    //We check if is a path only if the skillTree is an automatic Skill Tree
            else if (inv.getSkillTree().isPath(coordinates))
                item = pathTreeNode.display(inv, n);
            else
                //If it is none of the above we just display air
                return new ItemStack(Material.AIR);

            //We save the coordinates of the node
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(MMOCore.plugin, "coordinates.x"), PersistentDataType.INTEGER, coordinates.getX());
            container.set(new NamespacedKey(MMOCore.plugin, "coordinates.y"), PersistentDataType.INTEGER, coordinates.getY());
            item.setItemMeta(meta);
            return item;
        }


        @Override
        public Placeholders getPlaceholders(SkillTreeInventory inv, int n) {
            return new Placeholders();
        }

    }

    public class LockedSkillNodeItem extends InventoryItem<SkillTreeInventory> {

        public LockedSkillNodeItem(ConfigurationSection config) {
            super(config);
        }

        public ItemStack display(SkillTreeInventory inv, int n, IntegerCoordinates coordinates) {
            return super.display(inv, n);
        }

        @Override
        public Placeholders getPlaceholders(SkillTreeInventory inv, int n) {
            int slot = getSlots().get(n);
            int deltaX = (slot - inv.getMinSlot()) % 9;
            int deltaY = (slot - inv.getMinSlot()) / 9;
            IntegerCoordinates coordinates = new IntegerCoordinates(inv.getX() + deltaX, inv.getY() + deltaY);
            SkillTreeNode treeNode = inv.getSkillTree().getNode(coordinates);
            Placeholders holders = new Placeholders();
            holders.register("name", treeNode.getName());
            holders.register("node-state", inv.getPlayerData().getNodeState(treeNode));
            //Display what nodes this node unlocks
            String str = "";
            for (SkillTreeNode node : treeNode.getChildren())
                str += node.getName() + ",";
            //We remove the last comma
            str = str.substring(0, str.length() - 1);
            holders.register("unlocks", str);
            //Display all the modifiers this node gives
            str = "";
            for (PlayerModifier playerModifier : treeNode.getModifiers()) {
                //TODO
                str += "\n" + playerModifier.getKey();
            }

            holders.register("modifiers", str);

            return holders;
        }
    }

    public class UnlockedSkillNodeItem extends InventoryItem<SkillTreeInventory> {
        public UnlockedSkillNodeItem(ConfigurationSection config) {
            super(config);
        }


        public ItemStack display(SkillTreeInventory inv, int n, IntegerCoordinates coordinates) {
            return super.display(inv, n);
        }

        @Override
        public Placeholders getPlaceholders(SkillTreeInventory inv, int n) {
            int slot = getSlots().get(n);
            int deltaX = (slot - inv.getMinSlot()) % 9;
            int deltaY = (slot - inv.getMinSlot()) / 9;
            IntegerCoordinates coordinates = new IntegerCoordinates(inv.getX() + deltaX, inv.getY() + deltaY);
            SkillTreeNode treeNode = inv.getSkillTree().getNode(coordinates);
            Placeholders holders = new Placeholders();
            holders.register("name", treeNode.getName());
            holders.register("node-state", inv.getPlayerData().getNodeState(treeNode));
            String str = "";
            for (SkillTreeNode node : treeNode.getChildren())
                str += node.getName() + ",";
            //We remove the last comma
            str = str.substring(0, str.length() - 1);
            holders.register("unlocks", str);
            str = "";
            for (PlayerModifier playerModifier : treeNode.getModifiers()) {
                //TODO
                str += "\n" + playerModifier.getKey();
            }

            holders.register("modifiers", str);
            return holders;
        }


    }

    public class PathTreeNodeItem extends SimplePlaceholderItem<SkillTreeInventory> {
        public PathTreeNodeItem(ConfigurationSection config) {
            super(config);
        }

    }


    public class SkillTreeInventory extends GeneratedInventory {
        private int x, y;
        //width and height correspond to the the size of the 'board' representing the skill tree
        private int minSlot, middleSlot, maxSlot;
        private final int width, height;
        private int treeListPage;
        private final int maxTreeListPage;
        private final SkillTree skillTree;


        public SkillTreeInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);
            skillTree = playerData.getCachedSkillTree();

            maxTreeListPage = MMOCore.plugin.skillTreeManager.getAll().size() / 4;

            //We get the width and height of the GUI(corresponding to the slots given)
            List<Integer> slots = getByFunction("skill-tree-node").getSlots();
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
            middleSlot = minSlot + width / 2 + 9 * (height / 2);
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

        @Override
        public String calculateName() {
            return getEditable().getName().replace("{skill-tree-name}", skillTree.getName()).replace("{skill-tree-id}", skillTree.getId());
        }

        public SkillTree getSkillTree() {
            return skillTree;
        }

        public int getMinSlot() {
            return minSlot;
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                int offset = event.getSlot() - middleSlot;
                x += offset % 9;
                y += offset / 9;
                open();
                return;
            }
            if (item.getFunction().equals("next-tree-list-page")) {
                treeListPage++;
                open();
            }

            if (item.getFunction().equals("previous-tree-list-page")) {
                treeListPage--;
                open();
            }

            if (item.getFunction().equals("skill-tree")) {
                String id = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(
                        new NamespacedKey(MMOCore.plugin, "skill-tree-id"), PersistentDataType.STRING);
                playerData.setCachedSkillTree(MMOCore.plugin.skillTreeManager.get(id));
                newInventory(playerData).open();
            }
            if (item.getFunction().equals("skill-tree-node")) {

            }
        }
    }
}
