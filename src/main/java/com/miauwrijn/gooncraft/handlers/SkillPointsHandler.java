package com.miauwrijn.gooncraft.handlers;

import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.storage.PlayerData;
import com.miauwrijn.gooncraft.storage.StorageManager;

/**
 * Handles skill points system - awards points on rank up and tracks purchases.
 */
public class SkillPointsHandler {

    /**
     * Awards skill points when a player ranks up.
     * Should be called when a player's rank increases.
     */
    public static void awardSkillPointsOnRankUp(Player player, com.miauwrijn.gooncraft.ranks.BaseRank newRank) {
        PlayerData data = StorageManager.getPlayerData(player);
        
        // Award skill points for this rank
        int pointsToAward = newRank.getSkillPoints();
        if (pointsToAward > 0) {
            data.skillPoints += pointsToAward;
            player.sendMessage("§6§l+ " + pointsToAward + " Skill Point" + (pointsToAward > 1 ? "s" : "") + "! §7(Rank up reward)");
            player.sendMessage("§7Use §e/gc skillpoints §7to spend them!");
            StorageManager.savePlayerData(player.getUniqueId());
        }
    }

    /**
     * Get current skill points for a player.
     */
    public static int getSkillPoints(Player player) {
        PlayerData data = StorageManager.getPlayerData(player);
        return data.skillPoints;
    }

    /**
     * Spend skill points.
     * Returns true if successful, false if not enough points.
     */
    public static boolean spendSkillPoints(Player player, int amount) {
        PlayerData data = StorageManager.getPlayerData(player);
        if (data.skillPoints < amount) {
            return false;
        }
        data.skillPoints -= amount;
        StorageManager.savePlayerData(player.getUniqueId());
        return true;
    }

    /**
     * Add skill points (admin function).
     */
    public static void addSkillPoints(Player player, int amount) {
        PlayerData data = StorageManager.getPlayerData(player);
        data.skillPoints += amount;
        StorageManager.savePlayerData(player.getUniqueId());
    }

    /**
     * Set skill points to a specific amount (admin function).
     */
    public static void setSkillPoints(Player player, int amount) {
        PlayerData data = StorageManager.getPlayerData(player);
        data.skillPoints = Math.max(0, amount); // Ensure non-negative
        StorageManager.savePlayerData(player.getUniqueId());
    }

    /**
     * Remove skill points (admin function).
     */
    public static void removeSkillPoints(Player player, int amount) {
        PlayerData data = StorageManager.getPlayerData(player);
        data.skillPoints = Math.max(0, data.skillPoints - amount); // Ensure non-negative
        StorageManager.savePlayerData(player.getUniqueId());
    }

    /**
     * Reset all skill points and purchased perks.
     */
    public static void resetSkillPoints(Player player) {
        PlayerData data = StorageManager.getPlayerData(player);
        data.skillPoints = 0;
        data.purchasedPerks.clear();
        StorageManager.savePlayerData(player.getUniqueId());
    }

    /**
     * Check if player has purchased a specific perk.
     */
    public static boolean hasPerk(Player player, String perkId) {
        PlayerData data = StorageManager.getPlayerData(player);
        return data.purchasedPerks.contains(perkId);
    }

    /**
     * Purchase a perk.
     * Returns true if successful, false if already purchased or not enough points.
     */
    public static boolean purchasePerk(Player player, String perkId, int cost) {
        PlayerData data = StorageManager.getPlayerData(player);
        if (data.purchasedPerks.contains(perkId)) {
            return false; // Already purchased
        }
        if (data.skillPoints < cost) {
            return false; // Not enough points
        }
        data.skillPoints -= cost;
        data.purchasedPerks.add(perkId);
        StorageManager.savePlayerData(player.getUniqueId());
        return true;
    }
}

