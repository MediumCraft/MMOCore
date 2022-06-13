package net.Indyuce.mmocore.tree;

import com.gmail.nossr50.mcmmo.acf.annotation.HelpSearchTags;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.configobject.LineConfigObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.player.Unlockable;
import net.Indyuce.mmocore.tree.skilltree.AutomaticSkillTree;
import net.Indyuce.mmocore.tree.skilltree.SkillTree;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

//We must use generics to get the type of the corresponding tree
public class SkillTreeNode implements Unlockable {
    private final SkillTree tree;
    private final String name, id;
    private IntegerCoordinates coordinates;
    private boolean isRoot;
    /**
     * The lore corresponding to each level
     */
    private final HashMap<NodeContext, List<String>> lores = new HashMap<>();

    //TODO modifiers depending on level with drop tables
    private final HashMap<Integer, List<PlayerModifier>> modifiers = new HashMap<>();
    private final HashMap<Integer, List<Trigger>> triggers = new HashMap<>();
    //The max level the skill tree node can have and the max amount of children it can have.
    private final int maxLevel, maxChildren, size;
    private final ArrayList<SkillTreeNode> children = new ArrayList<>();
    /**
     * Associates the required level to each parent
     * You only need to have the requirement for one of your softParents but you need to fulfill the requirements
     * of all of your strong parents.
     **/


    private final HashMap<SkillTreeNode, Integer> softParents = new HashMap<>();
    private final HashMap<SkillTreeNode, Integer> strongParents = new HashMap<>();


    public SkillTreeNode(SkillTree tree, ConfigurationSection config) {

        Validate.notNull(config, "Config cannot be null");
        this.id = config.getName();
        this.tree = tree;
        name = Objects.requireNonNull(config.getString("name"), "Could not find node name");
        size = Objects.requireNonNull(config.getInt("size"));
        isRoot = config.contains("is-root") ? config.getBoolean("is-root") ? true : false : false;

        //We initialize the value of the lore for each skill tree node.
        for (String state : Objects.requireNonNull(config.getConfigurationSection("lores")).getKeys(false)) {
            NodeState nodeState = NodeState.valueOf(MMOCoreUtils.toEnumName(state));
            if (nodeState == NodeState.UNLOCKED) {
                //TODO: Message could'nt load ... instead of exce/*99+*-*99**9+-ption
                ConfigurationSection section = config.getConfigurationSection("lores." + state);
                for (String level : section.getKeys(false)) {
                    lores.put(new NodeContext(nodeState, Integer.parseInt(level)), section.getStringList(level));
                }
            } else {
                lores.put(new NodeContext(nodeState, 0), config.getStringList("lores." + state));
            }
        }
        //We load the triggers
        if (config.contains("triggers")) {
            try {
                ConfigurationSection section = config.getConfigurationSection("triggers");
                for (String level : section.getKeys(false)) {
                    int value = Integer.parseInt(level);
                    for (String str : section.getStringList(level)) {
                        Trigger trigger = MMOCore.plugin.loadManager.loadTrigger(new MMOLineConfig(str));
                        if (!triggers.containsKey(value)) {
                            triggers.put(value, new ArrayList<>());
                        }
                        triggers.get(value).add(trigger);
                    }
                }
            } catch (NumberFormatException e) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Couldn't load triggers for the skill node " + tree.getId() + "." + id + " :Problem with the Number Format.");
            }
        }
        //We load the player Modifiers
        if (config.contains("modifiers")) {
            try {
                ConfigurationSection section = config.getConfigurationSection("modifiers");
                for (String level : section.getKeys(false)) {
                    int value = Integer.parseInt(level);
                    for (String str : section.getStringList(level)) {
                        PlayerModifier modifier = MythicLib.plugin.getModifiers().loadPlayerModifier(new LineConfigObject(new MMOLineConfig(str)));
                        if (!modifiers.containsKey(value)) {
                            modifiers.put(value, new ArrayList<>());
                        }
                        modifiers.get(value).add(modifier);
                    }
                }

            } catch (NumberFormatException e) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Couldn't load modifiers for the skill node " + tree.getId() + "." + id+ " :Problem with the Number Format.");
            }
        }


        maxLevel = config.contains("max-level") ? config.getInt("max-level") : 1;
        maxChildren = config.contains("max-children") ? config.getInt("max-children") : 1;
        //If coordinates are precised adn we are not wiht an automaticTree we set them up
        if ((!(tree instanceof AutomaticSkillTree)) && config.contains("coordinates.x") && config.contains("coordinates.y")) {
            coordinates = new IntegerCoordinates(config.getInt("coordinates.x"), config.getInt("coordinates.y"));
        }
        /*
        if (config.contains("modifiers")) {
            for (String key : config.getConfigurationSection("modifiers").getKeys(false)) {
                PlayerModifier mod = MythicLib.plugin.getModifiers().loadPlayerModifier(new ConfigSectionObject(config.getConfigurationSection(key)));
                modifiers.put(1, mod);
            }
        }
        */

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

    public String getName() {
        return MythicLib.plugin.parseColors(name);
    }

    public IntegerCoordinates getCoordinates() {
        return coordinates;
    }

    public List<PlayerModifier> getModifiers(int level) {
        return modifiers.get(level);
    }


    public List<Trigger> getTriggers(int level) {
        return triggers.get(level);
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

        //List of all the children of the node
        String str = "";
        for (SkillTreeNode node : getChildren())
            str += node.getName() + ",";
        //We remove the last comma
        if (str.length() != 0)
            str = str.substring(0, str.length() - 1);
        holders.register("children", str);

        //list of parents with the level needed for each of them
        str = "";
        for (SkillTreeNode node : getSoftParents())
            str += node.getName() + " " + MMOCoreUtils.toRomanNumerals(getParentNeededLevel(node)) + ",";
        //We remove the last comma
        if (str.length() != 0)
            str = str.substring(0, str.length() - 1);
        holders.register("soft-parents-level", str);

        //list of parents
        str = "";
        for (SkillTreeNode node : getSoftParents())
            str += node.getName() + ",";
        //We remove the last comma
        if (str.length() != 0)
            str = str.substring(0, str.length() - 1);
        holders.register("soft-parents", str);

        //list of parents with the level needed for each of them
        str = "";
        for (SkillTreeNode node : getStrongParents())
            str += node.getName() + " " + MMOCoreUtils.toRomanNumerals(getParentNeededLevel(node)) + ",";
        //We remove the last comma
        if (str.length() != 0)
            str = str.substring(0, str.length() - 1);
        holders.register("strong-parents-level", str);

        //list of parents
        str = "";
        for (SkillTreeNode node : getStrongParents())
            str += node.getName() + ",";
        //We remove the last comma
        if (str.length() != 0)
            str = str.substring(0, str.length() - 1);
        holders.register("strong-parents", str);

        return holders;
    }

    public List<String> getLore(PlayerData playerData) {
        Placeholders holders = getPlaceholders(playerData);
        List<String> parsedLore = new ArrayList<>();
        NodeContext context = new NodeContext(playerData.getNodeState(this), playerData.getNodeLevel(this));
        lores.get(context).forEach(string -> parsedLore.add(
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
