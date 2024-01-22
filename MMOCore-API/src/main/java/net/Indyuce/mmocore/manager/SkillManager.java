package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.skill.list.Ambers;
import net.Indyuce.mmocore.skill.list.Neptune_Gift;
import net.Indyuce.mmocore.skill.list.Sneaky_Picky;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class SkillManager implements MMOCoreManager {
    private final Map<String, RegisteredSkill> skills = new LinkedHashMap<>();

    public void registerSkill(RegisteredSkill skill) {
        skills.put(skill.getHandler().getId().toUpperCase(), skill);
    }


    @Nullable
    public RegisteredSkill getSkill(String id) {
        return skills.get(id.toUpperCase());
    }

    @NotNull
    public RegisteredSkill getSkillOrThrow(String id) {
        return Objects.requireNonNull(skills.get(id), "Could not find skill with ID '" + id + "'");
    }

    public Collection<RegisteredSkill> getAll() {
        return skills.values();
    }

    public void initialize(boolean clearBefore) {

        if (clearBefore)
            skills.clear();

        // Register MMOCore specific skills
        MythicLib.plugin.getSkills().registerSkillHandler(new Ambers());
        MythicLib.plugin.getSkills().registerSkillHandler(new Neptune_Gift());
        MythicLib.plugin.getSkills().registerSkillHandler(new Sneaky_Picky());

        // Check for default files
        File skillFolder = new File(MMOCore.plugin.getDataFolder() + "/skills");
        if (!skillFolder.exists())
            try {
                skillFolder.mkdir();

                for (SkillHandler handler : MythicLib.plugin.getSkills().getHandlers()) {
                    InputStream res = MMOCore.plugin.getResource("default/skills/" + handler.getLowerCaseId() + ".yml");
                    if (res != null)
                        Files.copy(res, new File(MMOCore.plugin.getDataFolder() + "/skills/" + handler.getLowerCaseId() + ".yml").getAbsoluteFile().toPath());
                }
            } catch (IOException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not save default skill configs: " + exception.getMessage());
            }

        for (SkillHandler handler : MythicLib.plugin.getSkills().getHandlers()) {

            // Check if config file exists
            ConfigFile config = new ConfigFile("/skills", handler.getLowerCaseId());
            if (!config.exists()) {
                config.getConfig().set("name", MMOCoreUtils.caseOnWords(handler.getId().replace("_", " ").replace("-", " ").toLowerCase()));
                config.getConfig().set("lore", Arrays.asList("This is the default skill description", "", "&e{cooldown}s Cooldown", "&9Costs {mana} {mana_name}"));
                config.getConfig().set("material", "BOOK");
                for (Object param : handler.getParameters()) {
                    config.getConfig().set(param + ".base", 0);
                    config.getConfig().set(param + ".per-level", 0);
                    config.getConfig().set(param + ".min", 0);
                    config.getConfig().set(param + ".max", 0);
                }
                config.save();
            }

            try {
                final RegisteredSkill skill = new RegisteredSkill(handler, config.getConfig());
                this.skills.put(handler.getId(), skill);
            } catch (RuntimeException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load skill '" + handler.getId() + "': " + exception.getMessage());
            }
        }
    }
}
