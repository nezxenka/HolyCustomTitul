package ru.nezxenka.holycustomtitul;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.nezxenka.holycustomtitul.commands.Commands;
import ru.nezxenka.holycustomtitul.guis.Create;
import ru.nezxenka.holycustomtitul.guis.Home;
import ru.nezxenka.holycustomtitul.listeners.Chat;
import ru.nezxenka.holycustomtitul.listeners.Gui;

public final class HolyCustomTitul extends JavaPlugin implements Listener {
    public final Map<Player, Player> map = new HashMap();
    public Create create;
    public Home home;
    private static HolyCustomTitul instance;
    private Connection connection;

    public static HolyCustomTitul getInstance() {
        return instance;
    }

    public CompletableFuture<Integer> getPlayerAmount(String player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement ps = this.connection.prepareStatement("SELECT amount FROM player_amounts WHERE player = ?");

                label74: {
                    Integer var4;
                    try {
                        ps.setString(1, player);
                        ResultSet rs = ps.executeQuery();

                        label76: {
                            try {
                                if (rs.next()) {
                                    var4 = rs.getInt("amount");
                                    break label76;
                                }
                            } catch (Throwable var8) {
                                if (rs != null) {
                                    try {
                                        rs.close();
                                    } catch (Throwable var7) {
                                        var8.addSuppressed(var7);
                                    }
                                }

                                throw var8;
                            }

                            if (rs != null) {
                                rs.close();
                            }
                            break label74;
                        }

                        if (rs != null) {
                            rs.close();
                        }
                    } catch (Throwable var9) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var6) {
                                var9.addSuppressed(var6);
                            }
                        }

