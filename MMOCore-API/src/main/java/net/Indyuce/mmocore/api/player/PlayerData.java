package net.Indyuce.mmocore.api.player;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.cooldown.CooldownMap;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.event.PlayerExperienceGainEvent;
import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.event.unlocking.ItemLockedEvent;
import net.Indyuce.mmocore.api.event.unlocking.ItemUnlockedEvent;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttributes;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.api.player.profess.Subclass;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.skill.binding.BoundSkillInfo;
import net.Indyuce.mmocore.api.player.social.FriendRequest;
import net.Indyuce.mmocore.api.player.stats.PlayerStats;
import net.Indyuce.mmocore.api.quest.PlayerQuests;
import net.Indyuce.mmocore.api.quest.trigger.SkillModifierTrigger;
import net.Indyuce.mmocore.api.quest.trigger.StatTrigger;
import net.Indyuce.mmocore.api.util.Closable;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.ExperienceObject;
import net.Indyuce.mmocore.experience.ExperienceTableClaimer;
import net.Indyuce.mmocore.experience.PlayerProfessions;
import net.Indyuce.mmocore.experience.droptable.ExperienceItem;
import net.Indyuce.mmocore.experience.droptable.ExperienceTable;
import net.Indyuce.mmocore.guild.provided.Guild;
import net.Indyuce.mmocore.loot.chest.particle.SmallParticleEffect;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.party.provided.MMOCorePartyModule;
import net.Indyuce.mmocore.party.provided.Party;
import net.Indyuce.mmocore.player.ClassDataContainer;
import net.Indyuce.mmocore.player.CombatHandler;
import net.Indyuce.mmocore.player.Unlockable;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.skill.cast.SkillCastingHandler;
import net.Indyuce.mmocore.skilltree.IntegerCoordinates;
import net.Indyuce.mmocore.skilltree.NodeStatus;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import net.Indyuce.mmocore.skilltree.tree.display.DisplayInfo;
import net.Indyuce.mmocore.skilltree.tree.display.Icon;
import net.Indyuce.mmocore.waypoint.Waypoint;
import net.Indyuce.mmocore.waypoint.WaypointOption;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PlayerData extends OfflinePlayerData implements Closable, ExperienceTableClaimer, ClassDataContainer {

    /**
     * Corresponds to the MythicLib player data. It is used to keep
     * track of the Player instance corresponding to that player data,
     * as well as other things like the last time the player logged in/out
     */
    private final MMOPlayerData mmoData;

    /**
     * Can be null, the {@link #getProfess()} method will return the
     * player class, or the default one if this field is null.
     */
    @Nullable
    private PlayerClass profess;
    private int level, classPoints, skillPoints, attributePoints, attributeReallocationPoints, skillTreeReallocationPoints, skillReallocationPoints;
    private double experience;
    private double mana, stamina, stellium;

    /**
     * Health is stored in playerData because when saving the playerData we can't access the player health anymore as the payer is Offline.
     */
    private double health;
    private Guild guild;
    private SkillCastingHandler skillCasting;
    private final PlayerQuests questData;
    private final PlayerStats playerStats;
    private final List<UUID> friends = new ArrayList<>();

    /**
     * @deprecated Use {@link #hasUnlocked(Unlockable)} instead
     */
    @Deprecated
    private final Set<String> waypoints = new HashSet<>();
    private final Map<String, Integer> skills = new HashMap<>();
    // TODO change it to an array....... Map<Integer, BoundSkillInfo> is just BoundSkillInfo[]
    private final Map<Integer, BoundSkillInfo> boundSkills = new HashMap<>();
    private final PlayerProfessions collectSkills = new PlayerProfessions(this);
    private final PlayerAttributes attributes = new PlayerAttributes(this);
    private final Map<String, SavedClassInformation> classSlots = new HashMap<>();
    private final Map<PlayerActivity, Long> lastActivity = new HashMap<>();
    private final CombatHandler combat = new CombatHandler(this);

    /**
     * Cached for easier access. Amount of points spent in each skill tree.
     */
    private final Map<SkillTree, Integer> pointSpent = new HashMap<>();

    /**
     * Cached for easier access. Current status of each skill tree node.
     */
    private final Map<SkillTreeNode, NodeStatus> nodeStates = new HashMap<>();
    private final Map<SkillTreeNode, Integer> nodeLevels = new HashMap<>();
    private final Map<String, Integer> skillTreePoints = new HashMap<>();

    /**
     * Saves the namespacedkeys of the items that have been unlocked in the form "namespace:key".
     * This is used for:
     * - waypoints
     * - skills
     */
    private final Set<String> unlockedItems = new HashSet<>();

    /**
     * Saves the amount of times the player has claimed some
     * exp item in exp tables, for both exp tables used
     */
    private final Map<String, Integer> tableItemClaims = new HashMap<>();

    // NON-FINAL player data stuff made public to facilitate field change
    public boolean noCooldown;

    /**
     * Player data is stored in the data map before it's actually fully loaded
     * so that external plugins don't necessarily have to listen to the PlayerDataLoadEvent.
     */
    private boolean fullyLoaded = false;

    public PlayerData(MMOPlayerData mmoData) {
        super(mmoData.getUniqueId());

        this.mmoData = mmoData;
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
            getStats().updateStats();
        } catch (NullPointerException exception) {
            MMOCore.log(Level.SEVERE, "[Userdata] Could not find class " + getProfess().getId() + " while refreshing player data.");
        }

        final Iterator<Integer> ite = boundSkills.keySet().iterator();
        while (ite.hasNext())
            try {
                final int slot = ite.next();
                boundSkills.put(slot, new BoundSkillInfo(boundSkills.get(slot)));
            } catch (Exception exception) {
                exception.printStackTrace();
            }

        for (SkillTree skillTree : getProfess().getSkillTrees())
            for (SkillTreeNode node : skillTree.getNodes())
                if (!nodeLevels.containsKey(node)) nodeLevels.put(node, 0);

        setupSkillTree();
    }

    public void setupSkillTree() {

        // Node states setup
        for (SkillTree skillTree : getProfess().getSkillTrees())
            skillTree.setupNodeStates(this);

        // Stat triggers setup
        for (SkillTree skillTree : MMOCore.plugin.skillTreeManager.getAll())
            for (SkillTreeNode node : skillTree.getNodes())
                node.getExperienceTable().claimStatTriggers(this, node);
    }

    public int getPointSpent(SkillTree skillTree) {
        return pointSpent.getOrDefault(skillTree, 0);
    }

    public void setSkillTreePoints(String treeId, int points) {
        skillTreePoints.put(treeId, points);
    }

    public void giveSkillTreePoints(String id, int val) {
        skillTreePoints.put(id, skillTreePoints.getOrDefault(id, 0) + val);
    }

    public int countSkillTreePoints(SkillTree skillTree) {
        return nodeLevels.keySet().stream().filter(node -> node.getTree().equals(skillTree)).mapToInt(node -> nodeLevels.get(node) * node.getSkillTreePointsConsumed()).sum();
    }

    /**
     * Make a copy to make sure that the object
     * created is independent of the state of playerData.
     */
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

    public void clearSkillTreePoints() {
        skillTreePoints.clear();
    }

    public void clearNodeTimesClaimed() {
        final Iterator<String> ite = tableItemClaims.keySet().iterator();
        while (ite.hasNext()) if (ite.next().startsWith(SkillTreeNode.KEY_PREFIX)) ite.remove();
    }

    public Set<Map.Entry<String, Integer>> getNodeLevelsEntrySet() {
        HashMap<String, Integer> nodeLevelsString = new HashMap<>();
        for (SkillTreeNode node : nodeLevels.keySet())
            nodeLevelsString.put(node.getFullId(), nodeLevels.get(node));
        return nodeLevelsString.entrySet();
    }

    public void resetTriggerStats() {
        for (StatInstance instance : mmoData.getStatMap().getInstances()) {
            Iterator<StatModifier> iter = instance.getModifiers().iterator();
            while (iter.hasNext()) {
                StatModifier modifier = iter.next();
                if (modifier.getKey().startsWith(StatTrigger.TRIGGER_PREFIX)) iter.remove();
            }
        }
    }

    public Map<SkillTreeNode, Integer> getNodeLevels() {
        return new HashMap<>(nodeLevels);
    }

    public void clearNodeLevels() {
        nodeLevels.clear();
        pointSpent.clear();
    }

    public boolean canIncrementNodeLevel(SkillTreeNode node) {
        NodeStatus nodeStatus = nodeStates.get(node);
        //Check the State of the node
        if (nodeStatus != NodeStatus.UNLOCKED && nodeStatus != NodeStatus.UNLOCKABLE) return false;
        return getNodeLevel(node) < node.getMaxLevel() && (skillTreePoints.getOrDefault(node.getTree().getId(), 0) + skillTreePoints.getOrDefault("global", 0) >= node.getSkillTreePointsConsumed());
    }

    /**
     * Increments the node level by one, change the states of branches of the tree.
     * Consumes skill tree points from the tree first and then consumes the
     * global skill-tree points ('all')
     */
    public void incrementNodeLevel(SkillTreeNode node) {
        setNodeLevel(node, getNodeLevel(node) + 1);
        // Claims the nodes experience table.
        node.getExperienceTable().claim(this, getNodeLevel(node), node);

        if (nodeStates.get(node) == NodeStatus.UNLOCKABLE) setNodeState(node, NodeStatus.UNLOCKED);
        int pointToWithdraw = node.getSkillTreePointsConsumed();
        if (skillTreePoints.get(node.getTree().getId()) > 0) {
            int pointWithdrawn = Math.min(pointToWithdraw, skillTreePoints.get(node.getTree().getId()));
            withdrawSkillTreePoints(node.getTree().getId(), pointWithdrawn);
            pointToWithdraw -= pointWithdrawn;
        }
        if (pointToWithdraw > 0) withdrawSkillTreePoints("global", pointToWithdraw);
        // We unload the nodeStates map (for the skill tree) and reload it completely
        for (SkillTreeNode node1 : node.getTree().getNodes())
            nodeStates.remove(node1);
        node.getTree().setupNodeStates(this);
    }

    /**
     * Returns the icon the node should have.
     */
    public Icon getIcon(SkillTreeNode node) {
        SkillTree skillTree = node.getTree();

        DisplayInfo displayInfo = new DisplayInfo(nodeStates.get(node), node.getSize());

        return skillTree.getIcon(displayInfo);
    }

    public Icon getIcon(SkillTree skillTree, IntegerCoordinates coordinates) {

        if (skillTree.isNode(coordinates)) {
            SkillTreeNode node = skillTree.getNode(coordinates);
            DisplayInfo displayInfo = new DisplayInfo(nodeStates.get(node), node.getSize());
            return skillTree.getIcon(displayInfo);
        }
        if (skillTree.isPath(coordinates)) return skillTree.getIcon(DisplayInfo.pathInfo);
        return null;
    }

    public int getSkillTreePoint(String treeId) {
        return skillTreePoints.getOrDefault(treeId, 0);
    }

    public void withdrawSkillTreePoints(String treeId, int withdraw) {
        skillTreePoints.put(treeId, skillTreePoints.get(treeId) - withdraw);
    }

    public void setNodeState(SkillTreeNode node, NodeStatus nodeStatus) {
        nodeStates.put(node, nodeStatus);
    }

    public NodeStatus getNodeState(SkillTreeNode node) {
        return nodeStates.get(node);
    }

    public boolean hasNodeState(SkillTreeNode node) {
        return nodeStates.containsKey(node);
    }

    public int getNodeLevel(SkillTreeNode node) {
        return nodeLevels.getOrDefault(node, 0);
    }

    public void setNodeLevel(SkillTreeNode node, int nodeLevel) {
        int delta = (nodeLevel - nodeLevels.getOrDefault(node, 0)) * node.getSkillTreePointsConsumed();
        pointSpent.put(node.getTree(), pointSpent.getOrDefault(node.getTree(), 0) + delta);
        nodeLevels.put(node, nodeLevel);
    }

    public void resetSkillTree(SkillTree skillTree) {
        for (SkillTreeNode node : skillTree.getNodes()) {
            node.getExperienceTable().reset(this, node);
            setNodeLevel(node, 0);
            nodeStates.remove(node);
        }
        skillTree.setupNodeStates(this);
    }

    public Map<SkillTreeNode, NodeStatus> getNodeStates() {
        return new HashMap<>(nodeStates);
    }

    public void clearNodeStates() {
        nodeStates.clear();
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
        // Call the event synchronously
        if (wasLocked)
            Bukkit.getScheduler().runTask(MythicLib.plugin, () -> Bukkit.getPluginManager().callEvent(new ItemUnlockedEvent(this, unlockable.getUnlockNamespacedKey())));
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
        if (wasUnlocked)
            //Calls the event synchronously
            Bukkit.getScheduler().runTask(MythicLib.plugin, () -> Bukkit.getPluginManager().callEvent(new ItemLockedEvent(this, unlockable.getUnlockNamespacedKey())));
        return wasUnlocked;
    }

    public Set<String> getUnlockedItems() {
        return new HashSet<>(unlockedItems);
    }

    public void setUnlockedItems(Set<String> unlockedItems) {
        this.unlockedItems.clear();
        this.unlockedItems.addAll(unlockedItems);
    }

    public void resetTimesClaimed() {
        tableItemClaims.clear();
    }

    @Override
    public void close() {

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

        // Stop skill casting
        if (isCasting()) leaveSkillCasting();
    }

    public MMOPlayerData getMMOPlayerData() {
        return mmoData;
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

    public Player getPlayer() {
        return mmoData.getPlayer();
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
        return mmoData.getLastLogActivity();
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

    @Deprecated
    public List<ClassSkill> getUnlockedSkills() {
        return getProfess().getSkills().stream()
                .filter((classSkill) -> hasUnlocked(classSkill))
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

    @Override
    public int getClaims(ExperienceObject object, ExperienceTable table, ExperienceItem item) {
        return getClaims(object.getKey() + "." + table.getId() + "." + item.getId());
    }

    public int getClaims(String key) {
        return tableItemClaims.getOrDefault(key, 0);
    }

    @Override
    public void setClaims(ExperienceObject object, ExperienceTable table, ExperienceItem item, int times) {
        setClaims(object.getKey() + "." + table.getId() + "." + item.getId(), times);
    }

    public void setClaims(String key, int times) {
        tableItemClaims.put(key, times);
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
        return mmoData.isOnline();
    }

    public boolean inGuild() {
        return guild != null;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);

        if (isOnline()) {
            getStats().updateStats();
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
        classSlots.remove(profess.getId());
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
        double max = getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
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
        new ConfigMessage("friend-request").addPlaceholders("player", getPlayer().getName(), "uuid", request.getUniqueId().toString()).sendAsJSon(target.getPlayer());
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
            int t;

            public void run() {
                if (!isOnline() || getPlayer().getLocation().getBlockX() != x || getPlayer().getLocation().getBlockY() != y || getPlayer().getLocation().getBlockZ() != z) {
                    MMOCore.plugin.soundManager.getSound(SoundEvent.WARP_CANCELLED).playTo(getPlayer());
                    MMOCore.plugin.configManager.getSimpleMessage("warping-canceled").send(getPlayer());
                    giveStellium(cost, PlayerResourceUpdateEvent.UpdateReason.USE_WAYPOINT);
                    cancel();
                    return;
                }

                MMOCore.plugin.configManager.getSimpleMessage("warping-comencing", "left", String.valueOf((warpTime - t) / 20)).send(getPlayer());
                if (t++ >= warpTime) {
                    getPlayer().teleport(target.getLocation());
                    getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1, false, false));
                    MMOCore.plugin.soundManager.getSound(SoundEvent.WARP_TELEPORT).playTo(getPlayer());
                    cancel();
                    return;
                }

                MMOCore.plugin.soundManager.getSound(SoundEvent.WARP_CHARGE).playTo(getPlayer(), 1, (float) (.5 + t * 1.5 / warpTime));
                final double r = Math.sin((double) t / warpTime * Math.PI);
                for (double j = 0; j < Math.PI * 2; j += Math.PI / 4)
                    getPlayer().getLocation().getWorld().spawnParticle(Particle.REDSTONE, getPlayer().getLocation().add(Math.cos((double) 5 * t / warpTime + j) * r, (double) 2 * t / warpTime, Math.sin((double) 5 * t / warpTime + j) * r), 1, new Particle.DustOptions(Color.PURPLE, 1.25f));
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
    public void giveExperience(double value, @NotNull EXPSource source, @Nullable Location hologramLocation, boolean splitExp) {
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
            MMOCoreUtils.displayIndicator(hologramLocation, MMOCore.plugin.configManager.getSimpleMessage("exp-hologram", "exp", MythicLib.plugin.getMMOConfig().decimal.format(event.getExperience())).message());

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
            if (getProfess().hasExperienceTable()) getProfess().getExperienceTable().claim(this, level, getProfess());
        }

        if (level > oldLevel) {
            Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(this, null, oldLevel, level));
            if (isOnline()) {
                new ConfigMessage("level-up").addPlaceholders("level", String.valueOf(level)).send(getPlayer());
                MMOCore.plugin.soundManager.getSound(SoundEvent.LEVEL_UP).playTo(getPlayer());
                new SmallParticleEffect(getPlayer(), Particle.SPELL_INSTANT);
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

    public boolean isFullyLoaded() {
        return fullyLoaded;
    }

    public void setFullyLoaded() {
        this.fullyLoaded = true;
    }

    public boolean isCasting() {
        return skillCasting != null;
    }

    public void setSkillCasting(SkillCastingHandler skillCasting) {
        Validate.isTrue(!isCasting(), "Player already in casting mode");
        this.skillCasting = skillCasting;
    }

    @NotNull
    public SkillCastingHandler getSkillCasting() {
        return Objects.requireNonNull(skillCasting, "Player not in casting mode");
    }

    public void leaveSkillCasting() {
        Validate.isTrue(isCasting(), "Player not in casting mode");
        skillCasting.close();
        this.skillCasting = null;
        setLastActivity(PlayerActivity.ACTION_BAR_MESSAGE, 0); // Reset action bar
    }

    public void displayActionBar(String message) {
        setLastActivity(PlayerActivity.ACTION_BAR_MESSAGE);
        getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    @Deprecated
    public void setAttribute(PlayerAttribute attribute, int value) {
        setAttribute(attribute.getId(), value);
    }

    @Deprecated
    public void setAttribute(String id, int value) {
        attributes.setBaseAttribute(id, value);
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
        refreshBoundSkill(skill);
    }

    public void resetSkillLevel(String skill) {
        skills.remove(skill);
        refreshBoundSkill(skill);
    }

    public void refreshBoundSkill(String skill) {
        boundSkills.values().stream().filter(skillInfo -> skillInfo.getClassSkill().getSkill().getHandler().getId().equals(skill)).forEach(BoundSkillInfo::refresh);
    }

    @Deprecated
    public boolean hasSkillUnlocked(RegisteredSkill skill) {
        return getProfess().hasSkill(skill.getHandler().getId()) && hasSkillUnlocked(getProfess().getSkill(skill.getHandler().getId()));
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
    public boolean hasSkillUnlocked(ClassSkill skill) {
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
        return mmoData.getCooldownMap();
    }

    public void setClass(@Nullable PlayerClass profess) {
        this.profess = profess;

        // Clear old skills
        for (Iterator<BoundSkillInfo> iterator = boundSkills.values().iterator(); iterator.hasNext(); )
            if (!getProfess().hasSkill(iterator.next().getClassSkill().getSkill())) iterator.remove();

        // Update stats
        if (isOnline()) getStats().updateStats();
    }

    public boolean hasSkillBound(int slot) {
        return boundSkills.containsKey(slot);
    }

    @Nullable
    public ClassSkill getBoundSkill(int slot) {
        return boundSkills.containsKey(slot) ? boundSkills.get(slot).getClassSkill() : null;
    }

    @Deprecated
    public void setBoundSkill(int slot, ClassSkill skill) {
        bindSkill(slot, skill);
    }

    /**
     * Binds a skill to the player.
     *
     * @param slot  Slot to which you're binding the skill.
     *              Use -1 to force-register the skill
     * @param skill Skill being bound
     */
    public void bindSkill(int slot, ClassSkill skill) {
        Validate.notNull(skill, "Skill cannot be null");

        // Unbinds the previous skill (Important for passive skills.
        if (boundSkills.containsKey(slot)) boundSkills.get(slot).unbind();

        if (slot >= 0) {

            // We apply the skill buffs associated with the slot to the skill.
            for (SkillModifierTrigger skillBuffTrigger : profess.getSkillSlot(slot).getSkillBuffTriggers())
                if (skillBuffTrigger.getTargetSkills().contains(skill.getSkill().getHandler()))
                    skillBuffTrigger.apply(this, skill.getSkill().getHandler());

            boundSkills.put(slot, new BoundSkillInfo(skill, this));
        }
    }

    public void unbindSkill(int slot) {

        // We remove the skill buffs associated with the slot from the skill that is .
        profess.getSkillSlot(slot).getSkillBuffTriggers().forEach(skillBuffTrigger -> skillBuffTrigger.remove(this, boundSkills.get(slot).getClassSkill().getSkill().getHandler()));

        BoundSkillInfo boundSkillInfo = boundSkills.remove(slot);
        boundSkillInfo.unbind();
    }


    public List<ClassSkill> getBoundSkills() {
        return boundSkills.values().stream().map(BoundSkillInfo::getClassSkill).collect(Collectors.toList());
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
        return getUniqueId().equals(that.mmoData.getUniqueId());
    }

    @Override
    public int hashCode() {
        return mmoData.hashCode();
    }

    public static PlayerData get(OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    public static PlayerData get(UUID uuid) {
        return MMOCore.plugin.dataProvider.getDataManager().get(uuid);
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
        return MMOCore.plugin.dataProvider.getDataManager().isLoaded(uuid);
    }

    public static Collection<PlayerData> getAll() {
        return MMOCore.plugin.dataProvider.getDataManager().getLoaded();
    }
}
