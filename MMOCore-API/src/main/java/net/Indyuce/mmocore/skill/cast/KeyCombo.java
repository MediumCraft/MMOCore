package net.Indyuce.mmocore.skill.cast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * There is one key combo per skill slot. This means
 * that independently of both the player's class and
 * the skill bounty to the n-th slot, the key combo to
 * perform to cast the n-th skill is always the same
 */
public class KeyCombo {
    private final List<PlayerKey> keys = new ArrayList<>();

    public int countKeys() {
        return keys.size();
    }

    public void registerKey(@NotNull PlayerKey key) {
        keys.add(key);
    }

    public PlayerKey getAt(int index) {
        return keys.get(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyCombo keyCombo = (KeyCombo) o;
        return Objects.equals(keys, keyCombo.keys);
    }

    @Override
    public int hashCode() {
        return keys.hashCode();
    }
}
