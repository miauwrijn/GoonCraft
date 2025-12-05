package com.miauwrijn.gooncraft.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.data.PenisStatistics;
import com.miauwrijn.gooncraft.data.PlayerStats;

/**
 * Manages player statistics tracking and persistence.
 * Data is stored in the players folder alongside other player data.
 */
public class StatisticsManager implements Listener {

    private static final Map<UUID, PlayerStats> playerStats = new ConcurrentHashMap<>();
    private static File dataFolder;
    
    // Track last fart time for shart combo
    private static final Map<UUID, Long> lastFartTime = new ConcurrentHashMap<>();

    public StatisticsManager() {
        dataFolder = new File(Plugin.instance.getDataFolder(), "players");
        ensureDataFolderExists();
        loadOnlinePlayers();
        
        Bukkit.getPluginManager().registerEvents(this, Plugin.instance);
        
        // Auto-save every 5 minutes
        Bukkit.getScheduler().runTaskTimerAsynchronously(Plugin.instance, this::saveAll, 6000L, 6000L);
    }

    public static PlayerStats getStats(Player player) {
        return playerStats.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
    }

    public static PlayerStats getStats(UUID uuid) {
        return playerStats.get(uuid);
    }

    // ===== Original Stat Increment Methods =====

    public static void incrementFapCount(Player player) {
        PlayerStats stats = getStats(player);
        stats.fapCount++;
        
        // Track speed for speed achievement
        stats.trackFapSpeed();
        
        // Check location-based achievements
        checkFapLocation(player, stats);
        
        // Check if fapping in danger
        checkFapDanger(player, stats);
        
        AchievementManager.checkAchievements(player, stats);
        AchievementManager.checkLocationAchievements(player, stats);
    }

    public static void incrementCumOnOthers(Player player, Player target) {
        PlayerStats stats = getStats(player);
        stats.cumOnOthersCount++;
        stats.uniquePlayersCummedOn.add(target.getUniqueId());
        
        // Track ejaculation speed
        stats.trackEjaculationSpeed();
        
        AchievementManager.checkAchievements(player, stats);
    }
    
    public static void incrementCumOnOthers(Player player) {
        PlayerStats stats = getStats(player);
        stats.cumOnOthersCount++;
        stats.trackEjaculationSpeed();
        AchievementManager.checkAchievements(player, stats);
    }

    public static void incrementGotCummedOn(Player player, Player source) {
        PlayerStats stats = getStats(player);
        stats.gotCummedOnCount++;
        stats.uniquePlayersGotCummedBy.add(source.getUniqueId());
        AchievementManager.checkAchievements(player, stats);
    }
    
    public static void incrementGotCummedOn(Player player) {
        PlayerStats stats = getStats(player);
        stats.gotCummedOnCount++;
        AchievementManager.checkAchievements(player, stats);
    }

    public static void incrementButtfingersGiven(Player player, Player target) {
        PlayerStats stats = getStats(player);
        stats.buttfingersGiven++;
        stats.uniquePlayersButtfingered.add(target.getUniqueId());
        AchievementManager.checkAchievements(player, stats);
    }
    
    public static void incrementButtfingersGiven(Player player) {
        PlayerStats stats = getStats(player);
        stats.buttfingersGiven++;
        AchievementManager.checkAchievements(player, stats);
    }

    public static void incrementButtfingersReceived(Player player) {
        PlayerStats stats = getStats(player);
        stats.buttfingersReceived++;
        AchievementManager.checkAchievements(player, stats);
    }

    public static void incrementViagraUsed(Player player) {
        PlayerStats stats = getStats(player);
        stats.viagraUsed++;
        AchievementManager.checkAchievements(player, stats);
    }

    public static void startPenisOutTimer(Player player) {
        PlayerStats stats = getStats(player);
        stats.startPenisOutTimer();
    }

    public static void stopPenisOutTimer(Player player) {
        PlayerStats stats = getStats(player);
        stats.stopPenisOutTimer();
        AchievementManager.checkAchievements(player, stats);
    }

    // ===== New Bodily Function Stats =====

