package net.Indyuce.mmocore.api.load;

import org.bukkit.configuration.file.FileConfiguration;

public abstract class PostLoadObject {
	private FileConfiguration config;

	/*
	 * objects which must load some data afterwards, like quests which must load
	 * their parent quests after all quests were initialized or classes which
	 * must load their subclasses
	 */
	public PostLoadObject(FileConfiguration config) {
		this.config = config;
	}

	public void postLoad() {
		whenPostLoaded(config);

		/*
		 * clean config object for garbage collection
		 */
		config = null;
	}

	protected abstract void whenPostLoaded(FileConfiguration config);
}