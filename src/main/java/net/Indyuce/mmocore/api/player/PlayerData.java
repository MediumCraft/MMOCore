package net.Indyuce.mmocore.api.player;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.TemporaryPlayerData;
import io.lumine.mythic.lib.player.cooldown.CooldownMap;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.player.Unlockable;
import net.Indyuce.mmocore.waypoint.CostType;
import net.Indyuce.mmocore.waypoint.Waypoint;
import net.Indyuce.mmocore.api.event.PlayerExperienceGainEvent;
import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttributes;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.api.player.profess.Subclass;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.api.player.social.FriendRequest;
import net.Indyuce.mmocore.api.player.stats.PlayerStats;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.quest.PlayerQuests;
import net.Indyuce.mmocore.api.util.Closable;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.loot.chest.particle.SmallParticleEffect;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.ExperienceObject;
import net.Indyuce.mmocore.experience.ExperienceTableClaimer;
import net.Indyuce.mmocore.experience.PlayerProfessions;
import net.Indyuce.mmocore.experience.droptable.ExperienceItem;
import net.Indyuce.mmocore.experience.droptable.ExperienceTable;
import net.Indyuce.mmocore.guild.provided.Guild;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.party.provided.Party;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.skill.cast.SkillCastingHandler;
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


public class PlayerData extends OfflinePlayerData implements Closable, ExperienceTableClaimer {

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
    private int level, experience, classPoints, skillPoints, attributePoints, attributeReallocationPoints;// skillReallocationPoints,
    private double mana, stamina, stellium;
    private Guild guild;
    private SkillCastingHandler skillCasting;

    private final PlayerQuests questData;
    private final PlayerStats playerStats;
    private final List<UUID> friends = new ArrayList<>();
    private final Set<String> waypoints = new HashSet<>();
    private final Map<String, Integer> skills = new HashMap<>();
    private final List<ClassSkill> boundSkills = new ArrayList<>();
    private final PlayerProfessions collectSkills = new PlayerProfessions(this);
    private final PlayerAttributes attributes = new PlayerAttributes(this);
    private final Map<String, SavedClassInformation> classSlots = new HashMap<>();
    private final Map<PlayerActivity, Long> lastActivity = new HashMap<>();

    /**
     * Saves all the items that have been unlocked so far by
     * the player. This can be used by other plugins by
     * implementing the {@link Unlockable} interface
     *
     * @see {@link Unlockable}
     */
    private final Set<String> unlockedItems = new HashSet<>();

    /**
     * Saves the amount of times the player has claimed some
     * exp item in exp tables, for both exp tables used
     */
    private final Map<String, Integer> tableItemClaims = new HashMap<>();

    // NON-FINAL player data stuff made public to facilitate field change
    public int skillGuiDisplayOffset;
    public boolean noCooldown;
    public CombatRunnable combat;

    /**
     * Player data is stored in the data map before it's actually fully loaded
     * so that external plugins don't necessarily have to listen to the PlayerDataLoadEvent.
     */
    private boolean fullyLoaded = false;

    /**
     * If the player data was loaded using temporary data.
     * See {@link TemporaryPlayerData} for more info
     */
    private final boolean usingTemporaryData;

    public PlayerData(MMOPlayerData mmoData) {
        this(mmoData, false);
    }

    public PlayerData(MMOPlayerData mmoData, TemporaryPlayerData tempData) {
        this(mmoData, true);

        mana = tempData.getDouble("mana");
        stamina = tempData.getDouble("stamina");
        stellium = tempData.getDouble("stellium");
    }

    private PlayerData(MMOPlayerData mmoData, boolean usingTemporaryData) {
        super(mmoData.getUniqueId());

        this.mmoData = mmoData;
        this.playerStats = new PlayerStats(this);
        this.questData = new PlayerQuests(this);
        this.usingTemporaryData = usingTemporaryData;
    }

