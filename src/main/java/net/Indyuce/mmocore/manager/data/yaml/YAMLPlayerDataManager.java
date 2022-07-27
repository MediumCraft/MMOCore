package net.Indyuce.mmocore.manager.data.yaml;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.SavingPlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttributes;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.api.quest.PlayerQuests;
import net.Indyuce.mmocore.experience.PlayerProfessions;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.guild.provided.Guild;
import net.Indyuce.mmocore.manager.data.DataProvider;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class YAMLPlayerDataManager extends PlayerDataManager {
    private final DataProvider provider;

    public YAMLPlayerDataManager(DataProvider provider) {
        this.provider = provider;
    }

    @Override
    public void loadData(PlayerData data) {
        FileConfiguration config = new ConfigFile(data.getUniqueId()).getConfig();

        data.setClassPoints(config.getInt("class-points", getDefaultData().getClassPoints()));
        data.setSkillPoints(config.getInt("skill-points", getDefaultData().getSkillPoints()));
        data.setAttributePoints(config.getInt("attribute-points", getDefaultData().getAttributePoints()));
        data.setAttributeReallocationPoints(config.getInt("attribute-realloc-points", getDefaultData().getAttrReallocPoints()));
        data.setLevel(config.getInt("level", getDefaultData().getLevel()));
        data.setExperience(config.getInt("experience"));
        if (config.contains("class"))
            data.setClass(MMOCore.plugin.classManager.get(config.getString("class")));

        if (!data.hasUsedTemporaryData()) {
            data.setMana(data.getStats().getStat("MAX_MANA"));
            data.setStamina(data.getStats().getStat("MAX_STAMINA"));
            data.setStellium(data.getStats().getStat("MAX_STELLIUM"));
        }

        if (config.contains("guild")) {
            Guild guild = provider.getGuildManager().getGuild(config.getString("guild"));
            data.setGuild(guild.hasMember(data.getUniqueId()) ? guild : null);
        }
        if (config.contains("attribute"))
            data.getAttributes().load(config.getConfigurationSection("attribute"));
        if (config.contains("profession"))
            data.getCollectionSkills().load(config.getConfigurationSection("profession"));
        if (config.contains("quest"))
            data.getQuestData().load(config.getConfigurationSection("quest"));
        data.getQuestData().updateBossBar();
        if (config.contains("waypoints"))
            data.getWaypoints().addAll(config.getStringList("waypoints"));
        if (config.contains("friends"))
            config.getStringList("friends").forEach(str -> data.getFriends().add(UUID.fromString(str)));
        if (config.contains("skill"))
            config.getConfigurationSection("skill").getKeys(false).forEach(id -> data.setSkillLevel(id, config.getInt("skill." + id)));
        if (config.contains("bound-skills"))
            for (String id : config.getStringList("bound-skills"))
                if (data.getProfess().hasSkill(id))
                    data.getBoundSkills().add(data.getProfess().getSkill(id));

        if (config.contains("times-claimed"))
            for (String key : config.getConfigurationSection("times-claimed").getKeys(true))
                data.getItemClaims().put(key, config.getInt("times-claimed." + key));

        // Load class slots, use try so the player can log in.
        if (config.contains("class-info"))
            for (String key : config.getConfigurationSection("class-info").getKeys(false))
                try {
                    PlayerClass profess = MMOCore.plugin.classManager.get(key);
                    Validate.notNull(profess, "Could not find class '" + key + "'");
                    data.applyClassInfo(profess, new SavedClassInformation(config.getConfigurationSection("class-info." + key)));
                } catch (IllegalArgumentException exception) {
                    MMOCore.log(Level.WARNING, "Could not load class info '" + key + "': " + exception.getMessage());
                }

        data.setFullyLoaded();
    }

    @Override
    public void saveData(PlayerData data) {
        ConfigFile file = new ConfigFile(data.getUniqueId());
        FileConfiguration config = file.getConfig();

        config.set("class-points", data.getClassPoints());
        config.set("skill-points", data.getSkillPoints());
        config.set("attribute-points", data.getAttributePoints());
        // config.set("skill-realloc-points", skillReallocationPoints);
        config.set("attribute-realloc-points", data.getAttributeReallocationPoints());
        config.set("level", data.getLevel());
        config.set("experience", data.getExperience());
        config.set("class", data.getProfess().getId());
        config.set("waypoints", new ArrayList<>(data.getWaypoints()));
        config.set("friends", data.getFriends().stream().map(UUID::toString).collect(Collectors.toList()));
        config.set("last-login", data.getLastLogin());
        config.set("guild", data.hasGuild() ? data.getGuild().getId() : null);

        config.set("skill", null);
        data.mapSkillLevels().forEach((key1, value) -> config.set("skill." + key1, value));
        data.getItemClaims().forEach((key, times) -> config.set("times-claimed." + key, times));

        List<String> boundSkills = new ArrayList<>();
        data.getBoundSkills().forEach(skill -> boundSkills.add(skill.getSkill().getHandler().getId()));
        config.set("bound-skills", boundSkills);

        config.set("attribute", null);
        config.createSection("attribute");
        data.getAttributes().save(config.getConfigurationSection("attribute"));

        config.set("profession", null);
        config.createSection("profession");
        data.getCollectionSkills().save(config.getConfigurationSection("profession"));

        config.set("quest", null);
        config.createSection("quest");
        data.getQuestData().save(config.getConfigurationSection("quest"));

        config.set("class-info", null);
        for (String key : data.getSavedClasses()) {
            SavedClassInformation info = data.getClassInfo(key);
            config.set("class-info." + key + ".level", info.getLevel());
            config.set("class-info." + key + ".experience", info.getExperience());
            config.set("class-info." + key + ".skill-points", info.getSkillPoints());
            config.set("class-info." + key + ".attribute-points", info.getAttributePoints());
            config.set("class-info." + key + ".attribute-realloc-points", info.getAttributeReallocationPoints());
            info.getSkillKeys().forEach(skill -> config.set("class-info." + key + ".skill." + skill, info.getSkillLevel(skill)));
            info.getAttributeKeys()
                    .forEach(attribute -> config.set("class-info." + key + ".attribute." + attribute, info.getAttributeLevel(attribute)));
        }

        file.save();
    }


    /**
     * Used to save Data from a SavingPlayerDataInstance (Data of an offline player)
     *
     * @param data
     */
    @Override
    public void saveData(SavingPlayerData data) {
        ConfigFile file = new ConfigFile(data.uuid());
        FileConfiguration config = file.getConfig();

        config.set("class-points", data.classPoints());
        config.set("skill-points", data.skillPoints());
        config.set("attribute-points", data.attributePoints());
        // config.set("skill-realloc-points", skillReallocationPoints);
        config.set("attribute-realloc-points", data.attributeReallocationPoints());
        config.set("level", data.level());
        config.set("experience", data.experience());
        config.set("class", data.classId());
        config.set("waypoints", data.waypoints());
        config.set("friends", data.friends());
        config.set("last-login", data.lastLogin());
        config.set("guild", data.guildId());

        config.set("skill", null);
        data.skills().forEach((key1, value) -> config.set("skill." + key1, value));
        data.itemClaims().forEach((key, times) -> config.set("times-claimed." + key, times));

        List<String> boundSkills = new ArrayList<>();
        data.boundSkills().forEach(skill -> boundSkills.add(skill));
        config.set("bound-skills", boundSkills);

        config.set("attribute", null);
        Gson parser = new Gson();
        JsonObject jo = parser.fromJson(data.attributes(), JsonObject.class);
        for (Map.Entry<String, JsonElement> entry : jo.entrySet()) {
            try {
                String id = entry.getKey().toLowerCase().replace("_", "-").replace(" ", "-");
                config.set("attributes." + id, entry.getValue().getAsInt());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        jo = parser.fromJson(data.collectionsSkills(), JsonObject.class);
        config.createSection("profession");
        ConfigurationSection section = config.getConfigurationSection("profession");
        // Load profession exp and levels
        for (Map.Entry<String, JsonElement> entry : jo.entrySet())
            if (MMOCore.plugin.professionManager.has(entry.getKey())) {
                JsonObject value = entry.getValue().getAsJsonObject();
                section.set(entry.getKey() + ".exp", value.get("exp").getAsDouble());
                section.set(entry.getKey() + ".level", value.get("level").getAsInt());
            }


        config.set("quest", null);
        config.createSection("quest");
        section = config.getConfigurationSection("quest");
        jo = parser.fromJson(data.questData(), JsonObject.class);
        if (jo.has("current")) {
            JsonObject cur = jo.getAsJsonObject("current");
            try {
                section.set("current.id", cur.get("id").getAsString());
                section.set("current.objective", cur.get("objective").getAsInt());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (jo.has("finished"))
            for (Map.Entry<String, JsonElement> entry : jo.getAsJsonObject("finished").entrySet())
                section.set("finished." + entry.getKey(), entry.getValue().getAsLong());


        config.set("class-info", null);
        jo = parser.fromJson(data.classInfoData(), JsonObject.class);


        for (Map.Entry<String, JsonElement> entry : jo.entrySet()) {
            try {
                String key = entry.getKey();
                JsonObject info = entry.getValue().getAsJsonObject();
                config.set("class-info." + key + ".level", info.get("level").getAsInt());
                config.set("class-info." + key + ".experience", info.get("experience").getAsDouble());
                config.set("class-info." + key + ".skill-points", info.get("skill-points").getAsInt());
                config.set("class-info." + key + ".attribute-points", info.get("attribute-points").getAsInt());
                config.set("class-info." + key + ".attribute-realloc-points", info.get("attribute-realloc-points").getAsInt());

                if (info.has("attribute"))
                    for (Map.Entry<String, JsonElement> attributesEntry : info.getAsJsonObject("attribute").entrySet())
                        config.set("class-info." + key + ".attribute." + attributesEntry.getKey(), attributesEntry.getValue().getAsInt());

                if (info.has("skill"))
                    for (Map.Entry<String, JsonElement> skillsEntry : info.getAsJsonObject("skill").entrySet())
                        config.set("class-info." + key + ".skill." + skillsEntry.getKey(), skillsEntry.getValue().getAsInt());

            } catch (IllegalArgumentException exception) {
                MMOCore.log(Level.WARNING, "Could not load class info '" + entry.getKey() + "': " + exception.getMessage());
            }
        }
        file.save();
    }

    @NotNull
    @Override
    public OfflinePlayerData getOffline(UUID uuid) {
        return isLoaded(uuid) ? get(uuid) : new YAMLOfflinePlayerData(uuid);
    }

    @Override
    public void loadSavingPlayerData(UUID uuid, List<SavingPlayerData> savingPlayerDataList) {
        FileConfiguration config = new ConfigFile(uuid).getConfig();

        Map<String, Integer> skills = new HashMap<>();
        config.getConfigurationSection("skill").getKeys(false).forEach(id -> skills.put(id, config.getInt("skill." + id)));

        Map<String, Integer> itemClaims = new HashMap<>();
        for (String key : config.getConfigurationSection("times-claimed").getKeys(true))
            itemClaims.put(key, config.getInt("times-claimed." + key));


        //Creates the attributes json


        ConfigurationSection section = config.getConfigurationSection("attributes");
        JsonObject attributesJson = new JsonObject();
        for (String key : section.getKeys(false)) {
            String id = key.toLowerCase().replace("-", "_").replace(" ", "_");
            attributesJson.addProperty(id, section.getInt(key));
        }


        //Creates the profession json


        section = config.getConfigurationSection("profession");
        JsonObject collectionSkillsJson = new JsonObject();
        ;
        for (String key : config.getKeys(false)) {
            if (MMOCore.plugin.professionManager.has(key)) {
                JsonObject object = new JsonObject();
                object.addProperty("exp", section.getDouble(key + ".exp"));
                object.addProperty("level", section.getInt(key + ".level"));

                collectionSkillsJson.add(key, object);
            }
        }

        //Creates the questJson

        section = config.getConfigurationSection("quest");
        JsonObject questsJson = new JsonObject();

        if (section.contains("current")) {
            JsonObject cur = new JsonObject();
            cur.addProperty("id", section.getString("current.id"));
            cur.addProperty("objective", section.getInt("current.objective"));
            questsJson.add("current", cur);
        }
        JsonObject fin = new JsonObject();
        if (section.contains("finished"))
            for (String key : section.getConfigurationSection("finished").getKeys(false))
                fin.addProperty(key, section.getLong("finished." + key));

        if (fin.size() != 0)
            questsJson.add("finished", fin);


        JsonObject classInfoJson = new JsonObject();
        // Load class slots, use try so the player can log in.
        if (config.contains("class-info"))
            for (String key : config.getConfigurationSection("class-info").getKeys(false)) {
                SavedClassInformation info = new SavedClassInformation(config.getConfigurationSection("class-info." + key));
                JsonObject classinfo = new JsonObject();
                classinfo.addProperty("level", info.getLevel());
                classinfo.addProperty("experience", info.getExperience());
                classinfo.addProperty("skill-points", info.getSkillPoints());
                classinfo.addProperty("attribute-points", info.getAttributePoints());
                classinfo.addProperty("attribute-realloc-points", info.getAttributeReallocationPoints());
                JsonObject skillinfo = new JsonObject();
                for (String skill : info.getSkillKeys())
                    skillinfo.addProperty(skill, info.getSkillLevel(skill));
                classinfo.add("skill", skillinfo);
                JsonObject attributeinfo = new JsonObject();
                for (String attribute : info.getAttributeKeys())
                    attributeinfo.addProperty(attribute, info.getAttributeLevel(attribute));
                classinfo.add("attribute", attributeinfo);
                classInfoJson.add(key, classinfo);
            }


        SavingPlayerData data = new SavingPlayerData(
                uuid,
                config.getInt("class-points"),
                config.getInt("skill-points"),
                config.getInt("attribute-points"),
                config.getInt("attribute-realloc-points"),
                config.getInt("level"),
                config.getInt("experience"),
                config.getString("class"),
                config.getLong("last-login"),
                config.getString("guild"),
                config.getStringList("waypoints").stream().collect(Collectors.toSet()),
                config.getStringList("friends").stream().map(UUID::fromString).toList(),
                config.getStringList("bound-skills"),
                skills,
                itemClaims,
                attributesJson.toString(),
                collectionSkillsJson.toString(),
                questsJson.toString(),
                classInfoJson.toString());

        savingPlayerDataList.add(data);
    }

}

