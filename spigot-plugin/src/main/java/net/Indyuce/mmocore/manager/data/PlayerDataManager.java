package net.Indyuce.mmocore.manager.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.TemporaryPlayerData;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.AsyncPlayerDataLoadEvent;
import net.Indyuce.mmocore.api.event.PlayerDataLoadEvent;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.guild.provided.Guild;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public abstract class PlayerDataManager {
    private final static Map<UUID, PlayerData> data = Collections.synchronizedMap(new HashMap<>());

    private DefaultPlayerData defaultData = new DefaultPlayerData(1, 0, 0, 0, 0);

    public PlayerData get(OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    /**
     * Gets the player data, or throws an exception if not found.
     * The player data should be loaded when the player logs in
     * so it's really bad practice to setup the player data if it's not loaded.
     *
     * @param uuid Player UUID
     * @return Player data, if it's loaded
     */
    public PlayerData get(UUID uuid) {
        return Objects.requireNonNull(data.get(uuid), "Player data is not loaded");
    }


    public static void loadDataFromJson(PlayerData data, String json) {
        JsonObject object = MythicLib.plugin.getJson().parse(json, JsonObject.class);

        data.setClassPoints(object.get("class_points").getAsInt());
        data.setSkillPoints(object.get("skill_points").getAsInt());
        data.setAttributePoints(object.get("attribute_points").getAsInt());
        data.setAttributeReallocationPoints(object.get("attribute_realloc_points").getAsInt());
        data.setLevel(object.get("level").getAsInt());
        data.setExperience(object.get("experience").getAsInt());
        if (object.has("class"))
            data.setClass(MMOCore.plugin.classManager.get(object.get(("class")).getAsString()));

        if (object.has("times_claimed")) {
            JsonObject timesClaimed =object.get(("times_claimed")).getAsJsonObject();
            timesClaimed.entrySet().forEach(entry -> data.getItemClaims().put(entry.getKey(), entry.getValue().getAsInt()));
        }

        if (object.has(("guild"))) {
            Guild guild = MMOCore.plugin.dataProvider.getGuildManager().getGuild(object.get("guild").getAsString());
            data.setGuild(guild.hasMember(data.getUniqueId()) ? guild : null);
        }
        if (object.has(("attributes"))) data.getAttributes().load(object.get("attributes").getAsString());
        if (object.has(("professions")))
            data.getCollectionSkills().load(object.get("professions").getAsString());
        if (object.has(("quests"))) data.getQuestData().load(object.get("quests").getAsString());
        data.getQuestData().updateBossBar();
        if (object.has(("waypoints")))
            data.getWaypoints().addAll(MMOCoreUtils.jsonArrayToList(object.get("waypoints").getAsString()));
        if (object.has(("friends")))
            MMOCoreUtils.jsonArrayToList(object.get("friends").getAsString()).forEach(str -> data.getFriends().add(UUID.fromString(str)))
                    ;
        if (object.has(("skills"))) {
            JsonObject skillsObject = object.get("skills").getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : skillsObject.entrySet())
                data.setSkillLevel(entry.getKey(), entry.getValue().getAsInt());
        }
        if (object.has(("bound_skills")))
            for (String skill : MMOCoreUtils.jsonArrayToList(object.get("bound_skills").getAsString()))
                if (data.getProfess().hasSkill(skill))
                    data.getBoundSkills().add(data.getProfess().getSkill(skill));
        if (object.has(("class_info"))) {
            JsonObject classObject = object.get("class_info").getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : classObject.entrySet()) {
                try {
                    PlayerClass profess = MMOCore.plugin.classManager.get(entry.getKey());
                    Validate.notNull(profess, "Could not find class '" + entry.getKey() + "'");
                    data.applyClassInfo(profess, new SavedClassInformation(entry.getValue().getAsJsonObject()));
                } catch (IllegalArgumentException exception) {
                    MMOCore.log(Level.WARNING, "Could not load class info '" + entry.getKey() + "': " + exception.getMessage());
                }
            }
        }

    }


    /**
     * Safely unregisters the player data from the map.
     * This saves the player data either through SQL or YAML,
     * then closes the player data and clears it from the data map.
     *
     * @param playerData PLayer data to unregister
     */
    public void unregisterSafe(PlayerData playerData) {

        // Save data async if required
        if (playerData.isFullyLoaded())
            Bukkit.getScheduler().runTaskAsynchronously(MMOCore.plugin, () -> {
                saveData(playerData);

                // Unregister once the data was saved
                playerData.close();
                data.remove(playerData.getUniqueId());
            });

            // Just unregister data without saving
        else {
            playerData.close();
            data.remove(playerData.getUniqueId());
        }
    }

    /**
     * Offline player data is used to handle processes like friend removal
     * which can still occur if one of the two players is offline.
     * <p>
     * Unlike {@link #get(UUID)} this method never returns a null instance
     *
     * @param uuid Player unique id
     * @return Offline player data
     */
    @NotNull
    public abstract OfflinePlayerData getOffline(UUID uuid);

    /**
     * Called when a player logs in, loading the player data inside the map.
     * <p>
     * For YAML configs or SQL databases, data is loaded as not to overload
     * the main thread with SQL requests. Therefore, the player data returned
     * by that method, when the player joined for the first time, is not
     * fully loaded YET.
     *
     * @param uniqueId Player UUID
     * @return The loaded player data.
     */
    public PlayerData setup(UUID uniqueId) {
        // Load player data if it does not exist
        if (!data.containsKey(uniqueId)) {
            PlayerData newData = TemporaryPlayerData.has(uniqueId) ? new PlayerData(MMOPlayerData.get(uniqueId), TemporaryPlayerData.get(uniqueId)) : new PlayerData(MMOPlayerData.get(uniqueId));

            // Schedule async data loading
            Bukkit.getScheduler().runTaskAsynchronously(MMOCore.plugin, () -> {
                loadData(newData);
                newData.getStats().updateStats();
                Bukkit.getPluginManager().callEvent(new AsyncPlayerDataLoadEvent(newData));
                Bukkit.getScheduler().runTask(MMOCore.plugin, () -> Bukkit.getPluginManager().callEvent(new PlayerDataLoadEvent(newData)));
            });

            // Update data map
            data.put(uniqueId, newData);

            return newData;
        }

        return data.get(uniqueId);
    }

    public DefaultPlayerData getDefaultData() {
        return defaultData;
    }

    public void loadDefaultData(ConfigurationSection config) {
        defaultData = new DefaultPlayerData(config);
    }

    public boolean isLoaded(UUID uuid) {
        return data.containsKey(uuid);
    }

    public Collection<PlayerData> getLoaded() {
        return data.values();
    }

    /**
     * Called when player data must be loaded from database or config.
     *
     * @param data Player data to load
     */
    public abstract void loadData(PlayerData data);

    /**
     * Called when player data must be saved in configs or database.
     * This method should always be called sync because it DOES register
     * an async task in case MySQL storage is used.
     *
     * @param data Player data to save
     */
    public abstract void saveData(PlayerData data);

    public class DefaultPlayerData {
        private final int level, classPoints, skillPoints, attributePoints, attrReallocPoints;

        public DefaultPlayerData(ConfigurationSection config) {
            level = config.getInt("level", 1);
            classPoints = config.getInt("class-points");
            skillPoints = config.getInt("skill-points");
            attributePoints = config.getInt("attribute-points");
            attrReallocPoints = config.getInt("attribute-realloc-points");
        }

        public DefaultPlayerData(int level, int classPoints, int skillPoints, int attributePoints, int attrReallocPoints) {
            this.level = level;
            this.classPoints = classPoints;
            this.skillPoints = skillPoints;
            this.attributePoints = attributePoints;
            this.attrReallocPoints = attrReallocPoints;
        }

        public int getLevel() {
            return level;
        }

        public int getSkillPoints() {
            return skillPoints;
        }

        public int getClassPoints() {
            return classPoints;
        }

        public int getAttrReallocPoints() {
            return attrReallocPoints;
        }

        public int getAttributePoints() {
            return attributePoints;
        }

        public void apply(PlayerData player) {
            player.setLevel(level);
            player.setClassPoints(classPoints);
            player.setSkillPoints(skillPoints);
            player.setAttributePoints(attributePoints);
            player.setAttributeReallocationPoints(attrReallocPoints);
        }
    }
}
