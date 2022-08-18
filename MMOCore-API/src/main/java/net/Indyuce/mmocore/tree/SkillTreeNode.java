package net.Indyuce.mmocore.tree;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.ExpCurve;
import net.Indyuce.mmocore.experience.ExperienceObject;
import net.Indyuce.mmocore.experience.droptable.ExperienceTable;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.player.Unlockable;
import net.Indyuce.mmocore.tree.skilltree.AutomaticSkillTree;
import net.Indyuce.mmocore.tree.skilltree.SkillTree;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

//We must use generics to get the type of the corresponding tree
public class SkillTreeNode implements Unlockable, ExperienceObject {
    private final SkillTree tree;
    private final String name, id;
    private IntegerCoordinates coordinates;
    private boolean isRoot;

    /**
     * The lore corresponding to each level
     */
    private final List<String> lore = new ArrayList<>();

    private final ExperienceTable experienceTable;

    //The max level the skill tree node can have and the max amount of children it can have.
    private final int maxLevel, maxChildren, size;
    private final ArrayList<SkillTreeNode> children = new ArrayList<>();

    /**
     * Associates the required level to each parent.
     * <p>
     * You only need to have the requirement for one of your softParents
     * but you need to fulfill the requirements of all of your strong parents.
     **/
    private final HashMap<SkillTreeNode, Integer> softParents = new HashMap<>();
    private final HashMap<SkillTreeNode, Integer> strongParents = new HashMap<>();

    public SkillTreeNode(SkillTree tree, ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null");
        this.id = config.getName();
        this.tree = tree;
        name = Objects.requireNonNull(config.getString("name"), "Could not find node name");
        size = Objects.requireNonNull(config.getInt("size"));
        isRoot = config.getBoolean("is-root", false);
        lore.addAll(config.getStringList("lore"));
        String expTableId = config.getString("experience-table");
        Validate.notNull(expTableId, "You must specify an exp table for " + getFullId() + ".");
        this.experienceTable = MMOCore.plugin.experience.getTableOrThrow(expTableId);


        maxLevel = config.contains("max-level") ? config.getInt("max-level") : 1;
        maxChildren = config.contains("max-children") ? config.getInt("max-children") : 1;
        //If coordinates are precised adn we are not wiht an automaticTree we set them up
        if ((!(tree instanceof AutomaticSkillTree))) {
            Validate.isTrue(config.contains("coordinates.x") && config.contains("coordinates.y"), "No coordinates specified");
            coordinates = new IntegerCoordinates(config.getInt("coordinates.x"), config.getInt("coordinates.y"));
        }
    }


    public SkillTree getTree() {
        return tree;
    }


    public void setIsRoot() {
        isRoot = true;
    }

    public boolean isRoot() {
        return isRoot;
    }

    //Used when postLoaded
    public void addParent(SkillTreeNode parent, int requiredLevel, ParentType parentType) {
        if (parentType == ParentType.SOFT)
            softParents.put(parent, requiredLevel);
        else
            strongParents.put(parent, requiredLevel);
    }

    public void addChild(SkillTreeNode child) {
        children.add(child);
    }

    public void setCoordinates(IntegerCoordinates coordinates) {
        this.coordinates = coordinates;
    }

    public int getParentNeededLevel(SkillTreeNode parent) {
        return softParents.containsKey(parent) ? softParents.get(parent) : strongParents.containsKey(parent) ? strongParents.get(parent) : 0;
    }

    public boolean hasParent(SkillTreeNode parent) {
        return softParents.containsKey(parent) || strongParents.containsKey(parent);
    }

    public int getMaxLevel() {
        return maxLevel;
    }


    public int getMaxChildren() {
        return maxChildren;
    }

    public Set<SkillTreeNode> getSoftParents() {
        return softParents.keySet();
    }

    public Set<SkillTreeNode> getStrongParents() {
        return strongParents.keySet();
    }

