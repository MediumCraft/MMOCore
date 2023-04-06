package net.Indyuce.mmocore.skilltree;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.ExpCurve;
import net.Indyuce.mmocore.experience.ExperienceObject;
import net.Indyuce.mmocore.experience.droptable.ExperienceTable;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// We must use generics to get the type of the corresponding tree
public class SkillTreeNode implements ExperienceObject {
    private final SkillTree tree;
    private final String name, id;
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
    private final int maxLevel, maxChildren, size;
    private final List<SkillTreeNode> children = new ArrayList<>();

    /**
     * Associates the required level to each parent.
     * <p>
     * You only need to have the requirement for one of your softParents
     * but you need to fulfill the requirements of all of your strong parents.
     **/
    private final Map<SkillTreeNode, Integer> softParents = new HashMap<>();
    private final Map<SkillTreeNode, Integer> strongParents = new HashMap<>();

    /**
     * Prefix used in node key
     */
    public static final String KEY_PREFIX = "node";

    public SkillTreeNode(SkillTree tree, ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null");
        this.id = config.getName();
        this.tree = tree;
        name = Objects.requireNonNull(config.getString("name"), "Could not find node name");
        size = Objects.requireNonNull(config.getInt("size"));
        isRoot = config.getBoolean("is-root", false);
        skillTreePointsConsumed=config.getInt("point-consumed",1);
        Validate.isTrue(skillTreePointsConsumed>0,"The skill tree points consumed by a node must be greater than 0.");
        if (config.contains("lores"))
            for (String key : config.getConfigurationSection("lores").getKeys(false))
                try {
                    lores.put(Integer.parseInt(key), config.getStringList("lores." + key));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("You must only specifiy integers in lores.");
                }

        String expTableId = config.getString("experience-table");
        Validate.notNull(expTableId, "You must specify an exp table for " + getFullId() + ".");
        this.experienceTable = MMOCore.plugin.experience.getTableOrThrow(expTableId);

        maxLevel = config.contains("max-level") ? config.getInt("max-level") : 1;
        maxChildren = config.contains("max-children") ? config.getInt("max-children") : 1;
        // If coordinates are precised and we are not with an automaticTree we set them up
        Validate.isTrue(config.contains("coordinates.x") && config.contains("coordinates.y"), "No coordinates specified");
        coordinates = new IntegerCoordinates(config.getInt("coordinates.x"), config.getInt("coordinates.y"));
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

    // Used when postLoaded
    public void addParent(SkillTreeNode parent, int requiredLevel, ParentType parentType) {
        if (parentType == ParentType.SOFT)
            softParents.put(parent, requiredLevel);
        else
            strongParents.put(parent, requiredLevel);
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

    public List<SkillTreeNode> getChildren() {
        return children;
    }

    public int getSize() {
        return size;
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
        if (!lores.containsKey(playerData.getNodeLevel(this)))
            return parsedLore;
        List<String> lore = lores.get(playerData.getNodeLevel(this));
        lore.forEach(string -> parsedLore.add(
                MythicLib.plugin.parseColors(holders.apply(playerData.getPlayer(), string))));
        return parsedLore;

    }

    @Override
    public void giveExperience(PlayerData playerData, double experience, @Nullable Location hologramLocation, @NotNull EXPSource source) {
        throw new RuntimeException("Attributes don't have experience");
    }

    @Override
    public boolean shouldHandle(PlayerData playerData) {
        throw new RuntimeException("Attributes don't have experience");
    }
}
