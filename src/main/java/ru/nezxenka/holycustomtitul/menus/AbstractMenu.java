package ru.nezxenka.holycustomtitul.menus;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class AbstractMenu implements InventoryHolder {
    protected Inventory inventory;

    protected void createInventory(int size, String title) {
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    public void open(org.bukkit.entity.Player player) {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    protected org.bukkit.inventory.ItemStack createItem(org.bukkit.Material material, String name, java.util.List<String> lore) {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(material);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ru.nezxenka.holycustomtitul.utils.TextFormatter.color(name));
            if (lore != null) {
                meta.setLore(lore.stream()
                        .map(ru.nezxenka.holycustomtitul.utils.TextFormatter::color)
                        .collect(java.util.stream.Collectors.toList()));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}