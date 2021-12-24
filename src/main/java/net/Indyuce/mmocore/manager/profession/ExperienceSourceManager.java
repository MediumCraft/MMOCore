package net.Indyuce.mmocore.manager.profession;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.experience.source.type.ExperienceSource;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

public abstract class ExperienceSourceManager<T extends ExperienceSource> implements Listener {

    /**
     * List of all active experience sources
     */
    private final Set<T> sources = new HashSet<>();

    public ExperienceSourceManager() {
        Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
    }

    public void registerSource(T source) {
        sources.add(source);
    }

    public Set<T> getSources() {
        return sources;
    }
}
