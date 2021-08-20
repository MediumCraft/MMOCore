package net.Indyuce.mmocore.experience.source.type;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.provider.ExperienceDispenser;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;

/**
 * Atrocious API that really needs rewriting
 *
 * @author cympe
 */
public abstract class ExperienceSource<T> {
    private final ExperienceDispenser dispenser;

    public ExperienceSource(ExperienceDispenser dispenser) {
        this.dispenser = dispenser;
    }

    public ExperienceDispenser getDispenser() {
        return dispenser;
    }

    public abstract ExperienceSourceManager<?> newManager();

    public boolean matches(PlayerData player, T obj) {
        return getDispenser().matches(player) && matchesParameter(player, obj);
    }

    public abstract boolean matchesParameter(PlayerData player, T obj);
}
