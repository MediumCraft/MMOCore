package net.Indyuce.mmocore.manager.registry;

import net.Indyuce.mmocore.manager.MMOCoreManager;
import org.apache.commons.lang.Validate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class MMOCoreRegister<T extends RegisterObject> implements MMOCoreManager {
    protected final Map<String, T> registered = new HashMap<>();

    public void register(T t) {
        Validate.notNull(t, getRegisteredObjectName() + " cannot be null");
        Validate.isTrue(!registered.containsKey(t.getId()), "There is already a " + getRegisteredObjectName() + " registered with ID '" + t.getId() + "'");

        registered.put(t.getId(), t);
    }

    public T get(String id) {
        return Objects.requireNonNull(registered.get(id), "Could not find " + getRegisteredObjectName() + " with ID '" + id + "'");
    }
    public boolean has(String id){
        return registered.containsKey(id);
    }

    public Collection<T> getAll() {
        return registered.values();
    }

    public abstract String getRegisteredObjectName();
}
