package net.Indyuce.mmocore.api.player.stats;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.ClassSkill;

public class PlayerStats {
    private final PlayerData data;

    /**
     * Utilclass to easily manipulate the MMOLib stat map
     *
     * @param data Playerdata
     */
    public PlayerStats(PlayerData data) {
        this.data = data;
    }

    public PlayerData getData() {
        return data;
    }

    public StatMap getMap() {
        return data.getMMOPlayerData().getStatMap();
    }

    public StatInstance getInstance(StatType stat) {
        return getMap().getInstance(stat.name());
    }

    public StatInstance getInstance(String stat) {
        return getMap().getInstance(stat);
    }

    /*
     * applies relative attributes on the base stat too
     */
    public double getStat(StatType stat) {
        return getInstance(stat).getTotal();
    }

    public double getBase(StatType stat) {
        return data.getProfess().calculateStat(stat,
                stat.hasProfession() ? data.getCollectionSkills().getLevel(stat.getProfession()) : data.getLevel());
    }

    /**
     * Used to update MMOCore stat modifiers due to class and send them over to
     * MMOLib. Must be ran everytime the player levels up or changes class.
     * <p>
     * This is also called when reloading the plugin to make class setup easier,
     * see {@link PlayerData#update()} for more info
     */
    public synchronized void updateStats() {
        for (StatType stat : StatType.values()) {
            StatInstance instance = getMap().getInstance(stat.name());
            StatInstance.ModifierPacket packet = instance.newPacket();

            // Remove old stat modifiers
            packet.removeIf(str -> str.equals("mmocoreClass"));

            // Add newest one
            double total = getBase(stat) - instance.getBase();
            if (total != 0)
                packet.addModifier(new StatModifier("mmocoreClass", stat.name(), total, ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER));

            // Then update the stat
            packet.runUpdate();
        }

        /*
         * This is here because it requires updates for the same reasons
         * as statistics (when the player level changes, when his class
         * changes, when he logs on..)
         *
         * This updates the player's passive skills
         */
        data.getMMOPlayerData().getPassiveSkillMap().removeModifiers("MMOCorePassiveSkill");
        for (ClassSkill skill : data.getProfess().getSkills())
            if (skill.getSkill().hasTrigger())
                data.getMMOPlayerData().getPassiveSkillMap().addModifier(skill.toPassive(data));
    }
}
