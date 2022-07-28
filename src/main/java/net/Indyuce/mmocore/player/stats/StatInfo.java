package net.Indyuce.mmocore.player.stats;

import io.lumine.mythic.lib.manager.StatManager;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.experience.Profession;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Jules
 * @impl MMOCore used to have a giant enum of all the stat types
 *         which is now incompatible with MythicLib because the MMO plugins
 *         now have completely OPEN to edition numeric stat registries
 */
public class StatInfo {
    public final String name;

    /**
     * Profession linked to that stat. Stats which have a profession linked to
     * them do NOT scale on the main player level but rather on that specific
     * profession level
     */
    public Profession profession;

    /**
     * Default formula for the stat
     */
    public LinearValue defaultInfo;

    public StatInfo(String name) {
        this.name = name;
    }

    @NotNull
    @Deprecated
    public String format(double d) {
        return StatManager.format(name, d);
    }

    @NotNull
    public LinearValue getDefaultFormula() {
        return defaultInfo == null ? LinearValue.ZERO : defaultInfo;
    }

    @NotNull
    public static StatInfo valueOf(String str) {
        StatInfo found = MMOCore.plugin.statManager.getInfo(str);
        return found == null ? new StatInfo(str) : found;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatInfo statInfo = (StatInfo) o;
        return name.equals(statInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
