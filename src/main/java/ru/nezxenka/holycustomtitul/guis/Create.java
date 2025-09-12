package ru.nezxenka.holycustomtitul.guis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.nezxenka.holycustomtitul.HolyCustomTitul;
import ru.nezxenka.holycustomtitul.utils.Parser;

public class Create implements InventoryHolder {
    private final Inventory menu;
    private final HolyCustomTitul plugin;

    public Create(JavaPlugin plugin, int amount) {
        this.plugin = (HolyCustomTitul)plugin;
        this.menu = Bukkit.createInventory(this, 54, Parser.color("Создание титула"));
        this.initGlassPanes();
        this.addBackButton();
        this.addInfoItems(plugin, amount);
    }

    private void initGlassPanes() {
        Integer[] cyanSlots = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
        Integer[] orangeSlots = new Integer[]{18, 26, 27, 35};
        Material cyanMaterial = Material.valueOf(this.plugin.getConfig().getString("create.decor.1.material"));
        String cyanName = this.plugin.getConfig().getString("create.decor.1.name");
        Material orangeMaterial = Material.valueOf(this.plugin.getConfig().getString("create.decor.2.material"));
        String orangeName = this.plugin.getConfig().getString("create.decor.2.name");
        ItemStack cyan = this.createNamedItem(cyanMaterial, Parser.color(cyanName));
        ItemStack orange = this.createNamedItem(orangeMaterial, Parser.color(orangeName));
        Integer[] var9 = cyanSlots;
        int var10 = cyanSlots.length;

        int var11;
        int slot;
        for(var11 = 0; var11 < var10; ++var11) {
            slot = var9[var11];
            this.menu.setItem(slot, cyan);
        }

        var9 = orangeSlots;
        var10 = orangeSlots.length;

        for(var11 = 0; var11 < var10; ++var11) {
            slot = var9[var11];
            this.menu.setItem(slot, orange);
        }

    }

    private void addBackButton() {
        ItemStack back = this.createNamedItem(Material.SPECTRAL_ARROW, Parser.color(this.plugin.getConfig().getString("back")));
        this.menu.setItem(53, back);
    }

    private void addInfoItems(JavaPlugin plugin, int amount) {
        ItemStack info = this.createConfigItem(plugin, "creategui.info", (Create.LoreReplacer)null);
        ItemStack allInfo = this.createConfigItem(plugin, "creategui.allinfo", (Create.LoreReplacer)null);
        ItemStack create = this.createConfigItem(plugin, "creategui.create", (text) -> {
            return text.replace("%amount%", Integer.toString(amount));
        });
        this.menu.setItem(21, info);
        this.menu.setItem(23, allInfo);
        this.menu.setItem(31, create);
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

    private ItemStack createConfigItem(JavaPlugin plugin, String configPath, Create.LoreReplacer replacer) {
        String materialName = plugin.getConfig().getString(configPath + ".material");
        Material material = Material.valueOf((String)Objects.requireNonNull(materialName));
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = plugin.getConfig().getString(configPath + ".name");
            meta.setDisplayName(Parser.color((String)Objects.requireNonNull(name)));
            List<String> lore = new ArrayList();

            String processedLine;
            for(Iterator var10 = plugin.getConfig().getStringList(configPath + ".lore").iterator(); var10.hasNext(); lore.add(processedLine)) {
                String line = (String)var10.next();
                processedLine = Parser.color(line);
                if (replacer != null) {
                    processedLine = replacer.replace(processedLine);
                }
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

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
    
    private static void $$$reportNull$$$0(int var0) {
        throw new IllegalStateException(String.format("@NotNull method %s.%s must not return null", "ru/nezxenka/holycustomtitul/guis/Create", "getInventory"));
    }

    @FunctionalInterface
    private interface LoreReplacer {
        String replace(String var1);
    }
}