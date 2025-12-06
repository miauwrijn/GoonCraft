package com.miauwrijn.gooncraft.storage;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.miauwrijn.gooncraft.data.PlayerStats;
import com.miauwrijn.gooncraft.managers.AchievementManager.Achievement;
import com.miauwrijn.gooncraft.managers.GenderManager.Gender;

/**
 * Unified data container for all player-related data.
 * Used for transferring data to/from storage providers.
 */
public class PlayerData {

    // ===== Identification =====
    public UUID uuid;
    public String playerName;

    // ===== Penis Data =====
    public int penisSize;
    public int penisGirth;
    public boolean bbc;
    public int viagraBoost;

    // ===== Gender & Boobs Data =====
    public Gender gender;
    public int boobSize;
    public int boobPerkiness;

    // ===== Statistics =====
    public PlayerStats stats;

    // ===== Achievements =====
    public Set<Achievement> unlockedAchievements;
    
    // ===== Skill Points =====
    public int skillPoints;
    public Set<String> purchasedPerks; // Perks purchased with skill points
    public Set<String> disabledSkillPointPerks; // Skill point perks that are disabled
    
    // ===== Perk Management =====
    public Set<String> disabledPerks; // Rank perks that are disabled

    public PlayerData() {
        this.stats = new PlayerStats();
        this.unlockedAchievements = new HashSet<>();
        this.skillPoints = 0;
        this.purchasedPerks = new HashSet<>();
        this.disabledSkillPointPerks = new HashSet<>();
        this.disabledPerks = new HashSet<>();
    }

    public PlayerData(UUID uuid) {
        this();
        this.uuid = uuid;
    }

    /**
     * Check if this data has been initialized (loaded from storage).
     */
    public boolean isInitialized() {
        return uuid != null;
    }

    /**
     * Deep copy of player data.
     */
    public PlayerData copy() {
        PlayerData copy = new PlayerData(this.uuid);
        copy.playerName = this.playerName;
        copy.penisSize = this.penisSize;
        copy.penisGirth = this.penisGirth;
        copy.bbc = this.bbc;
        copy.viagraBoost = this.viagraBoost;
        copy.gender = this.gender;
        copy.boobSize = this.boobSize;
        copy.boobPerkiness = this.boobPerkiness;
        copy.skillPoints = this.skillPoints;
        // Note: stats and achievements are complex objects, shallow copy for now
        copy.stats = this.stats;
        copy.unlockedAchievements = new HashSet<>(this.unlockedAchievements);
        copy.purchasedPerks = new HashSet<>(this.purchasedPerks);
        copy.disabledSkillPointPerks = new HashSet<>(this.disabledSkillPointPerks);
        copy.disabledPerks = new HashSet<>(this.disabledPerks);
        return copy;
    }
}
