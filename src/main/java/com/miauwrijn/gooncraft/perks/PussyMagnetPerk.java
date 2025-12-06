package com.miauwrijn.gooncraft.perks;

import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.managers.RankPerkManager;

/**
 * Pussy Magnet perk - cats follow your pussy when it's out!
 */
public class PussyMagnetPerk extends BasePerk {
    
    public PussyMagnetPerk() {
        this("common");
    }
    
    public PussyMagnetPerk(String rarity) {
        super("Pussy Magnet", "🐱 Pussy Magnet: Cats follow your pussy when it's out!", "🐱", rarity);
    }
    
    @Override
    public boolean isAnimalFollowingPerk() {
        return true;
    }
    
    @Override
    public String getAnimalType() {
        return "pussy_magnet";
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

