package com.miauwrijn.gooncraft.perks;

import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.managers.GenderManager;
import com.miauwrijn.gooncraft.models.BoobModel;
import com.miauwrijn.gooncraft.storage.PlayerData;
import com.miauwrijn.gooncraft.storage.StorageManager;

/**
 * Perk that permanently increases boob size.
 */
public class BoobBoostPerk extends BasePerk {
    
    private final int boost;
    
    public BoobBoostPerk(int boost) {
        super("Boob Boost", "Permanent +" + boost + "cm boob size", "💋");
        this.boost = boost;
    }
    
    @Override
    public int getBoobSizeBoost() {
        return boost;
    }
    
    @Override
    public void apply(Player player) {
        if (GenderManager.hasBoobs(player)) {
            BoobModel boobModel = GenderManager.getActiveBoobModel(player);
            if (boobModel != null) {
                PlayerData data = StorageManager.getPlayerData(player);
                boobModel.reload(Math.min(10, data.boobSize + boost), data.boobPerkiness);
            }
        }
    }
    
    @Override
    public void remove(Player player) {
        if (GenderManager.hasBoobs(player)) {
            BoobModel boobModel = GenderManager.getActiveBoobModel(player);
            if (boobModel != null) {
                PlayerData data = StorageManager.getPlayerData(player);
                boobModel.reload(data.boobSize, data.boobPerkiness);
            }
        }
    }
}

