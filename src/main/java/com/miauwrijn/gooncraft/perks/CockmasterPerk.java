package com.miauwrijn.gooncraft.perks;

import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.managers.RankPerkManager;

/**
 * Cockmaster perk - chickens follow your cock when it's out!
 */
public class CockmasterPerk extends BasePerk {
    
    public CockmasterPerk() {
        super("Cockmaster", "🐓 Cockmaster: Chickens follow your cock when it's out!", "🐓");
    }
    
    @Override
    public boolean isAnimalFollowingPerk() {
        return true;
    }
    
    @Override
    public String getAnimalType() {
        return "cockmaster";
    }
    
    @Override
    public void apply(Player player) {
        RankPerkManager.checkAnimalFollowing(player);
    }
    
    @Override
    public void remove(Player player) {
        RankPerkManager.stopAnimalFollowing(player);
    }
}

