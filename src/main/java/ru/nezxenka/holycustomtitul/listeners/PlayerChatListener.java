package ru.nezxenka.holycustomtitul.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.nezxenka.holycustomtitul.HolyCustomTitul;
import ru.nezxenka.holycustomtitul.menus.TitleConfirmationMenu;

public class PlayerChatListener implements Listener {
    private final HolyCustomTitul plugin;

    public PlayerChatListener(HolyCustomTitul plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.map.containsKey(player)) {
            event.setCancelled(true);
            String title = event.getMessage();
            this.plugin.map.remove(player);
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                new TitleConfirmationMenu(player, title);
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.map.containsKey(player)) {
            refundToken(player.getName());
            this.plugin.map.remove(player);
        }
    }

    private void refundToken(String playerName) {
        try {
            this.plugin.giveAmount(playerName, 1);
        } catch (Exception e) {
            this.plugin.getLogger().warning("Ошибка при возвращения жетона: " + e.getMessage());
        }
    }
}