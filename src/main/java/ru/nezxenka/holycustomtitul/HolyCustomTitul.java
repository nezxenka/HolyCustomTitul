package ru.nezxenka.holycustomtitul;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.nezxenka.holycustomtitul.commands.TitleCommandExecutor;
import ru.nezxenka.holycustomtitul.database.DatabaseManager;
import ru.nezxenka.holycustomtitul.database.MySQLDatabase;
import ru.nezxenka.holycustomtitul.database.SQLiteDatabase;
import ru.nezxenka.holycustomtitul.listeners.InventoryClickListener;
import ru.nezxenka.holycustomtitul.listeners.PlayerChatListener;
import ru.nezxenka.holycustomtitul.menus.TitleCreationMenu;
import ru.nezxenka.holycustomtitul.menus.TitleMainMenu;

public final class HolyCustomTitul extends JavaPlugin implements Listener {
    public final Map<Player, Player> map = new HashMap();
    public TitleCreationMenu create;
    public TitleMainMenu home;
    private static HolyCustomTitul instance;
    private DatabaseManager databaseManager;
    private FileConfiguration dbConfig;
    private File dbConfigFile;

    public static HolyCustomTitul getInstance() {
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return databaseManager.getConnection();
    }

    public void reloadDatabaseConfig() {
        if (dbConfigFile == null) {
            dbConfigFile = new File(getDataFolder(), "db.yml");
        }
        dbConfig = YamlConfiguration.loadConfiguration(dbConfigFile);
    }

    public FileConfiguration getDatabaseConfig() {
        if (dbConfig == null) {
            reloadDatabaseConfig();
        }
        return dbConfig;
    }

    public void saveDatabaseConfig() {
        if (dbConfig == null || dbConfigFile == null) {
            return;
        }
        try {
            getDatabaseConfig().save(dbConfigFile);
        } catch (Exception e) {
            getLogger().severe("Не удалось сохранить конфигурацию базы данных: " + e.getMessage());
        }
    }

    public void saveDefaultDatabaseConfig() {
        if (dbConfigFile == null) {
            dbConfigFile = new File(getDataFolder(), "db.yml");
        }
        if (!dbConfigFile.exists()) {
            saveResource("db.yml", false);
        }
    }

    private DatabaseManager createDatabaseManager() {
        String dbType = getDatabaseConfig().getString("database.type", "sqlite").toLowerCase();

        if ("mysql".equals(dbType)) {
            return new MySQLDatabase(this);
        } else {
            return new SQLiteDatabase(this);
        }
    }

    public CompletableFuture<Integer> getPlayerAmount(String player) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT amount FROM player_amounts WHERE player = ?")) {

