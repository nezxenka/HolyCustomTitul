package ru.nezxenka.holycustomtitul.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.nezxenka.holycustomtitul.HolyCustomTitul;
import ru.nezxenka.holycustomtitul.utils.TextFormatter;
import java.util.ArrayList;
import java.util.List;

public class TitleCreationMenu extends AbstractMenu {
    private final HolyCustomTitul plugin;
    private final Player player;

    private static final int[] CYAN_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
    private static final int[] ORANGE_SLOTS = {18, 26, 27, 35};

    public TitleCreationMenu(HolyCustomTitul plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        initializeMenu();
    }

    private void initializeMenu() {
        createInventory(54, TextFormatter.color("Создание титула"));
        setupDecorations();
        setupInfoItems();
        setupBackButton();
    }

    private void setupDecorations() {
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfigManager().getConfig("create_menu");

        Material cyanMaterial = Material.valueOf(config.getString("decor.1.material", "CYAN_STAINED_GLASS_PANE"));
        String cyanName = config.getString("decor.1.name", " ");
        Material orangeMaterial = Material.valueOf(config.getString("decor.2.material", "ORANGE_STAINED_GLASS_PANE"));
        String orangeName = config.getString("decor.2.name", " ");

        ItemStack cyan = createItem(cyanMaterial, cyanName, null);
        ItemStack orange = createItem(orangeMaterial, orangeName, null);

        for (int slot : CYAN_SLOTS) {
            inventory.setItem(slot, cyan);
        }

        for (int slot : ORANGE_SLOTS) {
            inventory.setItem(slot, orange);
        }
    }

    private void setupInfoItems() {
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfigManager().getConfig("create_menu");

        ItemStack info = createConfigItem(config, "info");
        ItemStack allInfo = createConfigItem(config, "allinfo");

        plugin.getDatabaseManager().getPlayerTokens(player.getName()).thenAccept(tokens -> {
            ItemStack create = createCreateItem(config, tokens);
            inventory.setItem(21, info);
            inventory.setItem(23, allInfo);
            inventory.setItem(31, create);
        });
    }

    private void setupBackButton() {
        org.bukkit.configuration.file.FileConfiguration navigation = plugin.getConfigManager().getConfig("navigation");
        ItemStack back = createItem(
                Material.SPECTRAL_ARROW,
                navigation.getString("back", "&bНазад"),
                null
        );
        inventory.setItem(53, back);
    }

    private ItemStack createConfigItem(org.bukkit.configuration.file.FileConfiguration config, String path) {
        Material material = Material.valueOf(config.getString(path + ".material", "PAPER"));
        String name = config.getString(path + ".name", " ");
        List<String> lore = config.getStringList(path + ".lore");

        return createItem(material, name, lore);
    }

    private ItemStack createCreateItem(org.bukkit.configuration.file.FileConfiguration config, int tokens) {
        Material material = Material.valueOf(config.getString("create.material", "PAPER"));
        String name = config.getString("create.name", " ");
        List<String> lore = new ArrayList<>();

        for (String line : config.getStringList("create.lore")) {
            lore.add(line.replace("%amount%", String.valueOf(tokens)));
        }

        return createItem(material, name, lore);
    }

    public void open() {
        player.openInventory(inventory);
    }
}