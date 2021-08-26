package net.Indyuce.mmocore.comp.mythicmobs.skill;

import io.lumine.mythic.lib.api.util.EnumUtils;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.mobs.GenericCaster;
import io.lumine.xikage.mythicmobs.skills.SkillCaster;
import io.lumine.xikage.mythicmobs.skills.SkillTrigger;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.api.util.math.formula.IntegerLinearValue;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.comp.anticheat.CheatType;
import net.Indyuce.mmocore.skill.CasterMetadata;
import net.Indyuce.mmocore.skill.Skill;
import net.Indyuce.mmocore.skill.metadata.SkillMetadata;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class MythicSkill extends Skill {
    private final io.lumine.xikage.mythicmobs.skills.Skill skill;
    private final Map<CheatType, Integer> antiCheat = new HashMap<>();

    public MythicSkill(String id, FileConfiguration config) {
        super(id);

        String mmId = config.getString("mythicmobs-skill-id");
        Validate.notNull(mmId, "Could not find MM skill ID");

        Optional<io.lumine.xikage.mythicmobs.skills.Skill> opt = MythicMobs.inst().getSkillManager().getSkill(mmId);
        Validate.isTrue(opt.isPresent(), "Could not find MM skill " + mmId);
        skill = opt.get();

        String format = config.getString("material");
        Validate.notNull(format, "Could not load skill material");
        setIcon(MMOCoreUtils.readIcon(format));

        setName(config.getString("name"));
        setLore(config.getStringList("lore"));

        for (String key : config.getKeys(false)) {
            Object mod = config.get(key);
            if (mod instanceof ConfigurationSection)
                addModifier(key, readLinearValue((ConfigurationSection) mod));
        }

        if (config.isConfigurationSection("disable-anti-cheat"))
            for (String key : config.getKeys(false)) {
                Optional<CheatType> cheatType = EnumUtils.getIfPresent(CheatType.class, key.toUpperCase());
                if (cheatType.isPresent() && config.isInt("disable-anti-cheat." + key))
                    antiCheat.put(cheatType.get(), config.getInt("disable-anti-cheat." + key));
                else
                    MMOCore.log(Level.WARNING, "Invalid Anti-Cheat configuration for '" + id + "'!");
            }

        if (config.isString("passive-type")) {
            Optional<PassiveSkillType> passiveType = EnumUtils.getIfPresent(PassiveSkillType.class, config.getString("passive-type").toUpperCase());
            Validate.isTrue(passiveType.isPresent(), "Invalid passive skill type");
            setPassive();
            Bukkit.getPluginManager().registerEvents(passiveType.get().getHandler(this), MMOCore.plugin);
        }
    }

    public Map<CheatType, Integer> getAntiCheat() {
        return antiCheat;
    }

    public io.lumine.xikage.mythicmobs.skills.Skill getSkill() {
        return skill;
    }

    @Override
    public SkillMetadata whenCast(CasterMetadata caster, SkillInfo skill) {
        SkillMetadata cast = new SkillMetadata(caster, skill);
        if (!cast.isSuccessful() || isPassive())
            return cast;

        // Gather MythicMobs skill info
        HashSet<AbstractEntity> targetEntities = new HashSet<>();
        HashSet<AbstractLocation> targetLocations = new HashSet<>();

        AbstractEntity trigger = BukkitAdapter.adapt(caster.getPlayer());
        SkillCaster skillCaster = new GenericCaster(trigger);
        io.lumine.xikage.mythicmobs.skills.SkillMetadata skillMeta = new io.lumine.xikage.mythicmobs.skills.SkillMetadata(SkillTrigger.CAST, skillCaster, trigger, BukkitAdapter.adapt(caster.getPlayer().getLocation()), targetEntities, targetLocations, 1);

        // Disable anticheat
        if (MMOCore.plugin.hasAntiCheat())
            MMOCore.plugin.antiCheatSupport.disableAntiCheat(caster.getPlayer(), antiCheat);

        // Place cast skill info in a variable
        skillMeta.getVariables().putObject("MMOSkill", cast);
        skillMeta.getVariables().putObject("MMOStatMap", caster.getStats());

        //  Yo is that me or the second argument is f***ing useless
        if (this.skill.usable(skillMeta, SkillTrigger.CAST))
            this.skill.execute(skillMeta);
        else
            cast.abort();

        return cast;
    }

    /**
     * Used to load double modifiers from the config with a specific type, since
     * modifiers have initially a type for mmocore default skills
     */
    private LinearValue readLinearValue(ConfigurationSection section) {
        return section.getBoolean("int") ? new IntegerLinearValue(section) : new LinearValue(section);
    }
}
