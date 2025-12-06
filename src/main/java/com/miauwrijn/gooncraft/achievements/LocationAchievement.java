package com.miauwrijn.gooncraft.achievements;

import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.data.PlayerStats;

/**
 * Achievement based on location/biome where player gooned.
 */
public class LocationAchievement extends BaseAchievement {
    
    private final String locationTag;
    
    public LocationAchievement(String id, String name, String description, 
                              String category, long threshold, boolean hidden, String locationTag) {
        super(id, name, description, category, threshold, hidden);
        this.locationTag = locationTag;
    }
    
    public String getLocationTag() {
        return locationTag;
    }
    
    @Override
    public boolean checkCondition(Player player, PlayerStats stats) {
        return switch (locationTag) {
            case "nether" -> stats.goonedInNether;
            case "end" -> stats.goonedInEnd;
            case "underwater" -> stats.goonedUnderwater;
            case "desert" -> stats.goonedInDesert;
            case "snow" -> stats.goonedInSnow;
            case "high" -> stats.goonedHighAltitude;
            default -> false;
        };
    }
    
    @Override
    public long getCurrentProgress(Player player, PlayerStats stats) {
        return checkCondition(player, stats) ? 1 : 0;
    }
}

