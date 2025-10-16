package ru.nezxenka.holycustomtitul.managers;

import ru.nezxenka.holycustomtitul.HolyCustomTitul;
import ru.nezxenka.holycustomtitul.database.DatabaseHandler;
import ru.nezxenka.holycustomtitul.database.SQLiteHandler;
import ru.nezxenka.holycustomtitul.database.MySQLHandler;
import java.sql.Connection;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    private final HolyCustomTitul plugin;
    private DatabaseHandler databaseHandler;

    public DatabaseManager(HolyCustomTitul plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        String dbType = plugin.getConfigManager().getConfig("database").getString("type", "sqlite");

        if ("mysql".equalsIgnoreCase(dbType)) {
            databaseHandler = new MySQLHandler(plugin);
        } else {
            databaseHandler = new SQLiteHandler(plugin);
        }

        databaseHandler.initializeTables();
    }

    public Connection getConnection() {
        return databaseHandler.getConnection();
    }

    public void closeConnection() {
        databaseHandler.closeConnection();
    }

    public CompletableFuture<Integer> getPlayerTokens(String playerName) {
        return databaseHandler.getPlayerTokens(playerName);
    }

    public CompletableFuture<Void> addPlayerTokens(String playerName, int amount) {
        return databaseHandler.addPlayerTokens(playerName, amount);
    }

    public CompletableFuture<Void> removePlayerTokens(String playerName, int amount) {
        return databaseHandler.removePlayerTokens(playerName, amount);
    }

    public CompletableFuture<Void> addPendingTitle(String playerName, String title) {
        return databaseHandler.addPendingTitle(playerName, title);
    }

    public CompletableFuture<Void> approveTitle(String playerName, String title) {
        return databaseHandler.approveTitle(playerName, title);
    }

    public CompletableFuture<Void> rejectTitle(String playerName, String title) {
        return databaseHandler.rejectTitle(playerName, title);
    }

    public CompletableFuture<java.util.List<String[]>> getPendingTitles() {
        return databaseHandler.getPendingTitles();
    }

    public CompletableFuture<java.util.List<String>> getPlayerPendingTitles(String playerName) {
        return databaseHandler.getPlayerPendingTitles(playerName);
    }

    public CompletableFuture<java.util.Map<String, java.sql.Timestamp>> getApprovedTitles(String playerName) {
        return databaseHandler.getApprovedTitles(playerName);
    }
}