package com.miauwrijn.gooncraft.perks;

/**
 * Perk that increases fap speed by a multiplier.
 */
public class FapSpeedPerk extends BasePerk {
    
    private final double multiplier;
    
    public FapSpeedPerk(String name, String description, String icon, double multiplier) {
        super(name, description, icon);
        this.multiplier = multiplier;
    }
    
    public FapSpeedPerk(double multiplier) {
        this("Fap Speed Boost", (int)((multiplier - 1.0) * 100) + "% faster fap speed", "🔥", multiplier);
    }
    
    @Override
    public double getFapSpeedMultiplier() {
        return multiplier;
    }
}

