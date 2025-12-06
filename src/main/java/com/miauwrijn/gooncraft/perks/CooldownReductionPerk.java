package com.miauwrijn.gooncraft.perks;

/**
 * Perk that reduces cooldowns by a percentage.
 */
public class CooldownReductionPerk extends BasePerk {
    
    private final double reduction;
    
    public CooldownReductionPerk(String name, String description, String icon, double reduction, String rarity) {
        super(name, description, icon, rarity);
        this.reduction = reduction;
    }
    
    public CooldownReductionPerk(double reduction) {
        this(reduction, "common");
    }
    
    public CooldownReductionPerk(double reduction, String rarity) {
        this("Cooldown Reduction", (int)(reduction * 100) + "% faster cooldowns", "⚡", reduction, rarity);
    }
    
    @Override
    public double getCooldownReduction() {
        return reduction;
    }
}

