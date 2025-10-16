package ru.nezxenka.holycustomtitul.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.nezxenka.holycustomtitul.HolyCustomTitul;
import ru.nezxenka.holycustomtitul.utils.TextFormatter;
import java.sql.Timestamp;
import java.util.*;

public class TitleMainMenu extends AbstractMenu {
    private final HolyCustomTitul plugin;
    private final Player player;
    private final int currentPage;
    private int maxPages;
    private final Map<ItemStack, String> titleItems;

    private static final int[] TITLE_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41};
    private static final int[] CYAN_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 36, 44, 45, 46, 47, 48, 50, 51, 52};
    private static final int[] ORANGE_SLOTS = {18, 26, 27, 35};

    public TitleMainMenu(HolyCustomTitul plugin, Player player) {
        this(plugin, player, 1);
    }

    public TitleMainMenu(HolyCustomTitul plugin, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.currentPage = page;
        this.titleItems = new HashMap<>();
        initializeMenu();
    }

    private void initializeMenu() {
        createInventory(54, TextFormatter.color("Кастомные титулы"));
        setupDecorations();
        setupFunctionalItems();
        loadTitles();
    }

    private void setupDecorations() {
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfigManager().getConfig("home_menu");

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

    private void setupFunctionalItems() {
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfigManager().getConfig("home_menu");
        org.bukkit.configuration.file.FileConfiguration messages = plugin.getConfigManager().getConfig("messages");
        org.bukkit.configuration.file.FileConfiguration navigation = plugin.getConfigManager().getConfig("navigation");

        ItemStack clear = createItem(
                Material.valueOf(config.getString("clear.material", "BARRIER")),
                config.getString("clear.name", "&cОчистить титул"),
                null
        );
        inventory.setItem(0, clear);

        ItemStack createMenu = createItem(
                Material.valueOf(config.getString("menu.material", "ITEM_FRAME")),
                config.getString("menu.name", "&bСоздать титул"),
                null
        );
        inventory.setItem(49, createMenu);

        ItemStack back = createItem(
                Material.SPECTRAL_ARROW,
                navigation.getString("back", "&bНазад"),
                null
        );
        inventory.setItem(53, back);

        if (currentPage > 1) {
            ItemStack prev = createItem(
                    Material.ARROW,
                    navigation.getString("pagination.previous", "&bПредыдущая"),
                    null
            );
            inventory.setItem(42, prev);
        }

        if (currentPage < maxPages) {
            ItemStack next = createItem(
                    Material.ARROW,
                    navigation.getString("pagination.next", "&bСледующая"),
                    null
            );
            inventory.setItem(43, next);
        }
    }

    private void loadTitles() {
        plugin.getDatabaseManager().getApprovedTitles(player.getName()).thenAccept(approvedTitles -> {
            plugin.getDatabaseManager().getPlayerPendingTitles(player.getName()).thenAccept(pendingTitles -> {
                List<ItemStack> allTitleItems = new ArrayList<>();

                for (Map.Entry<String, Timestamp> entry : approvedTitles.entrySet()) {
                    allTitleItems.add(createApprovedTitleItem(entry.getKey(), entry.getValue()));
                }

                for (String title : pendingTitles) {
                    allTitleItems.add(createPendingTitleItem(title));
                }

                maxPages = (int) Math.ceil((double) allTitleItems.size() / TITLE_SLOTS.length);
                int startIndex = (currentPage - 1) * TITLE_SLOTS.length;
                int endIndex = Math.min(startIndex + TITLE_SLOTS.length, allTitleItems.size());

                for (int i = startIndex; i < endIndex; i++) {
                    int slotIndex = i - startIndex;
                    if (slotIndex < TITLE_SLOTS.length) {
                        ItemStack titleItem = allTitleItems.get(i);
                        inventory.setItem(TITLE_SLOTS[slotIndex], titleItem);
                        titleItems.put(titleItem, getTitleFromItem(titleItem));
                    }
                }
            });
        });
    }

    private ItemStack createApprovedTitleItem(String title, Timestamp approvedTime) {
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfigManager().getConfig("home_menu");
        List<String> lore = new ArrayList<>();

        for (String line : config.getStringList("titul_approved.lore")) {
            String processedLine = line.replace("%titul%", title)
                    .replace("%time%", TextFormatter.formatTime(approvedTime));
            lore.add(processedLine);
        }

        ItemStack item = createItem(Material.PAPER, config.getString("titul_approved.name", " "), lore);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "type"),
                org.bukkit.persistence.PersistentDataType.STRING,
                "approved"
        );
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "title"),
                org.bukkit.persistence.PersistentDataType.STRING,
                title
        );
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPendingTitleItem(String title) {
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfigManager().getConfig("home_menu");
        List<String> lore = new ArrayList<>();

        for (String line : config.getStringList("titul_pending.lore")) {
            lore.add(line.replace("%titul%", title));
        }

        ItemStack item = createItem(Material.PAPER, config.getString("titul_pending.name", " "), lore);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "type"),
                org.bukkit.persistence.PersistentDataType.STRING,
                "pending"
        );
        item.setItemMeta(meta);
        return item;
    }

    private String getTitleFromItem(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            return item.getItemMeta().getPersistentDataContainer().get(
                    new org.bukkit.NamespacedKey(plugin, "title"),
                    org.bukkit.persistence.PersistentDataType.STRING
            );
        }
        return "";
    }

    public boolean hasPreviousPage() {
        return currentPage > 1;
    }

    public boolean hasNextPage() {
        return currentPage < maxPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void open() {
        player.openInventory(inventory);
    }
}