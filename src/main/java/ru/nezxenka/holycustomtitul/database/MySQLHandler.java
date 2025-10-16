package ru.nezxenka.holycustomtitul.database;

import ru.nezxenka.holycustomtitul.HolyCustomTitul;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MySQLHandler implements DatabaseHandler {
    private final HolyCustomTitul plugin;
    private Connection connection;

    public MySQLHandler(HolyCustomTitul plugin) {
        this.plugin = plugin;
    }

    @Override
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                org.bukkit.configuration.file.FileConfiguration config = plugin.getConfigManager().getConfig("database");
                String host = config.getString("mysql.host", "localhost");
                int port = config.getInt("mysql.port", 3306);
                String database = config.getString("mysql.database", "customtutil");
                String username = config.getString("mysql.username", "root");
                String password = config.getString("mysql.password", "pass");
                boolean useSSL = config.getBoolean("mysql.useSSL", false);
                boolean autoReconnect = config.getBoolean("mysql.autoReconnect", true);

                String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=%b&autoReconnect=%b",
                        host, port, database, useSSL, autoReconnect);

                connection = DriverManager.getConnection(url, username, password);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка подключения к MySQL: " + e.getMessage());
        }
        return connection;
    }

    @Override
    public void initializeTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS pending_titles (player_name VARCHAR(16) NOT NULL, title VARCHAR(255) NOT NULL, PRIMARY KEY (player_name, title)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            stmt.execute("CREATE TABLE IF NOT EXISTS player_titles (player_name VARCHAR(16) NOT NULL, title VARCHAR(255) NOT NULL, approved_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (player_name, title)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            stmt.execute("CREATE TABLE IF NOT EXISTS player_tokens (player VARCHAR(16) NOT NULL, tokens INT DEFAULT 0, PRIMARY KEY (player)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка инициализации таблиц MySQL: " + e.getMessage());
        }
    }

    @Override
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка закрытия соединения MySQL: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<Integer> getPlayerTokens(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT tokens FROM player_tokens WHERE player = ?")) {

                ps.setString(1, playerName);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return rs.getInt("tokens");
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Ошибка получения токенов MySQL: " + e.getMessage());
            }
            return 0;
        });
    }

    @Override
    public CompletableFuture<Void> addPlayerTokens(String playerName, int amount) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO player_tokens (player, tokens) VALUES (?, ?) ON DUPLICATE KEY UPDATE tokens = tokens + ?")) {

                ps.setString(1, playerName);
                ps.setInt(2, amount);
                ps.setInt(3, amount);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Ошибка добавления токенов MySQL: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Void> removePlayerTokens(String playerName, int amount) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE player_tokens SET tokens = tokens - ? WHERE player = ? AND tokens >= ?")) {

                ps.setInt(1, amount);
                ps.setString(2, playerName);
                ps.setInt(3, amount);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Ошибка удаления токенов MySQL: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Void> addPendingTitle(String playerName, String title) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT IGNORE INTO pending_titles (player_name, title) VALUES (?, ?)")) {

                ps.setString(1, playerName);
                ps.setString(2, title);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Ошибка добавления титула MySQL: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Void> approveTitle(String playerName, String title) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT IGNORE INTO player_titles (player_name, title) VALUES (?, ?)")) {
                    ps.setString(1, playerName);
                    ps.setString(2, title);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM pending_titles WHERE player_name = ? AND title = ?")) {
                    ps.setString(1, playerName);
                    ps.setString(2, title);
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Ошибка одобрения титула MySQL: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Void> rejectTitle(String playerName, String title) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "DELETE FROM pending_titles WHERE player_name = ? AND title = ?")) {

                ps.setString(1, playerName);
                ps.setString(2, title);
                ps.executeUpdate();

                addPlayerTokens(playerName, 1);
            } catch (SQLException e) {
                plugin.getLogger().warning("Ошибка отклонения титула MySQL: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<List<String[]>> getPendingTitles() {
        return CompletableFuture.supplyAsync(() -> {
            List<String[]> titles = new ArrayList<>();
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT player_name, title FROM pending_titles");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    titles.add(new String[]{rs.getString("player_name"), rs.getString("title")});
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Ошибка получения pending titles MySQL: " + e.getMessage());
            }
            return titles;
        });
    }

    @Override
    public CompletableFuture<List<String>> getPlayerPendingTitles(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> titles = new ArrayList<>();
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT title FROM pending_titles WHERE player_name = ?")) {

                ps.setString(1, playerName);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    titles.add(rs.getString("title"));
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Ошибка получения pending titles игрока MySQL: " + e.getMessage());
            }
            return titles;
        });
    }

    @Override
    public CompletableFuture<Map<String, Timestamp>> getApprovedTitles(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Timestamp> titles = new HashMap<>();
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT title, approved_time FROM player_titles WHERE player_name = ?")) {

                ps.setString(1, playerName);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    titles.put(rs.getString("title"), rs.getTimestamp("approved_time"));
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Ошибка получения approved titles MySQL: " + e.getMessage());
            }
            return titles;
        });
    }
}