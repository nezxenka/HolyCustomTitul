package ru.nezxenka.holycustomtitul.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.nezxenka.holycustomtitul.HolyCustomTitul;
import ru.nezxenka.holycustomtitul.utils.TextFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminModerationMenu extends AbstractMenu {
    private final HolyCustomTitul plugin;
    private final int currentPage;
    private int maxPages;
    private final Map<ItemStack, String[]> titleItems;

    public AdminModerationMenu(HolyCustomTitul plugin, int page) {
        this.plugin = plugin;
        this.currentPage = page;
        this.titleItems = new HashMap<>();
        initializeMenu();
    }

    private void initializeMenu() {
        createInventory(54, getMenuTitle());
        setupNavigation();
        loadPendingTitles();
    }

    private String getMenuTitle() {
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfigManager().getConfig("admin_gui");
        String titleFormat = config.getString("title", "Заявки на титулы (%page%/%max%)");
        return TextFormatter.color(titleFormat.replace("%page%", String.valueOf(currentPage)).replace("%max%", String.valueOf(maxPages)));
    }

    private void setupNavigation() {
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfigManager().getConfig("admin_gui");
        org.bukkit.configuration.ConfigurationSection buttons = config.getConfigurationSection("buttons");

        if (currentPage > 1) {
            ItemStack prev = createItem(
                    Material.valueOf(buttons.getString("prev.material", "ARROW")),
                    buttons.getString("prev.name", "&bПредыдущая"),
                    null
            );
            inventory.setItem(45, prev);
        }

        if (currentPage < maxPages) {
            ItemStack next = createItem(
                    Material.valueOf(buttons.getString("next.material", "ARROW")),
                    buttons.getString("next.name", "&bСледующая"),
                    null
            );
            inventory.setItem(53, next);
        }
    }

    private void loadPendingTitles() {
        plugin.getDatabaseManager().getPendingTitles().thenAccept(titles -> {
            maxPages = (int) Math.ceil((double) titles.size() / 45.0);
            int startIndex = (currentPage - 1) * 45;
            int endIndex = Math.min(startIndex + 45, titles.size());
            int slot = 0;
            for (int i = startIndex; i < endIndex; i++) {
                if (slot >= 45) break;
                String[] titleData = titles.get(i);
                ItemStack titleItem = createTitleItem(titleData[0], titleData[1]);
                inventory.setItem(slot, titleItem);
                titleItems.put(titleItem, titleData);
                slot++;
            }
        });
    }

    private ItemStack createTitleItem(String playerName, String title) {
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfigManager().getConfig("admin_gui");
        org.bukkit.configuration.ConfigurationSection itemConfig = config.getConfigurationSection("item");

        String displayName = TextFormatter.color(itemConfig.getString("display_name", " ").replace("%player%", playerName));
        List<String> lore = new ArrayList<>();

        for (String line : itemConfig.getStringList("lore")) {
            lore.add(TextFormatter.color(line.replace("%player%", playerName).replace("%title%", title)));
        }

        return createItem(Material.PAPER, displayName, lore);
    }

    public String[] getTitleData(ItemStack item) {
        return titleItems.get(item);
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

    public void open(Player player) {
        player.openInventory(inventory);
    }
}