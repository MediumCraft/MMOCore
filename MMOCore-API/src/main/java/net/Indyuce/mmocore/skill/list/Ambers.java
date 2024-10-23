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
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.EntityLocationType;
import io.lumine.mythic.lib.util.ParabolicProjectile;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import io.lumine.mythic.lib.version.VParticle;
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
import org.jetbrains.annotations.NotNull;

public class Ambers extends SkillHandler<SimpleSkillResult> implements Listener {

    @BackwardsCompatibility(version = "1.20.5")
    private final boolean legacyParticles = MythicLib.plugin.getVersion().isUnder(1, 20, 5);

    public Ambers() {
        super(false);

        registerModifiers("percent");
    }

    @NotNull
    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult(meta.hasAttackSource()
                && meta.hasTargetEntity()
                && meta.getTargetEntityOrNull() instanceof LivingEntity);
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = (LivingEntity) skillMeta.getTargetEntityOrNull();
        Location loc = target.getLocation();

        double a = RANDOM.nextDouble() * 2 * Math.PI;
        new Amber(skillMeta.getCaster().getData(), EntityLocationType.BODY.getLocation(target), loc.clone().add(4 * Math.cos(a), 0, 4 * Math.sin(a)), skillMeta.getParameter("percent"));
    }

    @EventHandler
    public void spawnAmber(PlayerAttackEvent event) {
        MMOPlayerData data = event.getAttacker().getData();
        if (!event.getAttack().getDamage().hasType(DamageType.SKILL)) return;

        PassiveSkill passive = data.getPassiveSkillMap().getSkill(this);
        if (passive == null) return;

        passive.getTriggeredSkill().cast(new TriggerMetadata(event, TriggerType.API));
    }

    class Amber extends BukkitRunnable {
        private final Location loc;
        private final MMOPlayerData data;
        private final double percent;

        private int j;

        private static final double RADIUS_SQUARED = 3;

        private Amber(MMOPlayerData data, Location source, Location loc, double percent) {
            this.loc = loc;
            this.data = data;
            this.percent = percent / 100;

            final Amber amber = this;
            new ParabolicProjectile(source, loc, VParticle.REDSTONE.get(), () -> amber.runTaskTimer(MythicLib.plugin, 0, 3), 1, Color.ORANGE, 1.3f);
        }

        @Override
        public void run() {
            if (j++ > 66 || !data.isOnline() || !data.getPlayer().getWorld().equals(loc.getWorld())) {
                cancel();
                return;
            }

            if (data.getPlayer().getLocation().add(0, 1, 0).distanceSquared(loc) < RADIUS_SQUARED) {

                data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);

                // Give mana back
                PlayerData playerData = PlayerData.get(data);
                double missingMana = data.getStatMap().getStat("MAX_MANA") - playerData.getMana();
                playerData.giveMana(missingMana * percent, PlayerResourceUpdateEvent.UpdateReason.SKILL_REGENERATION);

                cancel();
                return;
            }

            for (int j = 0; j < 5; j++)
                if (legacyParticles)
                    loc.getWorld().spawnParticle(VParticle.ENTITY_EFFECT.get(), loc, 0, 1, 0.647, 0, 1);
                else
                    loc.getWorld().spawnParticle(VParticle.ENTITY_EFFECT.get(), loc, 0, 1, 0.647, 0, 1, Color.ORANGE);

            loc.getWorld().spawnParticle(VParticle.REDSTONE.get(), loc, 1, new Particle.DustOptions(Color.ORANGE, 1.3f));
        }
    }
}
