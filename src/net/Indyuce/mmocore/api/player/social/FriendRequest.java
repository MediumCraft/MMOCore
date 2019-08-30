package net.Indyuce.mmocore.api.player.social;

import org.bukkit.Sound;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;

public class FriendRequest extends Request {
	private PlayerData creator, target;

	public FriendRequest(PlayerData creator, PlayerData target) {
		this.creator = creator;
		this.target = target;
	}

	public PlayerData getCreator() {
		return creator;
	}

	public PlayerData getTarget() {
		return target;
	}

	public void deny() {
		MMOCore.plugin.requestManager.unregisterRequest(getUniqueId());
		target.getPlayer().playSound(target.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	}

	public void accept() {
		creator.addFriend(target.getUniqueId());
		target.addFriend(creator.getUniqueId());
		creator.getPlayer().sendMessage(MMOCore.plugin.configManager.getSimpleMessage("now-friends", "player", target.getPlayer().getName()));
		target.getPlayer().sendMessage(MMOCore.plugin.configManager.getSimpleMessage("now-friends", "player", creator.getPlayer().getName()));
		MMOCore.plugin.requestManager.unregisterRequest(getUniqueId());
	}
}