package net.Indyuce.mmocore.api.player;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.player.cooldown.CooldownMap;
import io.lumine.mythic.lib.util.Closeable;
import io.lumine.mythic.lib.version.Attributes;
import io.lumine.mythic.lib.version.VParticle;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.event.*;
import net.Indyuce.mmocore.api.event.unlocking.ItemLockedEvent;
import net.Indyuce.mmocore.api.event.unlocking.ItemUnlockedEvent;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttributes;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.api.player.profess.Subclass;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.api.player.social.FriendRequest;
import net.Indyuce.mmocore.api.player.stats.PlayerStats;
import net.Indyuce.mmocore.api.quest.PlayerQuests;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.api.quest.trigger.api.Removable;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.ExperienceObject;
import net.Indyuce.mmocore.experience.PlayerProfessions;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.experience.droptable.ExperienceItem;
import net.Indyuce.mmocore.experience.droptable.ExperienceTable;
import net.Indyuce.mmocore.gui.skilltree.NodeIncrementResult;
import net.Indyuce.mmocore.guild.provided.Guild;
import net.Indyuce.mmocore.loot.chest.particle.SmallParticleEffect;
import net.Indyuce.mmocore.manager.data.OfflinePlayerData;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.party.provided.MMOCorePartyModule;
import net.Indyuce.mmocore.party.provided.Party;
import net.Indyuce.mmocore.player.ClassDataContainer;
import net.Indyuce.mmocore.player.CombatHandler;
import net.Indyuce.mmocore.player.Unlockable;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.skill.binding.BoundSkillInfo;
import net.Indyuce.mmocore.skill.binding.SkillSlot;
import net.Indyuce.mmocore.skill.cast.SkillCastingInstance;
import net.Indyuce.mmocore.skill.cast.SkillCastingMode;
import net.Indyuce.mmocore.skilltree.NodeState;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import net.Indyuce.mmocore.waypoint.Waypoint;
import net.Indyuce.mmocore.waypoint.WaypointOption;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PlayerData extends SynchronizedDataHolder implements OfflinePlayerData, Closeable, ClassDataContainer {

    /**
     * Can be null, the {@link #getProfess()} method will return the
     * player class, or the default one if this field is null.
     * <p>
     * NEVER access the player class using this field, you must
     * use the {@link #getProfess()} method instead
     */
    @Nullable
    private PlayerClass profess;
    private int level, classPoints, skillPoints, attributePoints, attributeReallocationPoints, skillTreeReallocationPoints, skillReallocationPoints;
    private double experience;

    /**
     * Saving resources (especially health) right in player data fixes TONS of issues.
     */
    private double health, mana, stamina, stellium;
    private Guild guild;
    private SkillCastingInstance skillCasting;
    private final PlayerQuests questData;
    private final PlayerStats playerStats;
    private final List<UUID> friends = new ArrayList<>();

    /**
     * TODO Use {@link #hasUnlocked(Unlockable)} instead
     * <p>
     * Merge waypoints with unlocked items, and create a method
     * plugin-scope to check if some item is class-specific and
     * should be reset when switching class
     */
    @Deprecated
    private final Set<String> waypoints = new HashSet<>();
    private final Map<String, Integer> skills = new HashMap<>();
    private final Map<Integer, BoundSkillInfo> boundSkills = new HashMap<>();
    private final PlayerProfessions collectSkills = new PlayerProfessions(this);
    private final PlayerAttributes attributes = new PlayerAttributes(this);
    private final Map<String, SavedClassInformation> classSlots = new HashMap<>();
    private final Map<PlayerActivity, Long> lastActivity = new HashMap<>();
    private final CombatHandler combat = new CombatHandler(this);

    /**
     * Cached data
     * <p>
     * Current state of each node. This does not get saved in the player database
     * as it can be inferred from the skill tree node levels map.
     */
    private final Map<SkillTreeNode, NodeState> nodeStates = new HashMap<>();

    private final Map<SkillTreeNode, Integer> nodeLevels = new HashMap<>();
    private final Map<String, Integer> skillTreePoints = new HashMap<>();

    /**
     * Cached data
     * <p>
     * Amount of points spent in each tree. This does not get saved in the
     * player database as it can be inferred from the skill tree node levels map.
     */
    private final Map<SkillTree, Integer> skillTreePointsSpent = new HashMap<>();

    /**
     * Saves the NSK's of the items that have been unlocked in the format "namespace:key".
     * This is used for:
     * - waypoints
     * - skills
     */
    private final Set<String> unlockedItems = new HashSet<>();

    /**
     * Saves the amount of times the player has claimed some item in any exp
     * table. The key used in the map is the identifier of the exp table item.
     */
    private final Map<String, Integer> tableItemClaims = new HashMap<>();

    // NON-FINAL player data stuff made public to facilitate field change
    public boolean noCooldown;
    public long lastDropEvent;

    public PlayerData(MMOPlayerData mmoData) {
        super(MMOCore.plugin, mmoData);

        questData = new PlayerQuests(this);
        playerStats = new PlayerStats(this);
    }

    /**
     * Update all references after /mmocore reload so there can be garbage
     * collection with old plugin objects like class or skill instances.
     * <p>
     * It's OK if bound skills disappear because of a configuration issue
     * on the user's end. After all this method is only called when using
     * /mmocore reload
     */
    public void reload() {
        try {
            profess = profess == null ? null : MMOCore.plugin.classManager.get(profess.getId());
        } catch (NullPointerException exception) {
            MMOCore.log(Level.SEVERE, "[Userdata] Could not find class " + getProfess().getId() + " while refreshing player data.");
        }
        final Iterator<Map.Entry<Integer, BoundSkillInfo>> ite = new HashMap(boundSkills).entrySet().iterator();
        while (ite.hasNext())
            try {
                final Map.Entry<Integer, BoundSkillInfo> entry = ite.next();
                final @Nullable SkillSlot skillSlot = getProfess().getSkillSlot(entry.getKey());
                final String skillId = entry.getValue().getClassSkill().getSkill().getHandler().getId();
                final @Nullable ClassSkill classSkill = getProfess().getSkill(skillId);
                Validate.notNull(skillSlot, "Could not find skill slot n" + entry.getKey());
                Validate.notNull(classSkill, "Could not find skill with ID '" + skillId + "'");
                unbindSkill(entry.getKey());
                bindSkill(entry.getKey(), classSkill);
            } catch (Exception exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not reload data of '" + getPlayer().getName() + "': " + exception.getMessage());
                exception.printStackTrace();
            }

        for (SkillTree skillTree : getProfess().getSkillTrees())
            for (SkillTreeNode node : skillTree.getNodes())
                if (!nodeLevels.containsKey(node)) nodeLevels.put(node, 0);

        setupSkillTrees();
        applyTemporaryTriggers();
        getStats().updateStats();
    }

    @Deprecated
    public void setupRemovableTrigger() {
        applyTemporaryTriggers();
    }

    /**
     * Some triggers are marked with the {@link Removable} interface as
     * they are non-permanent triggers, and they need to be re-applied
     * everytime their MMOPlayerData gets flushed from the MythicLib cache
     * (everytime the player logs out).
     * <p>
     * This method goes through all the player's experience tables that
     * they have spent points into and register all their non-permanent triggers.
     */
    public void applyTemporaryTriggers() {

        // Remove all stats and buffs associated to triggers
        resetTriggerStats();

        // Experience tables from main class
        if (getProfess().hasExperienceTable())
            getProfess().getExperienceTable().applyTemporaryTriggers(this, getProfess());

        // Experience tables from professions
        for (Profession profession : MMOCore.plugin.professionManager.getAll())
            if (profession.hasExperienceTable())
                profession.getExperienceTable().applyTemporaryTriggers(this, profession);

        // Experience tables from skill tree nodes
        for (SkillTree skillTree : MMOCore.plugin.skillTreeManager.getAll())
            for (SkillTreeNode node : skillTree.getNodes())
                node.getExperienceTable().applyTemporaryTriggers(this, node);
    }

    public void setupSkillTrees() {

        // Node states setup
        for (SkillTree skillTree : getProfess().getSkillTrees())
            skillTree.setupNodeStates(this);
    }

    public int getPointsSpent(@NotNull SkillTree skillTree) {
        return skillTreePointsSpent.getOrDefault(skillTree, 0);
    }

    @Deprecated
    public int getPointSpent(SkillTree skillTree) {
        return getPointsSpent(skillTree);
    }

    public void setSkillTreePoints(@NotNull String treeId, int points) {
        if (points <= 0) skillTreePoints.remove(treeId);
        else skillTreePoints.put(treeId, points);
    }

    public void giveSkillTreePoints(@NotNull String id, int val) {
        skillTreePoints.merge(id, Math.max(0, val), (points, ignored) -> Math.max(0, points + val));
    }

    public int countSkillTreePoints(@NotNull SkillTree skillTree) {
        return nodeLevels.keySet().stream().filter(node -> node.getTree().equals(skillTree)).mapToInt(node -> nodeLevels.get(node) * node.getPointConsumption()).sum();
    }

    /**
     * Make a copy to make sure that the object
     * created is independent of the state of playerData.
     */
    @NotNull
    public Map<String, Integer> mapSkillTreePoints() {
        return new HashMap(skillTreePoints);
    }

    @Override
    public Map<Integer, String> mapBoundSkills() {
        Map<Integer, String> result = new HashMap<>();
        for (int slot : boundSkills.keySet())
            result.put(slot, boundSkills.get(slot).getClassSkill().getSkill().getHandler().getId());
        return result;
    }

    public Set<Map.Entry<String, Integer>> getNodeLevelsEntrySet() {
        HashMap<String, Integer> nodeLevelsString = new HashMap<>();
        for (SkillTreeNode node : nodeLevels.keySet())
            nodeLevelsString.put(node.getFullId(), nodeLevels.get(node));
        return nodeLevelsString.entrySet();
    }

    public void resetTriggerStats() {
        getMMOPlayerData().getStatMap().getInstances().forEach(statInstance -> statInstance.removeIf(Trigger.STAT_MODIFIER_KEY::equals));
        getMMOPlayerData().getSkillModifierMap().removeModifiers(Trigger.STAT_MODIFIER_KEY);
    }

    @Override
    public Map<String, Integer> getNodeLevels() {
        final Map<String, Integer> mapped = new HashMap<>();
        this.nodeLevels.forEach((node, level) -> mapped.put(node.getFullId(), level));
        return mapped;
    }

    @Deprecated
    public void clearSkillTrees() {
        resetSkillTrees();
    }

    public void resetSkillTrees() {

        // Un-apply triggers
        for (SkillTree tree : getProfess().getSkillTrees())
            for (SkillTreeNode node : tree.getNodes())
                node.resetAdvancement(this, false);

        // Skill trees progress
        nodeLevels.clear();
        nodeStates.clear(); // Cache data
        skillTreePointsSpent.clear();
        tableItemClaims.keySet().removeIf(s -> s.startsWith(SkillTreeNode.KEY_PREFIX)); // Clear node claim count

        // Skill tree (realloc) points
        skillTreePoints.clear();
        skillTreeReallocationPoints = 0;

        // Setup skill trees again
        setupSkillTrees();
    }

    public void clearNodeStates(@NotNull SkillTree skillTree) {
        for (SkillTreeNode node : skillTree.getNodes()) nodeStates.remove(node);
    }

    @NotNull
    public NodeIncrementResult canIncrementNodeLevel(@NotNull SkillTreeNode node) {
        final NodeState nodeState = nodeStates.get(node);

        // Check node state
        if (nodeState != NodeState.UNLOCKED && nodeState != NodeState.UNLOCKABLE)
            return NodeIncrementResult.LOCKED_NODE;

        // Check permission
        if (!node.hasPermissionRequirement(this)) return NodeIncrementResult.PERMISSION_DENIED;

        // Max node level
        if (getNodeLevel(node) >= node.getMaxLevel()) return NodeIncrementResult.MAX_LEVEL_REACHED;

        final int skillTreePoints = this.skillTreePoints.getOrDefault(node.getTree().getId(), 0) + this.skillTreePoints.getOrDefault("global", 0);
        if (skillTreePoints < node.getPointConsumption()) return NodeIncrementResult.NOT_ENOUGH_POINTS;

        return NodeIncrementResult.SUCCESS;
    }

    /**
     * Increments the node level by one, change the states of branches of the tree.
     * Consumes skill tree points from the tree first and then consumes the
     * global skill-tree points ('all')
     */
    public void incrementNodeLevel(@NotNull SkillTreeNode node) {
        final int newLevel = addNodeLevels(node, 1);
        node.updateAdvancement(this, newLevel); // Claim the node exp table

        // Update node state
        nodeStates.compute(node, (key, status) -> status == NodeState.UNLOCKABLE ? NodeState.UNLOCKED : status);

        // Consume skill tree points
        final AtomicInteger cost = new AtomicInteger(node.getPointConsumption());
        skillTreePoints.computeIfPresent(node.getTree().getId(), (key, points) -> {
            final int withdrawn = Math.min(points, cost.get());
            cost.set(cost.get() - withdrawn);
            return points <= withdrawn ? null : points - withdrawn;
        });
        if (cost.get() > 0) withdrawSkillTreePoints("global", cost.get());

        // Reload node states from full skill tree
        clearNodeStates(node.getTree());
        node.getTree().setupNodeStates(this);
    }

    @Deprecated
    public int getSkillTreePoint(String treeId) {
        return getSkillTreePoints(treeId);
    }

    public int getSkillTreePoints(@NotNull String treeId) {
        return skillTreePoints.getOrDefault(treeId, 0);
    }

    public void withdrawSkillTreePoints(@NotNull String treeId, int withdrawn) {
        final int cost = Math.max(0, withdrawn);
        skillTreePoints.computeIfPresent(treeId, (ignored, points) -> cost >= points ? null : points - cost);
    }

    public void setNodeState(SkillTreeNode node, NodeState nodeState) {
        nodeStates.put(node, nodeState);
    }

    public NodeState getNodeState(SkillTreeNode node) {
        return nodeStates.get(node);
    }

    @Deprecated
    public NodeState getNodeStatus(SkillTreeNode node) {
        return getNodeState(node);
    }

    public boolean hasNodeState(@NotNull SkillTreeNode node) {
        return nodeStates.containsKey(node);
    }

    public int getNodeLevel(@NotNull SkillTreeNode node) {
        return nodeLevels.getOrDefault(node, 0);
    }

    public void setNodeLevel(@NotNull SkillTreeNode node, int nodeLevel) {
        nodeLevels.compute(node, (ignored, currentLevelInteger) -> {
            final int currentLevel = currentLevelInteger == null ? 0 : currentLevelInteger;
            final int delta = (nodeLevel - currentLevel) * node.getPointConsumption();
            skillTreePointsSpent.merge(node.getTree(), delta, (level, ignored2) -> level + delta);
            return nodeLevel;
        });
    }

    public int addNodeLevels(@NotNull SkillTreeNode node, int increment) {
        final int delta = increment * node.getPointConsumption();
        skillTreePointsSpent.merge(node.getTree(), delta, (points, ignored) -> points + delta);
        return nodeLevels.merge(node, increment, (level, ignored) -> level + increment);
    }

    public void resetSkillTree(@NotNull SkillTree skillTree) {
        for (SkillTreeNode node : skillTree.getNodes()) {
            node.resetAdvancement(this, true);
            setNodeLevel(node, 0);
            nodeStates.remove(node);
        }
        skillTree.setupNodeStates(this);
    }

    @NotNull
    public Map<SkillTreeNode, NodeState> getNodeStates() {
        return new HashMap<>(nodeStates);
    }

    public boolean hasNodeStates() {
        return !nodeStates.isEmpty();
    }

    @Override
    public Map<String, Integer> getNodeTimesClaimed() {
        Map<String, Integer> result = new HashMap<>();
        tableItemClaims.forEach((str, val) -> {
            if (str.startsWith(SkillTreeNode.KEY_PREFIX)) result.put(str, val);
        });
        return result;
    }

    /**
     * @return If the item is unlocked by the player
     *         This is used for skills that can be locked & unlocked.
     */
    public boolean hasUnlocked(Unlockable unlockable) {
        return unlockable.isUnlockedByDefault() || unlockedItems.contains(unlockable.getUnlockNamespacedKey());
    }

    /**
     * Unlocks an item for the player. This is mainly used to unlock skills.
     *
     * @return If the item was locked when calling this method.
     */
    public boolean unlock(Unlockable unlockable) {
        Validate.isTrue(!unlockable.isUnlockedByDefault(), "Cannot unlock an item unlocked by default");
        final boolean wasLocked = unlockedItems.add(unlockable.getUnlockNamespacedKey());
        if (wasLocked) {
            unlockable.whenUnlocked(this);
            Bukkit.getScheduler().runTask(MythicLib.plugin, () -> Bukkit.getPluginManager().callEvent(new ItemUnlockedEvent(this, unlockable.getUnlockNamespacedKey())));
        }
        return wasLocked;
    }

    /**
     * Locks an item for the player by removing it from the unlocked items map if it is present.
     * This is mainly used to remove unlocked items when changing class or reallocating a skill tree.
     *
     * @return If the item was unlocked when calling this method.
     */
    public boolean lock(Unlockable unlockable) {
        Validate.isTrue(!unlockable.isUnlockedByDefault(), "Cannot lock an item unlocked by default");
        boolean wasUnlocked = unlockedItems.remove(unlockable.getUnlockNamespacedKey());
        if (wasUnlocked) {
            unlockable.whenLocked(this);
            Bukkit.getScheduler().runTask(MythicLib.plugin, () -> Bukkit.getPluginManager().callEvent(new ItemLockedEvent(this, unlockable.getUnlockNamespacedKey())));
        }
        return wasUnlocked;
    }

    public Set<String> getUnlockedItems() {
        return new HashSet<>(unlockedItems);
    }

    public void setUnlockedItems(Set<String> unlockedItems) {
        this.unlockedItems.clear();
        this.unlockedItems.addAll(unlockedItems);
    }

    @Deprecated
    public void resetTimesClaimed() {
        tableItemClaims.clear();
    }

    @Override
    public void close() {

        // Saves player health before saveData as the player will be considered offline into it if it is async
        health = getPlayer().getHealth();

        // Remove from party if it is MMO Party Module
        if (MMOCore.plugin.partyModule instanceof MMOCorePartyModule) {
            AbstractParty party = getParty();
            if (party != null && party instanceof Party) ((Party) party).removeMember(this);
        }

        // Close combat handler
        combat.close();

        // Close quest data
        questData.close();

        // Close bound skills
        boundSkills.forEach((slot, info) -> info.close());

        // Stop skill casting
        if (isCasting()) leaveSkillCasting(true);
    }

    public List<UUID> getFriends() {
        return friends;
    }

    public PlayerProfessions getCollectionSkills() {
        return collectSkills;
    }

    public PlayerQuests getQuestData() {
        return questData;
    }

    public long getLastActivity(PlayerActivity activity) {
        return this.lastActivity.getOrDefault(activity, 0l);
    }

    public long getActivityTimeOut(PlayerActivity activity) {
        return Math.max(0, getLastActivity(activity) + activity.getTimeOut() - System.currentTimeMillis());
    }

    public void setLastActivity(PlayerActivity activity) {
        setLastActivity(activity, System.currentTimeMillis());
    }

    public void setLastActivity(PlayerActivity activity, long timestamp) {
        this.lastActivity.put(activity, timestamp);
    }

    @Override
    public long getLastLogin() {
        return getMMOPlayerData().getLastLogActivity();
    }

    @Override
    public int getLevel() {
        return Math.max(1, level);
    }

    @Nullable
    public AbstractParty getParty() {
        return MMOCore.plugin.partyModule.getParty(this);
    }

    public boolean hasGuild() {
        return guild != null;
    }

    public Guild getGuild() {
        return guild;
    }

    public int getClassPoints() {
        return classPoints;
    }

    @Override
    public int getSkillPoints() {
        return skillPoints;
    }

    public void giveSkillReallocationPoints(int value) {
        skillReallocationPoints += value;
    }

    public int countSkillPointsSpent() {
        int sum = 0;
        for (ClassSkill skill : getProfess().getSkills())
            // 0 if the skill is level 1 (just unlocked) or 0 locked
            sum += Math.max(0, getSkillLevel(skill.getSkill()) - 1);

        return sum;
    }

    @NotNull
    public List<ClassSkill> getUnlockedSkills() {
        return getProfess().getSkills().stream()
                .filter(skill -> hasUnlocked(skill) && hasUnlockedLevel(skill))
                .collect(Collectors.toList());
    }

    @Override
    public int getAttributePoints() {
        return attributePoints;
    }

    @Override
    public int getAttributeReallocationPoints() {
        return attributeReallocationPoints;
    }

    @Override
    public int getSkillTreeReallocationPoints() {
        return skillTreeReallocationPoints;
    }

    public int getClaims(@NotNull ExperienceObject object, @NotNull ExperienceItem item) {
        final ExperienceTable table = object.getExperienceTable();
        return getClaims(object.getKey() + "." + table.getId() + "." + item.getId());
    }

    /**
     * @param key The identifier of an exp table item.
     * @return Amount of times an item has been claimed
     *         inside an experience table.
     */
    public int getClaims(@NotNull String key) {
        return tableItemClaims.getOrDefault(key, 0);
    }

    public void setClaims(@NotNull ExperienceObject object, @NotNull ExperienceItem item, int times) {
        final ExperienceTable table = object.getExperienceTable();
        setClaims(object.getKey() + "." + table.getId() + "." + item.getId(), times);
    }

    public void setClaims(@NotNull String itemKey, int times) {
        if (times <= 0) tableItemClaims.remove(itemKey);
        else tableItemClaims.put(itemKey, times);
    }

    @Deprecated
    public void setClaims(ExperienceObject object, ExperienceTable table, ExperienceItem item, int times) {
        setClaims(object.getKey() + "." + table.getId() + "." + item.getId(), times);
    }

    @Deprecated
    public int getClaims(ExperienceObject object, ExperienceTable table, ExperienceItem item) {
        return getClaims(object.getKey() + "." + table.getId() + "." + item.getId());
    }

    public Map<String, Integer> getItemClaims() {
        return tableItemClaims;
    }

    /**
     * @return Experience needed in order to reach next level
     */
    public int getLevelUpExperience() {
        return getProfess().getExpCurve().getExperience(getLevel() + 1);
    }

    public boolean isOnline() {
        return getMMOPlayerData().isOnline();
    }

    public boolean inGuild() {
        return guild != null;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
        if (isOnline()) {
            if (isSynchronized()) getStats().updateStats();
            refreshVanillaExp();
        }
    }

    public void takeLevels(int value) {
        setLevel(level - value);
    }

    public void giveLevels(int value, EXPSource source) {
        int total = 0;
        while (value-- > 0) total += getProfess().getExpCurve().getExperience(getLevel() + value + 1);
        giveExperience(total, source);
    }

    public void setExperience(double value) {
        experience = Math.max(0, value);

        if (isOnline()) refreshVanillaExp();
    }

    /**
     * Class experience can be displayed on the player's exp bar.
     * This updates the exp bar to display the player class level and exp.
     */
    public void refreshVanillaExp() {
        if (!MMOCore.plugin.configManager.overrideVanillaExp) return;

        getPlayer().sendExperienceChange(0.01f);
        getPlayer().setLevel(getLevel());
        getPlayer().setExp(Math.max(0, Math.min(1, (float) experience / (float) getLevelUpExperience())));
    }

    public void setAttributePoints(int value) {
        attributePoints = Math.max(0, value);
    }

    public void setAttributeReallocationPoints(int value) {
        attributeReallocationPoints = Math.max(0, value);
    }

    public void setSkillReallocationPoints(int value) {
        skillReallocationPoints = Math.max(0, value);
    }

    public int getSkillReallocationPoints() {
        return skillReallocationPoints;
    }

    public void setSkillPoints(int value) {
        skillPoints = Math.max(0, value);
    }

    public void setClassPoints(int value) {
        classPoints = Math.max(0, value);
    }

    public void setSkillTreeReallocationPoints(int value) {
        skillTreeReallocationPoints = Math.max(0, value);
    }

    public boolean hasSavedClass(PlayerClass profess) {
        return classSlots.containsKey(profess.getId());
    }

    public Set<String> getSavedClasses() {
        return classSlots.keySet();
    }

    @Nullable
    public SavedClassInformation getClassInfo(PlayerClass profess) {
        return getClassInfo(profess.getId());
    }

    @Nullable
    public SavedClassInformation getClassInfo(String profess) {
        return classSlots.get(profess);
    }

    public void applyClassInfo(PlayerClass profess, SavedClassInformation info) {
        classSlots.put(profess.getId(), info);
    }

    public void unloadClassInfo(PlayerClass profess) {
        unloadClassInfo(profess.getId());
    }

    public void unloadClassInfo(String profess) {
        classSlots.remove(profess);
    }

    public Set<String> getWaypoints() {
        return waypoints;
    }

    public boolean hasWaypoint(Waypoint waypoint) {
        return waypoint.hasOption(WaypointOption.DEFAULT) || waypoints.contains(waypoint.getId());
    }

    public void unlockWaypoint(Waypoint waypoint) {
        waypoints.add(waypoint.getId());
    }

    public void lockWaypoint(Waypoint waypoint) {
        waypoints.remove(waypoint.getId());
    }

    /**
     * @deprecated Provide a heal reason with {@link #heal(double, PlayerResourceUpdateEvent.UpdateReason)}
     */
    @Deprecated
    public void heal(double heal) {
        this.heal(heal, PlayerResourceUpdateEvent.UpdateReason.OTHER);
    }

    public void heal(double heal, PlayerResourceUpdateEvent.UpdateReason reason) {
        if (!isOnline()) return;

        // Avoid calling an useless event
        double max = getPlayer().getAttribute(Attributes.MAX_HEALTH).getValue();
        double newest = Math.max(0, Math.min(getPlayer().getHealth() + heal, max));
        if (getPlayer().getHealth() == newest) return;

        PlayerResourceUpdateEvent event = new PlayerResourceUpdateEvent(this, PlayerResource.HEALTH, heal, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        // Use updated amount from event
        getPlayer().setHealth(Math.max(0, Math.min(getPlayer().getHealth() + event.getAmount(), max)));
    }

    public void addFriend(UUID uuid) {
        friends.add(uuid);
    }

    @Override
    public void removeFriend(UUID uuid) {
        friends.remove(uuid);
    }

    @Override
    public boolean hasFriend(UUID uuid) {
        return friends.contains(uuid);
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
    }

    public void log(Level level, String message) {
        MMOCore.plugin.getLogger().log(level, "[Userdata:" + (isOnline() ? getPlayer().getName() : "Offline Player") + "] " + message);
    }

    public void sendFriendRequest(PlayerData target) {
        if (!isOnline() || !target.isOnline()) return;

        setLastActivity(PlayerActivity.FRIEND_REQUEST);
        FriendRequest request = new FriendRequest(this, target);
        ConfigMessage.fromKey("friend-request").addPlaceholders("player", getPlayer().getName(), "uuid", request.getUniqueId().toString()).send(target.getPlayer());
        MMOCore.plugin.requestManager.registerRequest(request);
    }

    /**
     * Teleports the player to a specific waypoint. This applies
     * the stellium waypoint cost and plays the teleport animation.
     *
     * @param target Target waypoint
     */
    public void warp(Waypoint target, double cost) {

        /*
         * This cooldown is only used internally to make sure the player is not
         * spamming waypoints. There is no need to reset it when resetting the
         * player waypoints data
         */
        setLastActivity(PlayerActivity.USE_WAYPOINT);
        giveStellium(-cost, PlayerResourceUpdateEvent.UpdateReason.USE_WAYPOINT);

        new BukkitRunnable() {
            final int x = getPlayer().getLocation().getBlockX();
            final int y = getPlayer().getLocation().getBlockY();
            final int z = getPlayer().getLocation().getBlockZ();
            final int warpTime = target.getWarpTime();
            final boolean hasPerm = getPlayer().hasPermission("mmocore.bypass-waypoint-wait");
            int t;

            public void run() {
                if (!isOnline() || getPlayer().getLocation().getBlockX() != x || getPlayer().getLocation().getBlockY() != y || getPlayer().getLocation().getBlockZ() != z) {
                    if (isOnline()) {
                        MMOCore.plugin.soundManager.getSound(SoundEvent.WARP_CANCELLED).playTo(getPlayer());
                        ConfigMessage.fromKey("warping-canceled").send(getPlayer());
                    }
                    giveStellium(cost, PlayerResourceUpdateEvent.UpdateReason.USE_WAYPOINT);
                    cancel();
                    return;
                }

                if (hasPerm || t++ >= warpTime) {
                    getPlayer().teleport(target.getLocation());
                    getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1, false, false));
                    MMOCore.plugin.soundManager.getSound(SoundEvent.WARP_TELEPORT).playTo(getPlayer());
                    cancel();
                    return;
                }

                ConfigMessage.fromKey("warping-comencing", "left", String.valueOf((warpTime - t) / 20)).send(getPlayer());
                MMOCore.plugin.soundManager.getSound(SoundEvent.WARP_CHARGE).playTo(getPlayer(), 1, (float) (.5 + t * 1.5 / warpTime));
                final double r = Math.sin((double) t / warpTime * Math.PI);
                for (double j = 0; j < Math.PI * 2; j += Math.PI / 4)
                    getPlayer().getLocation().getWorld().spawnParticle(VParticle.REDSTONE.get(), getPlayer().getLocation().add(Math.cos((double) 5 * t / warpTime + j) * r, (double) 2 * t / warpTime, Math.sin((double) 5 * t / warpTime + j) * r), 1, new Particle.DustOptions(Color.PURPLE, 1.25f));
            }
        }.runTaskTimer(MMOCore.plugin, 0, 1);
    }

    public boolean hasReachedMaxLevel() {
        return getProfess().getMaxLevel() > 0 && getLevel() >= getProfess().getMaxLevel();
    }

    /**
     * Gives experience without displaying an EXP hologram around the player
     *
     * @param value  Experience to give the player
     * @param source How the player earned experience
     */
    public void giveExperience(double value, EXPSource source) {
        giveExperience(value, source, null, true);
    }

    /**
     * Called when giving experience to a player
     *
     * @param value            Experience to give the player
     * @param source           How the player earned experience
     * @param hologramLocation Location used to display the hologram.
     *                         If it's null, no hologram will be displayed
     * @param splitExp         Should the exp be split among party members
     */
    public void giveExperience(double value, @NotNull EXPSource source, @Nullable Location hologramLocation,
                               boolean splitExp) {
        if (value <= 0) {
            experience = Math.max(0, experience + value);
            return;
        }

        // Splitting exp through party members
        final AbstractParty party;
        if (splitExp && (party = getParty()) != null && MMOCore.plugin.configManager.splitMainExp) {
            final List<PlayerData> nearbyMembers = party.getOnlineMembers().stream().filter(pd -> {
                if (equals(pd) || pd.hasReachedMaxLevel() || Math.abs(pd.getLevel() - getLevel()) > MMOCore.plugin.configManager.maxPartyLevelDifference)
                    return false;

                final double maxDis = MMOCore.plugin.configManager.partyMaxExpSplitRange;
                return maxDis <= 0 || (pd.getPlayer().getWorld().equals(getPlayer().getWorld()) && pd.getPlayer().getLocation().distanceSquared(getPlayer().getLocation()) < maxDis * maxDis);
            }).collect(Collectors.toList());
            value /= (nearbyMembers.size() + 1);
            for (PlayerData member : nearbyMembers)
                member.giveExperience(value, source, null, false);
        }

        // Must be placed after exp splitting
        if (hasReachedMaxLevel()) {
            setExperience(0);
            return;
        }

        // Apply buffs AFTER splitting exp
        value *= (1 + getStats().getStat("ADDITIONAL_EXPERIENCE") / 100) * MMOCore.plugin.boosterManager.getMultiplier(null);

        PlayerExperienceGainEvent event = new PlayerExperienceGainEvent(this, value, source);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        // Experience hologram
        if (hologramLocation != null && isOnline())
            MMOCoreUtils.displayIndicator(hologramLocation, ConfigMessage.fromKey("exp-hologram", "exp", MythicLib.plugin.getMMOConfig().decimal.format(event.getExperience())).asLine());

        experience = Math.max(0, experience + event.getExperience());

        // Calculate the player's next level
        int oldLevel = level, needed;
        while (experience >= (needed = getLevelUpExperience())) {

            if (hasReachedMaxLevel()) {
                experience = 0;
                break;
            }

            experience -= needed;
            level = getLevel() + 1;

            // Apply class experience table
            getProfess().updateAdvancement(this, level);
        }

        if (level > oldLevel) {
            Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(this, null, oldLevel, level));
            if (isOnline()) {
                ConfigMessage.fromKey("level-up").addPlaceholders("level", String.valueOf(level)).send(getPlayer());
                MMOCore.plugin.soundManager.getSound(SoundEvent.LEVEL_UP).playTo(getPlayer());
                new SmallParticleEffect(getPlayer(), VParticle.INSTANT_EFFECT.get());
            }
            getStats().updateStats();
        }

        refreshVanillaExp();
    }

    public double getExperience() {
        return experience;
    }

    @Override
    @NotNull
    public PlayerClass getProfess() {
        return profess == null ? MMOCore.plugin.classManager.getDefaultClass() : profess;
    }

    /**
     * @deprecated Provide reason with {@link #giveMana(double, PlayerResourceUpdateEvent.UpdateReason)}
     */
    @Deprecated
    public void giveMana(double amount) {
        giveMana(amount, PlayerResourceUpdateEvent.UpdateReason.OTHER);
    }

    public void giveMana(double amount, PlayerResourceUpdateEvent.UpdateReason reason) {

        // Avoid calling useless event
        double max = getStats().getStat("MAX_MANA");
        double newest = Math.max(0, Math.min(mana + amount, max));
        if (mana == newest) return;

        PlayerResourceUpdateEvent event = new PlayerResourceUpdateEvent(this, PlayerResource.MANA, amount, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        // Use updated amount from Bukkit event
        mana = Math.max(0, Math.min(mana + event.getAmount(), max));
    }

    /**
     * @deprecated Provide reason with {@link #giveStamina(double, PlayerResourceUpdateEvent.UpdateReason)}
     */
    @Deprecated
    public void giveStamina(double amount) {
        giveStamina(amount, PlayerResourceUpdateEvent.UpdateReason.OTHER);
    }

    public void giveStamina(double amount, PlayerResourceUpdateEvent.UpdateReason reason) {

        // Avoid calling useless event
        double max = getStats().getStat("MAX_STAMINA");
        double newest = Math.max(0, Math.min(stamina + amount, max));
        if (stamina == newest) return;

        PlayerResourceUpdateEvent event = new PlayerResourceUpdateEvent(this, PlayerResource.STAMINA, amount, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        // Use updated amount from Bukkit event
        stamina = Math.max(0, Math.min(stamina + event.getAmount(), max));
    }

    /**
     * @deprecated Provide reason with {@link #giveStellium(double, PlayerResourceUpdateEvent.UpdateReason)}
     */
    @Deprecated
    public void giveStellium(double amount) {
        giveStellium(amount, PlayerResourceUpdateEvent.UpdateReason.OTHER);
    }

    public void giveStellium(double amount, PlayerResourceUpdateEvent.UpdateReason reason) {

        // Avoid calling useless event
        double max = getStats().getStat("MAX_STELLIUM");
        double newest = Math.max(0, Math.min(stellium + amount, max));
        if (stellium == newest) return;

        PlayerResourceUpdateEvent event = new PlayerResourceUpdateEvent(this, PlayerResource.STELLIUM, amount, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        // Use updated amount from Bukkit event
        stellium = Math.max(0, Math.min(stellium + event.getAmount(), max));
    }

    @Deprecated
    @Override
    public double getHealth() {
        return isOnline() ? getPlayer().getHealth() : health;
    }

    @Override
    public double getMana() {
        return mana;
    }

    public double getStamina() {
        return stamina;
    }

    @Override
    public double getStellium() {
        return stellium;
    }

    public double getCachedHealth() {
        return health;
    }

    public PlayerStats getStats() {
        return playerStats;
    }

    public PlayerAttributes getAttributes() {
        return attributes;
    }

    public void setHealth(double amount) {
        this.health = amount;
    }

    public void setMana(double amount) {
        mana = Math.max(0, Math.min(amount, getStats().getStat("MAX_MANA")));
    }

    public void setStamina(double amount) {
        stamina = Math.max(0, Math.min(amount, getStats().getStat("MAX_STAMINA")));
    }

    public void setStellium(double amount) {
        stellium = Math.max(0, Math.min(amount, getStats().getStat("MAX_STELLIUM")));
    }

    @Deprecated
    public boolean isFullyLoaded() {
        return isSynchronized();
    }

    @Deprecated
    public void setFullyLoaded() {
        markAsSynchronized();
    }

    public boolean isCasting() {
        return skillCasting != null;
    }

    @Deprecated
    public boolean setSkillCasting(@NotNull SkillCastingInstance skillCasting) {
        return setSkillCasting();
    }

    /**
     * @return If the PlayerEnterCastingModeEvent successfully put the player
     *         into casting mode, otherwise if the event is cancelled, returns false.
     */
    public boolean setSkillCasting() {
        Validate.isTrue(!isCasting(), "Player already in casting mode");
        PlayerEnterCastingModeEvent event = new PlayerEnterCastingModeEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        this.skillCasting = SkillCastingMode.getInstance().newInstance(this);
        return true;
    }

    @NotNull
    public SkillCastingInstance getSkillCasting() {
        return Objects.requireNonNull(skillCasting, "Player not in casting mode");
    }

    /**
     * @return If player successfully left skill casting i.e the Bukkit
     *         event has not been cancelled
     */
    public boolean leaveSkillCasting() {
        return leaveSkillCasting(false);
    }

    /**
     * @param skipEvent Skip firing the exit event
     * @return If player successfully left skill casting i.e the Bukkit
     *         event has not been cancelled
     */
    public boolean leaveSkillCasting(boolean skipEvent) {
        Validate.isTrue(isCasting(), "Player not in casting mode");

        if (!skipEvent) {
            PlayerExitCastingModeEvent event = new PlayerExitCastingModeEvent(this);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return false;
        }

        skillCasting.close();
        this.skillCasting = null;
        setLastActivity(PlayerActivity.ACTION_BAR_MESSAGE, 0); // Reset action bar
        return true;
    }

    public void displayActionBar(@NotNull String message) {
        displayActionBar(message, false);
    }

    public void displayActionBar(@NotNull String message, boolean raw) {

        // TODO add an option to disable action-bar properly in all casting modes
        if (ChatColor.stripColor(message).isEmpty()) return;

        setLastActivity(PlayerActivity.ACTION_BAR_MESSAGE);
        if (raw) MythicLib.plugin.getVersion().getWrapper().sendActionBarRaw(getPlayer(), message);
        else getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    @Deprecated
    public void setAttribute(PlayerAttribute attribute, int value) {
        setAttribute(attribute.getId(), value);
    }

    @Deprecated
    public void setAttribute(String id, int value) {
        attributes.getInstance(id).setBase(value);
    }

    @Override
    public Map<String, Integer> mapAttributeLevels() {
        return getAttributes().mapPoints();
    }

    public int getSkillLevel(RegisteredSkill skill) {
        return skills.getOrDefault(skill.getHandler().getId(), 1);
    }

    public void setSkillLevel(RegisteredSkill skill, int level) {
        setSkillLevel(skill.getHandler().getId(), level);
    }

    public void setSkillLevel(String skill, int level) {
        skills.put(skill, level);
    }

    public void resetSkillLevel(String skill) {
        skills.remove(skill);
    }

    @Deprecated
    public boolean hasSkillUnlocked(RegisteredSkill skill) {
        return getProfess().hasSkill(skill.getHandler().getId()) && hasSkillUnlocked(getProfess().getSkill(skill.getHandler().getId()));
    }

    @Deprecated
    public boolean hasSkillUnlocked(ClassSkill skill) {
        return hasUnlockedLevel(skill);
    }

    /**
     * Checks for the player's level and compares it to the skill unlock level.
     * <p>
     * Any skill, when the player has the right level is instantly
     * unlocked, therefore one must NOT check if the player has unlocked the
     * skill by checking if the skills map contains the skill id as key. This
     * only checks if the player has spent any skill point.
     *
     * @return If the player unlocked the skill
     */
    public boolean hasUnlockedLevel(ClassSkill skill) {
        return getLevel() >= skill.getUnlockLevel();
    }

    public Map<String, Integer> mapSkillLevels() {
        return new HashMap<>(skills);
    }

    public void giveClassPoints(int value) {
        setClassPoints(classPoints + value);
    }

    public void giveSkillPoints(int value) {
        setSkillPoints(skillPoints + value);
    }

    public void giveAttributePoints(int value) {
        setAttributePoints(attributePoints + value);
    }

    // public void giveSkillReallocationPoints(int value) {
    // setSkillReallocationPoints(skillReallocationPoints + value);
    // }

    public void giveAttributeReallocationPoints(int value) {
        setAttributeReallocationPoints(attributeReallocationPoints + value);
    }

    public void giveSkillTreeReallocationPoints(int value) {
        setSkillTreeReallocationPoints(skillTreeReallocationPoints + value);
    }

    public CooldownMap getCooldownMap() {
        return getMMOPlayerData().getCooldownMap();
    }

    public void setClass(@Nullable PlayerClass profess) {
        this.profess = profess;

        // Clear bound skills
        boundSkills.forEach((slot, info) -> info.close());
        boundSkills.clear();
        applyTemporaryTriggers();

        // Update stats
        if (isOnline()) getStats().updateStats();
    }

    public boolean hasSkillBound(int slot) {
        return boundSkills.containsKey(slot);
    }

    @Nullable
    public ClassSkill getBoundSkill(int slot) {
        final BoundSkillInfo found = boundSkills.get(slot);
        return found != null ? found.getClassSkill() : null;
    }

    @Deprecated
    public void setBoundSkill(int slot, ClassSkill skill) {
        bindSkill(slot, skill);
    }

    /**
     * Binds a skill to the player.
     *
     * @param slot  Slot to which you're binding the skill
     * @param skill Skill being bound
     */
    public void bindSkill(int slot, @NotNull ClassSkill skill) {
        Validate.notNull(skill, "Skill cannot be null");
        if (slot <= 0) return;

        // Friendly error in case server owner makes a skill permanent while players have already bound it
        if (skill.isPermanent()) {
            MMOCore.plugin.getLogger().log(Level.WARNING, "Attempted to bind permanent skill " + skill.getSkill().getName() + " to player " + getUniqueId());
            return;
        }

        // Unbinds the previous skill (important for passive skills)
        unbindSkill(slot);
        final SkillSlot skillSlot = getProfess().getSkillSlot(slot);
        boundSkills.put(slot, new BoundSkillInfo(skillSlot, skill, this));
        SkillCastingMode.getInstance().onSkillBound(this);
    }

    @Nullable
    public BoundSkillInfo unbindSkill(int slot) {
        final @Nullable BoundSkillInfo boundSkillInfo = boundSkills.remove(slot);
        if (boundSkillInfo != null) boundSkillInfo.close();
        return boundSkillInfo;
    }

    @NotNull
    public Map<Integer, BoundSkillInfo> getBoundSkills() {
        return boundSkills;
    }

    /**
     * @return If the player has at least one active skill bound
     */
    public boolean hasActiveSkillBound() {
        for (BoundSkillInfo bound : boundSkills.values())
            if (!bound.isPassive()) return true;
        return false;
    }

    @NotNull
    public CombatHandler getCombat() {
        return combat;
    }

    public boolean isInCombat() {
        return getCombat().isInCombat();
    }

    /**
     * Loops through all the subclasses available to the player and
     * checks if they could potentially upgrade to one of these
     *
     * @return If the player can change its current class to
     *         a subclass
     */
    @Deprecated
    public boolean canChooseSubclass() {
        for (Subclass subclass : getProfess().getSubclasses())
            if (getLevel() >= subclass.getLevel()) return true;
        return false;
    }

    /**
     * Everytime a player does a combat action, like taking
     * or dealing damage to an entity, this method is called.
     */
    @Deprecated
    public void updateCombat() {
        getCombat().update();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerData that = (PlayerData) o;
        return getUniqueId().equals(that.getUniqueId());
    }

    @Override
    public int hashCode() {
        return getMMOPlayerData().hashCode();
    }

    public static PlayerData get(@NotNull MMOPlayerData playerData) {
        return get(playerData.getPlayer());
    }

    public static PlayerData get(@NotNull OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    public static PlayerData get(@NotNull UUID uuid) {
        return MMOCore.plugin.playerDataManager.get(uuid);
    }

    /**
     * This is used to check if the player data is loaded for a
     * specific player. This might seem redundant because the given
     * Player instance is linked to an online player, and data
     * is always loaded for an online player.
     * <p>
     * In fact a Player instance can be attached to a Citizens NPC
     * which has no player data loaded hence this method
     *
     * @param player Either a real player or an NPC
     * @return If player data for that player is loaded
     */
    public static boolean has(Player player) {
        return has(player.getUniqueId());
    }

    /**
     * This is used to check if the player data is loaded for a
     * specific player. This might seem redundant because the given
     * Player instance is linked to an online player, and data
     * is always loaded for an online player.
     * <p>
     * In fact a Player instance can be attached to a Citizens NPC
     * which has no player data loaded hence this method
     *
     * @param uuid A (real or fictive) player UUID
     * @return If player data for that player is loaded
     */
    public static boolean has(UUID uuid) {
        return MMOCore.plugin.playerDataManager.isLoaded(uuid);
    }

    public static Collection<PlayerData> getAll() {
        return MMOCore.plugin.playerDataManager.getLoaded();
    }
}
