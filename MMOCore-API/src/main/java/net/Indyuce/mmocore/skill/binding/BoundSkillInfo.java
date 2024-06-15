package net.Indyuce.mmocore.skill.binding;

import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.util.Closeable;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.SkillModifierTrigger;
import net.Indyuce.mmocore.skill.ClassSkill;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoundSkillInfo implements Closeable {
    private final SkillSlot skillSlot;
    private final PlayerData playerData;
    private final ClassSkill classSkill;

    /**
     * Non-permanent passive skills must be registered inside
     * MythicLib when bound. When set to null, the skill is either
     * active or permanent passive.
     * <p>
     * This does NOT indicate the skill being passive!
     */
    @Nullable
    private final PassiveSkill registered;

    private boolean open = true;

    public BoundSkillInfo(@NotNull SkillSlot skillSlot, @NotNull ClassSkill classSkill, @NotNull PlayerData playerData) {
        this.skillSlot = skillSlot;
        this.classSkill = classSkill;
        this.playerData = playerData;

        // Apply skill buffs associated to the slot
        for (SkillModifierTrigger skillModifierTrigger : skillSlot.getSkillModifierTriggers())
            if (skillModifierTrigger.getTargetSkills().contains(classSkill.getSkill().getHandler()))
                skillModifierTrigger.apply(playerData, classSkill.getSkill().getHandler());

        if (classSkill.getSkill().getTrigger().isPassive() && !classSkill.isPermanent()) {
            registered = classSkill.toPassive(playerData);
            registered.register(playerData.getMMOPlayerData());
        } else registered = null;
    }

    @NotNull
    public ClassSkill getClassSkill() {
        return classSkill;
    }

    @NotNull
    public PlayerData getPlayerData() {
        return playerData;
    }

    @NotNull
    public SkillSlot getSkillSlot() {
        return skillSlot;
    }

    public boolean isPassive() {
        return classSkill.getSkill().getTrigger().isPassive();
    }

    @Override
    public void close() {
        Validate.isTrue(open, "BoundSkillInfo has already been closed");
        open = false;

        // Unregister skill if non-permanent passive
        if (registered != null) registered.unregister(playerData.getMMOPlayerData());

        // Remove skill buffs associated to the slot
        skillSlot.getSkillModifierTriggers().forEach(skillBuffTrigger -> skillBuffTrigger.remove(playerData, classSkill.getSkill().getHandler()));
    }
}
