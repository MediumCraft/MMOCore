package net.Indyuce.mmocore.api.player.stats;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.player.skill.PassiveSkillMap;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.player.stats.StatInfo;
import net.Indyuce.mmocore.skill.ClassSkill;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerStats {
    private final PlayerData data;

    /**
     * Util class to easily manipulate the MythicLib stat map
     *
     * @param data Player
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

    @Deprecated
    public StatInstance getInstance(String stat) {
        return getMap().getInstance(stat);
    }

    public double getStat(String stat) {
        return getMap().getInstance(stat).getTotal();
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
     * MythicLib. Must be ran everytime the player levels up or changes class.
     * <p>
     * This is also called when reloading the plugin to make class setup easier,
     * see {@link PlayerData#reload()} for more info
     */
    public synchronized void updateStats() {
        for (String stat : MMOCore.plugin.statManager.getRegistered()) {
            final StatInstance instance = getMap().getInstance(stat);
            final StatInstance.ModifierPacket packet = instance.newPacket();

            // Remove old stat modifier
            packet.remove("mmocoreClass");

            // Add newest one
            final double total = getBase(instance.getStat()) - instance.getBase();
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
        final PassiveSkillMap skillMap = data.getMMOPlayerData().getPassiveSkillMap();

        if (!MMOCore.plugin.configManager.passiveSkillNeedBound) {
            skillMap.removeModifiers("MMOCorePassiveSkill");
            data.getProfess().getSkills()
                    .stream()
                    .filter((classSkill) -> classSkill.getSkill().getTrigger().isPassive() && data.hasUnlocked(classSkill) && data.hasUnlockedLevel(classSkill))
                    .forEach(classSkill -> skillMap.addModifier(classSkill.toPassive(data)));

        }

        // This updates the player's class SCRIPTS
        skillMap.removeModifiers("MMOCoreClassScript");
        for (PassiveSkill script : data.getProfess().getScripts())
            skillMap.addModifier(script);
    }
}
