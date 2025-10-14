package ru.nezxenka.holycustomtitul.menus;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.nezxenka.holycustomtitul.HolyCustomTitul;
import ru.nezxenka.holycustomtitul.utils.TextFormatter;

public class TitleMainMenu implements InventoryHolder {
    private static final int[] TITLE_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41};
    private final Map<ItemStack, String> titleMap;
    private final Inventory menu;
    private final int currentPage;
    private int maxPages;

    public TitleMainMenu(JavaPlugin plugin, Player player, HolyCustomTitul holyCustomTituls) {
        this(plugin, player, holyCustomTituls, 1);
    }

    public TitleMainMenu(JavaPlugin plugin, Player player, HolyCustomTitul holyCustomTituls, int page) {
        this.titleMap = new HashMap();
        this.currentPage = page;
        this.menu = Bukkit.createInventory(this, 54, "Кастомные титулы");

        ItemStack clear = new ItemStack(Material.valueOf(plugin.getConfig().getString("home.clear.material")));
        ItemMeta clearmeta = clear.getItemMeta();
        clearmeta.setDisplayName(TextFormatter.color(Objects.requireNonNull(plugin.getConfig().getString("home.clear.name"))));
        clear.setItemMeta(clearmeta);
        this.menu.setItem(0, clear);

        Integer[] cyanslots = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 36, 44, 45, 46, 47, 48, 50, 51, 52};
        Material cyanMaterial = Material.valueOf(plugin.getConfig().getString("home.decor.1.material"));
        String cyanName = plugin.getConfig().getString("home.decor.1.name");
        Material orangeMaterial = Material.valueOf(plugin.getConfig().getString("home.decor.2.material"));
        String orangeName = plugin.getConfig().getString("home.decor.2.name");

        ItemStack cyan = createNamedItem(cyanMaterial, TextFormatter.color(cyanName));
        ItemStack orange = createNamedItem(orangeMaterial, TextFormatter.color(orangeName));

        for (int slot : cyanslots) {
            this.menu.setItem(slot, cyan);
        }

        Integer[] orangeslots = new Integer[]{18, 26, 27, 35};
        for (int slot : orangeslots) {
            this.menu.setItem(slot, orange);
        }

        ItemStack main = new ItemStack(Material.valueOf(plugin.getConfig().getString("home.menu.material")));
        ItemMeta mainmeta = main.getItemMeta();
        mainmeta.setDisplayName(TextFormatter.color(plugin.getConfig().getString("home.menu.name")));
        main.setItemMeta(mainmeta);
        this.menu.setItem(49, main);

        ItemStack back = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta backmeta = back.getItemMeta();
        backmeta.setDisplayName(TextFormatter.color(plugin.getConfig().getString("back")));
        back.setItemMeta(backmeta);
        this.menu.setItem(53, back);

        try {
            Map<String, Timestamp> approvedTitles = holyCustomTituls.getApprovedTitlesWithTime(player).get();
            List<String> pendingTitles = holyCustomTituls.getPendingTitles(player.getName()).get();
            List<ItemStack> allTitleItems = new ArrayList();

            for (Entry<String, Timestamp> entry : approvedTitles.entrySet()) {
                allTitleItems.add(createApprovedItem(entry.getKey(), entry.getValue(), plugin));
            }

            for (String title : pendingTitles) {
                allTitleItems.add(createPendingItem(title, plugin));
            }

            this.maxPages = (int)Math.ceil((double)allTitleItems.size() / (double)TITLE_SLOTS.length);
            ItemStack nextPage;
            ItemMeta nextMeta;

            if (this.currentPage > 1) {
                nextPage = new ItemStack(Material.ARROW);
                nextMeta = nextPage.getItemMeta();
                nextMeta.setDisplayName(TextFormatter.color(plugin.getConfig().getString("pagination.previous")));
                nextPage.setItemMeta(nextMeta);
                this.menu.setItem(42, nextPage);
            }

            if (this.currentPage < this.maxPages) {
                nextPage = new ItemStack(Material.ARROW);
                nextMeta = nextPage.getItemMeta();
                nextMeta.setDisplayName(TextFormatter.color(plugin.getConfig().getString("pagination.next")));
                nextPage.setItemMeta(nextMeta);
                this.menu.setItem(43, nextPage);
            }

            int slotIndex = 0;
            int startIndex = (page - 1) * TITLE_SLOTS.length;
            int endIndex = Math.min(startIndex + TITLE_SLOTS.length, allTitleItems.size());

            for (int i = startIndex; i < endIndex; ++i) {
                ItemStack currentItem = allTitleItems.get(i);
                this.menu.setItem(TITLE_SLOTS[slotIndex], currentItem);
                List<String> lore = currentItem.getItemMeta().getLore();
                if (lore != null && !lore.isEmpty()) {
                    String firstLine = lore.get(0);
                    String title = firstLine.substring(firstLine.lastIndexOf(": ") + 2).replace("&r", "");
                    this.titleMap.put(currentItem, title);
                }
                ++slotIndex;
            }

        } catch (Exception e) {
        }
    }

    private ItemStack createNamedItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createApprovedItem(String titul, Timestamp approvedTime, JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        String nameFormat = plugin.getConfig().getString("home.titul_approved.name", " ");
        List<String> loreFormat = plugin.getConfig().getStringList("home.titul_approved.lore");
        meta.setDisplayName(TextFormatter.color(nameFormat));
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "type"), PersistentDataType.STRING, "approved");
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "titul"), PersistentDataType.STRING, titul);
        List<String> lore = new ArrayList();

        for (String line : loreFormat) {
            line = line.replace("%titul%", titul);
            line = line.replace("%time%", TextFormatter.formatTime(approvedTime));
            lore.add(TextFormatter.color(line));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPendingItem(String titul, JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        String nameFormat = plugin.getConfig().getString("home.titul_pending.name", " ");
        List<String> loreFormat = plugin.getConfig().getStringList("home.titul_pending.lore");
        meta.setDisplayName(TextFormatter.color(nameFormat));
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "type"), PersistentDataType.STRING, "pending");
        List<String> lore = new ArrayList();

        for (String line : loreFormat) {
            line = line.replace("%titul%", titul);
            lore.add(TextFormatter.color(line));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @NotNull
    public Inventory getInventory() {
        return this.menu;
    }

    public Map<ItemStack, String> getTitleMap() {
        return this.titleMap;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public int getMaxPages() {
        return this.maxPages;
    }
}