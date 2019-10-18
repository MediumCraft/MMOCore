package net.Indyuce.mmocore.manager.social;

import java.util.HashSet;
import java.util.Set;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.guilds.Guild;
import net.Indyuce.mmocore.manager.MMOManager;

public class GuildManager extends MMOManager {
	private final Set<Guild> guilds = new HashSet<>();

	public void registerGuild(Guild guild) {
		guilds.add(guild);
	}

	public Guild newRegisteredGuild(PlayerData owner, String name) {
		Guild guild = new Guild(owner, name);
		registerGuild(guild);
		return guild;
	}

	public boolean isRegistered(Guild guild) {
		return guilds.contains(guild);
	}

	public void unregisterGuild(Guild guild) {
		guild.getMembers().forEach(member -> guild.removeMember(member));
		guilds.remove(guild);
	}

	@Override
	public void reload() {
	}

	@Override
	public void clear() {
	}
}
