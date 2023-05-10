package net.Indyuce.mmocore.comp.vault;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.load.MMOLoader;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;

public class VaultMMOLoader extends MMOLoader {

    @Override
    public Trigger loadTrigger(MMOLineConfig config) {

        if (config.getKey().equalsIgnoreCase("money"))
            return new MoneyTrigger(config);

        return null;
    }
}
