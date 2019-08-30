package net.Indyuce.mmocore.manager.social;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.social.Request;

public class RequestManager {
	private Set<Request> requests = new HashSet<>();

	/*
	 * flush friend requests async not to consume performance every 5 minutes so
	 * there is no memory overleak.
	 */
	public RequestManager() {
		new BukkitRunnable() {
			public void run() {
				flushRequests();
			}
		}.runTaskTimerAsynchronously(MMOCore.plugin, 60 * 20, 60 * 20 * 5);
	}

	public Request getRequest(UUID uuid) {
		for (Request request : requests)
			if (request.getUniqueId().equals(uuid))
				return request;
		return null;
	}

	public void registerRequest(Request request) {
		requests.add(request);
	}

	public void unregisterRequest(UUID uuid) {
		for (Iterator<Request> iterator = requests.iterator(); iterator.hasNext();) {
			Request next = iterator.next();
			if (next.getUniqueId().equals(uuid)) {
				iterator.remove();
				break;
			}
		}
	}

	public void flushRequests() {
		for (Iterator<Request> iterator = requests.iterator(); iterator.hasNext();) {
			Request next = iterator.next();
			if (next.isTimedOut())
				iterator.remove();
		}
	}
}
