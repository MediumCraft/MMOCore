package net.Indyuce.mmocore.api.player.stats;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.player.stats.StatInfo;

public class PlayerStats {
    private final PlayerData data;

    /**
     * Util class to easily manipulate the MMOLib stat map
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

    @Deprecated
    public StatInstance getInstance(StatType stat) {
        return getMap().getInstance(stat.name());
    }

    public StatInstance getInstance(String stat) {
        return getMap().getInstance(stat);
    }

    public double getStat(String stat) {
        return getInstance(stat).getTotal();
    }

    /**
     * MMOCore base stat value differs from the one in MythicLib.
     * <p>
     * MythicLib: the base stat value is only defined for stats
     * which are based on vanilla player attributes. It corresponds
     * to the stat amount any player has with NO attribute modifier whatsoever.
     * <p>
     * MMOCore: the base stat value corresponds to the stat amount
     * the player CLASS grants. It can be similar or equal to the one
     * in MMOCore but it really is completely different.
     *
     * @return MMOCore base stat value
     */
    public double getBase(String stat) {
        final Profession profession = StatInfo.valueOf(stat).profession;
        return data.getProfess().calculateStat(stat, profession == null ? data.getLevel() : data.getCollectionSkills().getLevel(profession));
    }

    /**
     * Used to update MMOCore stat modifiers due to class and send them over to
     * MMOLib. Must be ran everytime the player levels up or changes class.
     * <p>
     * This is also called when reloading the plugin to make class setup easier,
     * see {@link PlayerData#update()} for more info
     */
    public synchronized void updateStats() {
        for (StatInstance instance : getMap().getInstances()) {
            StatInstance.ModifierPacket packet = instance.newPacket();

            // Remove old stat modifiers
            packet.removeIf(str -> str.equals("mmocoreClass"));

            // Add newest one
            double total = getBase(instance.getStat()) - instance.getBase();
            if (total != 0)
                packet.addModifier(new StatModifier("mmocoreClass", instance.getStat(), total, ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER));

            // Then update the stat
            packet.runUpdate();
        }

        /*
         * This is here because it requires updates for the same reasons
         * as statistics (when the player level changes, when his class
         * changes, when he logs on..)
         *
         * This updates the player's PASSIVE skills
         */
        data.getMMOPlayerData().getPassiveSkillMap().removeModifiers("MMOCorePassiveSkill");
        for (ClassSkill skill : data.getProfess().getSkills())
            if (skill.getSkill().getTrigger().isPassive())
                data.getMMOPlayerData().getPassiveSkillMap().addModifier(skill.toPassive(data));
    }
}
