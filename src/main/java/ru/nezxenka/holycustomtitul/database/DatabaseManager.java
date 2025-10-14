package ru.nezxenka.holycustomtitul.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseManager {
    Connection getConnection() throws SQLException;
    void initializeTables() throws SQLException;
    void closeConnection() throws SQLException;
    String getDatabaseType();
}