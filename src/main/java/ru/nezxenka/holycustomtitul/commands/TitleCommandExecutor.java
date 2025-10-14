package ru.nezxenka.holycustomtitul.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nezxenka.holycustomtitul.HolyCustomTitul;
import ru.nezxenka.holycustomtitul.menus.AdminModerationMenu;

public class TitleCommandExecutor implements TabExecutor {
    private final HolyCustomTitul plugin;

    public TitleCommandExecutor(HolyCustomTitul plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String commandName = command.getName().toLowerCase();

        switch(commandName) {
            case "customtitul":
                if (sender instanceof Player) {
                    Player player = (Player)sender;
                    this.plugin.home = new ru.nezxenka.holycustomtitul.menus.TitleMainMenu(this.plugin, player, this.plugin);
                    player.openInventory(this.plugin.home.getInventory());
                    return true;
                }
                return true;
            case "tituladmin":
                if (sender instanceof Player) {
                    Player player = (Player)sender;
                    if (!player.hasPermission("holycustomtitul.tituladmin")) {
                        return true;
                    }
                    AdminModerationMenu admin = new AdminModerationMenu(this.plugin, 1);
                    player.openInventory(admin.getInventory());
                    return true;
                }
                return true;
            case "customtitles":
                if (!sender.hasPermission("holycustomtitul.admin")) {
                    return true;
                } else if (args.length < 1) {
                    return true;
                } else {
                    String playerName;
                    int amount;
                    if (args[0].equals("give")) {
                        if (args.length < 3) {
                            return true;
                        }
                        playerName = args[1];
                        try {
                            amount = Integer.parseInt(args[2]);
                            this.plugin.giveAmount(playerName, amount);
                            sender.sendMessage("Выдано " + amount + " игроку " + playerName);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(Objects.requireNonNull(this.plugin.getConfig().getString("messages.error")));
                        }
                    }
                    if (args[0].equals("take")) {
                        if (args.length < 3) {
                            return true;
                        }
                        playerName = args[1];
                        try {
                            amount = Integer.parseInt(args[2]);
                            this.plugin.subtractTokens(playerName, amount);
                            sender.sendMessage("Отнято " + amount + " у " + playerName);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(Objects.requireNonNull(this.plugin.getConfig().getString("messages.error")));
                        }
                    }
                    if (args[0].equals("reload")) {
                        this.plugin.reloadConfig();
                        sender.sendMessage("Конфигурация успешно перезагружена.");
                    }
                }
            default:
                return true;
        }
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList();
        if (command.getName().equalsIgnoreCase("customtitles")) {
            if (!sender.hasPermission("holycustomtitul.admin")) {
                return completions;
            }
            if (args.length == 1) {
                String[] subCommands = new String[]{"give", "reload"};
                return filterCompletions(subCommands, args[0]);
            }
            if (args.length == 2 && args[0].equals("give")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(OfflinePlayer::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 3 && args[0].equals("give")) {
                completions.add("1");
                return completions;
            }
        }
        return completions;
    }

    private List<String> filterCompletions(String[] options, String input) {
        return Arrays.stream(options)
                .filter(option -> option.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}