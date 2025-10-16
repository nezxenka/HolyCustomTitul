package ru.nezxenka.holycustomtitul.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import ru.nezxenka.holycustomtitul.HolyCustomTitul;
import ru.nezxenka.holycustomtitul.menus.*;
import ru.nezxenka.holycustomtitul.utils.TextFormatter;

public class InventoryHandler implements Listener {
    private final HolyCustomTitul plugin;

    public InventoryHandler(HolyCustomTitul plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof AbstractMenu) {
            event.setCancelled(true);
            handleMenuClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof TitleConfirmationMenu) {
            TitleConfirmationMenu confirmation = (TitleConfirmationMenu) holder;
            if (!confirmation.isConfirmed()) {
                Player player = (Player) event.getPlayer();
                plugin.getDatabaseManager().addPlayerTokens(player.getName(), 1);
                player.sendMessage(TextFormatter.color("&cТитул отменен. Токен возвращен."));
            }
        }
    }

    private void handleMenuClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        InventoryHolder holder = event.getInventory().getHolder();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        if (holder instanceof TitleMainMenu) {
            handleMainMenuClick(event, (TitleMainMenu) holder, player);
        } else if (holder instanceof TitleCreationMenu) {
            handleCreationMenuClick(event, (TitleCreationMenu) holder, player);
        } else if (holder instanceof AdminModerationMenu) {
            handleAdminMenuClick(event, (AdminModerationMenu) holder, player);
        } else if (holder instanceof TitleConfirmationMenu) {
            handleConfirmationMenuClick(event, (TitleConfirmationMenu) holder, player);
        }
    }

    private void handleMainMenuClick(InventoryClickEvent event, TitleMainMenu menu, Player player) {
        int slot = event.getSlot();
        if (slot == 0) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " meta removesuffix 1");
            player.closeInventory();
        } else if (slot == 49) {
            new TitleCreationMenu(plugin, player).open();
        } else if (slot == 53) {
            player.closeInventory();
        } else if (slot == 42 && menu.hasPreviousPage()) {
            new TitleMainMenu(plugin, player, menu.getCurrentPage() - 1).open();
        } else if (slot == 43 && menu.hasNextPage()) {
            new TitleMainMenu(plugin, player, menu.getCurrentPage() + 1).open();
        } else {
            handleTitleItemClick(event, menu, player);
        }
    }

    private void handleTitleItemClick(InventoryClickEvent event, TitleMainMenu menu, Player player) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem.getType() == Material.PAPER) {
            String titleType = clickedItem.getItemMeta().getPersistentDataContainer()
                    .getOrDefault(new NamespacedKey(plugin, "type"), PersistentDataType.STRING, "none");
            String title = clickedItem.getItemMeta().getPersistentDataContainer()
                    .getOrDefault(new NamespacedKey(plugin, "title"), PersistentDataType.STRING, "");

            if ("approved".equalsIgnoreCase(titleType)) {
                player.closeInventory();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "lp user " + player.getName() + " meta setsuffix 1 \" " + title + "\"");
            } else {
                player.sendMessage(TextFormatter.color(plugin.getConfigManager().getConfig("messages")
                        .getString("title_on_moderation", "&cТитул на модерации")));
            }
        }
    }

    private void handleCreationMenuClick(InventoryClickEvent event, TitleCreationMenu menu, Player player) {
        int slot = event.getSlot();
        if (slot == 21) {
            String link = plugin.getConfigManager().getConfig("links").getString("link");
            java.util.List<String> lines = plugin.getConfigManager().getConfig("links").getStringList("linkmessage");
            TextFormatter.sendClickableMessage(player, lines, link);
            player.closeInventory();
        } else if (slot == 31) {
            plugin.getDatabaseManager().getPlayerTokens(player.getName()).thenAccept(tokens -> {
                if (tokens > 0) {
                    plugin.getDatabaseManager().removePlayerTokens(player.getName(), 1);
                    new TitleConfirmationMenu(plugin, player).open();
                } else {
                    player.sendMessage(TextFormatter.color("&cНедостаточно токенов"));
                }
            });
        } else if (slot == 53) {
            new TitleMainMenu(plugin, player).open();
        }
    }

    private void handleAdminMenuClick(InventoryClickEvent event, AdminModerationMenu menu, Player player) {
        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();

        if (slot == 45 && menu.hasPreviousPage()) {
            new AdminModerationMenu(plugin, menu.getCurrentPage() - 1).open(player);
        } else if (slot == 53 && menu.hasNextPage()) {
            new AdminModerationMenu(plugin, menu.getCurrentPage() + 1).open(player);
        } else if (clickedItem.getType() == Material.PAPER) {
            String[] titleData = menu.getTitleData(clickedItem);
            if (titleData != null) {
                String playerName = titleData[0];
                String title = titleData[1];
                if (event.isLeftClick()) {
                    plugin.getDatabaseManager().approveTitle(playerName, title);
                    player.sendMessage(TextFormatter.color("&aТитул одобрен для " + playerName));
                } else if (event.isRightClick()) {
                    plugin.getDatabaseManager().rejectTitle(playerName, title);
                    player.sendMessage(TextFormatter.color("&cТитул отклонен для " + playerName));
                }
                new AdminModerationMenu(plugin, menu.getCurrentPage()).open(player);
            }
        }
    }

    private void handleConfirmationMenuClick(InventoryClickEvent event, TitleConfirmationMenu menu, Player player) {
        int slot = event.getSlot();
        if (slot == 0) {
            menu.setConfirmed(false);
            player.closeInventory();
        } else if (slot == 4) {
            menu.setConfirmed(true);
            plugin.getDatabaseManager().addPendingTitle(player.getName(), menu.getTitle());
            player.closeInventory();
            player.sendMessage(TextFormatter.color(plugin.getConfigManager().getConfig("messages")
                    .getString("pending", "&aТитул отправлен на модерацию")));

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "litebans broadcast broadcast-type:titulrequest &f\\n&x&F&7&6&E&0&6 &n◢&f Заявка на титул от игрока &x&8&6&E&F&8&8" +
                                player.getName() + " &7( всего " + menu.getPendingCount() + " ).\\n&x&F&7&6&E&0&6 ◤&f Титул: &x&D&B&A&9&4&6" +
                                menu.getTitle() + "&8 | &x&D&B&A&9&4&6/tituladmin&f\\n");
            });
        }
    }
}