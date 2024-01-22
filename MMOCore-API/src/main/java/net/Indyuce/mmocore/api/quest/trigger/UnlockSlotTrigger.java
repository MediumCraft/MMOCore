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
        }catch(NumberFormatException e){
            throw new IllegalArgumentException("The slot should be a number");
        }
        Validate.isTrue(slot > 0 && slot <= MMOCore.plugin.configManager.maxSkillSlots, "The slot should be between 1 and " + MMOCore.plugin.configManager.maxSkillSlots);
    }

    @Override
    public void apply(PlayerData player) {
        final SkillSlot skillSlot = player.getProfess().getSkillSlot(slot);
        if (!player.hasUnlocked(skillSlot))
            player.unlock(skillSlot);
    }

    @Override
    public void remove(PlayerData player) {
        final SkillSlot skillSlot = player.getProfess().getSkillSlot(slot);
        if (player.hasUnlocked(skillSlot))
            player.lock(skillSlot);
    }
}
