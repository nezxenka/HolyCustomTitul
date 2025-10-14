package ru.nezxenka.holycustomtitul.listeners;

import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import ru.nezxenka.holycustomtitul.HolyCustomTitul;
import ru.nezxenka.holycustomtitul.menus.AdminModerationMenu;
import ru.nezxenka.holycustomtitul.menus.TitleConfirmationMenu;
import ru.nezxenka.holycustomtitul.menus.TitleCreationMenu;
import ru.nezxenka.holycustomtitul.menus.TitleMainMenu;
import ru.nezxenka.holycustomtitul.utils.TextFormatter;

public class InventoryClickListener implements Listener {
    private final HolyCustomTitul plugin;

    public InventoryClickListener(HolyCustomTitul plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof TitleMainMenu || holder instanceof TitleCreationMenu ||
                holder instanceof AdminModerationMenu || holder instanceof TitleConfirmationMenu) {
            event.setCancelled(true);
            if (holder instanceof TitleMainMenu) {
                this.handleMainMenu(event);
            } else if (holder instanceof TitleCreationMenu) {
                this.handleCreationMenu(event);
            } else if (holder instanceof AdminModerationMenu) {
                this.handleAdminMenu(event);
            } else {
                this.handleConfirmationClick(event);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof TitleConfirmationMenu) {
            TitleConfirmationMenu confirmation = (TitleConfirmationMenu)event.getView().getTopInventory().getHolder();
            if (confirmation.closed) {
                return;
            }
            Player player = (Player)event.getPlayer();
            String playerName = player.getName();
            player.sendMessage(TextFormatter.color("&x&5&A&D&9&F&B▶ &fТитул отменен."));
            this.plugin.giveAmount(playerName, 1);
        }
    }

