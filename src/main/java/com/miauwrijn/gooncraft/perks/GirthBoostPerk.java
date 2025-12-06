package com.miauwrijn.gooncraft.perks;

import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.data.PenisStatistics;
import com.miauwrijn.gooncraft.managers.PenisStatisticManager;

/**
 * Perk that permanently increases penis girth.
 */
public class GirthBoostPerk extends BasePerk {
    
    private final int boost;
    
    public GirthBoostPerk(int boost) {
        super("Girth Boost", "Permanent +" + boost + "cm girth", "📐");
        this.boost = boost;
    }
    
    @Override
    public int getGirthBoost() {
        return boost;
    }
    
    @Override
    public void apply(Player player) {
        PenisStatistics stats = PenisStatisticManager.getStatistics(player);
        stats.rankGirthBoost += boost;
        
        if (stats.penisModel != null) {
            stats.penisModel.reload(stats);
        }
    }
    
    @Override
    public void remove(Player player) {
        PenisStatistics stats = PenisStatisticManager.getStatistics(player);
        stats.rankGirthBoost = Math.max(0, stats.rankGirthBoost - boost);
        
        if (stats.penisModel != null) {
            stats.penisModel.reload(stats);
        }
    }
}