    public static void incrementFartCount(Player player) {
        PlayerStats stats = getStats(player);
        stats.fartCount++;
        lastFartTime.put(player.getUniqueId(), System.currentTimeMillis());
        
        // Track nearby players for social achievements
        trackNearbyPlayers(player, stats.uniquePlayersFartedNear);
        
        AchievementManager.checkAchievements(player, stats);
    }

    public static void incrementPoopCount(Player player) {
        PlayerStats stats = getStats(player);
        stats.poopCount++;
        
        // Check for shart achievement (fart within 5 seconds before poop)
        Long lastFart = lastFartTime.get(player.getUniqueId());
        if (lastFart != null && System.currentTimeMillis() - lastFart < 5000) {
            AchievementManager.tryUnlock(player, AchievementManager.Achievement.SHART);
        }
        
        AchievementManager.checkAchievements(player, stats);
    }

    public static void incrementPissCount(Player player) {
        PlayerStats stats = getStats(player);
        stats.pissCount++;
        
        // Track nearby players for social achievements
        trackNearbyPlayers(player, stats.uniquePlayersPissedNear);
        
        AchievementManager.checkAchievements(player, stats);
    }

    // ===== Boob Stats =====

    public static void incrementJiggleCount(Player player) {
        PlayerStats stats = getStats(player);
        stats.jiggleCount++;
        AchievementManager.checkAchievements(player, stats);
    }

    public static void incrementBoobToggleCount(Player player) {
        PlayerStats stats = getStats(player);
        stats.boobToggleCount++;
        AchievementManager.checkAchievements(player, stats);
    }

    // ===== Gender Stats =====

    public static void incrementGenderChanges(Player player) {
        PlayerStats stats = getStats(player);
        stats.genderChanges++;
        AchievementManager.checkAchievements(player, stats);
    }

    public static void unlockGenderOther(Player player) {
        AchievementManager.tryUnlock(player, AchievementManager.Achievement.GENDER_OTHER);
    }

    // ===== Danger Stats =====

    public static void incrementDamageWhileFapping(Player player) {
        PlayerStats stats = getStats(player);
        stats.damageWhileFapping++;
        AchievementManager.checkAchievements(player, stats);
    }

    public static void incrementDeathWhileExposed(Player player) {
        PlayerStats stats = getStats(player);
        stats.deathsWhileExposed++;
        AchievementManager.checkAchievements(player, stats);
    }

    public static void incrementCreeperDeathWhileExposed(Player player) {
        PlayerStats stats = getStats(player);
        stats.creeperDeathsWhileExposed++;
        AchievementManager.tryUnlock(player, AchievementManager.Achievement.CREEPER_DEATH);
    }

    // ===== Animal Stats =====

    public static void incrementPigsAffected(Player player) {
        PlayerStats stats = getStats(player);
        stats.pigsAffected++;
    }

    public static void incrementCowsAffected(Player player) {
        PlayerStats stats = getStats(player);
        stats.cowsAffected++;
    }

    public static void incrementWolvesAffected(Player player) {
        PlayerStats stats = getStats(player);
        stats.wolvesAffected++;
    }

    public static void incrementCatsAffected(Player player) {
        PlayerStats stats = getStats(player);
        stats.catsAffected++;
    }

    // ===== Helper Methods =====

