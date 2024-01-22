package net.Indyuce.mmocore.manager.profession;

import io.lumine.mythic.lib.util.Closeable;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.experience.source.type.ExperienceSource;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

public abstract class ExperienceSourceManager<T extends ExperienceSource> implements Listener, Closeable {

    /**
     * List of all active experience sources
     */
    private final Set<T> sources = new HashSet<>();
    private boolean open = true;

    public ExperienceSourceManager() {
        Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
    }

    public void registerSource(T source) {
        sources.add(source);
    }

    public void whenClosed() {
        // Nothing by default
    }

    @Override
    public void close() {
        Validate.isTrue(open, "Manager is already closed");
        open = false;

        HandlerList.unregisterAll(this);
        whenClosed();
    }

    public Set<T> getSources() {
        return sources;
    }
}
