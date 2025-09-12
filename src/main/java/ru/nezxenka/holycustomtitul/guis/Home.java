package ru.nezxenka.holycustomtitul.guis;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import lombok.Generated;
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
import ru.nezxenka.holycustomtitul.utils.Parser;

public class Home implements InventoryHolder {
    private static final int[] TITLE_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41};
    private final Map<ItemStack, String> map;
    private final Inventory menu;
    private final int currentPage;
    private int maxPages;

    public Home(JavaPlugin plugin, Player player, HolyCustomTitul holyCustomTituls) {
        this(plugin, player, holyCustomTituls, 1);
    }

    public Home(JavaPlugin plugin, Player player, HolyCustomTitul holyCustomTituls, int page) {
        this.map = new HashMap();
        this.currentPage = page;
        this.menu = Bukkit.createInventory(this, 54, "Кастомные титулы");
        ItemStack clear = new ItemStack(Material.valueOf(plugin.getConfig().getString("home.clear.material")));
        ItemMeta clearmeta = clear.getItemMeta();
        clearmeta.setDisplayName(Parser.color((String)Objects.requireNonNull(plugin.getConfig().getString("home.clear.name"))));
        clear.setItemMeta(clearmeta);
        this.menu.setItem(0, clear);
        Integer[] cyanslots = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 36, 44, 45, 46, 47, 48, 50, 51, 52};
        Material cyanMaterial = Material.valueOf(plugin.getConfig().getString("home.decor.1.material"));
        String cyanName = plugin.getConfig().getString("home.decor.1.name");
        Material orangeMaterial = Material.valueOf(plugin.getConfig().getString("home.decor.2.material"));
        String orangeName = plugin.getConfig().getString("home.decor.2.name");
        ItemStack cyan = this.createNamedItem(cyanMaterial, Parser.color(cyanName));
        ItemStack orange = this.createNamedItem(orangeMaterial, Parser.color(orangeName));
        ItemMeta cyanmeta = cyan.getItemMeta();
        cyan.setItemMeta(cyanmeta);
        ItemMeta orangemeta = orange.getItemMeta();
        orange.setItemMeta(orangemeta);
        Integer[] arrayOfInteger1 = cyanslots;
        int length = cyanslots.length;

        int j;
        for(j = 0; j < length; ++j) {
            int a = arrayOfInteger1[j];
            this.menu.setItem(a, cyan);
        }

        Integer[] orangeslots = new Integer[]{18, 26, 27, 35};
        Integer[] arrayOfInteger2 = orangeslots;
        j = orangeslots.length;

        for(int b = 0; b < j; ++b) {
            int a = arrayOfInteger2[b];
            this.menu.setItem(a, orange);
        }

        ItemStack main = new ItemStack(Material.valueOf(plugin.getConfig().getString("home.menu.material")));
        ItemMeta mainmeta = main.getItemMeta();
        mainmeta.setDisplayName(Parser.color(plugin.getConfig().getString("home.menu.name")));
        main.setItemMeta(mainmeta);
        this.menu.setItem(49, main);
        ItemStack back = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta backmeta = back.getItemMeta();
        backmeta.setDisplayName(Parser.color(plugin.getConfig().getString("back")));
        back.setItemMeta(backmeta);
        this.menu.setItem(53, back);

        try {
            Map<String, Timestamp> approvedTitles = (Map)holyCustomTituls.getApprovedTitlesWithTime(player).get();
            List<String> pendingTitles = (List)holyCustomTituls.getPendingTitles(player.getName()).get();
            List<ItemStack> allTitleItems = new ArrayList();
            Iterator var29 = approvedTitles.entrySet().iterator();

            while(var29.hasNext()) {
                Entry<String, Timestamp> entry = (Entry)var29.next();
                allTitleItems.add(this.createApprovedItem((String)entry.getKey(), (Timestamp)entry.getValue(), plugin));
            }

            var29 = pendingTitles.iterator();

            while(var29.hasNext()) {
                String title = (String)var29.next();
                allTitleItems.add(this.createPendingItem(title, plugin));
            }

            this.maxPages = (int)Math.ceil((double)allTitleItems.size() / (double)TITLE_SLOTS.length);
            ItemStack nextPage;
            ItemMeta nextMeta;
            if (this.currentPage > 1) {
                nextPage = new ItemStack(Material.ARROW);
                nextMeta = nextPage.getItemMeta();
                nextMeta.setDisplayName(Parser.color(plugin.getConfig().getString("pagination.previous")));
                nextPage.setItemMeta(nextMeta);
                this.menu.setItem(42, nextPage);
            }

            if (this.currentPage < this.maxPages) {
                nextPage = new ItemStack(Material.ARROW);
                nextMeta = nextPage.getItemMeta();
                nextMeta.setDisplayName(Parser.color(plugin.getConfig().getString("pagination.next")));
                nextPage.setItemMeta(nextMeta);
                this.menu.setItem(43, nextPage);
            }

            int slotIndex = 0;
            int startIndex = (page - 1) * TITLE_SLOTS.length;
            int endIndex = Math.min(startIndex + TITLE_SLOTS.length, allTitleItems.size());

            for(int i = startIndex; i < endIndex; ++i) {
                ItemStack currentItem = (ItemStack)allTitleItems.get(i);
                this.menu.setItem(TITLE_SLOTS[slotIndex], currentItem);
                List<String> lore = currentItem.getItemMeta().getLore();
                if (lore != null && !lore.isEmpty()) {
                    String firstLine = (String)lore.get(0);
                    String title = firstLine.substring(firstLine.lastIndexOf(": ") + 2).replace("&r", "");
                    this.map.put(currentItem, title);
                }

                ++slotIndex;
            }

        } catch (Exception var37) {
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
        meta.setDisplayName(Parser.color(nameFormat));
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "type"), PersistentDataType.STRING, "approved");
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "titul"), PersistentDataType.STRING, titul);
        List<String> lore = new ArrayList();
        Iterator var9 = loreFormat.iterator();

        while(var9.hasNext()) {
            String line = (String)var9.next();
            line = line.replace("%titul%", titul);
            line = line.replace("%time%", Parser.formatTime(approvedTime));
            lore.add(Parser.color(line));
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
        meta.setDisplayName(Parser.color(nameFormat));
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "type"), PersistentDataType.STRING, "pending");
        List<String> lore = new ArrayList();
        Iterator var8 = loreFormat.iterator();

        while(var8.hasNext()) {
            String line = (String)var8.next();
            line = line.replace("%titul%", titul);
            lore.add(Parser.color(line));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @NotNull
    public Inventory getInventory() {
        Inventory var10000 = this.menu;
        if (var10000 == null) {
            $$$reportNull$$$0(0);
        }

        return var10000;
    }

    @Generated
    public Map<ItemStack, String> getMap() {
        return this.map;
    }

    @Generated
    public int getCurrentPage() {
        return this.currentPage;
    }

    @Generated
    public int getMaxPages() {
        return this.maxPages;
    }

    // $FF: synthetic method
    private static void $$$reportNull$$$0(int var0) {
        throw new IllegalStateException(String.format("@NotNull method %s.%s must not return null", "ru/nezxenka/holycustomtitul/guis/Home", "getInventory"));
    }
}