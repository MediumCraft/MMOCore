package net.Indyuce.mmocore.command.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;

public abstract class CommandMap {
	private final Map<String, CommandMap> floors = new HashMap<>();
	private final String id;
	private final CommandMap parent;

	public CommandMap(CommandMap parent, String id) {
		this.id = id;
		this.parent = parent;
	}

	public String getId() {
		return id;
	}

	public String getPath() {
		return (hasParent() ? parent.getPath() + " " : "") + getId();
	}

	public Collection<CommandMap> getFloors() {
		return floors.values();
	}

	public boolean hasParent() {
		return parent != null;
	}

	public CommandMap getFloor(String str) {
		return floors.get(str.toLowerCase());
	}

	public boolean hasFloor(String str) {
		return floors.containsKey(str.toLowerCase());
	}

	public void addFloor(CommandMap floor) {
		floors.put(floor.getId(), floor);
	}

	public abstract CommandResult execute(CommandSender sender, String[] args);

	public Set<String> getKeys() {
		return floors.keySet();
	}

	public List<String> calculateTabCompletion(int n) {

		/*
		 * add extra floor keys
		 */
		List<String> list = new ArrayList<>();
		getKeys().forEach(key -> list.add(key));

		/*
		 * add final arguments if needed.
		 */
		if (isEnd())
			if (((CommandEnd) this).getParameters().size() > n)
				((CommandEnd) this).getParameters().get(n).autoComplete(list);

		return list;
	}

	public List<String> calculateUsageList() {
		return calculateUsageList(getPath(), new ArrayList<>());
	}

	private List<String> calculateUsageList(String path, List<String> usages) {

		/*
		 * calculate for final arguments
		 */
		if (isEnd())
			usages.add(path + " " + ((CommandEnd) this).formatParameters());

		for (CommandMap floor : getFloors())
			floor.calculateUsageList(new String(path + " " + floor.getId()), usages);

		return usages;
	}

	public boolean isEnd() {
		return this instanceof CommandEnd;
	}

	public enum CommandResult {

		/*
		 * command cast successfully, nothing to do
		 */
		SUCCESS,

		/*
		 * command cast unsuccessfully, display message handled via command
		 * floor
		 */
		FAILURE,

		/*
		 * send command usage
		 */
		THROW_USAGE;
	}
}
