package net.Indyuce.mmocore.skill.binding;

import io.lumine.mythic.lib.player.skill.PassiveSkill;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.ClassSkill;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class BoundSkillInfo {
    private final PlayerData playerData;
    private final ClassSkill classSkill;

    /**
     * Private skills must be registered inside of MythicLib when bound.
     * When set to null, the skill is NOT registered.
     */
    @Nullable
    private PassiveSkill registered;

    public BoundSkillInfo(ClassSkill classSkill, PlayerData playerData) {
        this.classSkill = classSkill;
        this.playerData = playerData;

        if (classSkill.getSkill().getTrigger().isPassive()) {
            registered = classSkill.toPassive(playerData);
            registered.register(playerData.getMMOPlayerData());
        }
    }

    /**
     * Used on update to refresh the classSkill & all references to old data.
     */
    public BoundSkillInfo(BoundSkillInfo info) {
        this.playerData = info.getPlayerData();
        this.classSkill = Objects.requireNonNull(playerData.getProfess().getSkill(info.getClassSkill().getSkill()));

        if (classSkill.getSkill().getTrigger().isPassive()) {
            info.unbind();
            registered = classSkill.toPassive(playerData);
            registered.register(playerData.getMMOPlayerData());
        }
    }

    @NotNull
    public ClassSkill getClassSkill() {
        return classSkill;
    }

    @NotNull
    public PlayerData getPlayerData() {
        return playerData;
    }

    public boolean isPassive() {
        return registered != null;
    }

    /**
     * This is used to refresh the PassiveSkill playerModifier
     * so it is always associated to the right skill level.
     */
    public void refresh() {
        if (isPassive()) {
            registered.unregister(playerData.getMMOPlayerData());
            registered = classSkill.toPassive(playerData);
            registered.register(playerData.getMMOPlayerData());
        }
    }

    public void unbind() {
        if (isPassive()) registered.unregister(playerData.getMMOPlayerData());
    }
}
