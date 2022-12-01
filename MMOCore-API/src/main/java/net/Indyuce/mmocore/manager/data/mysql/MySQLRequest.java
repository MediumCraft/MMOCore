package net.Indyuce.mmocore.manager.data.mysql;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.Indyuce.mmocore.MMOCore;

import java.util.*;

public class MySQLRequest {
    private final Map<String, String> requestMap = new HashMap<>();
    private final UUID uuid;

    public MySQLRequest(UUID uuid) {
        this.uuid = uuid;
    }

    public void addData(String key, Object value) {
        requestMap.put(key, "" + value);
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
        StringBuilder values = new StringBuilder();
        for (String key : requestMap.keySet()) {
            //values.append("'");
            values.append(key);
            //values.append("'");
            values.append("=");
            values.append("'");
            values.append(requestMap.get(key));
            values.append("'");
            values.append(",");
        }
        //Remove the last coma
        values.deleteCharAt(values.length() - 1);
        return values.toString();
    }


    public String getRequestString() {
        String result = "(uuid, " + formatCollection(requestMap.keySet(),false)
                + ") VALUES('" + uuid + "'," + formatCollection(requestMap.values(),true) + ")" +
                " ON DUPLICATE KEY UPDATE " + formatMap() + ";";
        return result;
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
