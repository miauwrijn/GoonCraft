package com.miauwrijn.gooncraft.achievements;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.miauwrijn.gooncraft.Plugin;

/**
 * Builds achievements from YAML configuration file.
 */
public class AchievementBuilder {
    
    private static final String ACHIEVEMENTS_FILE = "achievements.yml";
    private static final Map<String, BaseAchievement> achievementsById = new HashMap<>();
    
    /**
     * Load all achievements from the achievements.yml file.
     */
    public static List<BaseAchievement> loadAchievements() {
        List<BaseAchievement> achievements = new ArrayList<>();
        achievementsById.clear();
        
        // Ensure data folder exists
        Plugin.instance.getDataFolder().mkdirs();
        
        // Get the achievements.yml file
        File achievementsFile = new File(Plugin.instance.getDataFolder(), ACHIEVEMENTS_FILE);
        FileConfiguration config;
        
        // Load configuration
        if (achievementsFile.exists()) {
            config = YamlConfiguration.loadConfiguration(achievementsFile);
        } else {
            // Create default file from resource
            Plugin.instance.saveResource(ACHIEVEMENTS_FILE, false);
            config = YamlConfiguration.loadConfiguration(achievementsFile);
        }
        
        // Get achievements section
        ConfigurationSection achievementsSection = config.getConfigurationSection("achievements");
        if (achievementsSection == null) {
            Plugin.instance.getLogger().severe("No 'achievements' section found in achievements.yml! Using empty list.");
            return achievements;
        }
        
        // Build all achievements
        for (String key : achievementsSection.getKeys(false)) {
            ConfigurationSection achievementSection = achievementsSection.getConfigurationSection(key);
            
            if (achievementSection != null) {
                BaseAchievement achievement = buildAchievement(key, achievementSection);
                if (achievement != null) {
                    achievements.add(achievement);
                    // Store with lowercase ID for consistency
                    achievementsById.put(achievement.getId().toLowerCase(), achievement);
                }
            }
        }
        
        Plugin.instance.getLogger().info("Loaded " + achievements.size() + " achievements from achievements.yml");
        return achievements;
    }
    
    /**
     * Get an achievement by ID.
     */
    public static BaseAchievement getAchievementById(String id) {
        if (id == null) return null;
        // Try exact match first
        BaseAchievement achievement = achievementsById.get(id);
        if (achievement != null) return achievement;
        // Try lowercase
        return achievementsById.get(id.toLowerCase());
    }
    
    /**
     * Get all achievements by ID.
     */
    public static Map<String, BaseAchievement> getAllAchievementsById() {
        return new HashMap<>(achievementsById);
    }
    
    /**
     * Build a single achievement from a configuration section.
     */
    private static BaseAchievement buildAchievement(String id, ConfigurationSection section) {
        try {
            String name = section.getString("name", "Unknown Achievement");
            String description = section.getString("description", "");
            String category = section.getString("category", "misc");
            long threshold = section.getLong("threshold", 1);
            boolean hidden = section.getBoolean("hidden", false);
            String type = section.getString("type", "stat").toLowerCase();
            
            return switch (type) {
                case "stat" -> {
                    String statCategory = section.getString("stat_category", "");
                    yield new StatAchievement(id, name, description, category, threshold, hidden, statCategory);
                }
                case "location" -> {
                    String locationTag = section.getString("location_tag", "");
                    yield new LocationAchievement(id, name, description, category, threshold, hidden, locationTag);
                }
                case "mob_proximity" -> {
                    String mobType = section.getString("mob_type", "");
                    yield new MobProximityAchievement(id, name, description, category, threshold, hidden, mobType);
                }
                case "hidden" -> new HiddenAchievement(id, name, description, category, threshold);
                default -> {
                    Plugin.instance.getLogger().warning("Unknown achievement type: " + type + " for achievement: " + id);
                    yield null;
                }
            };
        } catch (Exception e) {
            Plugin.instance.getLogger().log(Level.WARNING, 
                "Failed to build achievement from section: " + section.getName(), e);
            return null;
        }
    }
}

