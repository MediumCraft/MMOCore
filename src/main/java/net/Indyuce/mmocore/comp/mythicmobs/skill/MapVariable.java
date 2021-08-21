package net.Indyuce.mmocore.comp.mythicmobs.skill;

import io.lumine.xikage.mythicmobs.skills.variables.Variable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Used by MMOCore to cache skill modifiers inside a variable.
 * The modifiers are cached in a variable with the skill scope.
 *
 * @deprecated Now using an {@link io.lumine.xikage.mythicmobs.skills.variables.types.AbstractVariable}
 */
@Deprecated
public class MapVariable extends Variable {

    @NotNull
    private final Map<String, Double> map = new HashMap<>();

    public Map<String, Double> getValue() {
        return map;
    }

    @Override
    public String toString() {
        return "VAR_Map(" + map + ')';
    }

    @Override
    public boolean shouldSave() {
        return false;
    }

    @Override
    public Object get() {
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapVariable that = (MapVariable) o;
        return map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }
}
