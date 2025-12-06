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
    protected final int skillPoints;
    protected final List<BasePerk> perks;
    
    public BaseRank(int requiredAchievements, String displayName, String color, 
                    String icon, String description, int skillPoints, List<BasePerk> perks) {
        this.requiredAchievements = requiredAchievements;
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
        this.description = description;
        this.skillPoints = skillPoints;
        this.perks = perks != null ? new ArrayList<>(perks) : new ArrayList<>();
    }
    
    // Getters
    public int getRequiredAchievements() { return requiredAchievements; }
    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
    public String getIcon() { return icon; }
    public String getDescription() { return description; }
    public int getSkillPoints() { return skillPoints; }
    
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

