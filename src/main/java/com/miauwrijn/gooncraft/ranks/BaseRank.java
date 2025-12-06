package com.miauwrijn.gooncraft.ranks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.perks.BasePerk;

/**
 * Rank class - created from YAML configuration.
 */
public class BaseRank {
    
    protected final int requiredAchievements;
    protected final String displayName;
    protected final String color;
    protected final String icon;
    protected final String description;
    protected final String rarity; // "common", "uncommon", "rare", "mythic", "legendary"
    protected final List<BasePerk> perks;
    
    public BaseRank(int requiredAchievements, String displayName, String color, 
                    String icon, String description, String rarity, List<BasePerk> perks) {
        this.requiredAchievements = requiredAchievements;
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
        this.description = description;
        // Normalize rarity: map "epic" to "mythic" for backward compatibility
        String normalizedRarity = rarity != null ? rarity.toLowerCase() : "common";
        if ("epic".equals(normalizedRarity)) {
            normalizedRarity = "mythic";
        }
        this.rarity = normalizedRarity;
        this.perks = perks != null ? new ArrayList<>(perks) : new ArrayList<>();
    }
    
    // Getters
    public int getRequiredAchievements() { return requiredAchievements; }
    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
    public String getIcon() { return icon; }
    public String getDescription() { return description; }
    public String getRarity() { return rarity; }
    
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
     * Get list of Perk objects for this rank.
     */
    public List<BasePerk> getPerks() {
        return new ArrayList<>(perks);
    }
    
    /**
     * Get list of perk descriptions for display (backward compatibility).
     */
    public List<String> getPerkDescriptions() {
        List<String> descriptions = new ArrayList<>();
        for (BasePerk perk : getPerks()) {
            descriptions.add(perk.getDisplayText());
        }
        return descriptions;
    }
    
    /**
     * Apply this rank's perks to a player (temporary, not saved).
     * Calls apply() on each perk.
     */
    public void applyPerks(Player player) {
        for (BasePerk perk : getPerks()) {
            perk.apply(player);
        }
    }
    
    /**
     * Remove this rank's perks from a player.
     * Calls remove() on each perk.
     */
    public void removePerks(Player player) {
        for (BasePerk perk : getPerks()) {
            perk.remove(player);
        }
    }
    
    /**
     * Get the ordinal/index of this rank (used for ordering).
     * This will be set by RankManager when ranks are registered.
     */
    private int ordinal = -1;
    
    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }
    
    public int getOrdinal() {
        return ordinal;
    }
}

