package net.Indyuce.mmocore.manager.data.mysql;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.player.social.guilds.Guild;
import net.Indyuce.mmocore.manager.data.GuildDataManager;

public class MySQLGuildDataManager extends GuildDataManager {
	@Override
	public void save(Guild guild) {
		ConfigFile config = new ConfigFile(guild);
		config.getConfig().set("name", guild.getName());
		config.getConfig().set("tag", guild.getTag());
		config.getConfig().set("owner", guild.getOwner().toString());
		
		List<String> memberList = new ArrayList<>();
		guild.getMembers().forEach(uuid -> memberList.add(uuid.toString()));
		config.getConfig().set("members", memberList);
		
		config.save();
	}
	
	@Override
	public void load() {
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
	public void delete(Guild guild) {
		new ConfigFile(guild).delete();
	}
}
