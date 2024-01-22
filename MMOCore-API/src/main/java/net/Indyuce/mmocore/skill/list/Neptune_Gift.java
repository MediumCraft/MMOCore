package net.Indyuce.mmocore.skill.list;

import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Neptune_Gift extends SkillHandler<SimpleSkillResult> implements Listener {
    public Neptune_Gift() {
        super(false);

        registerModifiers("extra");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        throw new RuntimeException("Not supported");
    }

    @EventHandler
    public void a(PlayerResourceUpdateEvent event) {
        if (event.getPlayer().getLocation().getBlock().getType() == Material.WATER) {
            PassiveSkill skill = event.getData().getMMOPlayerData().getPassiveSkillMap().getSkill(this);
            if (skill == null)
                return;

            event.setAmount(event.getAmount() * (1 + event.getData().getMMOPlayerData().getSkillModifierMap().calculateValue(skill.getTriggeredSkill(), "extra") / 100));
        }
    }
}
