package net.Indyuce.mmocore.api;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.SkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.skill.CastableSkill;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class MMOCoreAPI {
    private final JavaPlugin plugin;

    public MMOCoreAPI(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public PlayerData getPlayerData(OfflinePlayer player) {
        return PlayerData.get(player);
    }

    public boolean isInSameParty(Player player1, Player player2) {
        AbstractParty party = MMOCore.plugin.partyModule.getParty(PlayerData.get(player1));
        return party != null && party.hasMember(player2);
    }

    /**
     * Forces a player to cast a skill. It has the effect of caching
     * the player stats so when casting multiple skills at the same
     * time, it's better to copy and paste the content of this method
     * and cache the content of the <code>caster</code> and <code>triggerMeta</code>
     * fields for better performance.
     * <p>
     * Throws a NPE if no skill with such ID can be found
     *
     * @param playerData Player casting the skill
     * @param skillId    Name of skill being cast
     * @param level      Level of cast skill
     * @return Skill result (if it's canceled)
     */
    public SkillResult cast(PlayerData playerData, String skillId, int level) {
        return cast(playerData, MMOCore.plugin.skillManager.getSkillOrThrow(skillId), level);
    }

    /**
     * Forces a player to cast a skill. It has the effect of caching
     * the player stats so when casting multiple skills at the same
     * time, it's better to copy and paste the content of this method
     * and cache the content of the <code>caster</code> and <code>triggerMeta</code>
     * fields for better performance.
     *
     * @param playerData Player casting the skill
     * @param skill      Skill being cast
     * @return Skill result (if it's canceled)
     */
    public SkillResult cast(PlayerData playerData, ClassSkill skill) {
        PlayerMetadata casterMeta = playerData.getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND);
        TriggerMetadata triggerMeta = new TriggerMetadata(casterMeta, null, null);
        return new CastableSkill(skill, playerData).cast(triggerMeta);
    }

    /**
     * Forces a player to cast a skill. It has the effect of caching
     * the player stats so when casting multiple skills at the same
     * time, it's better to copy and paste the content of this method
     * and cache the content of the <code>caster</code> and <code>triggerMeta</code>
     * fields for better performance.
     * <p>
     * This method casts a skill with default modifier formulas.
     *
     * @param playerData Player casting the skill
     * @param skill      Skill being cast
     * @param level      Level of cast skill. This could be the returned value of
     *                   <code>playerData.getSkillLevel(skill)</code>
     * @return Skill result (if it's canceled)
     */
    public SkillResult cast(PlayerData playerData, RegisteredSkill skill, int level) {
        PlayerMetadata casterMeta = playerData.getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND);
        TriggerMetadata triggerMeta = new TriggerMetadata(casterMeta, null, null);
        return new CastableSkill(new ClassSkill(skill, 0, 0), level).cast(triggerMeta);
    }

    /**
     * Forces a player to cast a skill. It has the effect of caching
     * the player stats so when casting multiple skills at the same
     * time, it's better to copy and paste the content of this method
     * and cache the content of the <code>caster</code> and <code>triggerMeta</code>
     * fields for better performance.
     * <p>
     * Since the provided skill handler does NOT provide information about
     * the skill modifiers, MMOCore tries to find a corresponding registered
     * MMOCore skill in the MMOCore database. If it exists, it uses the
     * modifiers from this skill, otherwise all modifiers are set to 0
     *
     * @param playerData Player casting the skill
     * @param skill      Skill being cast
     * @param level      Level of cast skill
     * @return Skill result (if it's canceled)
     */
    public SkillResult cast(PlayerData playerData, SkillHandler<?> skill, int level) {
        PlayerMetadata casterMeta = playerData.getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND);
        TriggerMetadata triggerMeta = new TriggerMetadata(casterMeta, null, null);
        RegisteredSkill registered = Objects.requireNonNull(MMOCore.plugin.skillManager.getSkill(skill.getId()), "Could not find registered skill with such handler");
        return new CastableSkill(new ClassSkill(registered, 0, 0), level).cast(triggerMeta);
    }
}
