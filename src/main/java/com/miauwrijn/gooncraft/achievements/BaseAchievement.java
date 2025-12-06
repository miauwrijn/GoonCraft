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
    
    public BaseAchievement(String id, String name, String description, 
                          String category, long threshold, boolean hidden) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.threshold = threshold;
        this.hidden = hidden;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public long getThreshold() { return threshold; }
    public boolean isHidden() { return hidden; }
    
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

