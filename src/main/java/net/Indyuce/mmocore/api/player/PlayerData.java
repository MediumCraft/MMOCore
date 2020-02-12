package net.Indyuce.mmocore.api.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import net.Indyuce.mmoitems.MMOItems;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.Waypoint;
import net.Indyuce.mmocore.api.event.PlayerCastSkillEvent;
import net.Indyuce.mmocore.api.event.PlayerExperienceGainEvent;
import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import net.Indyuce.mmocore.api.math.particle.SmallParticleEffect;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttributes;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.PlayerClass.Subclass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.api.player.social.FriendRequest;
import net.Indyuce.mmocore.api.player.social.Party;
import net.Indyuce.mmocore.api.player.social.guilds.Guild;
import net.Indyuce.mmocore.api.player.stats.PlayerStats;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.skill.Skill.SkillInfo;
import net.Indyuce.mmocore.api.skill.SkillResult;
import net.Indyuce.mmocore.api.skill.SkillResult.CancelReason;
import net.Indyuce.mmocore.listener.SpellCast.SkillCasting;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.version.VersionSound;

public class PlayerData {

	private final UUID uuid;

	/*
	 * is updated everytime the player joins the server. it is kept when the
	 * player is offline so the plugin can use #isOnline to check if the player
	 * is online
	 */
	private Player player;

	private PlayerClass profess;
	private final Map<String, SavedClassInformation> classSlots = new HashMap<>();
	private int level, experience, classPoints, skillPoints, attributePoints, attributeReallocationPoints;// skillReallocationPoints,
	private double mana, stamina, stellium;
	private final Professions collectSkills = new Professions(this);
	private final PlayerQuests questData;
	private final Set<String> waypoints = new HashSet<>();
	private List<UUID> friends;
	private Party party;
	private Guild guild;
	private final List<SkillInfo> boundSkills = new ArrayList<>();
	private final PlayerAttributes attributes = new PlayerAttributes(this);

	private final PlayerStats playerStats;
	private long lastWaypoint, lastLogin, lastFriendRequest, actionBarTimeOut;

	private final Map<String, Integer> skills = new HashMap<>();
	private final PlayerSkillData skillData = new PlayerSkillData(this);

	/*
	 * NON-FINAL player data stuff made public to facilitate field change
	 */
	public int skillGuiDisplayOffset;
	public SkillCasting skillCasting;
	public boolean nocd;
	public CombatRunnable combat;

	private static Map<UUID, PlayerData> playerData = new HashMap<>();

	private PlayerData(Player player) {
		uuid = player.getUniqueId();
		setPlayer(player);
		playerStats = new PlayerStats(this);

		questData = new PlayerQuests(this);
	}

	public PlayerData load(FileConfiguration config) {
		this.classPoints = config.getInt("class-points");
		this.skillPoints = config.getInt("skill-points");
		this.attributePoints = config.getInt("attribute-points");
		// this.skillReallocationPoints = config.getInt("skill-realloc-points");
		this.attributeReallocationPoints = config.getInt("attribute-realloc-points");
		this.level = config.getInt("level");
		this.experience = config.getInt("experience");
		this.profess = config.contains("class") ? MMOCore.plugin.classManager.get(config.getString("class")) : MMOCore.plugin.classManager.getDefaultClass();
		this.mana = getStats().getStat(StatType.MAX_MANA);
		this.stamina = getStats().getStat(StatType.MAX_STAMINA);
		this.stellium = getStats().getStat(StatType.MAX_STELLIUM);
		if (config.contains("guild")) this.guild = MMOCore.plugin.guildManager.stillInGuild(getUniqueId(), config.getString("guild"));
		if (config.contains("attribute"))
			attributes.load(config.getConfigurationSection("attribute"));
		if (config.contains("profession"))
			collectSkills.load(config.getConfigurationSection("profession"));
		if (config.contains("quest"))
			questData.load(config.getConfigurationSection("quest"));
		questData.updateBossBar();
		if (config.contains("waypoints"))
			waypoints.addAll(config.getStringList("waypoints"));
		MMOCore.plugin.waypointManager.getDefault().forEach(waypoint -> waypoints.add(waypoint.getId()));
		this.friends = config.contains("friends") ? config.getStringList("friends").stream().map((str) -> UUID.fromString(str)).collect(Collectors.toList()) : new ArrayList<>();
		if (config.contains("skill"))
			config.getConfigurationSection("skill").getKeys(false).forEach(id -> skills.put(id, config.getInt("skill." + id)));
		if (config.contains("bound-skills"))
			for (String id : config.getStringList("bound-skills"))
				if (MMOCore.plugin.skillManager.has(id))
					boundSkills.add(getProfess().getSkill(id));

		/*
		 * load class slots, use try so the player can log in.
		 */
		if (config.contains("class-info"))
			for (String key : config.getConfigurationSection("class-info").getKeys(false))
				try {
					PlayerClass profess = MMOCore.plugin.classManager.get(key);
					Validate.notNull(profess, "Could not find class '" + key + "'");
					applyClassInfo(profess, new SavedClassInformation(config.getConfigurationSection("class-info." + key)));
				} catch (IllegalArgumentException exception) {
					log(Level.SEVERE, "Could not load class info " + key + ": " + exception.getMessage());
				}

		return this;
	}

