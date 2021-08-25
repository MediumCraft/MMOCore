package net.Indyuce.mmocore.loot.droptable.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;

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
