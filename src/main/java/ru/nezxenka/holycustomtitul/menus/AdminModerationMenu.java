package ru.nezxenka.holycustomtitul.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import ru.nezxenka.holycustomtitul.HolyCustomTitul;
import ru.nezxenka.holycustomtitul.utils.TextFormatter;

public class AdminModerationMenu implements InventoryHolder {
    private Inventory menu;
    private final int currentPage;
    private final Map<ItemStack, String[]> itemToTitleMap = new HashMap();
    private int maxPages;
    private final HolyCustomTitul plugin;

    public AdminModerationMenu(HolyCustomTitul plugin, int page) {
        this.plugin = plugin;
        this.currentPage = page;

        try {
            List<String[]> pendingTitles = plugin.getPendingTitles().get();
            this.maxPages = (int)Math.ceil((double)pendingTitles.size() / 45.0D);
            if (page > this.maxPages) {
                page = this.maxPages;
            }

            if (page < 1) {
                page = 1;
            }

            String titleFormat = plugin.getConfig().getString("admin_gui.title");
            this.menu = Bukkit.createInventory(this, 54, TextFormatter.color(titleFormat.replace("%page%", String.valueOf(page)).replace("%max%", String.valueOf(Math.max(1, this.maxPages)))));

            int startIndex = (page - 1) * 45;
            int endIndex = Math.min(startIndex + 45, pendingTitles.size());
            int slot = 0;

            for (int i = startIndex; i < endIndex; ++i) {
                String[] titleData = pendingTitles.get(i);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                String nameFormat = plugin.getConfig().getString("admin_gui.item.display_name");
                List<String> loreFormat = plugin.getConfig().getStringList("admin_gui.item.lore");

                paperMeta.setDisplayName(TextFormatter.color(nameFormat.replace("%player%", titleData[0])));
                List<String> lore = new ArrayList();

                for (String line : loreFormat) {
                    line = line.replace("%player%", titleData[0]);
                    line = line.replace("%title%", titleData[1]);
                    lore.add(TextFormatter.color(line));
                }

                paperMeta.setLore(lore);
                paper.setItemMeta(paperMeta);

                while (slot == 45 || slot == 53) {
                    ++slot;
                    if (slot >= 54) {
                        break;
                    }
                }

                this.menu.setItem(slot, paper);
                this.itemToTitleMap.put(paper, titleData);
                ++slot;
            }

            this.addNavigationButtons(page, this.maxPages);
            this.addBottomBar(page, this.maxPages);
        } catch (Exception e) {
        }
    }

    private void addBottomBar(int page, int maxPages) {
        int[] slots = new int[]{45, 46, 47, 48, 49, 50, 51, 52, 53};
        for (int slot : slots) {
            if ((slot != 45 || page <= 1) && (slot != 53 || page >= maxPages)) {
                this.menu.setItem(slot, createGlass());
            }
        }
    }

    private ItemStack createGlass() {
        Material glassMaterial = Material.valueOf(this.plugin.getConfig().getString("admin_gui.decor.material", "ORANGE_STAINED_GLASS_PANE"));
        String glassName = this.plugin.getConfig().getString("admin_gui.decor.name", " ");
        ItemStack glass = new ItemStack(glassMaterial);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TextFormatter.color(glassName));
            glass.setItemMeta(meta);
        }
        return glass;
    }

    private ItemStack createArrow(String path) {
        String name = this.plugin.getConfig().getString("admin_gui.buttons." + path + ".name");
        String mat = this.plugin.getConfig().getString("admin_gui.buttons." + path + ".material", "ARROW");
        ItemStack arrow = new ItemStack(Material.valueOf(mat));
        ItemMeta meta = arrow.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TextFormatter.color(name));
            arrow.setItemMeta(meta);
        }
        return arrow;
    }

    private void addNavigationButtons(int page, int maxPages) {
        int SLOT_PREV = 45;
        int SLOT_NEXT = 53;

        if (page > 1) {
            this.menu.setItem(SLOT_PREV, createArrow("prev"));
        }

        if (page < maxPages) {
            this.menu.setItem(SLOT_NEXT, createArrow("next"));
        }
    }

    @NotNull
    public Inventory getInventory() {
        return this.menu;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public Map<ItemStack, String[]> getItemToTitleMap() {
        return this.itemToTitleMap;
    }

    public int getMaxPages() {
        return this.maxPages;
    }
}