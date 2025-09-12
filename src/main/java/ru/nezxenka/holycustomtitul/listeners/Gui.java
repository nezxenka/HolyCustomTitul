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
import ru.nezxenka.holycustomtitul.guis.Admin;
import ru.nezxenka.holycustomtitul.guis.Create;
import ru.nezxenka.holycustomtitul.guis.Home;
import ru.nezxenka.holycustomtitul.guis.Proof;
import ru.nezxenka.holycustomtitul.utils.Parser;

public class Gui implements Listener {
    private final HolyCustomTitul plugin;

    public Gui(HolyCustomTitul plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void click(InventoryClickEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof Home || holder instanceof Create || holder instanceof Admin || holder instanceof Proof) {
            event.setCancelled(true);
            if (holder instanceof Home) {
                this.handleHome(event);
            } else if (holder instanceof Create) {
                this.handleCreate(event);
            } else if (holder instanceof Admin) {
                this.handleAdminMenu(event);
            } else {
                this.handleProofClick(event);
            }
        }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof Proof) {
            Proof proof = (Proof)event.getView().getTopInventory().getHolder();
            if (proof.closed) {
                return;
            }

            Player player = (Player)event.getPlayer();
            String playerName = player.getName();
            player.sendMessage(Parser.color("&x&5&A&D&9&F&B▶ &fТитул отменен."));
            this.plugin.giveAmount(playerName, 1);
        }

    }

    private void handleProofClick(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        String playerName = player.getName();
        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            Proof proof = (Proof)event.getView().getTopInventory().getHolder();
            switch(slot) {
                case 0:
                    player.closeInventory();
                    break;
                case 4:
                    proof.closed = true;
                    player.closeInventory();
                    this.plugin.addPlayerTitle(player.getName(), proof.titul);
                    String pendingMessage = this.plugin.getConfig().getString("messages.pending");
                    player.sendMessage(Parser.color((String)Objects.requireNonNull(pendingMessage)));
            }

        }
    }

    private void handleAdminMenu(InventoryClickEvent event) {
        try {
            event.setCancelled(true);
            Player player = (Player)event.getWhoClicked();
            int slot = event.getSlot();
            Admin admin = (Admin)((Inventory)Objects.requireNonNull(event.getClickedInventory())).getHolder();
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            Material type = clickedItem.getType();
            Admin nextMenu;
            if (slot == 44 && ((Admin)Objects.requireNonNull(admin)).getCurrentPage() > 1) {
                nextMenu = new Admin(this.plugin, admin.getCurrentPage() - 1);
                player.openInventory(nextMenu.getInventory());
                return;
            }

            if (type == Material.BLACK_STAINED_GLASS_PANE || type == Material.BARRIER || type == Material.ARROW) {
                event.setCancelled(true);
                return;
            }

            if (slot == 53 && ((Admin)Objects.requireNonNull(admin)).getCurrentPage() < admin.getMaxPages()) {
                nextMenu = new Admin(this.plugin, admin.getCurrentPage() + 1);
                player.openInventory(nextMenu.getInventory());
                return;
            }

            if (clickedItem.getType() == Material.PAPER) {
                String[] titleData = (String[])((Admin)Objects.requireNonNull(admin)).getItemToTitleMap().get(clickedItem);
                if (titleData != null) {
                    String playerName = titleData[0];
                    String title = titleData[1];
                    String msg;
                    if (event.isLeftClick()) {
                        this.plugin.approveTitle(playerName, title);
                        msg = this.plugin.getConfig().getString("messages.admin.approve", "&aТитул для &f%player% &aодобрен");
                        player.sendMessage(Parser.color(msg.replace("%player%", playerName)));
                    } else if (event.isRightClick()) {
                        this.plugin.rejectTitle(playerName, title);
                        msg = this.plugin.getConfig().getString("messages.admin.reject", "&cТитул для &f%player% &cотклонен");
                        player.sendMessage(Parser.color(msg.replace("%player%", playerName)));
                    }

                    Admin refreshedMenu = new Admin(this.plugin, admin.getCurrentPage());
                    player.openInventory(refreshedMenu.getInventory());
                }
            }
        } catch (Exception var11) {
        }

    }

    private void handleHome(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player)event.getWhoClicked();
        int slot = event.getSlot();
        Home nextPage;
        if (slot == 42 && this.plugin.home.getCurrentPage() > 1) {
            nextPage = new Home(this.plugin, player, this.plugin, this.plugin.home.getCurrentPage() - 1);
            player.openInventory(nextPage.getInventory());
        } else if (slot == 43 && this.plugin.home.getCurrentPage() < this.plugin.home.getMaxPages()) {
            nextPage = new Home(this.plugin, player, this.plugin, this.plugin.home.getCurrentPage() + 1);
            player.openInventory(nextPage.getInventory());
        } else {
            switch(slot) {
                case 0:
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " meta removesuffix 1");
                    player.closeInventory();
                    break;
                case 49:
                    try {
                        this.plugin.create = new Create(this.plugin, (Integer)this.plugin.getPlayerAmount(player.getName()).get());
                        player.openInventory(this.plugin.create.getInventory());
                        break;
                    } catch (Exception var5) {
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
                String titul = (String)clickedItem.getItemMeta().getPersistentDataContainer().getOrDefault(new NamespacedKey(this.plugin, "titul"), PersistentDataType.STRING, "none");
                if (((String)clickedItem.getItemMeta().getPersistentDataContainer().getOrDefault(new NamespacedKey(this.plugin, "type"), PersistentDataType.STRING, "none")).equalsIgnoreCase("approved")) {
                    player.closeInventory();
                    ConsoleCommandSender var10000 = Bukkit.getConsoleSender();
                    String var10001 = player.getName();
                    Bukkit.dispatchCommand(var10000, "lp user " + var10001 + " meta setsuffix 1 \" " + titul + "\"");
                } else {
                    event.setCancelled(true);
                    String message = this.plugin.getConfig().getString("messages.title_on_moderation", "&cЭтот титул еще на модерации!");
                    player.sendMessage(Parser.color(message));
                }

            } catch (Exception var6) {
            }
        }
    }

    private void handleCreate(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player)event.getWhoClicked();
        int slot = event.getSlot();
        switch(slot) {
            case 21:
                String link = this.plugin.getConfig().getString("link");
                List<String> lines = this.plugin.getConfig().getStringList("linkmessage");
                Parser.sendClickableMessage(player, lines, link, this.plugin);
                player.closeInventory();
                break;
            case 31:
                String playerName = player.getName();

                try {
                    int amount = (Integer)this.plugin.getPlayerAmount(playerName).get();
                    if (amount > 0) {
                        this.plugin.map.put(player, player);
                        String msg = this.plugin.getConfig().getString("messages.create.chat", "&aВведите титул в чат");
                        player.sendMessage(Parser.color(msg));
                        this.plugin.decreasePlayerAmount(playerName, 1);
                        player.closeInventory();
                    }
                    break;
                } catch (Exception var7) {
                    return;
                }
            case 53:
                player.openInventory(this.plugin.home.getInventory());
        }

    }
}