    /**
     * Update all references after /mmocore reload so there can be garbage
     * collection with old plugin objects like class or skill instances.
     * <p>
     * It's OK if bound skills disappear because of a configuration issue
     * on the user's end. After all this method is only called when using
     * /reload and /reload is considered a bad practice. If any error
     * happens then just don't update the player's skill.
     */
    public void update() {

        try {
            profess = profess == null ? null : MMOCore.plugin.classManager.get(profess.getId());
            getStats().updateStats();
        } catch (NullPointerException exception) {
            MMOCore.log(Level.SEVERE, "[Userdata] Could not find class " + getProfess().getId() + " while refreshing player data.");
        }

        int j = 0;
        while (j < boundSkills.size())
            try {
                boundSkills.set(j, Objects.requireNonNull(getProfess().getSkill(boundSkills.get(j).getSkill())));
            } catch (Exception ignored) {
            } finally {
                j++;
            }
    }

    @Override
    public void close() {

        // Remove from party
        AbstractParty party = getParty();
        if (party != null && party instanceof Party)
            ((Party) party).removeMember(this);

        // Close quest data
        questData.close();
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

    public int getSkillPoints() {
        return skillPoints;
    }

    @Override
    public int getClaims(ExperienceObject object, ExperienceTable table, ExperienceItem item) {
        String key = object.getKey() + "." + table.getId() + "." + item.getId();
        return tableItemClaims.getOrDefault(key, 0);
    }

    @Override
    public void setClaims(ExperienceObject object, ExperienceTable table, ExperienceItem item, int times) {
        String key = object.getKey() + "." + table.getId() + "." + item.getId();
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

    // public int getSkillReallocationPoints() {
    // return skillReallocationPoints;
    // }

    public int getAttributePoints() {
        return attributePoints;
    }

    public int getAttributeReallocationPoints() {
        return attributeReallocationPoints;
    }

    public boolean isOnline() {
        return mmoData.isOnline();
    }

    public boolean inGuild() {
        return guild != null;
    }

    public boolean hasUnlocked(Unlockable unlockable) {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Unlocks an item for the player
     *
     * @return If the item was already unlocked when calling this method
     */
    public boolean unlock(Unlockable unlockable) {
        throw new RuntimeException("Not implemented yet");
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);

        getStats().updateStats();
        refreshVanillaExp();
    }

    public void takeLevels(int value) {
        setLevel(level - value);
    }

    public void giveLevels(int value, EXPSource source) {
        int total = 0;
        while (value-- > 0)
            total += getProfess().getExpCurve().getExperience(getLevel() + value + 1);
        giveExperience(total, source);
    }

    public void setExperience(int value) {
        experience = Math.max(0, value);
        refreshVanillaExp();
    }

    /**
     * Class experience can be displayed on the player's exp bar.
     * This updates the exp bar to display the player class level and exp.
     */
    public void refreshVanillaExp() {
        if (!isOnline() || !MMOCore.plugin.configManager.overrideVanillaExp)
            return;

        getPlayer().setLevel(getLevel());
        getPlayer().setExp(Math.max(0, Math.min(1, (float) experience / (float) getLevelUpExperience())));
    }

    // public void setSkillReallocationPoints(int value) {
    // skillReallocationPoints = Math.max(0, value);
    // }

    public void setAttributePoints(int value) {
        attributePoints = Math.max(0, value);
    }

    public void setAttributeReallocationPoints(int value) {
        attributeReallocationPoints = Math.max(0, value);
    }

    public void setSkillPoints(int value) {
        skillPoints = Math.max(0, value);
    }

    public void setClassPoints(int value) {
        classPoints = Math.max(0, value);
    }

    public boolean hasSavedClass(PlayerClass profess) {
        return classSlots.containsKey(profess.getId());
    }

    public Set<String> getSavedClasses() {
        return classSlots.keySet();
    }

    public SavedClassInformation getClassInfo(PlayerClass profess) {
        return getClassInfo(profess.getId());
    }

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

    /**
     * @deprecated Provide a heal reason with {@link #heal(double, PlayerResourceUpdateEvent.UpdateReason)}
     */
    @Deprecated
    public void heal(double heal) {
        this.heal(heal, PlayerResourceUpdateEvent.UpdateReason.OTHER);
    }

    public void heal(double heal, PlayerResourceUpdateEvent.UpdateReason reason) {
        if (!isOnline())
            return;

        // Avoid calling an useless event
        double max = getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double newest = Math.max(0, Math.min(getPlayer().getHealth() + heal, max));
        if (getPlayer().getHealth() == newest)
            return;

        PlayerResourceUpdateEvent event = new PlayerResourceUpdateEvent(this, PlayerResource.HEALTH, heal, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

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
        if (!isOnline() || !target.isOnline())
            return;

        setLastActivity(PlayerActivity.FRIEND_REQUEST);
        FriendRequest request = new FriendRequest(this, target);
        new ConfigMessage("friend-request").addPlaceholders("player", getPlayer().getName(), "uuid", request.getUniqueId().toString())
                .sendAsJSon(target.getPlayer());
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
            int t;

            public void run() {
                if (!isOnline())
                    return;
                if (getPlayer().getLocation().getBlockX() != x || getPlayer().getLocation().getBlockY() != y
                        || getPlayer().getLocation().getBlockZ() != z) {
                    MMOCore.plugin.soundManager.getSound(SoundEvent.WARP_CANCELLED).playTo(getPlayer());
                    MMOCore.plugin.configManager.getSimpleMessage("warping-canceled").send(getPlayer());
                    giveStellium(cost, PlayerResourceUpdateEvent.UpdateReason.USE_WAYPOINT);
                    cancel();
                    return;
                }

                MMOCore.plugin.configManager.getSimpleMessage("warping-comencing", "left", "" + ((120 - t) / 20)).send(getPlayer());
                if (t++ >= 100) {
                    getPlayer().teleport(target.getLocation());
                    getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1, false, false));
                    MMOCore.plugin.soundManager.getSound(SoundEvent.WARP_TELEPORT).playTo(getPlayer());
                    cancel();
                    return;
                }

                MMOCore.plugin.soundManager.getSound(SoundEvent.WARP_CHARGE).playTo(getPlayer(), 1, (float) (t / Math.PI * .015 + .5));
                double r = Math.sin((double) t / 100 * Math.PI);
                for (double j = 0; j < Math.PI * 2; j += Math.PI / 4)
                    getPlayer().getLocation().getWorld().spawnParticle(Particle.REDSTONE,
                            getPlayer().getLocation().add(Math.cos((double) t / 20 + j) * r, (double) t / 50, Math.sin((double) t / 20 + j) * r), 1,
                            new Particle.DustOptions(Color.PURPLE, 1.25f));
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
    public void giveExperience(int value, EXPSource source) {
        giveExperience(value, source, null, true);
    }

    /**
     * Called when giving experience to a player
     *
     * @param value            Experience to give the player
     * @param source           How the player earned experience
     * @param hologramLocation Location used to display the hologram. If it's null, no
     *                         hologram will be displayed
     * @param splitExp         Should the exp be split among party members
     */
    public void giveExperience(double value, EXPSource source, @Nullable Location hologramLocation, boolean splitExp) {
        if (hasReachedMaxLevel()) {
            setExperience(0);
            return;
        }

        value = MMOCore.plugin.boosterManager.calculateExp(null, value);
        value *= 1 + getStats().getStat(StatType.ADDITIONAL_EXPERIENCE) / 100;

        // Splitting exp through party members
        AbstractParty party = getParty();
        if (splitExp && party != null) {
            List<PlayerData> onlineMembers = getParty().getOnlineMembers();
            value /= onlineMembers.size();
            for (PlayerData member : onlineMembers)
                member.giveExperience(value, EXPSource.PARTY_SHARING, null, false);
        }

        PlayerExperienceGainEvent event = new PlayerExperienceGainEvent(this, (int) value, source);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        // Experience hologram
        if (hologramLocation != null && isOnline())
            MMOCoreUtils.displayIndicator(hologramLocation, MMOCore.plugin.configManager.getSimpleMessage("exp-hologram", "exp", String.valueOf(event.getExperience())).message());

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
        }

        if (level > oldLevel) {
            Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(this, null, oldLevel, level));
            if (isOnline()) {
                new ConfigMessage("level-up").addPlaceholders("level", "" + level).send(getPlayer());
                MMOCore.plugin.soundManager.getSound(SoundEvent.LEVEL_UP).playTo(getPlayer());
                new SmallParticleEffect(getPlayer(), Particle.SPELL_INSTANT);
            }
            getStats().updateStats();

            // Apply class experience table
            if (getProfess().hasExperienceTable())
                getProfess().getExperienceTable().claim(this, level, getProfess());
        }

        refreshVanillaExp();
    }

    public int getExperience() {
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
        double max = getStats().getStat(StatType.MAX_MANA);
        double newest = Math.max(0, Math.min(mana + amount, max));
        if (mana == newest)
            return;

        PlayerResourceUpdateEvent event = new PlayerResourceUpdateEvent(this, PlayerResource.MANA, amount, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

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
        double max = getStats().getStat(StatType.MAX_STAMINA);
        double newest = Math.max(0, Math.min(stamina + amount, max));
        if (stamina == newest)
            return;

        PlayerResourceUpdateEvent event = new PlayerResourceUpdateEvent(this, PlayerResource.STAMINA, amount, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

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
        double max = getStats().getStat(StatType.MAX_STELLIUM);
        double newest = Math.max(0, Math.min(stellium + amount, max));
        if (stellium == newest)
            return;

        PlayerResourceUpdateEvent event = new PlayerResourceUpdateEvent(this, PlayerResource.STELLIUM, amount, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        // Use updated amount from Bukkit event
        stellium = Math.max(0, Math.min(stellium + event.getAmount(), max));
    }

    public double getMana() {
        return mana;
    }

    public double getStamina() {
        return stamina;
    }

    public double getStellium() {
        return stellium;
    }

    public PlayerStats getStats() {
        return playerStats;
    }

    public PlayerAttributes getAttributes() {
        return attributes;
    }

    public void setMana(double amount) {
        mana = Math.max(0, Math.min(amount, getStats().getStat(StatType.MAX_MANA)));
    }

    public void setStamina(double amount) {
        stamina = Math.max(0, Math.min(amount, getStats().getStat(StatType.MAX_STAMINA)));
    }

    public void setStellium(double amount) {
        stellium = Math.max(0, Math.min(amount, getStats().getStat(StatType.MAX_STELLIUM)));
    }

    public boolean isFullyLoaded() {
        return fullyLoaded;
    }

    public void setFullyLoaded() {
        this.fullyLoaded = true;
    }

    public boolean hasUsedTemporaryData() {
        return usingTemporaryData;
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

    public void leaveCastingMode() {
        Validate.isTrue(isCasting(), "Player not in casting mode");
        skillCasting.close();
        this.skillCasting = null;
    }

    public void displayActionBar(String message) {
        if (!isOnline())
            return;

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

    @Deprecated
    public Map<String, Integer> mapAttributePoints() {
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

    public CooldownMap getCooldownMap() {
        return mmoData.getCooldownMap();
    }

    public void setClass(PlayerClass profess) {
        this.profess = profess;

        // Clear old skills
        for (Iterator<ClassSkill> iterator = boundSkills.iterator(); iterator.hasNext(); )
            if (!getProfess().hasSkill(iterator.next().getSkill()))
                iterator.remove();

        // Update stats
        getStats().updateStats();
    }

    public boolean hasSkillBound(int slot) {
        return slot < boundSkills.size();
    }

    public ClassSkill getBoundSkill(int slot) {
        return slot >= boundSkills.size() ? null : boundSkills.get(slot);
    }

    public void setBoundSkill(int slot, ClassSkill skill) {
        Validate.notNull(skill, "Skill cannot be null");
        if (boundSkills.size() < 6)
            boundSkills.add(skill);
        else
            boundSkills.set(slot, skill);
    }

    public void unbindSkill(int slot) {
        boundSkills.remove(slot);
    }

    public List<ClassSkill> getBoundSkills() {
        return boundSkills;
    }

    public boolean isInCombat() {
        return combat != null;
    }

    /**
     * Loops through all the subclasses available to the player and
     * checks if they could potentially upgrade to one of these
     *
     * @return If the player can change its current class to
     * a subclass
     */
    public boolean canChooseSubclass() {
        for (Subclass subclass : getProfess().getSubclasses())
            if (getLevel() >= subclass.getLevel())
                return true;
        return false;
    }

    /**
     * Everytime a player does a combat action, like taking
     * or dealing damage to an entity, this method is called.
     */
    public void updateCombat() {
        if (isInCombat())
            combat.update();
        else
            combat = new CombatRunnable(this);
    }

    @Override
    public int hashCode() {
        return mmoData.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlayerData && ((PlayerData) obj).getUniqueId().equals(getUniqueId());
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
