package net.Indyuce.mmocore.api.util.input;

import net.Indyuce.mmocore.MMOCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Consumer;

public abstract class PlayerInput implements Listener {
	private final Player player;
	private final Consumer<String> output;

	public PlayerInput(Player player, Consumer<String> output) {
		this.player = player;
		this.output = output;

		Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
	}

	public void output(String input) {
		output.accept(input);
	}

	public Player getPlayer() {
		return player;
	}

	public abstract void close();

	public enum InputType {
		FRIEND_REQUEST,
		PARTY_INVITE,
		GUILD_INVITE,
		GUILD_CREATION_TAG,
		GUILD_CREATION_NAME;

		public String getLowerCaseName() {
			return name().toLowerCase().replace("_", "-");
		}
	}
}