    private static void trackNearbyPlayers(Player player, Set<UUID> trackingSet) {
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby != player && nearby.getLocation().distance(player.getLocation()) < 5) {
                trackingSet.add(nearby.getUniqueId());
            }
        }
    }

    private static void checkFapLocation(Player player, PlayerStats stats) {
        World world = player.getWorld();
        World.Environment env = world.getEnvironment();
        
        // Check dimension
        if (env == World.Environment.NETHER) {
            stats.fappedInNether = true;
        } else if (env == World.Environment.THE_END) {
            stats.fappedInEnd = true;
        }
        
        // Check underwater
        if (player.isInWater() || player.getEyeLocation().getBlock().isLiquid()) {
            stats.fappedUnderwater = true;
        }
        
        // Check altitude
        if (player.getLocation().getY() > 200) {
            stats.fappedHighAltitude = true;
        }
        
        // Check biome
        Biome biome = player.getLocation().getBlock().getBiome();
        String biomeName = biome.name().toLowerCase();
        
        if (biomeName.contains("desert") || biomeName.contains("badlands")) {
            stats.fappedInDesert = true;
        }
        
        if (biomeName.contains("snow") || biomeName.contains("ice") || 
            biomeName.contains("frozen") || biomeName.contains("cold") ||
            biomeName.contains("taiga")) {
            stats.fappedInSnow = true;
        }
    }

    private static void checkFapDanger(Player player, PlayerStats stats) {
        // Check if on fire
        if (player.getFireTicks() > 0) {
            stats.fapsWhileOnFire++;
            AchievementManager.tryUnlock(player, AchievementManager.Achievement.FAP_ON_FIRE);
        }
        
        // Check if falling (velocity Y is negative and not on ground)
        if (!player.isOnGround() && player.getVelocity().getY() < -0.5) {
            stats.fapsWhileFalling++;
            AchievementManager.tryUnlock(player, AchievementManager.Achievement.FAP_FALLING);
        }
    }

    // ===== Event Handlers for Danger Achievements =====

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        // Check if player is fapping (has penis model active)
        PenisStatistics penisStats = PenisStatisticManager.getStatistics(player);
        if (penisStats != null && penisStats.penisModel != null) {
            incrementDamageWhileFapping(player);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Check if player had penis out
        PenisStatistics penisStats = PenisStatisticManager.getStatistics(player);
        if (penisStats != null && penisStats.penisModel != null) {
            incrementDeathWhileExposed(player);
            
            // Check if killed by creeper
            if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent entityEvent) {
                if (entityEvent.getDamager().getType().name().contains("CREEPER")) {
                    incrementCreeperDeathWhileExposed(player);
                }
            }
        }
    }

    // ===== Persistence =====

    private void ensureDataFolderExists() {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            Plugin.instance.getLogger().warning("Failed to create players folder: " + dataFolder.getAbsolutePath());
        }
    }

    private void loadOnlinePlayers() {
        for (Player player : Plugin.instance.getServer().getOnlinePlayers()) {
            loadPlayerStats(player);
        }
    }

    private void loadPlayerStats(Player player) {
        File file = new File(dataFolder, player.getUniqueId() + ".yml");
        PlayerStats stats = new PlayerStats();
        
        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            
            // Original stats
            stats.fapCount = config.getInt("Stats.FapCount", 0);
            stats.cumOnOthersCount = config.getInt("Stats.CumOnOthersCount", 0);
            stats.gotCummedOnCount = config.getInt("Stats.GotCummedOnCount", 0);
            stats.totalTimeWithPenisOut = config.getLong("Stats.TotalTimeWithPenisOut", 0);
            stats.buttfingersGiven = config.getInt("Stats.ButtfingersGiven", 0);
            stats.buttfingersReceived = config.getInt("Stats.ButtfingersReceived", 0);
            stats.viagraUsed = config.getInt("Stats.ViagraUsed", 0);
            
            // Bodily function stats
            stats.fartCount = config.getInt("Stats.FartCount", 0);
            stats.poopCount = config.getInt("Stats.PoopCount", 0);
            stats.pissCount = config.getInt("Stats.PissCount", 0);
            
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
            stats.damageWhileFapping = config.getInt("Stats.DamageWhileFapping", 0);
            stats.fapsWhileFalling = config.getInt("Stats.FapsWhileFalling", 0);
            stats.fapsWhileOnFire = config.getInt("Stats.FapsWhileOnFire", 0);
            stats.creeperDeathsWhileExposed = config.getInt("Stats.CreeperDeaths", 0);
            
            // Location flags
            stats.fappedInNether = config.getBoolean("Stats.FappedInNether", false);
            stats.fappedInEnd = config.getBoolean("Stats.FappedInEnd", false);
            stats.fappedUnderwater = config.getBoolean("Stats.FappedUnderwater", false);
            stats.fappedInDesert = config.getBoolean("Stats.FappedInDesert", false);
            stats.fappedInSnow = config.getBoolean("Stats.FappedInSnow", false);
            stats.fappedHighAltitude = config.getBoolean("Stats.FappedHighAltitude", false);
            
            // Speed stats
            stats.maxFapsInMinute = config.getInt("Stats.MaxFapsInMinute", 0);
            stats.ejaculationsIn30Seconds = config.getInt("Stats.MaxEjaculationsIn30s", 0);
            
            // Animal stats
            stats.pigsAffected = config.getInt("Stats.PigsAffected", 0);
            stats.cowsAffected = config.getInt("Stats.CowsAffected", 0);
            stats.wolvesAffected = config.getInt("Stats.WolvesAffected", 0);
            stats.catsAffected = config.getInt("Stats.CatsAffected", 0);
        }
        
        playerStats.put(player.getUniqueId(), stats);
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

    private void savePlayerStats(Player player) {
        PlayerStats stats = playerStats.get(player.getUniqueId());
        if (stats == null) return;
        
        // Stop timer to capture current session time
        if (stats.isPenisOut) {
            stats.stopPenisOutTimer();
        }
        
        File file = new File(dataFolder, player.getUniqueId() + ".yml");
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            
            // Original stats
            config.set("Stats.FapCount", stats.fapCount);
            config.set("Stats.CumOnOthersCount", stats.cumOnOthersCount);
            config.set("Stats.GotCummedOnCount", stats.gotCummedOnCount);
            config.set("Stats.TotalTimeWithPenisOut", stats.totalTimeWithPenisOut);
            config.set("Stats.ButtfingersGiven", stats.buttfingersGiven);
            config.set("Stats.ButtfingersReceived", stats.buttfingersReceived);
            config.set("Stats.ViagraUsed", stats.viagraUsed);
            
            // Bodily function stats
            config.set("Stats.FartCount", stats.fartCount);
            config.set("Stats.PoopCount", stats.poopCount);
            config.set("Stats.PissCount", stats.pissCount);
            
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
            config.set("Stats.DamageWhileFapping", stats.damageWhileFapping);
            config.set("Stats.FapsWhileFalling", stats.fapsWhileFalling);
            config.set("Stats.FapsWhileOnFire", stats.fapsWhileOnFire);
            config.set("Stats.CreeperDeaths", stats.creeperDeathsWhileExposed);
            
            // Location flags
            config.set("Stats.FappedInNether", stats.fappedInNether);
            config.set("Stats.FappedInEnd", stats.fappedInEnd);
            config.set("Stats.FappedUnderwater", stats.fappedUnderwater);
            config.set("Stats.FappedInDesert", stats.fappedInDesert);
            config.set("Stats.FappedInSnow", stats.fappedInSnow);
            config.set("Stats.FappedHighAltitude", stats.fappedHighAltitude);
            
            // Speed stats
            config.set("Stats.MaxFapsInMinute", stats.maxFapsInMinute);
            config.set("Stats.MaxEjaculationsIn30s", stats.ejaculationsIn30Seconds);
            
            // Animal stats
            config.set("Stats.PigsAffected", stats.pigsAffected);
            config.set("Stats.CowsAffected", stats.cowsAffected);
            config.set("Stats.WolvesAffected", stats.wolvesAffected);
            config.set("Stats.CatsAffected", stats.catsAffected);
            
            config.save(file);
        } catch (IOException e) {
            Plugin.instance.getLogger().log(Level.WARNING, "Failed to save player stats", e);
        }
    }

    private void saveAll() {
        for (Player player : Plugin.instance.getServer().getOnlinePlayers()) {
            savePlayerStats(player);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadPlayerStats(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerStats stats = playerStats.get(player.getUniqueId());
        
        if (stats != null && stats.isPenisOut) {
            stats.stopPenisOutTimer();
        }
        
        savePlayerStats(player);
        playerStats.remove(player.getUniqueId());
        lastFartTime.remove(player.getUniqueId());
    }
}
