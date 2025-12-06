package com.miauwrijn.gooncraft.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.data.PlayerStats;
import com.miauwrijn.gooncraft.managers.GenderManager.Gender;
import com.miauwrijn.gooncraft.models.BoobModel;
import com.miauwrijn.gooncraft.models.PenisModel;

/**
 * File-based storage provider using YAML files.
 * Stores one file per player in the players folder.
 */
public class FileStorageProvider implements StorageProvider {

    private File dataFolder;
    private boolean initialized = false;

    @Override
    public boolean initialize() {
        dataFolder = new File(Plugin.instance.getDataFolder(), "players");
        
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            Plugin.instance.getLogger().severe("Failed to create players folder!");
            return false;
        }
        
        initialized = true;
        Plugin.instance.getLogger().info("File storage provider initialized.");
        return true;
    }

    @Override
    public void shutdown() {
        initialized = false;
    }

    @Override
    public boolean isConnected() {
        return initialized && dataFolder != null && dataFolder.exists();
    }

    @Override
    public PlayerData loadPlayerData(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        PlayerData data = new PlayerData(uuid);
        
        if (!file.exists()) {
            // New player - initialize with random starting sizes
            data.penisSize = PenisModel.getRandomSize();
            data.penisGirth = PenisModel.getRandomGirth();
            data.bbc = PenisModel.getRandomBbc();
            data.boobSize = BoobModel.getRandomSize();
            data.boobPerkiness = BoobModel.getRandomPerkiness();
            return data;
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            
            // Load penis data
            // If value is 0 or missing, initialize with random starting size
            int penisSize = config.getInt("Penis.Size", 0);
            data.penisSize = penisSize > 0 ? penisSize : PenisModel.getRandomSize();
            
            int penisGirth = config.getInt("Penis.Girth", 0);
            data.penisGirth = penisGirth > 0 ? penisGirth : PenisModel.getRandomGirth();
            
            data.bbc = config.getBoolean("Penis.BBC", PenisModel.getRandomBbc());
            data.viagraBoost = config.getInt("Penis.ViagraBoost", 0);
            
            // Load gender data
            String genderStr = config.getString("Gender", null);
            if (genderStr != null) {
                try {
                    data.gender = Gender.valueOf(genderStr);
                } catch (IllegalArgumentException ignored) {}
            }
            
            // Load boob data
            // If value is 0 or missing, initialize with random starting size
            int boobSize = config.getInt("Boobs.Size", 0);
            data.boobSize = boobSize > 0 ? boobSize : BoobModel.getRandomSize();
            
            int boobPerkiness = config.getInt("Boobs.Perkiness", 0);
            data.boobPerkiness = boobPerkiness > 0 ? boobPerkiness : BoobModel.getRandomPerkiness();
            
            // Load statistics
            data.stats = loadStats(config);
            
            // Load achievements
            data.unlockedAchievements = loadAchievements(config);
            
            // Load rank perk settings
            data.disabledPerks = new HashSet<>(config.getStringList("RankPerks.Disabled"));
            
        } catch (Exception e) {
            Plugin.instance.getLogger().log(Level.WARNING, "Failed to load player data for " + uuid, e);
        }
        
        return data;
    }

    @Override
    public CompletableFuture<PlayerData> loadPlayerDataAsync(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> loadPlayerData(uuid));
    }

    @Override
    public boolean savePlayerData(PlayerData data) {
        if (data == null || data.uuid == null) return false;
        
        File file = new File(dataFolder, data.uuid + ".yml");
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            
            // Save penis data
            config.set("Penis.Size", data.penisSize);
            config.set("Penis.Girth", data.penisGirth);
            config.set("Penis.BBC", data.bbc);
            config.set("Penis.ViagraBoost", data.viagraBoost);
            
            // Save gender data
            if (data.gender != null) {
                config.set("Gender", data.gender.name());
            }
            
            // Save boob data
            config.set("Boobs.Size", data.boobSize);
            config.set("Boobs.Perkiness", data.boobPerkiness);
            
            // Save statistics
            saveStats(config, data.stats);
            
            // Save achievements
            saveAchievements(config, data.unlockedAchievements);
            
            // Save rank perk settings
            config.set("RankPerks.Disabled", new ArrayList<>(data.disabledPerks));
            
            config.save(file);
            return true;
            
        } catch (IOException e) {
            Plugin.instance.getLogger().log(Level.WARNING, "Failed to save player data for " + data.uuid, e);
            return false;
        }
    }

    @Override
    public CompletableFuture<Boolean> savePlayerDataAsync(PlayerData data) {
        return CompletableFuture.supplyAsync(() -> savePlayerData(data));
    }

    @Override
    public boolean deletePlayerData(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        return !file.exists() || file.delete();
    }

    @Override
    public boolean hasPlayerData(UUID uuid) {
        return new File(dataFolder, uuid + ".yml").exists();
    }

    @Override
    public String getName() {
        return "File (YAML)";
    }

    // ===== Helper Methods =====

    private PlayerStats loadStats(FileConfiguration config) {
        PlayerStats stats = new PlayerStats();
        
        // Experience points
        stats.experience = config.getLong("Stats.Experience", 0);
        
        // Core stats
        stats.goonCount = config.getInt("Stats.GoonCount", 0);
        stats.cumOnOthersCount = config.getInt("Stats.CumOnOthersCount", 0);
        stats.gotCummedOnCount = config.getInt("Stats.GotCummedOnCount", 0);
        stats.totalExposureTime = config.getLong("Stats.TotalExposureTime", 0);
        stats.buttfingersGiven = config.getInt("Stats.ButtfingersGiven", 0);
        stats.buttfingersReceived = config.getInt("Stats.ButtfingersReceived", 0);
        stats.viagraUsed = config.getInt("Stats.ViagraUsed", 0);
        
        // Bodily function stats
        stats.fartCount = config.getInt("Stats.FartCount", 0);
        stats.poopCount = config.getInt("Stats.PoopCount", 0);
        stats.pissCount = config.getInt("Stats.PissCount", 0);
        
        // Mob goon counts
        ConfigurationSection mobSection = config.getConfigurationSection("Stats.MobGoonCounts");
        if (mobSection != null) {
            for (String mobType : mobSection.getKeys(false)) {
                stats.mobGoonCounts.put(mobType, mobSection.getInt(mobType, 0));
            }
        }
        
        // Boob stats
        stats.jiggleCount = config.getInt("Stats.JiggleCount", 0);
        stats.boobToggleCount = config.getInt("Stats.BoobToggleCount", 0);
        
        // Gender stats
        stats.genderChanges = config.getInt("Stats.GenderChanges", 0);
        
        // Unique player sets
        stats.uniquePlayersCummedOn = loadUUIDSet(config, "Stats.UniqueCummedOn");
        stats.uniquePlayersGotCummedBy = loadUUIDSet(config, "Stats.UniqueGotCummedBy");
        stats.uniquePlayersButtfingered = loadUUIDSet(config, "Stats.UniqueButtfingered");
        stats.uniquePlayersPissedNear = loadUUIDSet(config, "Stats.UniquePissedNear");
        stats.uniquePlayersFartedNear = loadUUIDSet(config, "Stats.UniqueFartedNear");
        
        // Danger stats
        stats.deathsWhileExposed = config.getInt("Stats.DeathsWhileExposed", 0);
        stats.damageWhileGooning = config.getInt("Stats.DamageWhileGooning", 0);
        stats.goonsWhileFalling = config.getInt("Stats.GoonsWhileFalling", 0);
        stats.goonsWhileOnFire = config.getInt("Stats.GoonsWhileOnFire", 0);
        stats.creeperDeathsWhileExposed = config.getInt("Stats.CreeperDeaths", 0);
        
        // Location flags
        stats.goonedInNether = config.getBoolean("Stats.GoonedInNether", false);
        stats.goonedInEnd = config.getBoolean("Stats.GoonedInEnd", false);
        stats.goonedUnderwater = config.getBoolean("Stats.GoonedUnderwater", false);
        stats.goonedInDesert = config.getBoolean("Stats.GoonedInDesert", false);
        stats.goonedInSnow = config.getBoolean("Stats.GoonedInSnow", false);
        stats.goonedHighAltitude = config.getBoolean("Stats.GoonedHighAltitude", false);
        
        // Speed stats
        stats.maxGoonsInMinute = config.getInt("Stats.MaxGoonsInMinute", 0);
        stats.ejaculationsIn30Seconds = config.getInt("Stats.MaxEjaculationsIn30s", 0);
        
        // Animal stats
        stats.pigsAffected = config.getInt("Stats.PigsAffected", 0);
        stats.cowsAffected = config.getInt("Stats.CowsAffected", 0);
        stats.wolvesAffected = config.getInt("Stats.WolvesAffected", 0);
        stats.catsAffected = config.getInt("Stats.CatsAffected", 0);
        
        // Detailed goon stats
        stats.totalEjaculations = config.getInt("Stats.TotalEjaculations", 0);
        
        // Special achievement stats
        stats.selfButtfingers = config.getInt("Stats.SelfButtfingers", 0);
        stats.blocksMinedWhileExposed = config.getInt("Stats.BlocksMinedWhileExposed", 0);
        stats.ejaculatedInOcean = config.getBoolean("Stats.EjaculatedInOcean", false);
        stats.goonsNearBabyVillagers = config.getInt("Stats.GoonsNearBabyVillagers", 0);

        // Streak stats
        stats.currentGoonStreak = config.getInt("Stats.CurrentGoonStreak", 0);
        stats.longestGoonStreak = config.getInt("Stats.LongestGoonStreak", 0);
        stats.lastGoonMinecraftDay = config.getLong("Stats.LastGoonMinecraftDay", 0);

        return stats;
    }

    private void saveStats(FileConfiguration config, PlayerStats stats) {
        if (stats == null) return;
        
        // Stop timer if active
        if (stats.isExposed) {
            stats.stopExposureTimer();
        }
        
        // Experience points
        config.set("Stats.Experience", stats.experience);
        
        // Core stats
        config.set("Stats.GoonCount", stats.goonCount);
        config.set("Stats.CumOnOthersCount", stats.cumOnOthersCount);
        config.set("Stats.GotCummedOnCount", stats.gotCummedOnCount);
        config.set("Stats.TotalExposureTime", stats.totalExposureTime);
        config.set("Stats.ButtfingersGiven", stats.buttfingersGiven);
        config.set("Stats.ButtfingersReceived", stats.buttfingersReceived);
        config.set("Stats.ViagraUsed", stats.viagraUsed);
        
        // Bodily function stats
        config.set("Stats.FartCount", stats.fartCount);
        config.set("Stats.PoopCount", stats.poopCount);
        config.set("Stats.PissCount", stats.pissCount);
        
        // Mob goon counts
        if (stats.mobGoonCounts != null && !stats.mobGoonCounts.isEmpty()) {
            for (var entry : stats.mobGoonCounts.entrySet()) {
                config.set("Stats.MobGoonCounts." + entry.getKey(), entry.getValue());
            }
        }
        
        // Boob stats
        config.set("Stats.JiggleCount", stats.jiggleCount);
        config.set("Stats.BoobToggleCount", stats.boobToggleCount);
        
        // Gender stats
        config.set("Stats.GenderChanges", stats.genderChanges);
        
        // Unique player sets
        config.set("Stats.UniqueCummedOn", stats.uniquePlayersCummedOn.stream().map(UUID::toString).toList());
        config.set("Stats.UniqueGotCummedBy", stats.uniquePlayersGotCummedBy.stream().map(UUID::toString).toList());
        config.set("Stats.UniqueButtfingered", stats.uniquePlayersButtfingered.stream().map(UUID::toString).toList());
        config.set("Stats.UniquePissedNear", stats.uniquePlayersPissedNear.stream().map(UUID::toString).toList());
        config.set("Stats.UniqueFartedNear", stats.uniquePlayersFartedNear.stream().map(UUID::toString).toList());
        
        // Danger stats
        config.set("Stats.DeathsWhileExposed", stats.deathsWhileExposed);
        config.set("Stats.DamageWhileGooning", stats.damageWhileGooning);
        config.set("Stats.GoonsWhileFalling", stats.goonsWhileFalling);
        config.set("Stats.GoonsWhileOnFire", stats.goonsWhileOnFire);
        config.set("Stats.CreeperDeaths", stats.creeperDeathsWhileExposed);
        
        // Location flags
        config.set("Stats.GoonedInNether", stats.goonedInNether);
        config.set("Stats.GoonedInEnd", stats.goonedInEnd);
        config.set("Stats.GoonedUnderwater", stats.goonedUnderwater);
        config.set("Stats.GoonedInDesert", stats.goonedInDesert);
        config.set("Stats.GoonedInSnow", stats.goonedInSnow);
        config.set("Stats.GoonedHighAltitude", stats.goonedHighAltitude);
        
        // Speed stats
        config.set("Stats.MaxGoonsInMinute", stats.maxGoonsInMinute);
        config.set("Stats.MaxEjaculationsIn30s", stats.ejaculationsIn30Seconds);
        
        // Animal stats
        config.set("Stats.PigsAffected", stats.pigsAffected);
        config.set("Stats.CowsAffected", stats.cowsAffected);
        config.set("Stats.WolvesAffected", stats.wolvesAffected);
        config.set("Stats.CatsAffected", stats.catsAffected);
        
        // Detailed goon stats
        config.set("Stats.TotalEjaculations", stats.totalEjaculations);
        
        // Special achievement stats
        config.set("Stats.SelfButtfingers", stats.selfButtfingers);
        config.set("Stats.BlocksMinedWhileExposed", stats.blocksMinedWhileExposed);
        config.set("Stats.EjaculatedInOcean", stats.ejaculatedInOcean);
        config.set("Stats.GoonsNearBabyVillagers", stats.goonsNearBabyVillagers);

        // Streak stats
        config.set("Stats.CurrentGoonStreak", stats.currentGoonStreak);
        config.set("Stats.LongestGoonStreak", stats.longestGoonStreak);
        config.set("Stats.LastGoonMinecraftDay", stats.lastGoonMinecraftDay);
    }

    private Set<UUID> loadUUIDSet(FileConfiguration config, String path) {
        Set<UUID> set = new HashSet<>();
        List<String> list = config.getStringList(path);
        for (String uuidStr : list) {
            try {
                set.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ignored) {}
        }
        return set;
    }

    private Set<String> loadAchievements(FileConfiguration config) {
        Set<String> unlocked = new HashSet<>();
        
        // Try new format first (list of achievement IDs)
        if (config.isList("Achievements")) {
            // New format: list of achievement IDs
            List<String> achievementIds = config.getStringList("Achievements");
            for (String id : achievementIds) {
                unlocked.add(id.toLowerCase()); // Store in lowercase for consistency
            }
        } else {
            // Old format: boolean map - migrate from old format
            ConfigurationSection achievementsSection = config.getConfigurationSection("Achievements");
            if (achievementsSection != null) {
                for (String key : achievementsSection.getKeys(false)) {
                    if (achievementsSection.getBoolean(key, false)) {
                        unlocked.add(key.toLowerCase()); // Store in lowercase
                    }
                }
            }
        }
        
        return unlocked;
    }

    private void saveAchievements(FileConfiguration config, Set<String> achievements) {
        if (achievements == null) return;
        
        // Save as simple list of achievement IDs
        List<String> achievementIds = new ArrayList<>();
        for (String achievementId : achievements) {
            achievementIds.add(achievementId);
        }
        
        config.set("Achievements", achievementIds);
        
        // Remove old format if it exists (clean up boolean map)
        ConfigurationSection oldSection = config.getConfigurationSection("Achievements");
        if (oldSection != null && !config.isList("Achievements")) {
            // This shouldn't happen, but just in case - clear the old section
            for (String key : oldSection.getKeys(false)) {
                if (!key.equals("Achievements")) {
                    config.set("Achievements." + key, null);
                }
            }
        }
    }
}
