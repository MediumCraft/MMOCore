package net.Indyuce.mmocore.api.player.stats;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.skill.ClassSkill;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

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

    /**
     * Allows for stat type enum to have dynamic professions.
     * ID FORMAT: STAT_TYPE_HERE_PROFESSION_HERE
     *
     * @param type the type of stat
     * @param profession the stat's specific permission
     * @return instance of found stat
     * @author Ehhthan
     */
    @NotNull
    public StatInstance getInstance(StatType type, @Nullable Profession profession) {
        if (profession == null)
            return getInstance(type);
        else {
            String id = (type.name() + '_' + profession.getId()).replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
            return getInstance(id);
        }
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
