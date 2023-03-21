package net.Indyuce.mmocore.manager.data.mysql;

import io.lumine.mythic.lib.sql.MMODataSource;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.manager.data.DataProvider;
import net.Indyuce.mmocore.manager.data.GuildDataManager;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import net.Indyuce.mmocore.manager.data.yaml.YAMLGuildDataManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.SQLException;

public class MySQLDataProvider extends MMODataSource implements DataProvider {
    private final MySQLPlayerDataManager playerManager = new MySQLPlayerDataManager(this);
    private final YAMLGuildDataManager guildManager = new YAMLGuildDataManager();

    private static final String[] NEW_COLUMNS = new String[]{
            "times_claimed", "LONGTEXT",
            "is_saved", "TINYINT",
            "skill_reallocation_points", "INT(11)",
            "skill_tree_reallocation_points", "INT(11)",
            "skill_tree_points", "LONGTEXT",
            "skill_tree_levels", "LONGTEXT",
            "unlocked_items","LONGTEXT",
            "mana","FLOAT",
            "stamina","FLOAT",
            "stellium","FLOAT"};

    public MySQLDataProvider(FileConfiguration config) {
        super(MMOCore.plugin);

        this.setup(config);
    }

    @Override
    public void load() {

        // Fully create table
        executeUpdateAsync("CREATE TABLE IF NOT EXISTS mmocore_playerdata(uuid VARCHAR(36)," +
                "class_points INT(11) DEFAULT 0," +
                "skill_points INT(11) DEFAULT 0," +
                "attribute_points INT(11) DEFAULT 0," +
                "attribute_realloc_points INT(11) DEFAULT 0," +
                "skill_reallocation_points INT(11) DEFAULT 0," +
                "skill_tree_reallocation_points INT(11) DEFAULT 0," +
                "skill_tree_points LONGTEXT," +
                "skill_tree_levels LONGTEXT," +
                "level INT(11) DEFAULT 1," +
                "experience INT(11) DEFAULT 0," +
                "class VARCHAR(20),guild VARCHAR(20)," +
                "last_login LONG," +
                "attributes LONGTEXT," +
                "professions LONGTEXT," +
                "times_claimed LONGTEXT," +
                "quests LONGTEXT," +
                "waypoints LONGTEXT," +
                "friends LONGTEXT," +
                "skills LONGTEXT," +
                "bound_skills LONGTEXT," +
                "unlocked_items LONGTEXT," +
                "class_info LONGTEXT," +
                "is_saved TINYINT," +
                "PRIMARY KEY (uuid));");

        // Add columns that might not be here by default
        for (int i = 0; i < NEW_COLUMNS.length; i += 2) {
            final String columnName = NEW_COLUMNS[i];
            final String dataType = NEW_COLUMNS[i + 1];
            getResultAsync("SELECT * FROM information_schema.COLUMNS WHERE TABLE_NAME = 'mmocore_playerdata' AND COLUMN_NAME = '" + columnName + "'", result -> {
                try {
                    if (!result.next())
                        executeUpdate("ALTER TABLE mmocore_playerdata ADD COLUMN " + columnName + " " + dataType);
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            });
        }
    }

    @Override
    public PlayerDataManager getDataManager() {
        return playerManager;
    }

    @Override
    public GuildDataManager getGuildManager() {
        return guildManager;
    }
}
