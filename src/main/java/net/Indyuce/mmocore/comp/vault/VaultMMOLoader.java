package net.Indyuce.mmocore.comp.vault;

import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.api.load.MMOLoader;
import io.lumine.mythic.lib.api.MMOLineConfig;

import java.util.Arrays;
import java.util.List;

public class VaultMMOLoader extends MMOLoader {

	@Override
	public List<Trigger> loadTrigger(MMOLineConfig config) {

		if (config.getKey().equalsIgnoreCase("money"))
			return Arrays.asList(new MoneyTrigger(config));

		return null;
	}
}
