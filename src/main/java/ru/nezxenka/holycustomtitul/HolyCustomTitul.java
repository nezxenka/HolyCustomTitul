package ru.nezxenka.holycustomtitul;

import org.bukkit.plugin.java.JavaPlugin;
import ru.nezxenka.holycustomtitul.managers.ConfigManager;
import ru.nezxenka.holycustomtitul.managers.DatabaseManager;
import ru.nezxenka.holycustomtitul.handlers.CommandHandler;
import ru.nezxenka.holycustomtitul.handlers.InventoryHandler;
import ru.nezxenka.holycustomtitul.handlers.ChatHandler;

public final class HolyCustomTitul extends JavaPlugin {
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private static HolyCustomTitul instance;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(this);

        configManager.loadAllConfigs();
        databaseManager.initialize();

        getServer().getPluginManager().registerEvents(new InventoryHandler(this), this);
        getServer().getPluginManager().registerEvents(new ChatHandler(this), this);

        getCommand("customtitul").setExecutor(new CommandHandler(this));
        getCommand("customtitles").setExecutor(new CommandHandler(this));
        getCommand("tituladmin").setExecutor(new CommandHandler(this));
    }

    @Override
    public void onDisable() {
        databaseManager.closeConnection();
    }

    public static HolyCustomTitul getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}