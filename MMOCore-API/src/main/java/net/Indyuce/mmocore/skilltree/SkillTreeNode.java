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
import net.Indyuce.mmocore.gui.skilltree.display.Icon;
import net.Indyuce.mmocore.gui.skilltree.display.NodeType;
import net.Indyuce.mmocore.skilltree.tree.ParentInformation;
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

    private String permissionRequired;

    private final Map<SkillTreeStatus, Icon> icons = new HashMap<>();

    private IntegerCoordinates coordinates;
    /**
     * The number of skill tree points this node requires.
     */
    private final int skillTreePointsConsumed;
    private boolean isRoot;

    /**
     * The lore corresponding to each level
     */
    private final Map<Integer, List<String>> lores = new HashMap<>();

    private final ExperienceTable experienceTable;

    // The max level the skill tree node can have and the max amount of children it can have.
    private final int maxLevel, maxChildren;
    private final List<SkillTreeNode> children = new ArrayList<>();

    /**
     * Associates the required level to each parent.
     * <p>
     * You only need to have the requirement for one of your softParents
     * but you need to fulfill the requirements of all of your strong parents.
     **/
    private final Map<ParentInformation, Integer> parents = new HashMap<>();

    /**
     * Prefix used in node key
     */
    public static final String KEY_PREFIX = "node";

    public SkillTreeNode(SkillTree tree, ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null");
        this.id = config.getName();
        this.tree = tree;
        if (config.isConfigurationSection("display")) {
            for (SkillTreeStatus status : SkillTreeStatus.values()) {
                String ymlStatus = MMOCoreUtils.ymlName(status.name());
                if (!config.isConfigurationSection("display." + ymlStatus)) {
                    MMOCore.log("Could not find node display for status " + ymlStatus + " for node " + id + " in tree " + tree.getId() + ". Using default display.");
                    continue;
                }
                icons.put(status, new Icon(config.getConfigurationSection("display." + MMOCoreUtils.ymlName(status.name()))));
            }
        }
        name = Objects.requireNonNull(config.getString("name"), "Could not find node name");
        isRoot = config.getBoolean("is-root", false);
        skillTreePointsConsumed = config.getInt("point-consumed", 1);
        permissionRequired = config.getString("permission-required");
        Validate.isTrue(skillTreePointsConsumed > 0, "The skill tree points consumed by a node must be greater than 0.");
        if (config.contains("lores"))
            for (String key : config.getConfigurationSection("lores").getKeys(false))
                try {
                    lores.put(Integer.parseInt(key), config.getStringList("lores." + key));
                } catch (NumberFormatException exception) {
                    throw new RuntimeException("You shall only specify integers in the 'lores' config section");
                }

        Validate.isTrue(config.contains("experience-table"), "You must specify an exp table");
        this.experienceTable = MMOCore.plugin.experience.loadExperienceTable(config.get("experience-table"));

        maxLevel = config.contains("max-level") ? config.getInt("max-level") : 1;
        maxChildren = config.contains("max-children") ? config.getInt("max-children") : 1;
        // If coordinates are precised and we are not with an automaticTree we set them up
        Validate.isTrue(config.contains("coordinates.x") && config.contains("coordinates.y"), "No coordinates specified");
        coordinates = new IntegerCoordinates(config.getInt("coordinates.x"), config.getInt("coordinates.y"));
    }

    public SkillTree getTree() {
        return tree;
    }

    public boolean hasIcon(SkillTreeStatus status) {
        return icons.containsKey(status);
    }

    public Icon getIcon(SkillTreeStatus status) {
        return icons.get(status);
    }

    public void setIsRoot() {
        isRoot = true;
    }

    public boolean isRoot() {
        return isRoot;
    }

    // Used when postLoaded
    public void addParent(SkillTreeNode parent, int requiredLevel, ParentType parentType) {
        parents.put(new ParentInformation(parent, parentType), requiredLevel);
    }

    public void addChild(SkillTreeNode child) {
        children.add(child);
    }

    public int getSkillTreePointsConsumed() {
        return skillTreePointsConsumed;
    }

    public void setCoordinates(IntegerCoordinates coordinates) {
        this.coordinates = coordinates;
    }


    public int getParentNeededLevel(SkillTreeNode parent) {
        for (Map.Entry<ParentInformation, Integer> entry : parents.entrySet())
            if (entry.getKey().node().equals(parent))
                return entry.getValue();
        throw new RuntimeException("Could not find parent " + parent.getId() + " for node " + id);
    }

    public int getParentNeededLevel(SkillTreeNode parent, ParentType parentType) {
        return parents.get(new ParentInformation(parent, parentType));
    }

    public boolean hasParent(SkillTreeNode parent) {
        for (Map.Entry<ParentInformation, Integer> entry : parents.entrySet())
            if (entry.getKey().node() == parent)
                return true;
        return false;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getMaxChildren() {
        return maxChildren;
    }

    public boolean hasPermissionRequirement(PlayerData playerData) {
        return permissionRequired == null || playerData.getPlayer().hasPermission(permissionRequired);
    }

    public Set<SkillTreeNode> getParents() {
        return parents.keySet().stream().map(ParentInformation::node).collect(Collectors.toSet());
    }

    public Set<SkillTreeNode> getParents(ParentType parentType) {
        return parents.entrySet().stream().filter(entry -> entry.getKey().type() == parentType).map((entry) -> entry.getKey().node()).collect(Collectors.toSet());
    }

    public List<SkillTreeNode> getChildren() {
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
     * the skill tree identifier, like "combat_extra_strength"
     */
    public String getFullId() {
        return tree.getId() + "_" + id;
    }

    public String getName() {
        return MythicLib.plugin.parseColors(name);
    }

    public IntegerCoordinates getCoordinates() {
        return coordinates;
    }

    @Override
    public String getKey() {
        return KEY_PREFIX + ":" + getFullId().replace("-", "_");
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

    public NodeType getNodeType() {
        boolean hasUpPathOrNode = tree.isPathOrNode(new IntegerCoordinates(coordinates.getX(), coordinates.getY() - 1));
        boolean hasDownPathOrNode = tree.isPathOrNode(new IntegerCoordinates(coordinates.getX(), coordinates.getY() + 1));
        boolean hasRightPathOrNode = tree.isPathOrNode(new IntegerCoordinates(coordinates.getX() + 1, coordinates.getY()));
        boolean hasLeftPathOrNode = tree.isPathOrNode(new IntegerCoordinates(coordinates.getX() - 1, coordinates.getY()));
        return NodeType.getNodeType(hasUpPathOrNode, hasRightPathOrNode, hasDownPathOrNode, hasLeftPathOrNode);
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
        holders.register("node-state", playerData.getNodeStatus(this));
        holders.register("level", playerData.getNodeLevel(this));
        holders.register("max-level", getMaxLevel());
        holders.register("max-children", getMaxChildren());
        return holders;
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

    @Override
    public void giveExperience(PlayerData playerData, double experience, @Nullable Location hologramLocation,
                               @NotNull EXPSource source) {
        throw new RuntimeException("Attributes don't have experience");
    }

    @Override
    public boolean shouldHandle(PlayerData playerData) {
        throw new RuntimeException("Attributes don't have experience");
    }
}
