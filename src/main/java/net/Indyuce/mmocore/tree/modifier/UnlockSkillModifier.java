package net.Indyuce.mmocore.tree.modifier;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import org.apache.commons.lang.NotImplementedException;

public class UnlockSkillModifier extends PlayerModifier {
    private RegisteredSkill unlocked = null;

    public UnlockSkillModifier(String key, EquipmentSlot slot, ModifierSource source) {
        super(key, slot, source);
    }

    @Override
    public void register(MMOPlayerData mmoPlayerData) {
        PlayerData playerData = PlayerData.get(mmoPlayerData.getUniqueId());
     //   playerData.unlock(unlocked);
    }

    @Override
    public void unregister(MMOPlayerData mmoPlayerData) {
        throw new NotImplementedException("");
    }
}
