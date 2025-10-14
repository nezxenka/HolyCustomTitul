package ru.nezxenka.holycustomtitul.menus;

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
import ru.nezxenka.holycustomtitul.utils.TextFormatter;

public class TitleCreationMenu implements InventoryHolder {
    private final Inventory menu;
    private final HolyCustomTitul plugin;

    public TitleCreationMenu(JavaPlugin plugin, int amount) {
        this.plugin = (HolyCustomTitul)plugin;
        this.menu = Bukkit.createInventory(this, 54, TextFormatter.color("Создание титула"));
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

        ItemStack cyan = createNamedItem(cyanMaterial, TextFormatter.color(cyanName));
        ItemStack orange = createNamedItem(orangeMaterial, TextFormatter.color(orangeName));

        for (int slot : cyanSlots) {
            this.menu.setItem(slot, cyan);
        }

        for (int slot : orangeSlots) {
            this.menu.setItem(slot, orange);
        }
    }

    private void addBackButton() {
        ItemStack back = createNamedItem(Material.SPECTRAL_ARROW, TextFormatter.color(this.plugin.getConfig().getString("back")));
        this.menu.setItem(53, back);
    }

    private void addInfoItems(JavaPlugin plugin, int amount) {
        ItemStack info = createConfigItem(plugin, "creategui.info", null);
        ItemStack allInfo = createConfigItem(plugin, "creategui.allinfo", null);
        ItemStack create = createConfigItem(plugin, "creategui.create", text -> text.replace("%amount%", Integer.toString(amount)));

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

    private ItemStack createConfigItem(JavaPlugin plugin, String configPath, LoreReplacer replacer) {
        String materialName = plugin.getConfig().getString(configPath + ".material");
        Material material = Material.valueOf(Objects.requireNonNull(materialName));
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = plugin.getConfig().getString(configPath + ".name");
            meta.setDisplayName(TextFormatter.color(Objects.requireNonNull(name)));
            List<String> lore = new ArrayList();

            for (String line : plugin.getConfig().getStringList(configPath + ".lore")) {
                String processedLine = TextFormatter.color(line);
                if (replacer != null) {
                    processedLine = replacer.replace(processedLine);
                }
                lore.add(processedLine);
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @NotNull
    public Inventory getInventory() {
        return this.menu;
    }

    @FunctionalInterface
    private interface LoreReplacer {
        String replace(String text);
    }
}