package net.Indyuce.mmocore.manager.data.mysql;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.denizen.dungeons.requests.RequestDataKey;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

public class MySQLTableEditor {
    private final Table table;
    private final UUID uuid;
    private final MySQLDataProvider provider;

    public MySQLTableEditor(Table table, UUID uuid, MySQLDataProvider provider) {
        this.table = table;
        this.uuid = uuid;
        this.provider = provider;
    }


    public void updateData(String key, Object value) {
        provider.executeUpdate("INSERT INTO " + table + "(uuid, " + key
                + ") VALUES('" + uuid + "', '" + value + "') ON DUPLICATE KEY UPDATE " + key + "='" + value + "';");
    }

    public void updateData(MySQLRequest request) {
        provider.executeUpdate("INSERT INTO " + table + request.getRequestString());
    }


    public enum Table {
        PLAYERDATA("mmocore_playerdata"), GUILDDATA("mmocore_guilddata");

        final String tableName;

        Table(String tN) {
            tableName = tN;
        }

        @Override
        public String toString() {
            return tableName;
        }
    }
}
