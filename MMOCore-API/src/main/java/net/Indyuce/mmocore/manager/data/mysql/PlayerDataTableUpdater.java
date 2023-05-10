package net.Indyuce.mmocore.manager.data.mysql;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.data.yaml.YAMLPlayerDataManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class PlayerDataTableUpdater {
    private final PlayerData playerData;
    private final MySQLDataProvider provider;
    private final Map<String, String> requestMap = new HashMap<>();

    public PlayerDataTableUpdater(MySQLDataProvider provider, PlayerData playerData) {
        this.playerData = playerData;
        this.provider = provider;
    }

    public void executeRequest(boolean logout) {
        final String request = "INSERT INTO mmocore_playerdata(uuid, " + formatCollection(requestMap.keySet(), false)
                + ") VALUES('" + playerData.getUniqueId() + "'," + formatCollection(requestMap.values(), true) + ")" +
                " ON DUPLICATE KEY UPDATE " + formatMap() + ";";

        try {
            final Connection connection = provider.getConnection();
            try {
                final PreparedStatement statement = connection.prepareStatement(request);
                try {
                    statement.executeUpdate();
                } catch (SQLException exception) {
                    MMOCore.log(Level.WARNING, "Could not save player data of " + playerData.getUniqueId() + ", saving through YAML instead");
                    new YAMLPlayerDataManager(provider).saveData(playerData, logout);
                    exception.printStackTrace();
                } finally {
                    statement.close();
                }
            } catch (SQLException exception) {
                MMOCore.log(Level.WARNING, "Could not save player data of " + playerData.getUniqueId() + ", saving through YAML instead");
                new YAMLPlayerDataManager(provider).saveData(playerData, logout);
                exception.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException exception) {
            MMOCore.log(Level.WARNING, "Could not save player data of " + playerData.getUniqueId() + ", saving through YAML instead");
            new YAMLPlayerDataManager(provider).saveData(playerData, logout);
            exception.printStackTrace();
        }
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

    public <T,V> void  addJSONObject(String key, Set<Map.Entry<T, V>> collection) {
        JsonObject json = new JsonObject();
        for (Map.Entry<T, V> entry : collection)
            json.addProperty(entry.getKey().toString(), entry.getValue().toString());
        addData(key, json.toString());
    }
}
