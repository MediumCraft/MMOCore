package net.Indyuce.mmocore.api.player.social.guilds;

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
		guild.getMembers().forEach(member -> MMOCore.plugin.configManager.getSimpleMessage("guild-joined-other", "player", target.getPlayer().getName()).send(member.getPlayer()));
		MMOCore.plugin.configManager.getSimpleMessage("guild-joined", "owner", guild.getOwner().getPlayer().getName()).send(target.getPlayer());
		guild.addMember(target);
		InventoryManager.GUILD_VIEW.newInventory(target).open();
		MMOCore.plugin.requestManager.unregisterRequest(getUniqueId());
	}
}