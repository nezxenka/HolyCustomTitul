package ru.nezxenka.holycustomtitul.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFormatter {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");
    private static final boolean SUPPORTS_RGB;

    public static String color(String message) {
        if (message == null) return "";

        if (SUPPORTS_RGB) {
            Matcher matcher = HEX_PATTERN.matcher(message);
            StringBuffer buffer = new StringBuffer();

            while (matcher.find()) {
                String hex = matcher.group(1);
                try {
                    String replacement = ChatColor.of("#" + hex).toString();
                    matcher.appendReplacement(buffer, replacement);
                } catch (Exception ignored) {}
            }

            matcher.appendTail(buffer);
            message = buffer.toString();
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void sendClickableMessage(Player player, java.util.List<String> lines, String url) {
        for (String line : lines) {
            BaseComponent[] components = parseColoredText(line);
            for (BaseComponent comp : components) {
                comp.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                comp.setHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Нажмите для перехода по ссылке").create()
                ));
            }
            player.spigot().sendMessage(components);
        }
    }

    public static String formatTime(Timestamp timestamp) {
        if (timestamp == null) return "Не указано";

        SimpleDateFormat sdf = new SimpleDateFormat("d MMM HH:mm:ss", new Locale("ru", "RU"));
        String formattedDate = sdf.format(timestamp);

        formattedDate = formattedDate
                .replace("янв.", "янв.")
                .replace("фев.", "фев.")
                .replace("мар.", "мар.")
                .replace("апр.", "апр.")
                .replace("май.", "май.")
                .replace("июн.", "июн.")
                .replace("июл.", "июл.")
                .replace("авг.", "авг.")
                .replace("сен.", "сен.")
                .replace("окт.", "окт.")
                .replace("ноя.", "ноя.")
                .replace("дек.", "дек.");

        return formattedDate;
    }

    private static BaseComponent[] parseColoredText(String message) {
        java.util.List<BaseComponent> components = new java.util.ArrayList<>();
        if (message == null) return new BaseComponent[0];

        message = ChatColor.translateAlternateColorCodes('&', message);
        TextComponent comp = new TextComponent(message);
        components.add(comp);

        return components.toArray(new BaseComponent[0]);
    }

    static {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        int subVersion = Integer.parseInt(version.replace("v", "").replace("1_", "").replaceAll("_R\\d", ""));
        SUPPORTS_RGB = subVersion >= 16;
    }
}