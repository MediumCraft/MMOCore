package net.Indyuce.mmocore.skilltree;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.ExpCurve;
import net.Indyuce.mmocore.experience.ExperienceObject;
import net.Indyuce.mmocore.experience.droptable.ExperienceTable;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.util.Icon;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

// We must use generics to get the type of the corresponding tree
public class SkillTreeNode implements ExperienceObject {
    private final SkillTree tree;
    private final String name, id;
    private final String permissionRequired;
    private final int pointConsumption;
    private final Map<NodeState, Icon> icons = new HashMap<>();
    private final IntegerCoordinates coordinates;
    private final int maxLevel, maxChildren;
    private final ExperienceTable experienceTable;
    private final List<ParentInformation> children = new ArrayList<>();
    private final List<ParentInformation> parents = new ArrayList<>();
    private final Map<Integer, List<String>> lores = new HashMap<>();

    private boolean root;

    public SkillTreeNode(SkillTree tree, ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null");
        this.id = config.getName();
        this.tree = tree;

        // Load icons for node states
        if (config.isConfigurationSection("display")) for (NodeState state : NodeState.values()) {
            final String ymlStatus = MMOCoreUtils.ymlName(state.name());
            if (config.isConfigurationSection("display." + ymlStatus))
                icons.put(state, Icon.from(config.get("display." + MMOCoreUtils.ymlName(state.name()))));
            else
                MMOCore.log("Could not find node display for state " + ymlStatus + " of node " + id + " in tree " + tree.getId() + ". Using default display.");
        }

        name = Objects.requireNonNull(config.getString("name"), "Could not find node name");
        root = config.getBoolean("root", config.getBoolean("is-root")); // backwards compatibility
        pointConsumption = config.getInt("point-consumed", 1);
        permissionRequired = config.getString("permission-required");
        Validate.isTrue(pointConsumption > 0, "The skill tree points consumed by a node must be greater than 0.");
        if (config.contains("lores"))
            for (String key : config.getConfigurationSection("lores").getKeys(false))
                try {
                    lores.put(Integer.parseInt(key), config.getStringList("lores." + key));
                } catch (NumberFormatException exception) {
                    throw new RuntimeException("You shall only specify integers in the 'lores' config section");
                }

        try {
            Validate.isTrue(config.contains("experience-table"), "You must specify an exp table");
            this.experienceTable = MMOCore.plugin.experience.loadExperienceTable(config.get("experience-table"));
        } catch (RuntimeException exception) {
            throw new RuntimeException("Could not load experience table: " + exception.getMessage());
        }

        maxLevel = config.getInt("max-level", 1);
        Validate.isTrue(maxLevel > 0, "Max level must be positive");
        maxChildren = config.getInt("max-children", 0);
        Validate.isTrue(maxChildren >= 0, "Max children must positive or zero");
        coordinates = IntegerCoordinates.from(config.get("coordinates"));
    }

    public SkillTree getTree() {
        return tree;
    }

    public boolean hasIcon(NodeState status) {
        return icons.containsKey(status);
    }

    public Icon getIcon(NodeState status) {
        return icons.get(status);
    }

    public boolean isRoot() {
        return root;
    }

    public void addParent(@NotNull SkillTreeNode parent, @NotNull ParentType parentType, int requiredLevel) {
        parents.add(new ParentInformation(parent, parentType, requiredLevel));
    }

    public void addChild(@NotNull SkillTreeNode child, @NotNull ParentType parentType, int requiredLevel) {
        children.add(new ParentInformation(child, parentType, requiredLevel));
    }

    public void setRoot() {
        root = true;
    }

    public int getPointConsumption() {
        return pointConsumption;
    }

    public int getParentNeededLevel(SkillTreeNode parent) {
        for (ParentInformation entry : parents)
            if (entry.getNode().equals(parent))
                return entry.getLevel();
        throw new RuntimeException("Could not find parent " + parent.getId() + " for node " + id);
    }

    @Deprecated
    public int getParentNeededLevel(SkillTreeNode parent, ParentType parentType) {
        for (ParentInformation entry : parents)
            if (entry.getNode().equals(parent) && entry.getType() == parentType)
                return entry.getLevel();
        return 0;
    }