	public void saveInConfig(FileConfiguration config) {
		config.set("class-points", classPoints);
		config.set("skill-points", skillPoints);
		config.set("attribute-points", attributePoints);
		// config.set("skill-realloc-points", skillReallocationPoints);
		config.set("attribute-realloc-points", attributeReallocationPoints);
		config.set("level", getLevel());
		config.set("experience", experience);
		config.set("class", profess == null ? null : profess.getId());
		config.set("waypoints", new ArrayList<>(waypoints));
		config.set("friends", toStringList(friends));
		config.set("last-login", lastLogin);
		if(guild != null) config.set("guild", guild.getId());
		else config.set("guild", null);
		
		config.set("skill", null);
		skills.entrySet().forEach(entry -> config.set("skill." + entry.getKey(), entry.getValue()));

		List<String> boundSkills = new ArrayList<>();
		this.boundSkills.forEach(skill -> boundSkills.add(skill.getSkill().getId()));
		config.set("bound-skills", boundSkills);

		config.set("attribute", null);
		config.createSection("attribute");
		attributes.save(config.getConfigurationSection("attribute"));

		config.set("profession", null);
		config.createSection("profession");
		collectSkills.save(config.getConfigurationSection("profession"));

		config.set("quest", null);
		config.createSection("quest");
		questData.save(config.getConfigurationSection("quest"));

		config.set("class-info", null);
		for (String key : classSlots.keySet()) {
			SavedClassInformation info = classSlots.get(key);
			config.set("class-info." + key + ".level", info.getLevel());
			config.set("class-info." + key + ".experience", info.getExperience());
			config.set("class-info." + key + ".skill-points", info.getSkillPoints());
			config.set("class-info." + key + ".attribute-points", info.getAttributePoints());
			config.set("class-info." + key + ".attribute-realloc-points", info.getAttributeReallocationPoints());
			info.getSkillKeys().forEach(skill -> config.set("class-info." + key + ".skill." + skill, info.getSkillLevel(skill)));
			info.getAttributeKeys().forEach(attribute -> config.set("class-info." + key + ".attribute." + attribute, info.getAttributeLevel(attribute)));
		}
	}

	/*
	 * update all references after /mmocore reload so there can be garbage
	 * collection with old plugin objects like class or skill instances.
	 */
	public void update() {

		try {
			profess = profess == null ? null : MMOCore.plugin.classManager.get(profess.getId());
		} catch (NullPointerException exception) {
			MMOCore.log(Level.SEVERE, "[Userdata] Could not find class " + getProfess().getId() + " while refreshing player data.");
		}

		int j = 0;
		while (j < boundSkills.size())
			try {
				boundSkills.set(j, getProfess().getSkill(boundSkills.get(j).getSkill()));
				j++;
			} catch (NullPointerException notFound) {
				boundSkills.remove(j);
				MMOCore.log(Level.SEVERE, "[Userdata] Could not find skill " + boundSkills.get(j).getSkill().getId() + " in class " + getProfess().getId() + " while refreshing player data.");
			}
	}

	public static PlayerData get(OfflinePlayer player) {
		return get(player.getUniqueId());
	}

	public static PlayerData get(UUID uuid) {
		return playerData.get(uuid);
	}

	public static void remove(Player player) {
		playerData.remove(player.getUniqueId());
	}

	public static PlayerData setup(Player player) {
		if (!playerData.containsKey(player.getUniqueId()))
			playerData.put(player.getUniqueId(), new PlayerData(player).load(new ConfigFile(player).getConfig()));
		return get(player).setPlayer(player);
	}

	public static boolean isLoaded(UUID uuid) {
		return playerData.containsKey(uuid);
	}

	public static Collection<PlayerData> getAll() {
		return playerData.values();
	}

	/**
	 * START OF EXPERIMENTAL CODE
	 * 
	 * This must be more simple to do than my 2AM brain could think of...
	 * - Aria
	 */
	
