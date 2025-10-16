package ru.nezxenka.holycustomtitul.handlers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.nezxenka.holycustomtitul.HolyCustomTitul;
import ru.nezxenka.holycustomtitul.menus.TitleConfirmationMenu;
import java.util.HashMap;
import java.util.Map;

public class ChatHandler implements Listener {
    private final HolyCustomTitul plugin;
    private final Map<Player, Boolean> awaitingTitleInput;

    public ChatHandler(HolyCustomTitul plugin) {
        this.plugin = plugin;
        this.awaitingTitleInput = new HashMap<>();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (awaitingTitleInput.containsKey(player)) {
            event.setCancelled(true);
            String title = event.getMessage();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                new TitleConfirmationMenu(plugin, player, title).open();
                awaitingTitleInput.remove(player);
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (awaitingTitleInput.containsKey(player)) {
            plugin.getDatabaseManager().addPlayerTokens(player.getName(), 1);
            awaitingTitleInput.remove(player);
        }
    }

    public void setAwaitingTitleInput(Player player) {
        awaitingTitleInput.put(player, true);
    }

    public boolean isAwaitingTitleInput(Player player) {
        return awaitingTitleInput.containsKey(player);
    }
}