    public boolean hasParent(SkillTreeNode parent) {
        for (ParentInformation entry : parents)
            if (entry.getNode() == parent) return true;
        return false;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getMaxChildren() {
        return maxChildren;
    }

    public boolean hasPermissionRequirement(@NotNull PlayerData playerData) {
        return permissionRequired == null || playerData.getPlayer().hasPermission(permissionRequired);
    }

    @NotNull
    public List<ParentInformation> getParents() {
        return parents;
    }

    @NotNull
    @Deprecated
    public List<SkillTreeNode> getParents(ParentType parentType) {
        return parents.stream().filter(integer -> integer.getType() == parentType).map(ParentInformation::getNode).collect(Collectors.toList());
    }

    @NotNull
    public List<ParentInformation> getChildren() {
        return children;
    }

    /**
     * @return The node identifier relative to its skill tree, like "extra_strength"
     */
    public String getId() {
        return id;
    }

    /**
     * @return Full node identifier, containing both the node identifier AND
     *         the skill tree identifier, like "combat_extra_strength"
     */
    @NotNull
    public String getFullId() {
        return tree.getId() + "_" + id;
    }

    @NotNull
    public String getName() {
        return MythicLib.plugin.parseColors(name);
    }

    @NotNull
    public IntegerCoordinates getCoordinates() {
        return coordinates;
    }

    public static final String KEY_PREFIX = "node";

    @Override
    public String getKey() {
        return KEY_PREFIX + ":" + getFullId().replace("-", "_");
    }

    @Override
    @NotNull
    public ExperienceTable getExperienceTable() {
        return Objects.requireNonNull(experienceTable, "Skill tree has no exp table");
    }

    @Override
    public boolean hasExperienceTable() {
        return experienceTable != null;
    }

    public NodeType getNodeType() {
        boolean up = tree.isPathOrNode(new IntegerCoordinates(coordinates.getX(), coordinates.getY() - 1));
        boolean down = tree.isPathOrNode(new IntegerCoordinates(coordinates.getX(), coordinates.getY() + 1));
        boolean right = tree.isPathOrNode(new IntegerCoordinates(coordinates.getX() + 1, coordinates.getY()));
        boolean left = tree.isPathOrNode(new IntegerCoordinates(coordinates.getX() - 1, coordinates.getY()));

        if (up && right && down && left) return NodeType.UP_RIGHT_DOWN_LEFT;
        else if (up && right && down) return NodeType.UP_RIGHT_DOWN;
        else if (up && right && left) return NodeType.UP_RIGHT_LEFT;
        else if (up && down && left) return NodeType.UP_DOWN_LEFT;
        else if (down && right && left) return NodeType.DOWN_RIGHT_LEFT;
        else if (up && right) return NodeType.UP_RIGHT;
        else if (up && down) return NodeType.UP_DOWN;
        else if (up && left) return NodeType.UP_LEFT;
        else if (down && right) return NodeType.DOWN_RIGHT;
        else if (down && left) return NodeType.DOWN_LEFT;
        else if (right && left) return NodeType.RIGHT_LEFT;
        else if (up) return NodeType.UP;
        else if (down) return NodeType.DOWN;
        else if (right) return NodeType.RIGHT;
        else if (left) return NodeType.LEFT;
        return NodeType.NO_PATH;
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

    public List<String> getLore(PlayerData playerData) {
        final int nodeLevel = playerData.getNodeLevel(this);
        final List<String> parsedLore = new ArrayList<>();

        for (int i = nodeLevel; i >= 0; i--) {
            final List<String> found = lores.get(i);
            if (found == null) continue;

            final Placeholders holders = getPlaceholders(playerData);
            found.forEach(string -> parsedLore.add(MythicLib.plugin.parseColors(holders.apply(playerData.getPlayer(), string))));
            break;
        }

        return parsedLore;
    }

    private Placeholders getPlaceholders(@NotNull PlayerData playerData) {
        Placeholders holders = new Placeholders();
        holders.register("name", getName());
        holders.register("node-state", playerData.getNodeState(this));
        holders.register("level", playerData.getNodeLevel(this));
        holders.register("max-level", getMaxLevel());
        holders.register("max-children", getMaxChildren());
        return holders;
    }

    @Override
    public void giveExperience(PlayerData playerData, double experience, @Nullable Location hologramLocation,
                               @NotNull EXPSource source) {
        throw new RuntimeException("Skill trees don't have experience");
    }

    @Override
    public boolean shouldHandle(PlayerData playerData) {
        throw new RuntimeException("Skill trees don't have experience");
    }

    @Nullable
    @Override
    public ExpCurve getExpCurve() {
        throw new RuntimeException("Skill trees don't have experience");
    }
}
