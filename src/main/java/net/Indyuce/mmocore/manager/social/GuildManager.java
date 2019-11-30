package net.Indyuce.mmocore.manager.social;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.player.social.guilds.Guild;
import net.Indyuce.mmocore.manager.MMOManager;

public class GuildManager extends MMOManager {
	private final Map<String, Guild> guilds = new HashMap<>();
	private GuildConfiguration config;

	public Guild newRegisteredGuild(UUID owner, String name, String tag) {
		Guild guild = new Guild(owner, name, tag);
		registerGuild(guild);
		return guild;
	}

	public void registerGuild(Guild guild) {
		guilds.put(guild.getId(), guild);
	}

	public boolean isRegistered(Guild guild) {
		return guilds.containsValue(guild);
	}

	public boolean isRegistered(String tag) {
		return guilds.containsKey(tag);
	}
	
	public void unregisterGuild(Guild guild) {
		guild.getMembers().forEach(member -> guild.removeMember(member));
		guilds.remove(guild.getId());
		
		new ConfigFile(guild).delete();
	}

	public void save() {
		for(Guild guild : guilds.values()) {
			ConfigFile config = new ConfigFile(guild);
			guild.saveInConfig(config.getConfig());
			config.save();
		}
	}
	
	public GuildConfiguration getConfig()
	{ return config; }

	@Override
	public void reload() {
		config = new GuildConfiguration();

		File guildsFolder = new File(MMOCore.plugin.getDataFolder(), "guilds");
		if(!guildsFolder.exists()) guildsFolder.mkdirs();
	    for (File file : guildsFolder.listFiles()) {
	        if (!file.isDirectory() && file.getName().substring(file.getName().lastIndexOf('.')).equalsIgnoreCase(".yml")) {
	        	FileConfiguration c = YamlConfiguration.loadConfiguration(file);
	        	Guild guild = newRegisteredGuild(UUID.fromString(c.getString("owner")), c.getString("name"), c.getString("tag"));
	        	for(String m : c.getStringList("members"))
	        		guild.registerMember(UUID.fromString(m));
	        }
	    }
	}

	@Override
	public void clear() { }

	public Guild getGuild(String guild)
	{ return guilds.get(guild); }
	
	public class GuildConfiguration {
		private final String prefix;
		private final boolean uppercaseTags;
		private final NamingRules tagRules, nameRules;
		
		public GuildConfiguration() {
			FileConfiguration config = new ConfigFile("guilds").getConfig();
			
			prefix = config.getString("chat-prefix", "*");
			uppercaseTags = config.getBoolean("uppercase-tags", true);
			tagRules = new NamingRules(config.getConfigurationSection("rules.tag"));
			nameRules = new NamingRules(config.getConfigurationSection("rules.name"));
		}
		
		public String getPrefix()
		{ return prefix; }
		public boolean shouldUppercaseTags()
		{ return uppercaseTags; }
		public NamingRules getTagRules()
		{ return tagRules; }
		public NamingRules getNameRules()
		{ return nameRules; }
		
		public class NamingRules {
			private final String regex;
			private final int min, max;
			
			public NamingRules(ConfigurationSection config) {
				regex = config.getString("matches", "[a-zA-Z-_!?]+");
				min = config.getInt("min-length", 3);
				max = config.getInt("max-length", 5);
			}
			
			public String getRegex()
			{ return regex; }
			public int getMin()
			{ return min; }
			public int getMax()
			{ return max; }
		}
	}

	// Used to check if player was kicked while offline
	public Guild stillInGuild(UUID uuid, String id) {
		Guild guild = getGuild(id);
		if(guild.getMembers().has(uuid))
			return guild;
		return null;
	}
}
