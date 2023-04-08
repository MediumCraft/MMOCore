package net.Indyuce.mmocore.api.player.profess.skillbinding;

import io.lumine.mythic.lib.player.skill.PassiveSkill;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.ClassSkill;

import java.util.Objects;
import java.util.UUID;

public class BoundSkillInfo {
    private final PlayerData playerData;
    private final ClassSkill classSkill;
    private UUID passiveSkillUUID;

    public BoundSkillInfo(ClassSkill classSkill, PlayerData playerData) {
        this.classSkill = classSkill;
        this.playerData = playerData;
    }

    public BoundSkillInfo(ClassSkill classSkill, PlayerData playerData, UUID passiveSkillUUID) {
        this.classSkill = classSkill;
        this.playerData = playerData;
        this.passiveSkillUUID = passiveSkillUUID;
    }

    /**
     * Used on update to refresh the classSkill & all references to old data.
     */
    public BoundSkillInfo(BoundSkillInfo info) {
        this.playerData=info.getPlayerData();
        this.classSkill= Objects.requireNonNull(playerData.getProfess().getSkill(info.getClassSkill().getSkill()));
        info.unbind();
        PassiveSkill passiveSkill = classSkill.toPassive(playerData);
        passiveSkill.register(playerData.getMMOPlayerData());
        this.passiveSkillUUID=passiveSkill.getUniqueId();
    }

    public ClassSkill getClassSkill() {
        return classSkill;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public UUID getPassiveSkillUUID() {
        return passiveSkillUUID;
    }

    /**
     * This is used to refresh the PassiveSkill playerModifier so it is always associated to the
     */
    public void refresh() {
        if (classSkill.getSkill().getTrigger().isPassive()) {
            playerData.getMMOPlayerData().getPassiveSkillMap().removeModifier(passiveSkillUUID);
            PassiveSkill passiveSkill = classSkill.toPassive(playerData);
            passiveSkill.register(playerData.getMMOPlayerData());
            this.passiveSkillUUID=passiveSkill.getUniqueId();
        }
    }

    public void unbind() {
        if (classSkill.getSkill().getTrigger().isPassive()) {
            playerData.getMMOPlayerData().getPassiveSkillMap().removeModifier(passiveSkillUUID);
        }
    }
}
