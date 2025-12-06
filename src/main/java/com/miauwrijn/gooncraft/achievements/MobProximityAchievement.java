package com.miauwrijn.gooncraft.achievements;

import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.data.PlayerStats;

/**
 * Achievement for gooning near a specific mob type.
 * These are checked separately via checkMobProximityAchievements.
 */
public class MobProximityAchievement extends BaseAchievement {
    
    private final String mobType;
    
    public MobProximityAchievement(String id, String name, String description, 
                                   String category, long threshold, boolean hidden, String mobType) {
        super(id, name, description, category, threshold, hidden);
        this.mobType = mobType;
    }
    
    public String getMobType() {
        return mobType;
    }
    
    @Override
    public boolean checkCondition(Player player, PlayerStats stats) {
        // Mob proximity achievements are checked separately via checkMobProximityAchievements
        // This returns false by default - actual checking happens in AchievementManager
        return false;
    }
    
    @Override
    public long getCurrentProgress(Player player, PlayerStats stats) {
        return 0; // Not tracked in stats directly
    }
}

