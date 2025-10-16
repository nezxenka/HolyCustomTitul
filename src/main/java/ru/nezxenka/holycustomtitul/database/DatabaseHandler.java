package ru.nezxenka.holycustomtitul.database;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface DatabaseHandler {
    Connection getConnection();
    void initializeTables();
    void closeConnection();

    CompletableFuture<Integer> getPlayerTokens(String playerName);
    CompletableFuture<Void> addPlayerTokens(String playerName, int amount);
    CompletableFuture<Void> removePlayerTokens(String playerName, int amount);
    CompletableFuture<Void> addPendingTitle(String playerName, String title);
    CompletableFuture<Void> approveTitle(String playerName, String title);
    CompletableFuture<Void> rejectTitle(String playerName, String title);
    CompletableFuture<List<String[]>> getPendingTitles();
    CompletableFuture<List<String>> getPlayerPendingTitles(String playerName);
    CompletableFuture<Map<String, Timestamp>> getApprovedTitles(String playerName);
}