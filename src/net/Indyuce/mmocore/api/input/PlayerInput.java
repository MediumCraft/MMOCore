package net.Indyuce.mmocore.api.input;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Consumer;

import net.Indyuce.mmocore.MMOCore;

public abstract class PlayerInput implements Listener {
	private final Player player;
	private final Consumer<String> output;

	public PlayerInput(Player player, Consumer<String> output) {
		this.player = player;
		this.output = output;

		Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
	}

	public Consumer<String> getOutput() {
		return output;
	}

	public Player getPlayer() {
		return player;
	}

	public abstract void close();

	public enum InputType {
		FRIEND_REQUEST,

		PARTY_INVITE;

		public String getLowerCaseName() {
			return name().toLowerCase().replace("_", "-");
		}
	}
}
