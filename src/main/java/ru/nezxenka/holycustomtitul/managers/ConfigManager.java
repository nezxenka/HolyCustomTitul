package ru.nezxenka.holycustomtitul.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.nezxenka.holycustomtitul.HolyCustomTitul;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final HolyCustomTitul plugin;
    private final Map<String, FileConfiguration> configs;
    private final Map<String, File> configFiles;

    public ConfigManager(HolyCustomTitul plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        this.configFiles = new HashMap<>();
    }

    public void loadAllConfigs() {
        loadConfig("admin_gui");
        loadConfig("create_menu");
        loadConfig("home_menu");
        loadConfig("messages");
        loadConfig("navigation");
        loadConfig("links");
        loadConfig("database");
    }

    private void loadConfig(String name) {
        File configFile = new File(plugin.getDataFolder(), name + ".yml");
        if (!configFile.exists()) {
            plugin.saveResource(name + ".yml", false);
        }
        configFiles.put(name, configFile);
        configs.put(name, YamlConfiguration.loadConfiguration(configFile));
    }

    public FileConfiguration getConfig(String name) {
        return configs.get(name);
    }

    public void saveConfig(String name) {
        try {
            configs.get(name).save(configFiles.get(name));
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка сохранения конфига " + name + ": " + e.getMessage());
        }
    }

    public void reloadConfig(String name) {
        configs.put(name, YamlConfiguration.loadConfiguration(configFiles.get(name)));
    }
}