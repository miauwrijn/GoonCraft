package com.miauwrijn.gooncraft.achievements;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                    // Final validation: ensure category is valid
                    String category = achievement.getCategory();
                    if (!VALID_CATEGORIES.contains(category)) {
                        Plugin.instance.getLogger().severe(
                            "Achievement '" + key + "' has invalid category '" + category + 
                            "' after building. This should not happen!"
                        );
                    }
                    
                    achievements.add(achievement);
                    // Store with lowercase ID for consistency
                    achievementsById.put(achievement.getId().toLowerCase(), achievement);
                } else {
                    Plugin.instance.getLogger().warning(
                        "Failed to build achievement '" + key + "' - skipping."
                    );
                }
            } else {
                Plugin.instance.getLogger().warning(
                    "Achievement section '" + key + "' is null or invalid - skipping."
                );
            }
        }
        
        Plugin.instance.getLogger().info("Loaded " + achievements.size() + " achievements from achievements.yml");
        
        // Validate all loaded achievements have valid categories
        int invalidCount = 0;
        for (BaseAchievement achievement : achievements) {
            if (!VALID_CATEGORIES.contains(achievement.getCategory())) {
                invalidCount++;
                Plugin.instance.getLogger().warning(
                    "Achievement '" + achievement.getId() + "' has invalid category: " + 
                    achievement.getCategory()
                );
            }
        }
        
        if (invalidCount > 0) {
            Plugin.instance.getLogger().warning(
                "Found " + invalidCount + " achievements with invalid categories. " +
                "Valid categories are: location, gooning, exposure, hidden, advanced"
            );
        }
        
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
     * Valid categories for achievements.
     */
    private static final Set<String> VALID_CATEGORIES = Set.of(
        "location", "gooning", "exposure", "hidden", "advanced"
    );
    
    /**
     * Map old category names to new category names.
     */
    private static String mapCategory(String oldCategory, String achievementId) {
        if (oldCategory == null) {
            return "advanced";
        }
        
        String category = oldCategory.toLowerCase();
        
        // If already a valid category, return it
        if (VALID_CATEGORIES.contains(category)) {
            return category;
        }
        
        // Map old categories to new ones
        return switch (category) {
            // Gooning-related
            case "goon", "cum_on", "got_cummed", "group_goon", "group_goon_3", 
                 "group_goon_5", "group_goon_7", "rapid_fire", "speed_goon" -> "gooning";
            
            // Exposure-related
            case "time_out" -> "exposure";
            
            // Location-related
            case "location", "mob_proximity" -> "location";
            
            // Hidden
            case "hidden" -> "hidden";
            
            // Advanced (everything else)
            default -> {
                Plugin.instance.getLogger().warning(
                    "Achievement '" + achievementId + "' has unknown category '" + oldCategory + 
                    "'. Mapping to 'advanced'. Valid categories: location, gooning, exposure, hidden, advanced"
                );
                yield "advanced";
            }
        };
    }
    
    /**
     * Valid rarities for achievements.
     */
    private static final Set<String> VALID_RARITIES = Set.of(
        "common", "uncommon", "rare", "mythic", "legendary"
    );
    
    /**
     * Check if a rarity is valid.
     */
    private static boolean isValidRarity(String rarity) {
        if (rarity == null) return false;
        String normalized = rarity.toLowerCase();
        // Also accept "epic" for backward compatibility
        return VALID_RARITIES.contains(normalized) || "epic".equals(normalized);
    }
    
    /**
     * Validate and normalize category name.
     */
    private static String validateCategory(String category, String achievementId) {
        String normalized = mapCategory(category, achievementId);
        
        if (!VALID_CATEGORIES.contains(normalized)) {
            Plugin.instance.getLogger().severe(
                "Achievement '" + achievementId + "' has invalid category '" + category + 
                "'. Using 'advanced' as fallback."
            );
            return "advanced";
        }
        
        if (!normalized.equals(category) && category != null) {
            Plugin.instance.getLogger().info(
                "Achievement '" + achievementId + "': Category '" + category + 
                "' mapped to '" + normalized + "'"
            );
        }
        
        return normalized;
    }
    
    /**
     * Build a single achievement from a configuration section.
     */
    private static BaseAchievement buildAchievement(String id, ConfigurationSection section) {
        try {
            String name = section.getString("name", "Unknown Achievement");
            String description = section.getString("description", "");
            String category = section.getString("category", "advanced");
            category = validateCategory(category, id); // Validate and map category
            long threshold = section.getLong("threshold", 1);
            boolean hidden = section.getBoolean("hidden", false);
            String type = section.getString("type", "stat").toLowerCase();
            
            String rarity = section.getString("rarity", "common").toLowerCase();
            // Validate rarity
            if (!isValidRarity(rarity)) {
                Plugin.instance.getLogger().warning(
                    "Achievement '" + id + "' has invalid rarity '" + rarity + 
                    "'. Valid rarities: common, uncommon, rare, mythic, legendary. Using 'common'."
                );
                rarity = "common";
            }
            
            return switch (type) {
                case "stat" -> {
                    String statCategory = section.getString("stat_category", "");
                    yield new StatAchievement(id, name, description, category, threshold, hidden, rarity, statCategory);
                }
                case "location" -> {
                    String locationTag = section.getString("location_tag", "");
                    yield new LocationAchievement(id, name, description, category, threshold, hidden, rarity, locationTag);
                }
                case "mob_proximity" -> {
                    String mobType = section.getString("mob_type", "");
                    yield new MobProximityAchievement(id, name, description, category, threshold, hidden, rarity, mobType);
                }
                case "hidden" -> new HiddenAchievement(id, name, description, category, threshold, rarity);
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

