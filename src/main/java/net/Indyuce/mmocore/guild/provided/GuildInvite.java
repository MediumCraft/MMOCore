package net.Indyuce.mmocore.guild.provided;

import org.bukkit.Bukkit;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.Request;
import net.Indyuce.mmocore.manager.InventoryManager;

public class GuildInvite extends Request {
	private final PlayerData target;
	private final Guild guild;

	public GuildInvite(Guild guild, PlayerData creator, PlayerData target) {
		super(creator);

		this.guild = guild;
		this.target = target;
	}

	public Guild getGuild() {
		return guild;
	}

	public PlayerData getPlayer() {
		return target;
	}

	public void deny() {
		MMOCore.plugin.requestManager.unregisterRequest(getUniqueId());
	}

	public void accept() {
		guild.removeLastInvite(getCreator().getPlayer());
		guild.getMembers().forEach(member -> {
			if(Bukkit.getPlayer(member) != null) {
					MMOCore.plugin.configManager.getSimpleMessage("guild-joined-other", "player",
							target.getPlayer().getName()).send(Bukkit.getPlayer(member));

				MMOCore.plugin.configManager.getSimpleMessage("guild-joined", "owner",
						Bukkit.getPlayer(guild.getOwner()).getName()).send(target.getPlayer());
				}}

				);

		guild.addMember(target.getUniqueId());
		InventoryManager.GUILD_VIEW.newInventory(target).open();
		MMOCore.plugin.requestManager.unregisterRequest(getUniqueId());
	}
}