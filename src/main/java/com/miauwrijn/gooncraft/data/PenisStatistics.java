package com.miauwrijn.gooncraft.data;

import com.miauwrijn.gooncraft.models.PenisModel;

public class PenisStatistics {
    
    public int size;
    public int girth;
    public boolean bbc;
    public int viagraBoost;
    public PenisModel penisModel;
    public int runnableTaskId;
    
    // Temporary rank perk boosts (not saved, reset on logout)
    public int rankSizeBoost = 0;
    public int rankGirthBoost = 0;

    public PenisStatistics(int size, int girth, boolean bbc) {
        this.size = size;
        this.girth = girth;
        this.bbc = bbc;
        this.viagraBoost = 0;
        this.penisModel = null;
        this.runnableTaskId = 0;
        this.rankSizeBoost = 0;
        this.rankGirthBoost = 0;
    }
    
    /**
     * Get effective size including all temporary boosts.
     */
    public int getEffectiveSize() {
        return Math.min(30, size + viagraBoost + rankSizeBoost);
    }
    
    /**
     * Get effective girth including all temporary boosts.
     */
    public int getEffectiveGirth() {
        return Math.min(15, girth + rankGirthBoost);
    }
}
