package ru.nezxenka.holycustomtitul.handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import ru.nezxenka.holycustomtitul.HolyCustomTitul;
import ru.nezxenka.holycustomtitul.menus.AdminModerationMenu;
import ru.nezxenka.holycustomtitul.menus.TitleMainMenu;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHandler implements TabExecutor {
    private final HolyCustomTitul plugin;

    public CommandHandler(HolyCustomTitul plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName().toLowerCase();

        switch (commandName) {
            case "customtitul":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    new TitleMainMenu(plugin, player).open();
                }
                return true;

            case "tituladmin":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.hasPermission("holycustomtitul.tituladmin")) {
                        new AdminModerationMenu(plugin, 1).open(player);
                    }
                }
                return true;

            case "customtitles":
                if (!sender.hasPermission("holycustomtitul.admin")) {
                    return true;
                }

                if (args.length < 1) {
                    return false;
                }

                switch (args[0].toLowerCase()) {
                    case "give":
                        if (args.length < 3) return false;
                        String playerName = args[1];
                        try {
                            int amount = Integer.parseInt(args[2]);
                            plugin.getDatabaseManager().addPlayerTokens(playerName, amount);
                            sender.sendMessage("Выдано " + amount + " токенов игроку " + playerName);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("Неверное количество");
                        }
                        break;

                    case "take":
                        if (args.length < 3) return false;
                        playerName = args[1];
                        try {
                            int amount = Integer.parseInt(args[2]);
                            plugin.getDatabaseManager().removePlayerTokens(playerName, amount);
                            sender.sendMessage("Отнято " + amount + " токенов у " + playerName);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("Неверное количество");
                        }
                        break;

                    case "reload":
                        plugin.getConfigManager().loadAllConfigs();
                        sender.sendMessage("Конфигурация перезагружена");
                        break;
                }
                return true;

            default:
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("customtitles")) {
            if (!sender.hasPermission("holycustomtitul.admin")) {
                return completions;
            }

            if (args.length == 1) {
                return Arrays.asList("give", "take", "reload").stream()
                        .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take"))) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (args.length == 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take"))) {
                completions.add("1");
                completions.add("5");
                completions.add("10");
            }
        }

        return completions;
    }
}