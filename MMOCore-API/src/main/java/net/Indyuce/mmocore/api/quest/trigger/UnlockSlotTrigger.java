package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.skillbinding.SkillSlot;
import net.Indyuce.mmocore.api.quest.trigger.api.Removable;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import org.apache.commons.lang.Validate;

import java.util.Objects;


public class UnlockSlotTrigger extends Trigger implements Removable {
    private final int slot;

    public UnlockSlotTrigger(MMOLineConfig config) {
        super(config);
        config.validateKeys("slot");
        slot = Integer.parseInt("slot");
        Validate.isTrue(slot > 0 && slot <= MMOCore.plugin.configManager.maxSlots, "The slot should be between 1 and " + MMOCore.plugin.configManager.maxSlots);
    }

    @Override
    public void apply(PlayerData player) {
        SkillSlot skillSlot = player.getProfess().getSkillSlot(slot);
        if (!player.hasUnlocked(skillSlot))
            player.unlock(skillSlot);
    }

    @Override
    public void remove(PlayerData player) {
        SkillSlot skillSlot = player.getProfess().getSkillSlot(slot);
        if (player.hasUnlocked(skillSlot))
            player.lock(skillSlot);
    }
}
