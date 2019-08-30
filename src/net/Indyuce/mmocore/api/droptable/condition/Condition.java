package net.Indyuce.mmocore.api.droptable.condition;

import net.Indyuce.mmocore.api.load.MMOLineConfig;

public abstract class Condition {
	private final String id;

	public Condition(MMOLineConfig config) {
		this.id = config.getKey();
	}

	public String getId() {
		return id;
	}

	public abstract boolean isMet(ConditionInstance entity);
}
