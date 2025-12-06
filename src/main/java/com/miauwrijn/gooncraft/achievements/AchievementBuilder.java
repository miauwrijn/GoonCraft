package com.miauwrijn.gooncraft.achievements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
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
 * Builds achievements from YAML configuration files.
 * Supports loading from a single achievements.yml or multiple files in an achievements/ folder.
 */
public class AchievementBuilder {
    
    private static final String ACHIEVEMENTS_FILE = "achievements.yml";
    private static final String ACHIEVEMENTS_FOLDER = "achievements";
    private static final Map<String, BaseAchievement> achievementsById = new HashMap<>();
    
    // List of default achievement files to save to achievements/ folder
    private static final String[] DEFAULT_ACHIEVEMENT_FILES = {
        "achievements/gooning.yml",
        "achievements/exposure.yml",
        "achievements/advanced.yml",
        "achievements/location.yml",
        "achievements/hidden.yml",
        "achievements/mobs.yml",
        "achievements/mobs_tiered.yml"
    };
    
    /**
     * Load all achievements from YAML files.
     * First checks for achievements/ folder with multiple files.
     * Falls back to single achievements.yml if folder doesn't exist.
     */
    public static List<BaseAchievement> loadAchievements() {
        List<BaseAchievement> achievements = new ArrayList<>();
        achievementsById.clear();
        
        // Ensure data folder exists
        Plugin.instance.getDataFolder().mkdirs();
        
        // Check for achievements folder first
        File achievementsFolder = new File(Plugin.instance.getDataFolder(), ACHIEVEMENTS_FOLDER);
        
        if (achievementsFolder.exists() && achievementsFolder.isDirectory()) {
            // Load from multiple files in folder
            achievements = loadFromFolder(achievementsFolder);
        } else {
            // Check for legacy single file
            File achievementsFile = new File(Plugin.instance.getDataFolder(), ACHIEVEMENTS_FILE);
            
            if (achievementsFile.exists()) {
                // Load from single legacy file
                achievements = loadFromSingleFile(achievementsFile);
            } else {
                // Create achievements folder with default files
                createDefaultAchievementFiles(achievementsFolder);
                achievements = loadFromFolder(achievementsFolder);
            }
        }
        
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
     * Create the achievements folder with default achievement files from resources.
     */
    private static void createDefaultAchievementFiles(File folder) {
        folder.mkdirs();
        
        for (String resourcePath : DEFAULT_ACHIEVEMENT_FILES) {
            try (InputStream in = Plugin.instance.getResource(resourcePath)) {
                if (in != null) {
                    File outFile = new File(Plugin.instance.getDataFolder(), resourcePath);
                    outFile.getParentFile().mkdirs();
                    
                    try (OutputStream out = Files.newOutputStream(outFile.toPath())) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }
                    }
                    Plugin.instance.getLogger().info("Created default achievement file: " + resourcePath);
                } else {
                    Plugin.instance.getLogger().warning("Resource not found: " + resourcePath);
                }
            } catch (IOException e) {
                Plugin.instance.getLogger().log(Level.WARNING, "Failed to save " + resourcePath, e);
            }
        }
    }
    
    /**
     * Load achievements from all YAML files in a folder.
     */
    private static List<BaseAchievement> loadFromFolder(File folder) {
        List<BaseAchievement> achievements = new ArrayList<>();
        
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files == null || files.length == 0) {
            Plugin.instance.getLogger().warning("No achievement files found in " + folder.getPath());
            return achievements;
        }
        
        int totalLoaded = 0;
        for (File file : files) {
            List<BaseAchievement> fileAchievements = loadFromSingleFile(file);
            achievements.addAll(fileAchievements);
            totalLoaded += fileAchievements.size();
            Plugin.instance.getLogger().info("Loaded " + fileAchievements.size() + " achievements from " + file.getName());
        }
        
        Plugin.instance.getLogger().info("Loaded " + totalLoaded + " total achievements from " + files.length + " files");
        
        return achievements;
    }
    
    /**
     * Load achievements from a single YAML file.
     */
    private static List<BaseAchievement> loadFromSingleFile(File file) {
        List<BaseAchievement> achievements = new ArrayList<>();
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        // Get achievements section
        ConfigurationSection achievementsSection = config.getConfigurationSection("achievements");
        if (achievementsSection == null) {
            Plugin.instance.getLogger().warning("No 'achievements' section found in " + file.getName());
            return achievements;
        }
        
        // Build all achievements
        for (String key : achievementsSection.getKeys(false)) {
            ConfigurationSection achievementSection = achievementsSection.getConfigurationSection(key);
            
            if (achievementSection != null) {
                BaseAchievement achievement = buildAchievement(key, achievementSection);
                if (achievement != null) {
                    // Check for duplicate IDs
                    if (achievementsById.containsKey(achievement.getId().toLowerCase())) {
                        Plugin.instance.getLogger().warning(
                            "Duplicate achievement ID '" + key + "' in " + file.getName() + " - skipping."
                        );
                        continue;
                    }
                    
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
                        "Failed to build achievement '" + key + "' in " + file.getName() + " - skipping."
                    );
                }
            } else {
                Plugin.instance.getLogger().warning(
                    "Achievement section '" + key + "' is null or invalid in " + file.getName() + " - skipping."
                );
            }
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
            long xpReward = section.getLong("xp_reward", 0); // 0 means use default based on rarity
            
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
                    yield new StatAchievement(id, name, description, category, threshold, hidden, rarity, statCategory, xpReward);
                }
                case "location" -> {
                    String locationTag = section.getString("location_tag", "");
                    yield new LocationAchievement(id, name, description, category, threshold, hidden, rarity, locationTag, xpReward);
                }
                case "mob_proximity" -> {
                    String mobType = section.getString("mob_type", "");
                    yield new MobProximityAchievement(id, name, description, category, threshold, hidden, rarity, mobType, xpReward);
                }
                case "hidden" -> new HiddenAchievement(id, name, description, category, threshold, rarity, xpReward);
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

