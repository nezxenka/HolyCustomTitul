package ru.nezxenka.holycustomtitul.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.configuration.file.FileConfiguration;
import ru.nezxenka.holycustomtitul.HolyCustomTitul;

public class SQLiteDatabase implements DatabaseManager {
    private final HolyCustomTitul plugin;
    private final FileConfiguration dbConfig;
    private Connection connection;

    public SQLiteDatabase(HolyCustomTitul plugin) {
        this.plugin = plugin;
        this.dbConfig = plugin.getDatabaseConfig();
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String filename = dbConfig.getString("database.sqlite.filename", "titles.db");
            File dataFolder = plugin.getDataFolder();
            File dbFile = new File(dataFolder, filename);

            if (!dbFile.exists()) {
                dbFile.getParentFile().mkdirs();
            }

            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            plugin.getLogger().info("Подключение к SQLite установлено: " + dbFile.getAbsolutePath());
        }
        return connection;
    }

    @Override
    public void initializeTables() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS pending_titles (player_name VARCHAR(16) NOT NULL, title VARCHAR(255) NOT NULL, PRIMARY KEY (player_name, title))");
            stmt.execute("CREATE TABLE IF NOT EXISTS player_titles (player_name VARCHAR(16) NOT NULL, title VARCHAR(255) NOT NULL, approved_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (player_name, title))");
            stmt.execute("CREATE TABLE IF NOT EXISTS player_amounts (player VARCHAR(16) NOT NULL, amount INT DEFAULT 0, PRIMARY KEY (player))");

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
            plugin.getLogger().info("Соединение с SQLite закрыто");
        }
    }

    @Override
    public String getDatabaseType() {
        return "SQLite";
    }
}