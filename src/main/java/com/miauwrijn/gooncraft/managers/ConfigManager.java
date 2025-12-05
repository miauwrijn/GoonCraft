package com.miauwrijn.gooncraft.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.miauwrijn.gooncraft.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ConfigManager {

    private static FileConfiguration config;
    private static final String CONFIG_VERSION_KEY = "config-version";
    private static final int CURRENT_CONFIG_VERSION = 2; // Increment when config structure changes

    public static void load() {
        File configFile = new File(Plugin.instance.getDataFolder(), "config.yml");
        
        if (configFile.exists()) {
            // Load existing config and check for updates
            config = YamlConfiguration.loadConfiguration(configFile);
            
            // Get default config from JAR
            InputStream defaultStream = Plugin.instance.getResource("config.yml");
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream));
                
                int existingVersion = config.getInt(CONFIG_VERSION_KEY, 1);
                int defaultVersion = defaultConfig.getInt(CONFIG_VERSION_KEY, CURRENT_CONFIG_VERSION);
                
                if (existingVersion < defaultVersion) {
                    // Merge configs - add missing keys from default
                    Plugin.instance.getLogger().info("Updating config from version " + existingVersion + " to " + defaultVersion);
                    mergeConfigs(config, defaultConfig);
                    config.set(CONFIG_VERSION_KEY, defaultVersion);
                    
                    try {
                        config.save(configFile);
                        Plugin.instance.getLogger().info("Config updated successfully! New options have been added.");
                    } catch (IOException e) {
                        Plugin.instance.getLogger().log(Level.WARNING, "Failed to save merged config", e);
                    }
                }
            }
        } else {
            // No config exists, save default
            Plugin.instance.saveDefaultConfig();
            config = Plugin.instance.getConfig();
        }
        
        Plugin.instance.reloadConfig();
        config = Plugin.instance.getConfig();
    }

    /**
     * Merges missing keys from defaultConfig into existingConfig.
     * Preserves existing user values, only adds new keys.
     */
    private static void mergeConfigs(FileConfiguration existing, FileConfiguration defaults) {
        mergeSection(existing, defaults, "");
    }

    private static void mergeSection(FileConfiguration existing, FileConfiguration defaults, String path) {
        ConfigurationSection defaultSection = path.isEmpty() ? defaults : defaults.getConfigurationSection(path);
        if (defaultSection == null) return;
        
        Set<String> keys = defaultSection.getKeys(false);
        
        for (String key : keys) {
            String fullPath = path.isEmpty() ? key : path + "." + key;
            
            // Skip the version key - we handle that separately
            if (fullPath.equals(CONFIG_VERSION_KEY)) continue;
            
            if (defaults.isConfigurationSection(fullPath)) {
                // Recurse into sections
                if (!existing.isConfigurationSection(fullPath)) {
                    // Section doesn't exist in existing config, copy entire section
                    existing.createSection(fullPath);
                }
                mergeSection(existing, defaults, fullPath);
            } else {
                // It's a value - only add if missing in existing config
                if (!existing.contains(fullPath)) {
                    Object defaultValue = defaults.get(fullPath);
                    existing.set(fullPath, defaultValue);
                    Plugin.instance.getLogger().info("  + Added new config option: " + fullPath);
                }
            }
        }
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
