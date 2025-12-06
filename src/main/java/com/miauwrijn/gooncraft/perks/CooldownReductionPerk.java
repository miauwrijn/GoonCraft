package com.miauwrijn.gooncraft.perks;

/**
 * Perk that reduces cooldowns by a percentage.
 */
public class CooldownReductionPerk extends BasePerk {
    
    private final double reduction;
    
    public CooldownReductionPerk(String name, String description, String icon, double reduction) {
        super(name, description, icon);
        this.reduction = reduction;
    }
    
    public CooldownReductionPerk(double reduction) {
        this("Cooldown Reduction", (int)(reduction * 100) + "% faster cooldowns", "⚡", reduction);
    }
    
    @Override
    public double getCooldownReduction() {
        return reduction;
    }
}

