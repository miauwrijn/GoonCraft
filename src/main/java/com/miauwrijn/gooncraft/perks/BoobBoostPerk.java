package com.miauwrijn.gooncraft.perks;

import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.managers.GenderManager;

/**
 * Perk that permanently increases boob size.
 */
public class BoobBoostPerk extends BasePerk {
    
    private final int boost;
    
    public BoobBoostPerk(int boost) {
        this(boost, "common");
    }
    
    public BoobBoostPerk(int boost, String rarity) {
        super("Boob Boost", "Permanent +" + boost + "cm boob size", "💋", rarity);
        this.boost = boost;
    }
    
    @Override
    public int getBoobSizeBoost() {
        return boost;
    }
    
    @Override
    public void apply(Player player) {
        if (GenderManager.hasBoobs(player)) {
            GenderManager.addRankBoobBoost(player, boost);
        }
    }
    
    @Override
    public void remove(Player player) {
        if (GenderManager.hasBoobs(player)) {
            GenderManager.removeRankBoobBoost(player, boost);
        }
    }
}

