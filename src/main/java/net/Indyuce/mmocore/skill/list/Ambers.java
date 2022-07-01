package net.Indyuce.mmocore.skill.list;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.util.EntityLocationType;
import io.lumine.mythic.lib.util.ParabolicProjectile;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class Ambers extends SkillHandler<SimpleSkillResult> implements Listener {
    public Ambers() {
        super(false);

        registerModifiers("percent");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult(meta.hasAttackBound() && meta.hasTargetEntity() && meta.getTargetEntityOrNull() instanceof LivingEntity);
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = (LivingEntity) skillMeta.getTargetEntityOrNull();
        Location loc = target.getLocation();

        double a = random.nextDouble() * 2 * Math.PI;
        new Amber(skillMeta.getCaster().getData(), EntityLocationType.BODY.getLocation(target), loc.clone().add(4 * Math.cos(a), 0, 4 * Math.sin(a)), skillMeta.getModifier("percent"));
    }

    @EventHandler
    public void spawnAmber(PlayerAttackEvent event) {
        MMOPlayerData data = event.getData();
        if (!event.getAttack().getDamage().hasType(DamageType.SKILL))
            return;

        PassiveSkill passive = data.getPassiveSkillMap().getSkill(this);
        if (passive == null)
            return;

        passive.getTriggeredSkill().cast(new TriggerMetadata(event.getAttack(), event.getEntity()));
    }

    public static class Amber extends BukkitRunnable {
        private final Location loc;
        private final MMOPlayerData data;
        private final double percent;

        private int j;

        private Amber(MMOPlayerData data, Location source, Location loc, double percent) {
            this.loc = loc;
            this.data = data;
            this.percent = percent / 100;

            final Amber amber = this;
            new ParabolicProjectile(source, loc, Particle.REDSTONE, () -> amber.runTaskTimer(MythicLib.plugin, 0, 3), 1, Color.ORANGE, 1.3f);
        }

        @Override
        public void run() {
            if (j++ > 66 || !data.isOnline() || !data.getPlayer().getWorld().equals(loc.getWorld())) {
                cancel();
                return;
            }

            if (data.getPlayer().getLocation().distanceSquared(loc) < 2) {

                data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);
                // data.getSkillData().ambers++;

                // Give mana back
                PlayerData playerData = PlayerData.get(data.getUniqueId());
                double missingMana = data.getStatMap().getStat("MAX_MANA") - playerData.getMana();
                playerData.giveMana(missingMana * percent, PlayerResourceUpdateEvent.UpdateReason.SKILL_REGENERATION);

                cancel();
                return;
            }

            for (int j = 0; j < 5; j++)
                loc.getWorld().spawnParticle(Particle.SPELL_MOB, loc, 0, 1, 0.647, 0, 1);
            loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, new Particle.DustOptions(Color.ORANGE, 1.3f));
        }
    }
}
