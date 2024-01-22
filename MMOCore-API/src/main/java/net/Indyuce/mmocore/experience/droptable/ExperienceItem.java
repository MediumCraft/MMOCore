package net.Indyuce.mmocore.experience.droptable;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.StatTrigger;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.api.quest.trigger.api.Removable;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExperienceItem {
    private final String id;

    private final int period, firstTrigger, lastTrigger, fixedLevel;
    private final double claimChance, failReduction;
    private final List<Trigger> triggers;

    private static final Random random = new Random();

    @Deprecated
    public ExperienceItem(String id, int period, int firstTrigger, double claimChance, double failReduction, List<Trigger> triggers) {
        this(id, period, firstTrigger, Integer.MAX_VALUE, claimChance, failReduction, triggers);
    }

    /**
     * One item for an experience table.
     *
     * @param period        The experience item is claimed every X level ups
     * @param firstTrigger  The experience item if claimed for the first time at X level ups.
     * @param lastTrigger   The last level at which the item can be claimed.
     * @param claimChance   Chance for that item to be claimed every X level ups
     * @param failReduction Between 0 and 1, by how much the fail chance is reduced
     *                      every time the item is not claimed when leveling up.
     *                      <p>
     *                      Failing chance follows a geometric sequence therefore
     *                      <code>successChance = 1 - (1 - initialSuccessChance) * failReduction^n</code>
     *                      where n is the amount of successive claiming fails
     * @param triggers      Actions cast when the exp item is claimed
     */
    public ExperienceItem(String id, int period, int firstTrigger, int lastTrigger, double claimChance, double failReduction, List<Trigger> triggers) {
        this.id = id;
        this.period = period;
        this.claimChance = claimChance;
        this.failReduction = failReduction;
        this.triggers = triggers;
        this.firstTrigger = firstTrigger;
        this.lastTrigger = lastTrigger;
        this.fixedLevel = 0;
    }

    /**
     * One item for an experience table.
     *
     * @param fixedLevel  Level at which your item should drop.
     * @param claimChance Chance for that item to be claimed every X level ups
     * @param triggers    Actions cast when the exp item is claimed
     */
    public ExperienceItem(String id, int fixedLevel, double claimChance, List<Trigger> triggers) {
        this.id = id;
        this.period = 0;
        this.claimChance = claimChance;
        this.failReduction = 0;
        this.triggers = triggers;
        this.firstTrigger = 0;
        this.lastTrigger = 0;
        this.fixedLevel = fixedLevel;
    }

    public ExperienceItem(ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null");
        Validate.isTrue(config.contains("triggers"));
        id = config.getName();

        period = config.getInt("period", 1);
        firstTrigger = config.getInt("first-trigger", period);
        lastTrigger = config.getInt("last-trigger", Integer.MAX_VALUE);
        fixedLevel = config.getInt("level", -1);
        claimChance = config.getDouble("chance", 100) / 100;
        failReduction = config.getDouble("fail-reduction", 80) / 100;
        triggers = new ArrayList<>();

        for (String triggerFormat : config.getStringList("triggers"))
            triggers.add(MMOCore.plugin.loadManager.loadTrigger(new MMOLineConfig(triggerFormat)));
    }

    public String getId() {
        return id;
    }

    /**
     * @param professionLevel The profession level the player just reached
     * @param timesCollected  Amount of times the exp item has already been claimed by the player
     * @return If the item should be claimed right now taking into
     * account the randomness factor from the 'chance' parameter
     */
    public boolean roll(int professionLevel, int timesCollected) {

        // Exact level. Overrides everything
        if (fixedLevel > -1) return fixedLevel == professionLevel;

        // Check for the last triggering level
        if (professionLevel > lastTrigger) return false;

        // A period of 0 means the item only triggers once
        if (period == 0 && timesCollected > 0) return false;

        // Basic formula
        final int claimsRequired = (professionLevel + 1 - (firstTrigger + timesCollected * period));
        if (claimsRequired < 1) return false;

        final double chance = 1 - (1 - claimChance) * Math.pow(failReduction, claimsRequired);
        return random.nextDouble() < chance;
    }

    public void applyTriggers(PlayerData levelingUp) {
        for (Trigger trigger : triggers)
            trigger.apply(levelingUp);
    }

    /**
     * Used when the player level is reset to 0, when using
     * skill tree reallocation points for instance.
     */
    public void removeTriggers(PlayerData playerData) {
        for (Trigger trigger : triggers)
            if (trigger instanceof Removable) ((Removable) trigger).remove(playerData);
    }

    /**
     * Used when a player connects back to give back all the stats that he should have.
     *
     * @param playerData
     */
    public void applyRemovableTrigger(PlayerData playerData) {
        for (Trigger trigger : triggers)
            if (trigger instanceof Removable) trigger.apply(playerData);
    }
}
