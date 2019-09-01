package net.Indyuce.mmocore.command.api;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.util.Consumer;

import net.Indyuce.mmocore.MMOCore;

public class Parameter {
	private final String key;
	// private final ParameterType type;ParameterType type,
	private final Consumer<List<String>> autoComplete;

	public static final Parameter PROFESSION = new Parameter("<profession/main>", list -> {
		MMOCore.plugin.professionManager.getAll().forEach(profession -> list.add(profession.getId()));
		list.add("main");
	});
	public static final Parameter PLAYER = new Parameter("<player>", list -> Bukkit.getOnlinePlayers().forEach(online -> list.add(online.getName())));
	public static final Parameter PLAYER_OPTIONAL = new Parameter("(player)", list -> Bukkit.getOnlinePlayers().forEach(online -> list.add(online.getName())));
	public static final Parameter AMOUNT = new Parameter("<amount>", list -> {
		for (int j = 0; j <= 10; j++)
			list.add("" + j);
	});

	public Parameter(String key, Consumer<List<String>> autoComplete) {
		this.key = key;
		// this.type = type;
		this.autoComplete = autoComplete;
	}

	public String getKey() {
		return key;
	}

	// public ParameterType getType() {
	// return type;
	// }

	public void autoComplete(List<String> list) {
		autoComplete.accept(list);
	}

	// public enum ParameterType {
	// OPTIONAL('(', ')'),
	// REQUIRED('<', '>');
	//
	// private final char prefix, suffix;
	//
	// private ParameterType(char... chars) {
	// this.prefix = chars[0];
	// this.suffix = chars[1];
	// }
	//
	// public String format(Parameter parameter) {
	// return prefix + parameter.getKey() + suffix;
	// }
	// }
}