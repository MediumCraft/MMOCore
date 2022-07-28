package net.Indyuce.mmocore.listener;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.result.SkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerChangeClassEvent;
import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.player.playerclass.ClassTrigger;
import net.Indyuce.mmocore.player.playerclass.ClassTriggerType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 * This class still uses quest triggers because this was implemented
 * in 1.9 and 1.9 is big enough as an update.
 * <p>
 * Future implementation will utilize skill mechanics. MMOCore 1.10
 * will merge the quest triggers with the ML skill mechanics.
 *
 * @author jules
 * @see {@link ClassTrigger}
 */
@Deprecated
public class ClassTriggers implements Listener {

    @Deprecated
    private static final Map<DamageType, ClassTriggerType> damageTriggers = new HashMap<>();

    static {
        damageTriggers.put(DamageType.MAGIC, ClassTriggerType.MAGIC_DAMAGE);
        damageTriggers.put(DamageType.PHYSICAL, ClassTriggerType.PHYSICAL_DAMAGE);
        damageTriggers.put(DamageType.PROJECTILE, ClassTriggerType.PROJECTILE_DAMAGE);
        damageTriggers.put(DamageType.WEAPON, ClassTriggerType.WEAPON_DAMAGE);
        damageTriggers.put(DamageType.SKILL, ClassTriggerType.SKILL_DAMAGE);
        damageTriggers.put(DamageType.UNARMED, ClassTriggerType.SKILL_DAMAGE);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onAttack(PlayerAttackEvent event) {
        for (Map.Entry<DamageType, ClassTriggerType> entry : damageTriggers.entrySet())
            if (event.getDamage().hasType(entry.getKey()))
                applyTriggers(event.getPlayer(), entry.getValue(), () -> new TriggerMetadata(event.getAttack(), event.getEntity()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onClassChange(PlayerChangeClassEvent event) {
        Bukkit.getScheduler().runTask(MMOCore.plugin, () -> applyTriggers(event.getData(), ClassTriggerType.CLASS_CHOSEN));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLevelUp(PlayerLevelUpEvent event) {
        applyTriggers(event.getData(), ClassTriggerType.LEVEL_UP);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        applyTriggers(event.getPlayer(), ClassTriggerType.BREAK_BLOCK);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockBreakEvent event) {
        applyTriggers(event.getPlayer(), ClassTriggerType.PLACE_BLOCK);
    }

    private SkillResult applyTriggers(Player player, ClassTriggerType type) {
        return applyTriggers(PlayerData.get(player), type);
    }

    private SkillResult applyTriggers(PlayerData player, ClassTriggerType type) {
        return applyTriggers(player, type, () -> new TriggerMetadata(player.getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND), null, null));
    }

    @Nullable
    private SkillResult applyTriggers(Player player, ClassTriggerType type, Provider<TriggerMetadata> triggerMetaProvider) {
        return applyTriggers(PlayerData.get(player), type, triggerMetaProvider);
    }

    /**
     * Apply class shortcut skills from a specific class trigger type.
     *
     * @param player              Player triggering
     * @param type                Trigger type
     * @param triggerMetaProvider Small optimization: if no shortcut skill is found
     *                            with the corresponding trigger type, trigger meta
     *                            is not calculated which saves computations
     * @return Skill result or null if no shortcut skill was cast
     */
    @Nullable
    private SkillResult applyTriggers(PlayerData player, ClassTriggerType type, Provider<TriggerMetadata> triggerMetaProvider) {
        ClassTrigger trigger = player.getProfess().getClassTrigger(type);
        return trigger == null ? null : trigger.trigger(triggerMetaProvider.get());
    }
}