	private static Map<UUID, PlayerDataOfflineValues> offlineValues = new HashMap<>();
	public static PlayerDataOfflineValues getOfflineValues(UUID uuid) {
		if(!offlineValues.containsKey(uuid))
			offlineValues.put(uuid, new PlayerDataOfflineValues(uuid));
		return offlineValues.get(uuid) ;
	}
	
	public static class PlayerDataOfflineValues {
		// Values can be added as they are needed
		private final PlayerClass profess;
		private final int level;
		private final long lastLogin;
		
		public PlayerDataOfflineValues(UUID uuid) {
			FileConfiguration config = new ConfigFile(uuid).getConfig();
			this.profess = MMOCore.plugin.classManager.get(config.getString("class"));
			this.level = config.getInt("level");
			this.lastLogin = config.getLong("last-login");
		}
		
		public PlayerClass getProfess()
		{ return profess; }
		public int getLevel()
		{ return level; }
		public long getLastLogin()
		{ return lastLogin; }
	}
	/**
	 * END OF EXPERIMENTAL CODE
	 */
	
	private PlayerData setPlayer(Player player) {
		this.player = player;
		this.lastLogin = System.currentTimeMillis();
		return this;
	}

	public List<UUID> getFriends() {
		return friends;
	}

	public Professions getCollectionSkills() {
		return collectSkills;
	}

	public PlayerQuests getQuestData() {
		return questData;
	}

	public Player getPlayer() {
		return player;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public long getLastLogin() {
		return lastLogin;
	}

	public long getLastFriendRequest() {
		return lastFriendRequest;
	}

	public int getLevel() {
		return Math.max(1, level);
	}

	public Party getParty() {
		return party;
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

	// public int getSkillReallocationPoints() {
	// return skillReallocationPoints;
	// }

	public int getAttributePoints() {
		return attributePoints;
	}

	public int getAttributeReallocationPoints() {
		return attributeReallocationPoints;
	}

	public boolean hasParty() {
		return party != null;
	}

	public boolean inGuild() {
		return guild != null;
	}

	public boolean isOnline() {
		return player.isOnline();
	}

	public void setLevel(int level) {
		this.level = Math.max(1, level);
		getStats().getMap().updateAll();
	}

	public void giveLevels(int value) {
		int total = 0;
		while (value-- > 0)
			total += MMOCore.plugin.configManager.getNeededExperience(getLevel() + value + 1);
		giveExperience(total);
	}

	public void setExperience(int value) {
		experience = Math.max(0, value);
		refreshVanillaExp(MMOCore.plugin.configManager.getNeededExperience(getLevel() + 1));
	}

	public void refreshVanillaExp(float needed) {
		if (MMOCore.plugin.configManager.overrideVanillaExp) {
			player.setLevel(getLevel());
			player.setExp((float) experience / needed);
		}
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

	public SavedClassInformation getClassInfo(PlayerClass profess) {
		return classSlots.get(profess.getId());
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
		return waypoints.contains(waypoint.getId());
	}

	public void unlockWaypoint(Waypoint waypoint) {
		waypoints.add(waypoint.getId());
	}

	public long getNextWaypointMillis() {
		return Math.max(0, lastWaypoint + 5000 - System.currentTimeMillis());
	}

	public void heal(double heal) {
		getPlayer().setHealth(Math.max(0, Math.min(player.getHealth() + heal, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())));
	}

	public void addFriend(UUID uuid) {
		friends.add(uuid);
	}

	public void removeFriend(UUID uuid) {
		friends.remove(uuid);
	}

	public boolean hasFriend(UUID uuid) {
		return friends.contains(uuid);
	}

	public void setParty(Party party) {
		this.party = party;
	}

	public void setGuild(Guild guild) {
		this.guild = guild;
	}

	public void log(Level level, String message) {
		MMOCore.plugin.getLogger().log(level, "[Userdata:" + player.getName() + "] " + message);
	}

	public void setLastFriendRequest(long ms) {
		lastFriendRequest = Math.max(0, ms);
	}

	public void sendFriendRequest(PlayerData target) {
		setLastFriendRequest(System.currentTimeMillis());

		FriendRequest request = new FriendRequest(this, target);
		new ConfigMessage("friend-request").addPlaceholders("player", getPlayer().getName(), "uuid", request.getUniqueId().toString()).sendAsJSon(target.getPlayer());
		MMOCore.plugin.requestManager.registerRequest(request);
	}

	public void warp(Waypoint waypoint) {
		lastWaypoint = System.currentTimeMillis();
		giveStellium(-waypoint.getStelliumCost());

		new BukkitRunnable() {
			int x = player.getLocation().getBlockX(), y = player.getLocation().getBlockY(), z = player.getLocation().getBlockZ(), t;

			public void run() {
				if (player.getLocation().getBlockX() != x || player.getLocation().getBlockY() != y || player.getLocation().getBlockZ() != z) {
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, .5f);
					MMOCore.plugin.configManager.getSimpleMessage("warping-canceled").send(player);
					giveStellium(waypoint.getStelliumCost());
					cancel();
					return;
				}

				MMOCore.plugin.configManager.getSimpleMessage("warping-comencing", "left", "" + ((120 - t) / 20)).send(player);
				if (t++ >= 100) {
					player.teleport(waypoint.getLocation());
					player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1, false, false));
					player.playSound(player.getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 1, .5f);
					cancel();
					return;
				}

				player.playSound(player.getLocation(), VersionSound.BLOCK_NOTE_BLOCK_BELL.toSound(), 1, (float) (t / Math.PI * .015 + .5));
				double r = Math.sin((double) t / 100 * Math.PI);
				for (double j = 0; j < Math.PI * 2; j += Math.PI / 4)
					MMOLib.plugin.getVersion().getWrapper().spawnParticle(Particle.REDSTONE, player.getLocation().add(Math.cos((double) t / 20 + j) * r, (double) t / 50, Math.sin((double) t / 20 + j) * r), 1.25f, Color.PURPLE);
			}
		}.runTaskTimer(MMOCore.plugin, 0, 1);
	}

