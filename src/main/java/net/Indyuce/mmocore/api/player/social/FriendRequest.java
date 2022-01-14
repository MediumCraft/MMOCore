package net.Indyuce.mmocore.api.player.social;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Sound;

public class FriendRequest extends Request {
	private final PlayerData target;

	public FriendRequest(PlayerData creator, PlayerData target) {
		super(creator);

		this.target = target;
	}

	public PlayerData getTarget() {
		return target;
	}

	public void deny() {
		MMOCore.plugin.requestManager.unregisterRequest(getUniqueId());
		if(!target.isOnline()) return;
		target.getPlayer().playSound(target.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	}

	public void accept() {
		getCreator().setLastActivity(PlayerActivity.FRIEND_REQUEST, 0);
		getCreator().addFriend(target.getUniqueId());
		target.addFriend(getCreator().getUniqueId());
		if(target.isOnline() && getCreator().isOnline()) {
			MMOCore.plugin.configManager.getSimpleMessage("now-friends", "player", target.getPlayer().getName()).send(getCreator().getPlayer());
			MMOCore.plugin.configManager.getSimpleMessage("now-friends", "player", getCreator().getPlayer().getName()).send(target.getPlayer());
		}
		MMOCore.plugin.requestManager.unregisterRequest(getUniqueId());
	}
}