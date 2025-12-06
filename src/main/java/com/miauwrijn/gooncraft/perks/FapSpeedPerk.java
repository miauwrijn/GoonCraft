package com.miauwrijn.gooncraft.perks;

/**
 * Perk that increases fap speed by a multiplier.
 */
public class FapSpeedPerk extends BasePerk {
    
    private final double multiplier;
    
    public FapSpeedPerk(String name, String description, String icon, double multiplier, String rarity) {
        super(name, description, icon, rarity);
        this.multiplier = multiplier;
    }
    
    public FapSpeedPerk(double multiplier) {
        this(multiplier, "common");
    }
    
    public FapSpeedPerk(double multiplier, String rarity) {
        this("Fap Speed Boost", (int)((multiplier - 1.0) * 100) + "% faster fap speed", "🔥", multiplier, rarity);
    }
    
    @Override
    public double getFapSpeedMultiplier() {
        return multiplier;
    }
}

