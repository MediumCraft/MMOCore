package net.Indyuce.mmocore.manager.profession;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import net.Indyuce.mmocore.MMOCore;

public abstract class ExperienceManager<T> implements Listener {
	private final Set<T> sources = new HashSet<>();

	public ExperienceManager() {
		Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
	}

	public void register(T source) {
		sources.add(source);
	}
	
	public Set<T> getSources() {
		return sources;
	}
}
