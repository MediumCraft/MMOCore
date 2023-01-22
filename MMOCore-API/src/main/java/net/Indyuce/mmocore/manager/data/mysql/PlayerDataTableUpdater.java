package net.Indyuce.mmocore.manager.data.mysql;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerDataTableUpdater {
    private final UUID uuid;
    private final MySQLDataProvider provider;
    private final Map<String, String> requestMap = new HashMap<>();

    public PlayerDataTableUpdater(MySQLDataProvider provider, UUID uuid) {
        this.uuid = uuid;
        this.provider = provider;
    }

    public void updateData(String key, Object value) {
        addData(key, value);
        executeRequest();
        requestMap.clear();
    }

    public void executeRequest() {
        final String request = "INSERT INTO mmocore_playerdata(uuid, " + formatCollection(requestMap.keySet(), false)
                + ") VALUES('" + uuid + "'," + formatCollection(requestMap.values(), true) + ")" +
                " ON DUPLICATE KEY UPDATE " + formatMap() + ";";
        provider.executeUpdate(request);
    }

    public void addData(@NotNull String key, @Nullable Object value) {
        requestMap.put(key, String.valueOf(value));
    }

    public String formatCollection(Collection<String> strings, boolean withComma) {
        StringBuilder values = new StringBuilder();
        for (String key : strings) {
            if (withComma)
                values.append("'");
            values.append(key);
            if (withComma)
                values.append("'");
            values.append(",");
        }
        //Remove the last coma
        values.deleteCharAt(values.length() - 1);
        return values.toString();
    }

    public String formatMap() {
        final StringBuilder values = new StringBuilder();
        for (String key : requestMap.keySet())
            values.append(key).append("='").append(requestMap.get(key)).append("',");

        // Remove the last comma
        values.deleteCharAt(values.length() - 1);
        return values.toString();
    }

    public void addJSONArray(String key, Collection<String> collection) {
        JsonArray json = new JsonArray();
        for (String s : collection)
            json.add(s);
        addData(key, json.toString());
    }

    public void addJSONObject(String key, Set<Map.Entry<String, Integer>> collection) {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, Integer> entry : collection)
            json.addProperty(entry.getKey(), entry.getValue());
        addData(key, json.toString());
    }
}
