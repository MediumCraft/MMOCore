package net.Indyuce.mmocore.api.player.profess.skillslot;

import io.lumine.mythic.lib.api.math.BooleanExpressionParser;
import net.Indyuce.mmocore.skill.ClassSkill;
import org.bukkit.configuration.ConfigurationSection;

public class SkillSlot {
    private final int slot;
    private final String expression;

    public SkillSlot(int slot, String expression) {
        this.slot = slot;
         this.expression = expression;
    }

    public SkillSlot(ConfigurationSection section) {
        this.slot = Integer.parseInt(section.getName());
        this.expression = section.getString("expression");
    }

    public boolean canBePlaced(ClassSkill classSkill) {
        return new BooleanExpressionParser(expression).parse(classSkill.getSkill().getCategories());
    }
}