                        throw var9;
                    }

                    if (ps != null) {
                        ps.close();
                    }

                    return var4;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException var10) {
                this.getLogger().warning("Ошибка при получении amount: " + var10.getMessage());
            }

            return 0;
        });
    }

    public CompletableFuture<List<String[]>> getPendingTitles() {
        return CompletableFuture.supplyAsync(() -> {
            ArrayList titles = new ArrayList();

            try {
                Statement stmt = this.connection.createStatement();

                try {
                    ResultSet rs = stmt.executeQuery("SELECT player_name, title FROM pending_titles");

                    try {
                        while(rs.next()) {
                            String[] titleData = new String[]{rs.getString("player_name"), rs.getString("title")};
                            titles.add(titleData);
                        }
                    } catch (Throwable var8) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var7) {
                                var8.addSuppressed(var7);
                            }
                        }

                        throw var8;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var9) {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (Throwable var6) {
                            var9.addSuppressed(var6);
                        }
                    }

                    throw var9;
                }

                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException var10) {
                this.getLogger().warning("Ошибка при получении pending titles: " + var10.getMessage());
            }

            return titles;
        });
    }

    public CompletableFuture<Void> decreasePlayerAmount(String playerName, int value) {
        return CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement ps = this.connection.prepareStatement("UPDATE player_amounts SET amount = amount - ? WHERE player = ? AND amount >= ?");

                try {
                    ps.setInt(1, value);
                    ps.setString(2, playerName);
                    ps.setInt(3, value);
                    ps.executeUpdate();
                } catch (Throwable var7) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var6) {
                            var7.addSuppressed(var6);
                        }
                    }

                    throw var7;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException var8) {
                this.getLogger().warning("Ошибка при уменьшении amount: " + var8.getMessage());
            }

        });
    }

    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.initializeDatabase();
        this.getServer().getPluginManager().registerEvents(new Chat(this), this);
        this.getServer().getPluginManager().registerEvents(new Gui(this), this);
        ((PluginCommand)Objects.requireNonNull(this.getCommand("customtitul"))).setExecutor(new Commands(this));
        ((PluginCommand)Objects.requireNonNull(this.getCommand("customtitul"))).setTabCompleter(new Commands(this));
        ((PluginCommand)Objects.requireNonNull(this.getCommand("customtitles"))).setExecutor(new Commands(this));
        ((PluginCommand)Objects.requireNonNull(this.getCommand("customtitles"))).setTabCompleter(new Commands(this));
        ((PluginCommand)Objects.requireNonNull(this.getCommand("tituladmin"))).setExecutor(new Commands(this));
        ((PluginCommand)Objects.requireNonNull(this.getCommand("tituladmin"))).setTabCompleter(new Commands(this));
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    private void initializeDatabase() {
        CompletableFuture.runAsync(() -> {
            try {
                String url = "jdbc:mysql://localhost:3306/customtutil?useSSL=false&autoReconnect=true";
                String username = "plugins";
                String password = "plugins_customtitul";
                this.connection = DriverManager.getConnection(url, username, password);
                Statement stmt = this.connection.createStatement();

                try {
                    stmt.execute("CREATE TABLE IF NOT EXISTS pending_titles (player_name VARCHAR(16) NOT NULL, title VARCHAR(255) NOT NULL, PRIMARY KEY (player_name, title)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                    stmt.execute("CREATE TABLE IF NOT EXISTS player_titles (player_name VARCHAR(16) NOT NULL, title VARCHAR(255) NOT NULL, approved_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (player_name, title)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                    stmt.execute("CREATE TABLE IF NOT EXISTS player_amounts (player VARCHAR(16) NOT NULL, amount INT DEFAULT 0, PRIMARY KEY (player)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

                    try {
                        DatabaseMetaData dbm = this.connection.getMetaData();
                        ResultSet columns = dbm.getColumns((String)null, (String)null, "player_titles", "approved_time");
                        if (!columns.next()) {
                            stmt.execute("ALTER TABLE player_titles ADD COLUMN approved_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                        }

                        columns.close();
                    } catch (SQLException var8) {
                        this.getLogger().warning("Ошибка при проверке колонки approved_time: " + var8.getMessage());
                    }
                } catch (Throwable var9) {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (Throwable var7) {
                            var9.addSuppressed(var7);
                        }
                    }

                    throw var9;
                }

                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception var10) {
                this.getLogger().severe("Ошибка инициализации базы данных MySQL: " + var10.getMessage());
                StackTraceElement[] var2 = var10.getStackTrace();
                int var3 = var2.length;

                for(int var4 = 0; var4 < var3; ++var4) {
                    StackTraceElement element = var2[var4];
                    this.getLogger().severe("\t" + element.toString());
                }
            }

        });
    }

    public CompletableFuture<Map<String, Timestamp>> getApprovedTitlesWithTime(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            HashMap titles = new HashMap();

            try {
                PreparedStatement ps = this.connection.prepareStatement("SELECT title, approved_time FROM player_titles WHERE player_name = ?");

                try {
                    ps.setString(1, player.getName());
                    ResultSet rs = ps.executeQuery();

                    String title;
                    Timestamp timestamp;
                    try {
                        for(; rs.next(); titles.put(title, timestamp)) {
                            title = rs.getString("title");
                            String timestampStr = rs.getString("approved_time");
                            timestamp = null;

                            try {
                                if (timestampStr != null && !timestampStr.isEmpty()) {
                                    timestamp = Timestamp.valueOf(timestampStr);
                                }
                            } catch (IllegalArgumentException var13) {
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
                                } catch (Exception var12) {
                                    timestamp = new Timestamp(System.currentTimeMillis());
                                    this.getLogger().warning("Невозможно обработать timestamp: " + timestampStr + ", используется текущее время");
                                }
                            }
                        }
                    } catch (Throwable var14) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var11) {
                                var14.addSuppressed(var11);
                            }
                        }

                        throw var14;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var15) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var10) {
                            var15.addSuppressed(var10);
                        }
                    }

                    throw var15;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException var16) {
                Logger var10000 = this.getLogger();
                String var10001 = player.getName();
                var10000.warning("Ошибка при получении одобренных титулов с временем для " + var10001 + ": " + var16.getMessage());
            }

            return titles;
        });
    }

    public CompletableFuture<List<String>> getPendingTitles(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            ArrayList titles = new ArrayList();

            try {
                PreparedStatement ps = this.connection.prepareStatement("SELECT title FROM pending_titles WHERE player_name = ?");

                try {
                    ps.setString(1, player.getName());
                    ResultSet rs = ps.executeQuery();

                    try {
                        while(rs.next()) {
                            titles.add(rs.getString("title"));
                        }
                    } catch (Throwable var9) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var8) {
                                var9.addSuppressed(var8);
                            }
                        }

                        throw var9;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var10) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var7) {
                            var10.addSuppressed(var7);
                        }
                    }

                    throw var10;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException var11) {
                Logger var10000 = this.getLogger();
                String var10001 = player.getName();
                var10000.warning("Ошибка при получении титулов на модерации для " + var10001 + ": " + var11.getMessage());
            }

            return titles;
        });
    }

    public CompletableFuture<List<String>> getPendingTitles(String player) {
        return CompletableFuture.supplyAsync(() -> {
            ArrayList titles = new ArrayList();

            try {
                PreparedStatement ps = this.connection.prepareStatement("SELECT title FROM pending_titles WHERE player_name = ?");

                try {
                    ps.setString(1, player);
                    ResultSet rs = ps.executeQuery();

                    try {
                        while(rs.next()) {
                            titles.add(rs.getString("title"));
                        }
                    } catch (Throwable var9) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var8) {
                                var9.addSuppressed(var8);
                            }
                        }

                        throw var9;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var10) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var7) {
                            var10.addSuppressed(var7);
                        }
                    }

                    throw var10;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException var11) {
                this.getLogger().warning("Ошибка при получении титулов на модерации для " + player + ": " + var11.getMessage());
            }

            return titles;
        });
    }

    public CompletableFuture<Void> giveAmount(String playerName, int amount) {
        return CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement ps = this.connection.prepareStatement("INSERT INTO player_amounts (player, amount) VALUES (?, ?) ON DUPLICATE KEY UPDATE amount = amount + VALUES(amount)");

                try {
                    ps.setString(1, playerName);
                    ps.setInt(2, amount);
                    ps.executeUpdate();
                } catch (Throwable var7) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var6) {
                            var7.addSuppressed(var6);
                        }
                    }

                    throw var7;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException var8) {
                this.getLogger().warning("Ошибка при обновлении amount для " + playerName + ": " + var8.getMessage());
            }

        });
    }

    public CompletableFuture<Void> approveTitle(String playerName, String title) {
        return CompletableFuture.runAsync(() -> {
            PreparedStatement ps;
            try {
                ps = this.connection.prepareStatement("INSERT IGNORE INTO player_titles (player_name, title, approved_time) VALUES (?, ?, CURRENT_TIMESTAMP)");

                try {
                    ps.setString(1, playerName);
                    ps.setString(2, title);
                    ps.executeUpdate();
                } catch (Throwable var10) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var7) {
                            var10.addSuppressed(var7);
                        }
                    }

                    throw var10;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException var11) {
                this.getLogger().warning("Ошибка при одобрении титула для " + playerName + ": " + var11.getMessage());
            }

            try {
                ps = this.connection.prepareStatement("DELETE FROM pending_titles WHERE player_name = ? AND title = ?");

                try {
                    ps.setString(1, playerName);
                    ps.setString(2, title);
                    ps.executeUpdate();
                } catch (Throwable var8) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var6) {
                            var8.addSuppressed(var6);
                        }
                    }

                    throw var8;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException var9) {
                this.getLogger().warning("Ошибка при удалении одобренного титула для " + playerName + ": " + var9.getMessage());
            }

        });
    }

    public CompletableFuture<Void> rejectTitle(String playerName, String title) {
        return CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement ps = this.connection.prepareStatement("DELETE FROM pending_titles WHERE player_name = ? AND title = ?");

                try {
                    ps.setString(1, playerName);
                    ps.setString(2, title);
                    ps.executeUpdate();
                } catch (Throwable var11) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var8) {
                            var11.addSuppressed(var8);
                        }
                    }

                    throw var11;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException var12) {
                this.getLogger().warning("Ошибка при отклонении титула для " + playerName + ": " + var12.getMessage());
            }

            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null) {
                try {
                    PreparedStatement psx = this.connection.prepareStatement("UPDATE player_amounts SET amount = amount + 1 WHERE player = ?");

                    try {
                        psx.setString(1, playerName);
                        psx.executeUpdate();
                    } catch (Throwable var9) {
                        if (psx != null) {
                            try {
                                psx.close();
                            } catch (Throwable var7) {
                                var9.addSuppressed(var7);
                            }
                        }

                        throw var9;
                    }

                    if (psx != null) {
                        psx.close();
                    }
                } catch (SQLException var10) {
                    this.getLogger().warning("Ошибка при возврате amount для " + playerName + ": " + var10.getMessage());
                }
            }

        });
    }

    public CompletableFuture<Void> addPlayerTitle(String playerName, String title) {
        return CompletableFuture.runAsync(() -> {
            Bukkit.getScheduler().runTask(this, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "litebans broadcast broadcast-type:titulrequest &f\\n&x&F&7&6&E&0&6 &n◢&f Заявка на титул от игрока &x&8&6&E&F&8&8" + playerName + " &7( всего " + ((List)this.getPendingTitles(playerName).join()).size() + " ).\\n&x&F&7&6&E&0&6 ◤&f Титул: &x&D&B&A&9&4&6" + title + "&8 | &x&D&B&A&9&4&6/tituladmin&f\\n");
            });

            try {
                PreparedStatement ps = this.connection.prepareStatement("INSERT IGNORE INTO pending_titles (player_name, title) VALUES (?, ?)");

                try {
                    ps.setString(1, playerName);
                    ps.setString(2, title);
                    ps.executeUpdate();
                } catch (Throwable var7) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var6) {
                            var7.addSuppressed(var6);
                        }
                    }

                    throw var7;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException var8) {
                this.getLogger().warning("Ошибка при добавлении титула для " + playerName + ": " + var8.getMessage());
            }

        });
    }

    public CompletableFuture<Void> subtractTokens(String playerName, int amount) {
        return this.decreasePlayerAmount(playerName, amount);
    }

    public void onDisable() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (SQLException var2) {
            this.getLogger().warning("Ошибка при закрытии соединения с базой данных: " + var2.getMessage());
        }

    }
}