    private void handleConfirmationClick(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        String playerName = player.getName();
        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            TitleConfirmationMenu confirmation = (TitleConfirmationMenu)event.getView().getTopInventory().getHolder();
            switch(slot) {
                case 0:
                    player.closeInventory();
                    break;
                case 4:
                    confirmation.closed = true;
                    player.closeInventory();
                    this.plugin.addPlayerTitle(player.getName(), confirmation.titul);
                    String pendingMessage = this.plugin.getConfig().getString("messages.pending");
                    player.sendMessage(TextFormatter.color(Objects.requireNonNull(pendingMessage)));
            }
        }
    }

    private void handleAdminMenu(InventoryClickEvent event) {
        try {
            event.setCancelled(true);
            Player player = (Player)event.getWhoClicked();
            int slot = event.getSlot();
            AdminModerationMenu admin = (AdminModerationMenu)Objects.requireNonNull(event.getClickedInventory()).getHolder();
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            Material type = clickedItem.getType();
            AdminModerationMenu nextMenu;
            if (slot == 44 && admin.getCurrentPage() > 1) {
                nextMenu = new AdminModerationMenu(this.plugin, admin.getCurrentPage() - 1);
                player.openInventory(nextMenu.getInventory());
                return;
            }

            if (type == Material.BLACK_STAINED_GLASS_PANE || type == Material.BARRIER || type == Material.ARROW) {
                event.setCancelled(true);
                return;
            }

            if (slot == 53 && admin.getCurrentPage() < admin.getMaxPages()) {
                nextMenu = new AdminModerationMenu(this.plugin, admin.getCurrentPage() + 1);
                player.openInventory(nextMenu.getInventory());
                return;
            }

            if (clickedItem.getType() == Material.PAPER) {
                String[] titleData = admin.getItemToTitleMap().get(clickedItem);
                if (titleData != null) {
                    String playerName = titleData[0];
                    String title = titleData[1];
                    String msg;
                    if (event.isLeftClick()) {
                        this.plugin.approveTitle(playerName, title);
                        msg = this.plugin.getConfig().getString("messages.admin.approve", "&aТитул для &f%player% &aодобрен");
                        player.sendMessage(TextFormatter.color(msg.replace("%player%", playerName)));
                    } else if (event.isRightClick()) {
                        this.plugin.rejectTitle(playerName, title);
                        msg = this.plugin.getConfig().getString("messages.admin.reject", "&cТитул для &f%player% &cотклонен");
                        player.sendMessage(TextFormatter.color(msg.replace("%player%", playerName)));
                    }
                    AdminModerationMenu refreshedMenu = new AdminModerationMenu(this.plugin, admin.getCurrentPage());
                    player.openInventory(refreshedMenu.getInventory());
                }
            }
        } catch (Exception e) {
        }
    }

    private void handleMainMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player)event.getWhoClicked();
        int slot = event.getSlot();
        TitleMainMenu nextPage;
        if (slot == 42 && this.plugin.home.getCurrentPage() > 1) {
            nextPage = new TitleMainMenu(this.plugin, player, this.plugin, this.plugin.home.getCurrentPage() - 1);
            player.openInventory(nextPage.getInventory());
        } else if (slot == 43 && this.plugin.home.getCurrentPage() < this.plugin.home.getMaxPages()) {
            nextPage = new TitleMainMenu(this.plugin, player, this.plugin, this.plugin.home.getCurrentPage() + 1);
            player.openInventory(nextPage.getInventory());
        } else {
            switch(slot) {
                case 0:
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " meta removesuffix 1");
                    player.closeInventory();
                    break;
                case 49:
                    try {
                        this.plugin.create = new TitleCreationMenu(this.plugin, this.plugin.getPlayerAmount(player.getName()).get());
                        player.openInventory(this.plugin.create.getInventory());
                        break;
                    } catch (Exception e) {
                        return;
                    }
                case 53:
                    player.closeInventory();
                    player.performCommand("titul");
                    break;
                default:
                    this.handlePaperClick(event);
            }
        }
    }

    private void handlePaperClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && clickedItem.getType() == Material.PAPER) {
            Player player = (Player)event.getWhoClicked();
            try {
                String titul = clickedItem.getItemMeta().getPersistentDataContainer().getOrDefault(new NamespacedKey(this.plugin, "titul"), PersistentDataType.STRING, "none");
                if (clickedItem.getItemMeta().getPersistentDataContainer().getOrDefault(new NamespacedKey(this.plugin, "type"), PersistentDataType.STRING, "none").equalsIgnoreCase("approved")) {
                    player.closeInventory();
                    ConsoleCommandSender console = Bukkit.getConsoleSender();
                    Bukkit.dispatchCommand(console, "lp user " + player.getName() + " meta setsuffix 1 \" " + titul + "\"");
                } else {
                    event.setCancelled(true);
                    String message = this.plugin.getConfig().getString("messages.title_on_moderation", "&cЭтот титул еще на модерации!");
                    player.sendMessage(TextFormatter.color(message));
                }
            } catch (Exception e) {
            }
        }
    }

    private void handleCreationMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player)event.getWhoClicked();
        int slot = event.getSlot();
        switch(slot) {
            case 21:
                String link = this.plugin.getConfig().getString("link");
                List<String> lines = this.plugin.getConfig().getStringList("linkmessage");
                TextFormatter.sendClickableMessage(player, lines, link, this.plugin);
                player.closeInventory();
                break;
            case 31:
                String playerName = player.getName();
                try {
                    int amount = this.plugin.getPlayerAmount(playerName).get();
                    if (amount > 0) {
                        this.plugin.map.put(player, player);
                        String msg = this.plugin.getConfig().getString("messages.create.chat", "&aВведите титул в чат");
                        player.sendMessage(TextFormatter.color(msg));
                        this.plugin.decreasePlayerAmount(playerName, 1);
                        player.closeInventory();
                    }
                    break;
                } catch (Exception e) {
                    return;
                }
            case 53:
                player.openInventory(this.plugin.home.getInventory());
        }
    }
}