                ps.setString(1, player);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return rs.getInt("amount");
                }
            } catch (SQLException e) {
                this.getLogger().warning("Ошибка при получении amount: " + e.getMessage());
            }
            return 0;
        });
    }

    public CompletableFuture<List<String[]>> getPendingTitles() {
        return CompletableFuture.supplyAsync(() -> {
            List<String[]> titles = new ArrayList<>();
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT player_name, title FROM pending_titles");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String[] titleData = new String[]{rs.getString("player_name"), rs.getString("title")};
                    titles.add(titleData);
                }
            } catch (SQLException e) {
                this.getLogger().warning("Ошибка при получении pending titles: " + e.getMessage());
            }
            return titles;
        });
    }

    public CompletableFuture<Void> decreasePlayerAmount(String playerName, int value) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE player_amounts SET amount = amount - ? WHERE player = ? AND amount >= ?")) {

                ps.setInt(1, value);
                ps.setString(2, playerName);
                ps.setInt(3, value);
                ps.executeUpdate();
            } catch (SQLException e) {
                this.getLogger().warning("Ошибка при уменьшении amount: " + e.getMessage());
            }
        });
    }

    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.saveDefaultDatabaseConfig();
        this.reloadDatabaseConfig();

        this.databaseManager = createDatabaseManager();
        this.initializeDatabase();

        this.getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        this.getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        ((PluginCommand)Objects.requireNonNull(this.getCommand("customtitul"))).setExecutor(new TitleCommandExecutor(this));
        ((PluginCommand)Objects.requireNonNull(this.getCommand("customtitul"))).setTabCompleter(new TitleCommandExecutor(this));
        ((PluginCommand)Objects.requireNonNull(this.getCommand("customtitles"))).setExecutor(new TitleCommandExecutor(this));
        ((PluginCommand)Objects.requireNonNull(this.getCommand("customtitles"))).setTabCompleter(new TitleCommandExecutor(this));
        ((PluginCommand)Objects.requireNonNull(this.getCommand("tituladmin"))).setExecutor(new TitleCommandExecutor(this));
        ((PluginCommand)Objects.requireNonNull(this.getCommand("tituladmin"))).setTabCompleter(new TitleCommandExecutor(this));
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    private void initializeDatabase() {
        CompletableFuture.runAsync(() -> {
            try {
                databaseManager.initializeTables();
                getLogger().info("База данных " + databaseManager.getDatabaseType() + " успешно инициализирована");
            } catch (Exception e) {
                getLogger().severe("Ошибка инициализации базы данных: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Map<String, Timestamp>> getApprovedTitlesWithTime(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Timestamp> titles = new HashMap<>();
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT title, approved_time FROM player_titles WHERE player_name = ?")) {

                ps.setString(1, player.getName());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String title = rs.getString("title");
                        String timestampStr = rs.getString("approved_time");
                        Timestamp timestamp = null;

                        try {
                            if (timestampStr != null && !timestampStr.isEmpty()) {
                                timestamp = Timestamp.valueOf(timestampStr);
                            }
                        } catch (IllegalArgumentException e) {
                            try {
                                if (timestampStr.contains("T")) {
                                    timestampStr = timestampStr.replace("T", " ");
                                    if (timestampStr.contains("Z")) {
                                        timestampStr = timestampStr.replace("Z", "");
                                    }
                                    if (timestampStr.contains(".")) {
                                        timestampStr = timestampStr.substring(0, timestampStr.indexOf("."));
                                    }
                                    timestamp = Timestamp.valueOf(timestampStr);
                                } else {
                                    timestamp = new Timestamp(System.currentTimeMillis());
                                    this.getLogger().warning("Используется текущее время для записи с невалидным timestamp: " + timestampStr);
                                }
                            } catch (Exception ex) {
                                timestamp = new Timestamp(System.currentTimeMillis());
                                this.getLogger().warning("Невозможно обработать timestamp: " + timestampStr + ", используется текущее время");
                            }
                        }
                        titles.put(title, timestamp);
                    }
                }
            } catch (SQLException e) {
                Logger var10000 = this.getLogger();
                String var10001 = player.getName();
                var10000.warning("Ошибка при получении одобренных титулов с временем для " + var10001 + ": " + e.getMessage());
            }
            return titles;
        });
    }

    public CompletableFuture<List<String>> getPendingTitles(Player player) {
        return getPendingTitles(player.getName());
    }

    public CompletableFuture<List<String>> getPendingTitles(String player) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> titles = new ArrayList<>();
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT title FROM pending_titles WHERE player_name = ?")) {

                ps.setString(1, player);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        titles.add(rs.getString("title"));
                    }
                }
            } catch (SQLException e) {
                this.getLogger().warning("Ошибка при получении титулов на модерации для " + player + ": " + e.getMessage());
            }
            return titles;
        });
    }

    public CompletableFuture<Void> giveAmount(String playerName, int amount) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO player_amounts (player, amount) VALUES (?, ?) ON DUPLICATE KEY UPDATE amount = amount + VALUES(amount)")) {

                ps.setString(1, playerName);
                ps.setInt(2, amount);
                ps.executeUpdate();
            } catch (SQLException e) {
                this.getLogger().warning("Ошибка при обновлении amount для " + playerName + ": " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> approveTitle(String playerName, String title) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT IGNORE INTO player_titles (player_name, title, approved_time) VALUES (?, ?, CURRENT_TIMESTAMP)")) {
                    ps.setString(1, playerName);
                    ps.setString(2, title);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM pending_titles WHERE player_name = ? AND title = ?")) {
                    ps.setString(1, playerName);
                    ps.setString(2, title);
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                this.getLogger().warning("Ошибка при одобрении титула для " + playerName + ": " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> rejectTitle(String playerName, String title) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM pending_titles WHERE player_name = ? AND title = ?")) {

                ps.setString(1, playerName);
                ps.setString(2, title);
                ps.executeUpdate();

                Player player = Bukkit.getPlayerExact(playerName);
                if (player != null) {
                    try (PreparedStatement psx = conn.prepareStatement("UPDATE player_amounts SET amount = amount + 1 WHERE player = ?")) {
                        psx.setString(1, playerName);
                        psx.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                this.getLogger().warning("Ошибка при отклонении титула для " + playerName + ": " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> addPlayerTitle(String playerName, String title) {
        return CompletableFuture.runAsync(() -> {
            Bukkit.getScheduler().runTask(this, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "litebans broadcast broadcast-type:titulrequest &f\\n&x&F&7&6&E&0&6 &n◢&f Заявка на титул от игрока &x&8&6&E&F&8&8" + playerName + " &7( всего " + ((List)this.getPendingTitles(playerName).join()).size() + " ).\\n&x&F&7&6&E&0&6 ◤&f Титул: &x&D&B&A&9&4&6" + title + "&8 | &x&D&B&A&9&4&6/tituladmin&f\\n");
            });

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT IGNORE INTO pending_titles (player_name, title) VALUES (?, ?)")) {

                ps.setString(1, playerName);
                ps.setString(2, title);
                ps.executeUpdate();
            } catch (SQLException e) {
                this.getLogger().warning("Ошибка при добавлении титула для " + playerName + ": " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> subtractTokens(String playerName, int amount) {
        return this.decreasePlayerAmount(playerName, amount);
    }

    public void onDisable() {
        try {
            if (databaseManager != null) {
                databaseManager.closeConnection();
            }
        } catch (SQLException e) {
            this.getLogger().warning("Ошибка при закрытии соединения с базой данных: " + e.getMessage());
        }
    }
}