package net.Indyuce.mmocore.manager.social;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.social.Request;

public class RequestManager {
	private final Set<Request> requests = new HashSet<>();

	/*
	 * flush friend requests every 5 minutes so there is no memory overleak
	 */
	public RequestManager() {
		Bukkit.getScheduler().runTaskTimer(MMOCore.plugin, this::flushRequests, 60 * 20, 60 * 20 * 5);
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
		requests.removeIf(Request::isTimedOut);
	}
}
