package com.miauwrijn.gooncraft.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import com.miauwrijn.gooncraft.Plugin;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {

    private static FileConfiguration config;

    public static void load() {
        Plugin.instance.saveDefaultConfig();
        Plugin.instance.reloadConfig();
        config = Plugin.instance.getConfig();
    }

    public static void reload() {
        Plugin.instance.reloadConfig();
        config = Plugin.instance.getConfig();
    }

    public static String getMessage(String path) {
        String message = config.getString("messages." + path, "&cMissing message: " + path);
        return colorize(message);
    }

    public static String getMessage(String path, String... replacements) {
        String message = getMessage(path);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        return message;
    }

    public static List<String> getMessageList(String path) {
        return config.getStringList("messages." + path).stream()
                .map(ConfigManager::colorize)
                .collect(Collectors.toList());
    }

    public static int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public static String getString(String path, String defaultValue) {
        return colorize(config.getString(path, defaultValue));
    }

    public static List<String> getStringList(String path) {
        return config.getStringList(path).stream()
                .map(ConfigManager::colorize)
                .collect(Collectors.toList());
    }

    public static int getViagraBoost() {
        return config.getInt("viagra.boost-amount", 5);
    }

    public static String getViagraName() {
        return colorize(config.getString("viagra.item-name", "&dViagra"));
    }

    public static List<String> getViagraLore() {
        return config.getStringList("viagra.item-lore").stream()
                .map(s -> colorize(s.replace("{value}", String.valueOf(getViagraBoost()))))
                .collect(Collectors.toList());
    }

    public static int getButtfingerCooldown() {
        return config.getInt("cooldowns.buttfinger", 5);
    }

    public static boolean showFapMessages() {
        return config.getBoolean("chat.show-fap-messages", true);
    }

    public static boolean showEjaculateMessages() {
        return config.getBoolean("chat.show-ejaculate-messages", true);
    }

    public static boolean showCummedOnMessages() {
        return config.getBoolean("chat.show-cummed-on-messages", true);
    }

    public static boolean showFartMessages() {
        return config.getBoolean("chat.show-fart-messages", true);
    }

    public static boolean showPoopMessages() {
        return config.getBoolean("chat.show-poop-messages", true);
    }

    public static boolean showPissMessages() {
        return config.getBoolean("chat.show-piss-messages", true);
    }

    private static String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
