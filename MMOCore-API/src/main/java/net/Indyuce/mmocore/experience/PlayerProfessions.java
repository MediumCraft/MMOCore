package net.Indyuce.mmocore.experience;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerExperienceGainEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.loot.chest.particle.SmallParticleEffect;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PlayerProfessions {
    private final Map<String, Double> exp = new HashMap<>();
    private final Map<String, Integer> level = new HashMap<>();

    private final PlayerData playerData;

    public PlayerProfessions(PlayerData playerData) {
        this.playerData = playerData;
    }

    /**
     * When loading data through YAML
     */
    public PlayerProfessions load(ConfigurationSection config) {
        for (String key : config.getKeys(false))
            if (MMOCore.plugin.professionManager.has(key)) {
                exp.put(key, config.getDouble(key + ".exp"));
                level.put(key, config.getInt(key + ".level"));
            }

        if (config.contains("times-claimed"))
            // Watch out for the deep section lookup
            for (String key : config.getConfigurationSection("times-claimed").getKeys(true))
                playerData.getItemClaims().put("profession." + key, config.getInt("times-claimed." + key));

        return this;
    }

    /**
     * When saving data through YAML
     */
    public void save(ConfigurationSection config) {
        for (String id : exp.keySet())
            config.set(id + ".exp", exp.get(id));
        for (String id : level.keySet())
            config.set(id + ".level", level.get(id));
    }

    /**
     * When saving data through SQL
     */
    public String toJsonString() {
        JsonObject json = new JsonObject();
        for (Profession profession : MMOCore.plugin.professionManager.getAll()) {
            JsonObject object = new JsonObject();
            object.addProperty("exp", getExperience(profession));
            object.addProperty("level", getLevel(profession));

            json.add(profession.getId(), object);
        }

        return json.toString();
    }

    /**
     * When loading data through SQL
     */
    public void load(String json) {
        JsonObject obj = new Gson().fromJson(json, JsonObject.class);

        // Load profession exp and levels
        for (Entry<String, JsonElement> entry : obj.entrySet())
            if (MMOCore.plugin.professionManager.has(entry.getKey())) {
                JsonObject value = entry.getValue().getAsJsonObject();
                exp.put(entry.getKey(), value.get("exp").getAsDouble());
                level.put(entry.getKey(), value.get("level").getAsInt());
            }

        // Load times claimed
        if (obj.has("timesClaimed"))
            for (Entry<String, JsonElement> entry : obj.getAsJsonObject("timesClaimed").entrySet())
                playerData.getItemClaims().put("profession." + entry.getKey(), entry.getValue().getAsInt());

        if (!playerData.areStatsLoaded()) {
            for (Profession profession : MMOCore.plugin.professionManager.getAll()) {
                if (profession.hasExperienceTable())
                    profession.getExperienceTable().claimStatTriggers(playerData, profession);
            }
            if (playerData.getProfess().hasExperienceTable())
                playerData.getProfess().getExperienceTable().claimStatTriggers(playerData, playerData.getProfess());

        }

    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public int getLevel(String profession) {
        return Math.max(1, level.getOrDefault(profession, 1));
    }

    public int getLevel(Profession profession) {
        return getLevel(profession.getId());
    }

    public double getExperience(String id) {
        return exp.getOrDefault(id, 0.);
    }

    public double getExperience(Profession profession) {
        return getExperience(profession.getId());
    }

    public int getLevelUpExperience(Profession profession) {
        return profession.getExpCurve().getExperience(getLevel(profession) + 1);
    }

    public int getLevelUpExperience(String id) {
        return MMOCore.plugin.professionManager.has(id) ? MMOCore.plugin.professionManager.get(id).getExpCurve().getExperience(getLevel(id) + 1) : 0;
    }

    public void setLevel(Profession profession, int value) {
        level.put(profession.getId(), value);
    }

    public void takeLevels(Profession profession, int value) {
        int current = level.getOrDefault(profession.getId(), 1);
        level.put(profession.getId(), Math.max(1, current - value));
    }

    public void setExperience(Profession profession, double value) {
        exp.put(profession.getId(), value);
    }

    public void giveLevels(Profession profession, int value, EXPSource source) {
        int total = 0, level = getLevel(profession);
        while (value-- > 0)
            total += profession.getExpCurve().getExperience(level + value + 1);
        giveExperience(profession, total, source);
    }

    public boolean hasReachedMaxLevel(Profession profession) {
        return profession.hasMaxLevel() && getLevel(profession) >= profession.getMaxLevel();
    }

    public void giveExperience(Profession profession, double value, EXPSource source) {
        giveExperience(profession, value, source, null, true);
    }

    public void giveExperience(Profession profession, double value, EXPSource source, @Nullable Location hologramLocation, boolean splitExp) {
        Validate.isTrue(playerData.isOnline(), "Cannot give experience to offline player");
        if (value <= 0) {
            exp.put(profession.getId(), Math.max(0, exp.get(profession.getId()) + value));
            return;
        }

        if (hasReachedMaxLevel(profession)) {
            setExperience(profession, 0);
            return;
        }

        // Splitting exp through party members
        AbstractParty party;
        if (splitExp && (party = playerData.getParty()) != null && MMOCore.plugin.configManager.splitProfessionExp) {
            List<PlayerData> onlineMembers = party.getOnlineMembers();
            value /= onlineMembers.size();
            for (PlayerData member : onlineMembers)
                if (!member.equals(playerData))
                    member.getCollectionSkills().giveExperience(profession, value, source, null, false);
        }

        // Apply buffs AFTER splitting exp
        value *= (1 + playerData.getStats().getStat("ADDITIONAL_EXPERIENCE_" + UtilityMethods.enumName(profession.getId())) / 100) * MMOCore.plugin.boosterManager.getMultiplier(profession);

        PlayerExperienceGainEvent event = new PlayerExperienceGainEvent(playerData, profession, value, source);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        // Display hologram
        if (hologramLocation != null)
            MMOCoreUtils.displayIndicator(hologramLocation.add(.5, 1.5, .5), MMOCore.plugin.configManager.getSimpleMessage("exp-hologram", "exp", MythicLib.plugin.getMMOConfig().decimal.format(event.getExperience())).message());

        exp.put(profession.getId(), Math.max(0, exp.getOrDefault(profession.getId(), 0.) + event.getExperience()));
        int level, oldLevel = getLevel(profession);
        double needed, exp;

        /*
         * Loop for exp overload when leveling up, will continue
         * looping until exp is 0 or max level has been reached
         */
        boolean check = false;
        while ((exp = this.exp.get(profession.getId())) >= (needed = profession.getExpCurve().getExperience((level = getLevel(profession)) + 1))) {
            if (hasReachedMaxLevel(profession)) {
                setExperience(profession, 0);
                check = true;
                break;
            }

            this.exp.put(profession.getId(), exp - needed);
            this.level.put(profession.getId(), level + 1);
            check = true;
            playerData.giveExperience(profession.getExperience().calculate(level), null);

            // Apply profession experience table
            if (profession.hasExperienceTable())
                profession.getExperienceTable().claim(playerData, level, profession);
        }

        if (check) {
            Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(playerData, profession, oldLevel, level));
            new SmallParticleEffect(playerData.getPlayer(), Particle.SPELL_INSTANT);
            new ConfigMessage("profession-level-up").addPlaceholders("level", String.valueOf(level), "profession", profession.getName())
                    .send(playerData.getPlayer());
            MMOCore.plugin.soundManager.getSound(SoundEvent.LEVEL_UP).playTo(playerData.getPlayer());
            playerData.getStats().updateStats();
        }

        StringBuilder bar = new StringBuilder("" + ChatColor.BOLD);
        int chars = (int) ((double) exp / needed * 20);
        for (int j = 0; j < 20; j++)
            bar.append(j == chars ? "" + ChatColor.WHITE + ChatColor.BOLD : "").append("|");
        if (playerData.isOnline())
            MMOCore.plugin.configManager.getSimpleMessage("exp-notification", "profession", profession.getName(), "progress", bar.toString(), "ratio",
                    MythicLib.plugin.getMMOConfig().decimal.format((double) exp / needed * 100)).send(playerData.getPlayer());
    }
}
