package net.Indyuce.mmocore.manager.data.mysql;

import io.lumine.mythic.lib.sql.MMODataSource;
import net.Indyuce.mmocore.manager.data.DataProvider;
import net.Indyuce.mmocore.manager.data.GuildDataManager;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import net.Indyuce.mmocore.manager.data.yaml.YAMLGuildDataManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.SQLException;

public class MySQLDataProvider extends MMODataSource implements DataProvider {
    private final MySQLPlayerDataManager playerManager = new MySQLPlayerDataManager(this);
    private final YAMLGuildDataManager guildManager = new YAMLGuildDataManager();

    public MySQLDataProvider(FileConfiguration config) {
        this.setup(config);
    }

    @Override
    public void load() {

        // Fully create table
        executeUpdateAsync(
                "CREATE TABLE IF NOT EXISTS mmocore_playerdata(uuid VARCHAR(36),class_points "
                        + "INT(11) DEFAULT 0,skill_points INT(11) DEFAULT 0,attribute_points INT(11) "
                        + "DEFAULT 0,attribute_realloc_points INT(11) DEFAULT 0,level INT(11) DEFAULT 1,"
                        + "experience INT(11) DEFAULT 0,class VARCHAR(20),guild VARCHAR(20),last_login LONG,"
                        + "attributes LONGTEXT,professions LONGTEXT,times_claimed LONGTEXT,quests LONGTEXT,"
                        + "waypoints LONGTEXT,friends LONGTEXT,skills LONGTEXT,bound_skills LONGTEXT,"
                        + "class_info LONGTEXT, is_saved TINYINT, PRIMARY KEY (uuid));");

        // Add 'times_claimed' if it doesn't exist
        getResultAsync("SELECT * FROM information_schema.COLUMNS WHERE TABLE_NAME = 'mmocore_playerdata' AND COLUMN_NAME = 'times_claimed'", result -> {
            try {
                if (!result.next())
                    executeUpdateAsync("ALTER TABLE mmocore_playerdata ADD COLUMN times_claimed LONGTEXT");
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });

        // Add 'is_saved' if it doesn't exist
        getResultAsync("SELECT * FROM information_schema.COLUMNS WHERE TABLE_NAME = 'mmocore_playerdata' AND COLUMN_NAME = 'is_saved'", result -> {
            try {
                if (!result.next())
                    executeUpdate("ALTER TABLE mmocore_playerdata ADD COLUMN is_saved TINYINT");
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });

        // Add 'skill_reallocation_points' if it doesn't exist
        getResultAsync("SELECT * FROM information_schema.COLUMNS WHERE TABLE_NAME = 'mmocore_playerdata' AND COLUMN_NAME = 'skill_reallocation_points'", result -> {
            try {
                if (!result.next())
                    executeUpdate("ALTER TABLE mmocore_playerdata ADD COLUMN skill_reallocation_points INT(11)");
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
        // Add 'skill_tree_reallocation_points' if it doesn't exist
        getResultAsync("SELECT * FROM information_schema.COLUMNS WHERE TABLE_NAME = 'mmocore_playerdata' AND COLUMN_NAME = 'skill_tree_reallocation_points'", result -> {
            try {
                if (!result.next())
                    executeUpdate("ALTER TABLE mmocore_playerdata ADD COLUMN skill_tree_reallocation_points INT(11)");
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
        // Add 'skill_tree_points' if it doesn't exist
        getResultAsync("SELECT * FROM information_schema.COLUMNS WHERE TABLE_NAME = 'mmocore_playerdata' AND COLUMN_NAME = 'skill_tree_points'", result -> {
            try {
                if (!result.next())
                    executeUpdate("ALTER TABLE mmocore_playerdata ADD COLUMN skill_tree_points LONGTEXT");
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
        // Add 'skill_tree_levels' if it doesn't exist
        getResultAsync("SELECT * FROM information_schema.COLUMNS WHERE TABLE_NAME = 'mmocore_playerdata' AND COLUMN_NAME = 'skill_tree_levels'", result -> {
            try {
                if (!result.next())
                    executeUpdate("ALTER TABLE mmocore_playerdata ADD COLUMN skill_tree_levels LONGTEXT");
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
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
