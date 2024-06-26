package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.binding.SkillSlot;
import net.Indyuce.mmocore.api.quest.trigger.api.Removable;
import org.apache.commons.lang.Validate;


public class UnlockSlotTrigger extends Trigger implements Removable {
    private final int slot;

    public UnlockSlotTrigger(MMOLineConfig config) {
        super(config);

        config.validateKeys("slot");
        try {
            slot = Integer.parseInt(config.getString("slot"));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Slot should be a number");
        }
        Validate.isTrue(slot > 0, "Slot number must be positive");
    }

    @Override
    public void apply(PlayerData player) {
        player.unlock(player.getProfess().getSkillSlot(slot));
    }

    @Override
    public void remove(PlayerData player) {
        player.lock(player.getProfess().getSkillSlot(slot));
    }
}
