package com.miauwrijn.gooncraft.perks;

import org.bukkit.entity.Player;

/**
 * Villager Discount perk - villagers you cum on give you cheaper trades!
 */
public class VillagerDiscountPerk extends BasePerk {
    
    private final int discountLevel;
    
    public VillagerDiscountPerk() {
        this(1, "common");
    }
    
    public VillagerDiscountPerk(int discountLevel, String rarity) {
        super("Villager Charmer", 
              "🏪 Villager Charmer: Villagers you cum on give discounted trades!", 
              "🏪", 
              rarity);
        this.discountLevel = discountLevel;
    }
    
    /**
     * Get the discount level (1-5, affects price reduction).
     */
    public int getDiscountLevel() {
        return discountLevel;
    }
    
    @Override
    public boolean isVillagerDiscountPerk() {
        return true;
    }
    
    @Override
    public int getVillagerDiscountLevel() {
        return discountLevel;
    }
    
    @Override
    public void apply(Player player) {
        // The actual discount is applied when cumming on villagers
        // See StatisticsManager for implementation
    }
    
    @Override
    public void remove(Player player) {
        // Discounts remain on villagers even after perk removal
    }
}
