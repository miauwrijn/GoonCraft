package com.miauwrijn.gooncraft.perks;

import org.bukkit.entity.Player;

/**
 * Base class for all perks.
 * Each perk type should extend this and implement its specific logic.
 */
public abstract class BasePerk {
    
    protected final String name;
    protected final String description;
    protected final String icon;
    protected final String rarity; // "common", "uncommon", "rare", "mythic", "legendary"
    
    public BasePerk(String name, String description, String icon) {
        this(name, description, icon, "common");
    }
    
    public BasePerk(String name, String description, String icon, String rarity) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        // Normalize rarity: map "epic" to "mythic" for backward compatibility
        String normalizedRarity = rarity != null ? rarity.toLowerCase() : "common";
        if ("epic".equals(normalizedRarity)) {
            normalizedRarity = "mythic";
        }
        this.rarity = normalizedRarity;
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
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
     * Get display text for this perk.
     */
    public String getDisplayText() {
        return icon + " " + name;
    }
    
    /**
     * Apply this perk to a player (temporary, not saved).
     * Override for perks that need runtime application.
     */
    public void apply(Player player) {
        // Default: no runtime application needed
    }
    
    /**
     * Remove this perk from a player.
     * Override for perks that need runtime removal.
     */
    public void remove(Player player) {
        // Default: no runtime removal needed
    }
    
    /**
     * Get cooldown reduction percentage (0.0 to 1.0).
     * Override in CooldownReductionPerk.
     */
    public double getCooldownReduction() {
        return 0.0;
    }
    
    /**
     * Get fap speed multiplier (1.0 = normal, 2.0 = 2x speed).
     * Override in FapSpeedPerk.
     */
    public double getFapSpeedMultiplier() {
        return 1.0;
    }
    
    /**
     * Get size boost in cm.
     * Override in SizeBoostPerk.
     */
    public int getSizeBoost() {
        return 0;
    }
    
    /**
     * Get girth boost in cm.
     * Override in GirthBoostPerk.
     */
    public int getGirthBoost() {
        return 0;
    }
    
    /**
     * Get boob size boost.
     * Override in BoobBoostPerk.
     */
    public int getBoobSizeBoost() {
        return 0;
    }
    
    /**
     * Check if this is an animal following perk (Cockmaster/Pussy Magnet).
     */
    public boolean isAnimalFollowingPerk() {
        return false;
    }
    
    /**
     * Get the animal type for following perk ("cockmaster" or "pussy_magnet").
     */
    public String getAnimalType() {
        return null;
    }
    
    /**
     * Check if this is a villager discount perk.
     */
    public boolean isVillagerDiscountPerk() {
        return false;
    }
    
    /**
     * Get the villager discount level (1-5).
     */
    public int getVillagerDiscountLevel() {
        return 0;
    }
}

