package net.Indyuce.mmocore.manager;

import net.Indyuce.mmocore.manager.registry.MMOCoreRegister;
import net.Indyuce.mmocore.tree.SkillTree;

@Deprecated
public class SkillTreeManager extends MMOCoreRegister<SkillTree> {

    @Override
    public String getRegisteredObjectName() {
        return "skill tree";
    }

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore)
            registered.clear();

        // TODO
    }
}
