package ru.nezxenka.holycustomtitul.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.configuration.file.FileConfiguration;
import ru.nezxenka.holycustomtitul.HolyCustomTitul;

public class MySQLDatabase implements DatabaseManager {
    private final HolyCustomTitul plugin;
    private final FileConfiguration dbConfig;
    private Connection connection;

    public MySQLDatabase(HolyCustomTitul plugin) {
        this.plugin = plugin;
        this.dbConfig = plugin.getDatabaseConfig();
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String host = dbConfig.getString("database.mysql.host", "localhost");
            int port = dbConfig.getInt("database.mysql.port", 3306);
            String database = dbConfig.getString("database.mysql.database", "customtutil");
            String username = dbConfig.getString("database.mysql.username", "plugins");
            String password = dbConfig.getString("database.mysql.password", "plugins_customtitul");
            boolean useSSL = dbConfig.getBoolean("database.mysql.useSSL", false);
            boolean autoReconnect = dbConfig.getBoolean("database.mysql.autoReconnect", true);

            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=%b&autoReconnect=%b",
                    host, port, database, useSSL, autoReconnect);

            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("Подключение к MySQL установлено");
        }
        return connection;
    }

    @Override
    public void initializeTables() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS pending_titles (player_name VARCHAR(16) NOT NULL, title VARCHAR(255) NOT NULL, PRIMARY KEY (player_name, title)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            stmt.execute("CREATE TABLE IF NOT EXISTS player_titles (player_name VARCHAR(16) NOT NULL, title VARCHAR(255) NOT NULL, approved_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (player_name, title)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            stmt.execute("CREATE TABLE IF NOT EXISTS player_amounts (player VARCHAR(16) NOT NULL, amount INT DEFAULT 0, PRIMARY KEY (player)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            try {
                java.sql.DatabaseMetaData dbm = conn.getMetaData();
                java.sql.ResultSet columns = dbm.getColumns(null, null, "player_titles", "approved_time");
                if (!columns.next()) {
                    stmt.execute("ALTER TABLE player_titles ADD COLUMN approved_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                }
                columns.close();
            } catch (SQLException e) {
                plugin.getLogger().warning("Ошибка при проверке колонки approved_time: " + e.getMessage());
            }
        }
    }

    @Override
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            plugin.getLogger().info("Соединение с MySQL закрыто");
        }
    }

    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}