package com.miauwrijn.gooncraft.achievements;

import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.data.PlayerStats;

/**
 * Base class for all achievements.
 * Each achievement type should extend this and implement checkCondition().
 */
public abstract class BaseAchievement {
    
    protected final String id;
    protected final String name;
    protected final String description;
    protected final String category;
    protected final long threshold;
    protected final boolean hidden;
    protected final String rarity; // "common", "uncommon", "rare", "mythic", "legendary"
    protected final long xpReward; // XP awarded when unlocked
    
    public BaseAchievement(String id, String name, String description, 
                          String category, long threshold, boolean hidden, String rarity) {
        this(id, name, description, category, threshold, hidden, rarity, calculateDefaultXp(rarity));
    }
    
    public BaseAchievement(String id, String name, String description, 
                          String category, long threshold, boolean hidden, String rarity, long xpReward) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.threshold = threshold;
        this.hidden = hidden;
        // Normalize rarity: map "epic" to "mythic" for backward compatibility
        String normalizedRarity = rarity != null ? rarity.toLowerCase() : "common";
        if ("epic".equals(normalizedRarity)) {
            normalizedRarity = "mythic";
        }
        this.rarity = normalizedRarity;
        this.xpReward = xpReward > 0 ? xpReward : calculateDefaultXp(this.rarity);
    }
    
    /**
     * Calculate default XP reward based on rarity.
     */
    private static long calculateDefaultXp(String rarity) {
        if (rarity == null) return 5;
        return switch (rarity.toLowerCase()) {
            case "legendary" -> 100;
            case "mythic" -> 50;
            case "rare" -> 25;
            case "uncommon" -> 15;
            case "common" -> 5;
            default -> 5;
        };
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public long getThreshold() { return threshold; }
    public boolean isHidden() { return hidden; }
    public String getRarity() { return rarity; }
    public long getXpReward() { return xpReward; }
    
    /**
     * Get rarity order for sorting (higher = rarer).
     */
    public int getRarityOrder() {
        return switch (rarity) {
            case "legendary" -> 5;
            case "mythic" -> 4;
            case "rare" -> 3;
            case "uncommon" -> 2;
            case "common" -> 1;
            default -> 0;
        };
    }
    
    /**
     * Get rarity color code for display.
     */
    public String getRarityColor() {
        return switch (rarity) {
            case "legendary" -> "§6§l"; // Gold, bold
            case "mythic" -> "§5§l"; // Purple, bold
            case "rare" -> "§b§l"; // Aqua, bold
            case "uncommon" -> "§a§l"; // Green, bold
            case "common" -> "§f"; // White
            default -> "§7"; // Gray
        };
    }
    
    /**
     * Get rarity display name.
     */
    public String getRarityDisplayName() {
        return switch (rarity) {
            case "legendary" -> "§6§lLEGENDARY";
            case "mythic" -> "§5§lMYTHIC";
            case "rare" -> "§b§lRARE";
            case "uncommon" -> "§a§lUNCOMMON";
            case "common" -> "§fCommon";
            default -> "§7Unknown";
        };
    }
    
    /**
     * Check if this achievement's condition is met for the player.
     * Returns true if the achievement should be unlocked.
     */
    public abstract boolean checkCondition(Player player, PlayerStats stats);
    
    /**
     * Get the current progress value for this achievement.
     * Used for display purposes (e.g., "5/10 goons").
     */
    public abstract long getCurrentProgress(Player player, PlayerStats stats);
}

