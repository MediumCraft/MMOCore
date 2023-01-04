package net.Indyuce.mmocore.api.quest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.Closable;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

public class PlayerQuests implements Closable {
    private final PlayerData playerData;
    private final Map<String, Long> finished = new HashMap<>();

    @Nullable
    private final BossBar bossbar;
    private final NamespacedKey bossbarNamespacedKey;

    private QuestProgress current;

    public PlayerQuests(PlayerData playerData) {
        this.playerData = playerData;

        if (!MMOCore.plugin.configManager.disableQuestBossBar && playerData.isOnline()) {
            bossbarNamespacedKey = new NamespacedKey(MMOCore.plugin, "mmocore_quest_progress_" + playerData.getUniqueId().toString());
            bossbar = Bukkit.createBossBar(bossbarNamespacedKey, "", BarColor.PURPLE, BarStyle.SEGMENTED_20);
            bossbar.addPlayer(playerData.getPlayer());

            // Bossbar is disabled
        } else {
            bossbarNamespacedKey = null;
            bossbar = null;
        }
    }

    public PlayerQuests load(ConfigurationSection config) {
        if (config.contains("current"))
            try {
                current = MMOCore.plugin.questManager.get(config.getString("current.id")).generateNewProgress(playerData, config.getInt("current.objective"));
            } catch (Exception e) {
                playerData.log(Level.WARNING, "Couldn't load current quest progress (ID '" + config.getString("current.id") + "')");
            }

        if (config.contains("finished"))
            for (String key : config.getConfigurationSection("finished").getKeys(false))
                finished.put(key, config.getLong("finished." + key));

        /*
         * Must update the boss bar once the instance is loaded, otherwise it
         * won't detect the current quest. THE BOSS BAR UPDATE is in the player
         * data class, this way it is still set invisible even if the player has
         * no quest data
         */

        return this;
    }

    public void save(ConfigurationSection config) {
        if (current != null) {
            config.set("current.id", current.getQuest().getId());
            config.set("current.objective", current.getObjectiveNumber());
        } else
            config.set("current", null);

        for (String key : finished.keySet())
            config.set("finished." + key, finished.get(key));
    }

    public String toJsonString() {
        JsonObject json = new JsonObject();
        if (current != null) {
            JsonObject cur = new JsonObject();
            cur.addProperty("id", current.getQuest().getId());
            cur.addProperty("objective", current.getObjectiveNumber());
            json.add("current", cur);
        }

        JsonObject fin = new JsonObject();
        for (String key : finished.keySet())
            fin.addProperty(key, finished.get(key));

        if (finished.size() != 0)
            json.add("finished", fin);
        return json.toString();
    }

    public void load(String json) {
        Gson parser = new Gson();
        JsonObject jo = parser.fromJson(json, JsonObject.class);
        if (jo.has("current")) {
            JsonObject cur = jo.getAsJsonObject("current");
            try {
                current = MMOCore.plugin.questManager.get(cur.get("id").getAsString()).generateNewProgress(playerData, cur.get("objective").getAsInt());
            } catch (Exception e) {
                playerData.log(Level.WARNING, "Couldn't load current quest progress (ID '" + cur.get("id").getAsString() + "')");
            }
        }

        if (jo.has("finished"))
            for (Entry<String, JsonElement> entry : jo.getAsJsonObject("finished").entrySet())
                finished.put(entry.getKey(), entry.getValue().getAsLong());
    }

    public QuestProgress getCurrent() {
        return current;
    }

    public boolean hasCurrent() {
        return current != null;
    }

    public Set<String> getFinishedQuests() {
        return finished.keySet();
    }

    public boolean hasCurrent(Quest quest) {
        return hasCurrent() && current.getQuest().equals(quest);
    }

    public boolean hasFinished(Quest quest) {
        return finished.containsKey(quest.getId());
    }

    public void finishCurrent() {
        finished.put(current.getQuest().getId(), System.currentTimeMillis());
        start(null);
    }

    public void resetFinishedQuests() {
        finished.clear();
    }

    public Date getFinishDate(Quest quest) {
        return new Date(finished.get(quest.getId()));
    }

    public void start(Quest quest) {

        // Close current objective progress if quest is active
        closeCurrentQuest();

        // Apply newest quest
        current = quest == null ? null : quest.generateNewProgress(playerData);
        updateBossBar();
    }

    public void closeCurrentQuest() {
        if (current == null)
            return;

        current.getProgress().close();
        current = null;
    }

    @Override
    public void close() {

        // Remove boss bar
        if (bossbar != null) {
            bossbar.removeAll();
            Bukkit.removeBossBar(bossbarNamespacedKey);
        }
    }

    public boolean checkCooldownAvailability(Quest quest) {
        return (finished.get(quest.getId()) + quest.getDelayMillis()) < System.currentTimeMillis();
    }

    public long getDelayFeft(Quest quest) {
        return Math.max(finished.get(quest.getId()) + quest.getDelayMillis() - System.currentTimeMillis(), 0);
    }

    public boolean checkParentAvailability(Quest quest) {
        for (Quest parent : quest.getParents())
            if (!hasFinished(parent))
                return false;
        return true;
    }

    public void updateBossBar() {

        // Bossbar is disabled
        if (bossbar == null)
            return;

        if (!hasCurrent() || !current.getProgress().getObjective().hasLore()) {
            bossbar.setVisible(false);
            return;
        }

        bossbar.setVisible(true);
        bossbar.setColor(current.getProgress().getObjective().getBarColor());
        bossbar.setTitle(current.getFormattedLore());
        bossbar.setProgress((double) current.getObjectiveNumber() / current.getQuest().getObjectives().size());
    }
}
