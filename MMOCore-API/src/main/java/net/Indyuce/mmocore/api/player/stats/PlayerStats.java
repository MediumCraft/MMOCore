package net.Indyuce.mmocore.api.player.stats;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.player.skill.PassiveSkillMap;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.player.stats.StatInfo;
import net.Indyuce.mmocore.skill.ClassSkill;

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
        return getMap().getStat(stat);
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

    public void updateStats() {
        updateStats(false);
    }

    /**
     * Used to update MMOCore stat modifiers due to class and send them over to
     * MythicLib. Must be ran everytime the player levels up, changes class or
     * when the plugin reloads.
     * <p>
     * Login scripts are a pretty special case of scripts/skills since they are
     * not loaded yet when MythicLib triggers them naturally. Therefore, they
     * need to be cast as soon as they are loaded into the MMOCore player data.
     *
     * @param castLoginScripts Should login scripts be cast
     */
    public synchronized void updateStats(boolean castLoginScripts) {

        // Update player stats
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

        // Updates the player's unbindable CLASS passive skills
        final PassiveSkillMap skillMap = data.getMMOPlayerData().getPassiveSkillMap();
        skillMap.removeModifiers("MMOCorePassiveSkillNotBound");
        for (ClassSkill skill : data.getProfess().getSkills())
            if (!skill.needsBound()
                    && skill.getSkill().getTrigger().isPassive()
                    && skill.getSkill().getTrigger() != TriggerType.LOGIN
                    && data.hasUnlocked(skill)
                    && data.hasUnlockedLevel(skill))
                skillMap.addModifier(skill.toPassive(data));

        // Updates the player's CLASS scripts
        skillMap.removeModifiers("MMOCoreClassScript");
        for (PassiveSkill script : data.getProfess().getScripts())
            if (script.getType() != TriggerType.LOGIN) skillMap.addModifier(script);

        // If data hasn't been synchronized yet, cast LOGIN scripts
        if (castLoginScripts) {

            // Call class login skills
            for (ClassSkill skill : data.getProfess().getSkills())
                if (skill.getSkill().getTrigger() == TriggerType.LOGIN)
                    skill.toCastable(data).cast(data.getMMOPlayerData());

            // Call class login scripts
            for (PassiveSkill skill : data.getProfess().getScripts())
                if (skill.getType() == TriggerType.LOGIN) skill.getTriggeredSkill().cast(data.getMMOPlayerData());
        }
    }
}
