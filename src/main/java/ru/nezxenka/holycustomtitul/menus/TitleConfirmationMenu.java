package ru.nezxenka.holycustomtitul.menus;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import ru.nezxenka.holycustomtitul.utils.TextFormatter;

public class TitleConfirmationMenu implements InventoryHolder {
    private final Inventory menu;
    public String titul;
    public Boolean closed = false;

    public TitleConfirmationMenu(Player player, String titul) {
        this.titul = titul;
        this.menu = Bukkit.createInventory(this, InventoryType.HOPPER, TextFormatter.color("&0Подтверждение титула"));

        ItemStack itemStack = new ItemStack(Material.ITEM_FRAME);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(TextFormatter.color(" "));
        List<String> lore = new ArrayList();
        lore.add(ChatColor.translateAlternateColorCodes('&', " &x&5&A&D&9&F&B&n▍&f &x&5&A&D&9&F&BТитул: " + titul));
        lore.add(TextFormatter.color(" &x&5&A&D&9&F&B&n▍&f Нажмите на кнопки слева или справа, чтобы"));
        lore.add(TextFormatter.color(" &x&5&A&D&9&F&B▍&f подтвердить или отменить титул."));
        lore.add(TextFormatter.color(""));
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        this.menu.setItem(2, itemStack);

        itemStack = new ItemStack(Material.GREEN_CONCRETE);
        itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(TextFormatter.color(" "));
        lore = new ArrayList();
        lore.add(TextFormatter.color(" &a[ЛКМ] &f- подтвердить отправку титула"));
        lore.add(TextFormatter.color(""));
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        this.menu.setItem(4, itemStack);

        itemStack = new ItemStack(Material.RED_CONCRETE);
        itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(TextFormatter.color(" "));
        lore = new ArrayList();
        lore.add(TextFormatter.color(" &c[ЛКМ] &f- отменить отправку титула и вернуть жетон"));
        lore.add(TextFormatter.color(""));
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        this.menu.setItem(0, itemStack);

        player.openInventory(this.menu);
    }

    @NotNull
    public Inventory getInventory() {
        return this.menu;
    }
}