	public boolean hasReachedMaxLevel() {
		return getProfess().getMaxLevel() > 0 && getLevel() >= getProfess().getMaxLevel();
	}

	public void giveExperience(int value) {
		giveExperience(value, null);
	}
	
	public void giveExperience(int value, Location loc) {
		if (profess == null || hasReachedMaxLevel()) {
			setExperience(0);
			return;
		}
		
		// display hologram
		if (MMOItems.plugin.getConfig().getBoolean("game-indicators.exp.enabled")) {
			if (loc != null && MMOCore.plugin.hologramSupport != null)
				MMOCore.plugin.hologramSupport.displayIndicator(loc.add(.5, 1.5, .5), MMOCore.plugin.configManager.getSimpleMessage("exp-hologram", "exp", "" + value).message(), getPlayer());
		}

		value = MMOCore.plugin.boosterManager.calculateExp(null, value);
		value *= 1 + getStats().getStat(StatType.ADDITIONAL_EXPERIENCE) / 100;

		PlayerExperienceGainEvent event = new PlayerExperienceGainEvent(this, value);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;

		experience += event.getExperience();

		int needed;
		boolean check = false;
		while (experience >= (needed = MMOCore.plugin.configManager.getNeededExperience(getLevel() + 1))) {

			if (hasReachedMaxLevel()) {
				experience = 0;
				break;
			}

			experience -= needed;
			level = getLevel() + 1;
			check = true;
			Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(this, null, level + 1));
		}

		if (check) {
			new ConfigMessage("level-up").addPlaceholders("level", "" + level).send(player);
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
			new SmallParticleEffect(player, Particle.SPELL_INSTANT);
			getStats().getMap().updateAll();
		}

