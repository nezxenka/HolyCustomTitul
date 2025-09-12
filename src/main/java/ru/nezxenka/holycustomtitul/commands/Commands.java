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
import ru.nezxenka.holycustomtitul.guis.Admin;
import ru.nezxenka.holycustomtitul.guis.Home;

public class Commands implements TabExecutor {
    private final HolyCustomTitul plugin;

    public Commands(HolyCustomTitul plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender == null) {
            $$$reportNull$$$0(0);
        }

        if (command == null) {
            $$$reportNull$$$0(1);
        }

        if (label == null) {
            $$$reportNull$$$0(2);
        }

        if (args == null) {
            $$$reportNull$$$0(3);
        }

        String var5 = command.getName().toLowerCase();
        byte var6 = -1;
        switch(var5.hashCode()) {
            case -1758456155:
                if (var5.equals("customtitul")) {
                    var6 = 0;
                }
                break;
            case 1322425292:
                if (var5.equals("customtitles")) {
                    var6 = 2;
                }
                break;
            case 2120980889:
                if (var5.equals("tituladmin")) {
                    var6 = 1;
                }
        }

        Player player;
        switch(var6) {
            case 0:
                if (sender instanceof Player) {
                    player = (Player)sender;
                    this.plugin.home = new Home(this.plugin, player, this.plugin);
                    player.openInventory(this.plugin.home.getInventory());
                    return true;
                }

                return true;
            case 1:
                if (sender instanceof Player) {
                    player = (Player)sender;
                    if (!player.hasPermission("holycustomtitul.tituladmin")) {
                        return true;
                    }

                    Admin admin = new Admin(this.plugin, 1);
                    player.openInventory(admin.getInventory());
                    return true;
                }

                return true;
            case 2:
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
                        } catch (NumberFormatException var10) {
                            sender.sendMessage((String)Objects.requireNonNull(this.plugin.getConfig().getString("messages.error")));
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
                        } catch (NumberFormatException var9) {
                            sender.sendMessage((String)Objects.requireNonNull(this.plugin.getConfig().getString("messages.error")));
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
        if (sender == null) {
            $$$reportNull$$$0(4);
        }

        if (command == null) {
            $$$reportNull$$$0(5);
        }

        if (alias == null) {
            $$$reportNull$$$0(6);
        }

        if (args == null) {
            $$$reportNull$$$0(7);
        }

        List<String> completions = new ArrayList();
        if (command.getName().equalsIgnoreCase("customtitles")) {
            if (!sender.hasPermission("holycustomtitul.admin")) {
                return completions;
            }

            if (args.length == 1) {
                String[] subCommands = new String[]{"give", "reload"};
                return this.filterCompletions(subCommands, args[0]);
            }

            if (args.length == 2 && args[0].equals("give")) {
                return (List)Bukkit.getOnlinePlayers().stream().map(OfflinePlayer::getName).filter((name) -> {
                    return name.toLowerCase().startsWith(args[1].toLowerCase());
                }).collect(Collectors.toList());
            }

            if (args.length == 3 && args[0].equals("give")) {
                completions.add("1");
                return completions;
            }
        }

        return completions;
    }

    private List<String> filterCompletions(String[] options, String input) {
        return (List)Arrays.stream(options).filter((option) -> {
            return option.toLowerCase().startsWith(input.toLowerCase());
        }).collect(Collectors.toList());
    }

    private static void $$$reportNull$$$0(int var0) {
        Object[] var10001 = new Object[3];
        switch(var0) {
            case 0:
            case 4:
            default:
                var10001[0] = "sender";
                break;
            case 1:
            case 5:
                var10001[0] = "command";
                break;
            case 2:
                var10001[0] = "label";
                break;
            case 3:
            case 7:
                var10001[0] = "args";
                break;
            case 6:
                var10001[0] = "alias";
        }

        var10001[1] = "ru/nezxenka/holycustomtitul/commands/Commands";
        switch(var0) {
            case 0:
            case 1:
            case 2:
            case 3:
            default:
                var10001[2] = "onCommand";
                break;
            case 4:
            case 5:
            case 6:
            case 7:
                var10001[2] = "onTabComplete";
        }

        throw new IllegalArgumentException(String.format("Argument for @NotNull parameter '%s' of %s.%s must not be null", var10001));
    }
}