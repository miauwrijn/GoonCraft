package com.miauwrijn.gooncraft.achievements;

import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.data.PlayerStats;

/**
 * Hidden achievement that must be manually unlocked (easter eggs).
 */
public class HiddenAchievement extends BaseAchievement {
    
    public HiddenAchievement(String id, String name, String description, 
                            String category, long threshold, String rarity) {
        super(id, name, description, category, threshold, true, rarity);
    }
    
    @Override
    public boolean checkCondition(Player player, PlayerStats stats) {
        // Hidden achievements are manually unlocked via tryUnlock()
        return false;
    }
    
    @Override
    public long getCurrentProgress(Player player, PlayerStats stats) {
        return 0;
    }
}

