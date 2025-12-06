package com.miauwrijn.gooncraft.perks;

import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.data.PenisStatistics;
import com.miauwrijn.gooncraft.managers.PenisStatisticManager;

/**
 * Perk that permanently increases penis size.
 */
public class SizeBoostPerk extends BasePerk {
    
    private final int boost;
    
    public SizeBoostPerk(int boost) {
        this(boost, "common");
    }
    
    public SizeBoostPerk(int boost, String rarity) {
        super("Size Boost", "Permanent +" + boost + "cm size", "📏", rarity);
        this.boost = boost;
    }
    
    @Override
    public int getSizeBoost() {
        return boost;
    }
    
    @Override
    public void apply(Player player) {
        PenisStatistics stats = PenisStatisticManager.getStatistics(player);
        stats.rankSizeBoost += boost;
        
        if (stats.penisModel != null) {
            stats.penisModel.reload(stats);
        }
    }
    
    @Override
    public void remove(Player player) {
        PenisStatistics stats = PenisStatisticManager.getStatistics(player);
        stats.rankSizeBoost = Math.max(0, stats.rankSizeBoost - boost);
        
        if (stats.penisModel != null) {
            stats.penisModel.reload(stats);
        }
    }
}