    public ArrayList<SkillTreeNode> getChildren() {
        return children;
    }

    public int getSize() {
        return size;
    }

    public String getId() {
        return id;
    }


    public String getFullId() {
        return tree.getId() + "." + id;
    }

    public String getName() {
        return MythicLib.plugin.parseColors(name);
    }

    public IntegerCoordinates getCoordinates() {
        return coordinates;
    }

    @Override
    public String getKey() {
        return "skill_tree_node:" + getFullId().replace("-", "_");
    }

    @Nullable
    @Override
    public ExpCurve getExpCurve() {
        throw new RuntimeException("Attributes don't have experience");
    }

    @Override
    @NotNull
    public ExperienceTable getExperienceTable() {
        return Objects.requireNonNull(experienceTable);
    }

    @Override
    public boolean hasExperienceTable() {
        return experienceTable != null;
    }

    @Override
    public String getUnlockNamespacedKey() {
        return "skill_tree:" + tree.getId() + "_" + coordinates.getX() + "_" + coordinates.getY();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillTreeNode that = (SkillTreeNode) o;
        return tree.equals(that.tree) && (id.equals(that.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(tree, id);
    }

    public Placeholders getPlaceholders(PlayerData playerData) {
        Placeholders holders = new Placeholders();
        holders.register("name", getName());
        holders.register("node-state", playerData.getNodeState(this));
        holders.register("size", getSize());
        holders.register("level", playerData.getNodeLevel(this));
        holders.register("max-level", getMaxLevel());
        holders.register("max-children", getMaxChildren());
        return holders;
    }

    public List<String> getLore(PlayerData playerData) {
        Placeholders holders = getPlaceholders(playerData);
        List<String> parsedLore = new ArrayList<>();
        lore.forEach(string -> parsedLore.add(
                MythicLib.plugin.parseColors(holders.apply(playerData.getPlayer(), string))));
        return parsedLore;

    }

    /**
     * @param namespacedKey Something like "skill_tree:tree_name_1_5"
     * @return The corresponding skill tree node
     * @throws RuntimeException If the string cannot be parsed, if the specified
     *                          skill tree does not exist or if the skill tree has no such node
     */
    @NotNull
    public static SkillTreeNode getFromNamespacedKey(String namespacedKey) {
        String[] split = namespacedKey.substring(11).split("_");
        int n = split.length;

        IntegerCoordinates coords = new IntegerCoordinates(Integer.valueOf(split[n - 2]), Integer.valueOf(split[n - 1]));
        StringBuilder treeIdBuilder = new StringBuilder();
        for (int i = 0; i < n - 2; i++) {
            if (i > 0)
                treeIdBuilder.append("_");
            treeIdBuilder.append(split[i]);
        }
        String treeId = treeIdBuilder.toString();
        return MMOCore.plugin.skillTreeManager.get(treeId).getNode(coords);
    }

    @Override
    public void giveExperience(PlayerData playerData, double experience, @Nullable Location hologramLocation, @NotNull EXPSource source) {
        throw new RuntimeException("Attributes don't have experience");
    }

    @Override
    public boolean shouldHandle(PlayerData playerData) {
        throw new RuntimeException("Attributes don't have experience");
    }

    public class NodeContext {
        private final NodeState nodeState;
        private final int nodeLevel;

        public NodeContext(NodeState nodeState, int nodeLevel) {
            this.nodeState = nodeState;
            this.nodeLevel = nodeLevel;
        }

        public NodeState getNodeState() {
            return nodeState;
        }

        public int getNodeLevel() {
            return nodeLevel;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NodeContext that = (NodeContext) o;
            return nodeLevel == that.nodeLevel && nodeState == that.nodeState;
        }

        @Override
        public String toString() {
            return "NodeContext{" +
                    "nodeState=" + nodeState +
                    ", nodeLevel=" + nodeLevel +
                    '}';
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeState, nodeLevel);
        }
    }
}
