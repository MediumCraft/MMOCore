package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.util.FileUtils;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.profess.ClassOption;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.event.EventTriggerHandler;
import net.Indyuce.mmocore.api.player.profess.event.trigger.*;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;

import java.util.*;
import java.util.logging.Level;

public class ClassManager implements MMOCoreManager {
    private final Map<String, PlayerClass> map = new HashMap<>();

    /**
     * Cached default class. If there happens to have two default
     * classes, the last one overrides the previous.
     */
    private PlayerClass defaultClass;

    /**
     * Same different types of trigger events to be able to
     * map them later in the player class instances.
     */
    private final Set<EventTriggerHandler> triggerHandlers = new HashSet<>();

    public ClassManager() {
        registerEvent(new LevelUpEventTrigger());
        registerEvent(new AttackEventTrigger());
        registerEvent(new ClassChosenEventTrigger());
        registerEvent(new BlockBrokenTrigger());
        registerEvent(new BlockPlacedTrigger());
        registerEvent(new MultipleLevelUpEventTrigger());
    }

    @Deprecated
    public void registerEvent(EventTriggerHandler handler) {
        triggerHandlers.add(handler);
    }

    public void register(PlayerClass playerClass) {
        map.put(playerClass.getId(), playerClass);
        MMOCore.plugin.statManager.getRegistered().addAll(playerClass.getStats());
    }

    public boolean has(String id) {
        return map.containsKey(id);
    }

    public PlayerClass get(String id) {
        return map.get(id);
    }

    public PlayerClass getOrThrow(String id) {
        final PlayerClass found = map.get(id);
        Validate.notNull(found, "Could not find class with ID '" + id + "'");
        return found;
    }

    public Collection<PlayerClass> getAll() {
        return map.values();
    }

    public PlayerClass getDefaultClass() {
        return defaultClass;
    }

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore) {
            map.clear();

            /*
             * Does not clear the list of trigger listeners, since it's
             * only setup once the server loads and it is never modified.
             */
            triggerHandlers.forEach(HandlerList::unregisterAll);
        }

        FileUtils.loadObjectsFromFolder(MMOCore.plugin, "classes", true, (name, config) -> {
            register(new PlayerClass(name, config));
        }, "Could not load class from file '%s': %s");

        for (PlayerClass profess : map.values())
            try {
                profess.getPostLoadAction().performAction();
            } catch (IllegalArgumentException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not post-load class '" + profess.getId() + "': " + exception.getMessage());
            }

        defaultClass = map.values().stream().filter(profess -> profess.hasOption(ClassOption.DEFAULT)).findFirst()
                .orElse(new PlayerClass("HUMAN", "Human", Material.LEATHER_BOOTS));

        // Register event triggers
        triggerHandlers.forEach(handler -> Bukkit.getPluginManager().registerEvents(handler, MMOCore.plugin));
    }
}
