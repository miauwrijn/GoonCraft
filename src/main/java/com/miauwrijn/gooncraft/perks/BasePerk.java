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
    
    public BasePerk(String name, String description, String icon) {
        this.name = name;
        this.description = description;
        this.icon = icon;
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    
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
}

