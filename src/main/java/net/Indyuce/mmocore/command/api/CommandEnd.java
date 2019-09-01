package net.Indyuce.mmocore.command.api;

import java.util.ArrayList;
import java.util.List;

public abstract class CommandEnd extends CommandMap {
	private final List<Parameter> parameters = new ArrayList<>();

	public CommandEnd(CommandMap parent, String id) {
		super(parent, id);
	}

	public void addParameter(Parameter param) {
		parameters.add(param);
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public String formatParameters() {
		String str = "";
		for (Parameter param : parameters)
			str += param.getKey() + " ";
		return str.isEmpty() ? str : str.substring(0, str.length() - 1);
	}
}