		refreshVanillaExp(needed);
	}

	public int getExperience() {
		return experience;
	}

	public PlayerClass getProfess() {
		return profess == null ? MMOCore.plugin.classManager.getDefaultClass() : profess;
	}

	public void giveMana(double amount) {
		mana = Math.max(0, Math.min(getStats().getStat(StatType.MAX_MANA), mana + amount));
	}

	public void giveStamina(double amount) {
		stamina = Math.max(0, Math.min(getStats().getStat(StatType.MAX_STAMINA), stamina + amount));
	}

	public void giveStellium(double amount) {
		stellium = Math.max(0, Math.min(getStats().getStat(StatType.MAX_STELLIUM), stellium + amount));
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

	public boolean canRegen(PlayerResource resource) {
		return getProfess().getHandler(resource).isAvailable(this);
	}

	public double calculateRegen(PlayerResource resource) {
		return getProfess().getHandler(resource).getRegen(this);
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

	public boolean isCasting() {
		return skillCasting != null;
	}

	/*
	 * returns if the action bar is not being used to display anything else and
	 * if the general info action bar can be displayed
	 */
	public boolean canSeeActionBar() {
		return actionBarTimeOut < System.currentTimeMillis();
	}
	
	public void setActionBarTimeOut(long timeOut) {
		actionBarTimeOut = System.currentTimeMillis() + (timeOut * 50);
	}

	public void displayActionBar(String message) {
		setActionBarTimeOut(60);
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
	}

	public void setAttribute(PlayerAttribute attribute, int value) {
		setAttribute(attribute.getId(), value);
	}

	public void setAttribute(String id, int value) {
		attributes.setBaseAttribute(id, value);
	}

	public void clearAttributePoints() {
		attributes.getAttributeInstances().forEach(ins -> ins.setBase(0));
	}

	public Map<String, Integer> mapAttributePoints() {
		Map<String, Integer> ap = new HashMap<String, Integer>();
		attributes.getAttributeInstances().forEach(ins -> ap.put(ins.getId(), ins.getBase()));
		return ap;
	}

	public void setSkillLevel(Skill skill, int level) {
		setSkillLevel(skill.getId(), level);
	}

	public void setSkillLevel(String skill, int level) {
		skills.put(skill, level);
	}

	public void lockSkill(Skill skill) {
		skills.remove(skill.getId());
	}

	public boolean hasSkillUnlocked(Skill skill) {
		return skills.containsKey(skill.getId());
	}

	public int getSkillLevel(Skill skill) {
		return skills.containsKey(skill.getId()) ? skills.get(skill.getId()) : 1;
	}

	public Map<String, Integer> mapSkillLevels() {
		return new HashMap<>(skills);
	}

	public void clearSkillLevels() {
		skills.clear();
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

	public PlayerSkillData getSkillData() {
		return skillData;
	}

	public void setClass(PlayerClass profess) {
		this.profess = profess;

		// for (Iterator<SkillInfo> iterator = boundSkills.iterator();
		// iterator.hasNext();)
		// if (!getProfess().hasSkill(iterator.next().getSkill()))
		// iterator.remove();

		getStats().getMap().updateAll();
	}

	public void setProfess(PlayerClass profess) {
		this.profess = profess;
	}

	public boolean hasSkillBound(int slot) {
		return slot < boundSkills.size();
	}

	public SkillInfo getBoundSkill(int slot) {
		return slot >= boundSkills.size() ? null : boundSkills.get(slot);
	}

	public void setBoundSkill(int slot, SkillInfo skill) {
		if (boundSkills.size() < 6)
			boundSkills.add(skill);
		else
			boundSkills.set(slot, skill);
	}

	public void unbindSkill(int slot) {
		boundSkills.remove(slot);
	}

	public List<SkillInfo> getBoundSkills() {
		return boundSkills;
	}

	public boolean isInCombat() {
		return combat != null;
	}

	public boolean canChooseSubclass() {
		for (Subclass subclass : getProfess().getSubclasses())
			if (getLevel() >= subclass.getLevel())
				return true;
		return false;
	}

	public void updateCombat() {
		if (isInCombat())
			combat.update();
		else
			combat = new CombatRunnable(this);
	}

	private List<String> toStringList(List<UUID> list) {
		if (list.isEmpty())
			return null;

		List<String> stringList = new ArrayList<>();
		list.forEach(uuid -> stringList.add(uuid.toString()));
		return stringList;
	}

	public SkillResult cast(Skill skill) {
		return cast(getProfess().getSkill(skill));
	}

	public SkillResult cast(SkillInfo skill) {

		if (skill.getSkill().isPassive())
			return new SkillResult(this, skill, CancelReason.OTHER);

		PlayerCastSkillEvent event = new PlayerCastSkillEvent(this, skill);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return new SkillResult(this, skill, CancelReason.OTHER);

		SkillResult cast = skill.getSkill().whenCast(this, skill);
		if (!cast.isSuccessful()) {

			if (cast.getCancelReason() == CancelReason.MANA)
				MMOCore.plugin.configManager.getSimpleMessage("casting.no-mana").send(player);

			if (cast.getCancelReason() == CancelReason.COOLDOWN)
				MMOCore.plugin.configManager.getSimpleMessage("casting.on-cooldown").send(player);

			return cast;
		}

		if (!nocd) {

			// calculate skill cooldown reduction only if stat is higher than 0
			// to save performance
			double red = getStats().getStat(StatType.COOLDOWN_REDUCTION) * 10;
			red *= red > 0 ? skill.getModifier("cooldown", getSkillLevel(skill.getSkill())) : 0;

			skillData.setLastCast(cast.getSkill(), System.currentTimeMillis() - (long) red);
			giveMana(-cast.getManaCost());
		}

		return cast;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof PlayerData && ((PlayerData) obj).uuid.equals(uuid);
	}
}
