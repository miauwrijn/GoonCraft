package com.miauwrijn.gooncraft.ranks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.perks.BasePerk;
import com.miauwrijn.gooncraft.perks.BoobBoostPerk;
import com.miauwrijn.gooncraft.perks.CockmasterPerk;
import com.miauwrijn.gooncraft.perks.CooldownReductionPerk;
import com.miauwrijn.gooncraft.perks.FapSpeedPerk;
import com.miauwrijn.gooncraft.perks.GirthBoostPerk;
import com.miauwrijn.gooncraft.perks.PussyMagnetPerk;
import com.miauwrijn.gooncraft.perks.SizeBoostPerk;

/**
 * Builds ranks from YAML configuration file.
 */
public class RankBuilder {
    
    private static final String RANKS_FILE = "ranks.yml";
    
    /**
     * Load all ranks from the ranks.yml file.
     */
    public static List<BaseRank> loadRanks() {
        List<BaseRank> ranks = new ArrayList<>();
        
        // Ensure data folder exists
        Plugin.instance.getDataFolder().mkdirs();
        
        // Get the ranks.yml file
        File ranksFile = new File(Plugin.instance.getDataFolder(), RANKS_FILE);
        FileConfiguration config;
        
        // Load configuration
        if (ranksFile.exists()) {
            config = YamlConfiguration.loadConfiguration(ranksFile);
        } else {
            // Create default file from resource
            Plugin.instance.saveResource(RANKS_FILE, false);
            config = YamlConfiguration.loadConfiguration(ranksFile);
        }
        
        // Get ranks section
        ConfigurationSection ranksSection = config.getConfigurationSection("ranks");
        if (ranksSection == null) {
            Plugin.instance.getLogger().severe("No 'ranks' section found in ranks.yml! Using empty list.");
            return ranks;
        }
        
        // Get all rank keys and sort by required achievements
        Map<Integer, String> rankMap = new HashMap<>();
        for (String key : ranksSection.getKeys(false)) {
            ConfigurationSection rankSection = ranksSection.getConfigurationSection(key);
            if (rankSection != null) {
                int required = rankSection.getInt("required_achievements", 0);
                rankMap.put(required, key);
            }
        }
        
        // Sort by required achievements and build ranks
        List<Integer> sorted = new ArrayList<>(rankMap.keySet());
        sorted.sort(Integer::compareTo);
        
        int ordinal = 0;
        for (int required : sorted) {
            String key = rankMap.get(required);
            ConfigurationSection rankSection = ranksSection.getConfigurationSection(key);
            
            if (rankSection != null) {
                BaseRank rank = buildRank(rankSection, ordinal);
                if (rank != null) {
                    ranks.add(rank);
                    ordinal++;
                }
            }
        }
        
        Plugin.instance.getLogger().info("Loaded " + ranks.size() + " ranks from ranks.yml");
        return ranks;
    }
    
    /**
     * Build a single rank from a configuration section.
     */
    private static BaseRank buildRank(ConfigurationSection section, int ordinal) {
        try {
            int requiredAchievements = section.getInt("required_achievements", 0);
            String displayName = section.getString("display_name", "Unknown Rank");
            String color = section.getString("color", "§f");
            String icon = section.getString("icon", "⭐");
            String description = section.getString("description", "");
            
            // Build perks
            List<BasePerk> perks = new ArrayList<>();
            if (section.isList("perks")) {
                List<Map<?, ?>> perkList = section.getMapList("perks");
                for (Map<?, ?> perkMap : perkList) {
                    BasePerk perk = buildPerk(perkMap);
                    if (perk != null) {
                        perks.add(perk);
                    }
                }
            }
            
            BaseRank rank = new BaseRank(requiredAchievements, displayName, color, 
                                        icon, description, perks);
            rank.setOrdinal(ordinal);
            return rank;
            
        } catch (Exception e) {
            Plugin.instance.getLogger().log(Level.WARNING, 
                "Failed to build rank from section: " + section.getName(), e);
            return null;
        }
    }
    
    /**
     * Build a perk from a map (from YAML).
     */
    private static BasePerk buildPerk(Map<?, ?> perkMap) {
        if (perkMap == null || !perkMap.containsKey("type")) {
            return null;
        }
        
        String type = perkMap.get("type").toString().toLowerCase();
        
        try {
            switch (type) {
                case "cooldown_reduction":
                    double reduction = getDouble(perkMap, "value", 0.0);
                    return new CooldownReductionPerk(reduction);
                
                case "fap_speed":
                    double multiplier = getDouble(perkMap, "value", 1.0);
                    return new FapSpeedPerk(multiplier);
                
                case "size_boost":
                    int sizeBoost = getInt(perkMap, "value", 0);
                    return new SizeBoostPerk(sizeBoost);
                
                case "girth_boost":
                    int girthBoost = getInt(perkMap, "value", 0);
                    return new GirthBoostPerk(girthBoost);
                
                case "boob_boost":
                    int boobBoost = getInt(perkMap, "value", 0);
                    return new BoobBoostPerk(boobBoost);
                
                case "cockmaster":
                    return new CockmasterPerk();
                
                case "pussy_magnet":
                    return new PussyMagnetPerk();
                
                default:
                    Plugin.instance.getLogger().warning("Unknown perk type: " + type);
                    return null;
            }
        } catch (Exception e) {
            Plugin.instance.getLogger().log(Level.WARNING, "Failed to build perk of type: " + type, e);
            return null;
        }
    }
    
    private static double getDouble(Map<?, ?> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private static int getInt(Map<?, ?> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

