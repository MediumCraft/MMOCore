package net.Indyuce.mmocore.skill.list;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Sneaky_Picky extends SkillHandler<SimpleSkillResult> implements Listener {
    public Sneaky_Picky() {
        super(false);

        registerModifiers("extra");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult(meta.hasAttackBound() && meta.hasTargetEntity() && meta.getTargetEntityOrNull() instanceof LivingEntity);
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = (LivingEntity) skillMeta.getTargetEntity();
        skillMeta.getAttack().getDamage().multiplicativeModifier(1 + skillMeta.getModifier("extra") / 100, DamageType.WEAPON);
        target.getWorld().spawnParticle(Particle.SMOKE_NORMAL, target.getLocation().add(0, target.getHeight() / 2, 0), 64, 0, 0, 0, .05);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 2);
    }

    @EventHandler
    public void a(PlayerAttackEvent event) {
        MMOPlayerData data = event.getData();
        if (!event.getAttack().getDamage().hasType(DamageType.WEAPON) || PlayerData.get(data.getUniqueId()).isInCombat())
            return;

        PassiveSkill skill = data.getPassiveSkillMap().getSkill(this);
        if (skill == null)
            return;

        skill.getTriggeredSkill().cast(new TriggerMetadata(event.getAttack(), event.getEntity()));
    }
}
