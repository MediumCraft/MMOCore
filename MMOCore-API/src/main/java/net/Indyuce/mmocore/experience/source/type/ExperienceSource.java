package net.Indyuce.mmocore.experience.source.type;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;

public abstract class ExperienceSource<T> {
    private final ExperienceDispenser dispenser;

    public ExperienceSource(ExperienceDispenser dispenser) {
        this.dispenser = dispenser;
    }

    public ExperienceDispenser getDispenser() {
        return dispenser;
    }

    /**
     * This method is called ONCE per type of experience source.
     * This instanciates the manager for all of the exp sources
     * under that specific type.
     *
     * @return Instanciated experience source list
     */
    public abstract ExperienceSourceManager<?> newManager();

    public boolean matches(PlayerData player, T obj) {
        return getDispenser().shouldHandle(player) && matchesParameter(player, obj);
    }

    public abstract boolean matchesParameter(PlayerData player, T obj);
}
