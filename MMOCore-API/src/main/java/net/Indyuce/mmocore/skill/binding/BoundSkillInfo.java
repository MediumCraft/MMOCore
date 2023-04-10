package net.Indyuce.mmocore.skill.binding;

import io.lumine.mythic.lib.player.skill.PassiveSkill;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.ClassSkill;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class BoundSkillInfo {
    private final PlayerData playerData;
    private final ClassSkill classSkill;

    private UUID passiveSkillId;

    public BoundSkillInfo(ClassSkill classSkill, PlayerData playerData) {
        this.classSkill = classSkill;
        this.playerData = playerData;
    }

    public BoundSkillInfo(ClassSkill classSkill, PlayerData playerData, UUID passiveSkillId) {
        this.classSkill = classSkill;
        this.playerData = playerData;
        this.passiveSkillId = passiveSkillId;
    }

    /**
     * Used on update to refresh the classSkill & all references to old data.
     */
    public BoundSkillInfo(BoundSkillInfo info) {
        this.playerData = info.getPlayerData();
        this.classSkill = Objects.requireNonNull(playerData.getProfess().getSkill(info.getClassSkill().getSkill()));
        info.unbind();
        PassiveSkill passiveSkill = classSkill.toPassive(playerData);
        passiveSkill.register(playerData.getMMOPlayerData());
        this.passiveSkillId = passiveSkill.getUniqueId();
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
    public UUID getPassiveSkillId() {
        return passiveSkillId;
    }

    /**
     * This is used to refresh the PassiveSkill playerModifier so it is always associated to the
     */
    public void refresh() {
        if (classSkill.getSkill().getTrigger().isPassive()) {
            playerData.getMMOPlayerData().getPassiveSkillMap().removeModifier(passiveSkillId);
            PassiveSkill passiveSkill = classSkill.toPassive(playerData);
            passiveSkill.register(playerData.getMMOPlayerData());
            this.passiveSkillId = passiveSkill.getUniqueId();
        }
    }

    public void unbind() {
        if (classSkill.getSkill().getTrigger().isPassive())
            playerData.getMMOPlayerData().getPassiveSkillMap().removeModifier(passiveSkillId);
    }
}
