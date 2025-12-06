package com.miauwrijn.gooncraft.managers;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.data.PlayerStats;
import com.miauwrijn.gooncraft.ranks.BaseRank;
import com.miauwrijn.gooncraft.storage.PlayerData;
import com.miauwrijn.gooncraft.storage.StorageManager;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Manages player statistics tracking.
 * Uses StorageManager for persistence (supports file and database storage).
 */
public class StatisticsManager implements Listener {
    
    // Track last fart time for shart combo
    private static final Map<UUID, Long> lastFartTime = new ConcurrentHashMap<>();

    public StatisticsManager() {
        Bukkit.getPluginManager().registerEvents(this, Plugin.instance);
    }

    /**
     * Get stats for a player (from StorageManager cache).
     */
    public static PlayerStats getStats(Player player) {
        PlayerData data = StorageManager.getPlayerData(player);
        if (data.stats == null) {
            data.stats = new PlayerStats();
        }
        return data.stats;
    }

    /**
     * Get stats for a UUID (from StorageManager cache).
     */
    public static PlayerStats getStats(UUID uuid) {
        PlayerData data = StorageManager.getPlayerData(uuid);
        return data != null ? data.stats : null;
    }

    // Track active progress bar displays to avoid spam
    private static final Map<UUID, Long> lastProgressBarTime = new ConcurrentHashMap<>();
    private static final long PROGRESS_BAR_COOLDOWN = 500; // ms between progress bar updates

    // ===== Goon (Masturbation) Stats - Gender Neutral =====

    /**
     * Increment goon count - works for both penis fapping and vagina gooning.
     * Awards 1 XP per goon and shows progress bar.
     */
    public static void incrementGoonCount(Player player) {
        PlayerStats stats = getStats(player);
        stats.goonCount++;
        
        // Track streak and strokes (using Minecraft day)
        long currentMcDay = player.getWorld().getFullTime() / 24000L;
        stats.trackGoonStreak(currentMcDay);
        
        // Award XP and show progress
        awardXpWithProgress(player, stats, 1, "Gooning");
        
        // Track speed for speed achievement
        stats.trackGoonSpeed();
        
        // Check location-based achievements
        checkGoonLocation(player, stats);
        
        // Check if gooning in danger
        checkGoonDanger(player, stats);
        
        AchievementManager.checkAchievements(player, stats);
        AchievementManager.checkLocationAchievements(player, stats);
        AchievementManager.checkMobProximityAchievements(player);
    }

    /** @deprecated Use incrementGoonCount instead */
    @Deprecated
    public static void incrementFapCount(Player player) {
        incrementGoonCount(player);
    }

    // ===== Cum/Squirt Stats (Gender Neutral) =====

    public static void incrementCumOnOthers(Player player, Player target) {
        PlayerStats stats = getStats(player);
        stats.cumOnOthersCount++;
        stats.uniquePlayersCummedOn.add(target.getUniqueId());
        
        // Track ejaculation speed and total ejaculations
        stats.trackEjaculationSpeed();
        stats.trackEjaculation();
        
        AchievementManager.checkAchievements(player, stats);
    }
    
    public static void incrementCumOnOthers(Player player) {
        PlayerStats stats = getStats(player);
        stats.cumOnOthersCount++;
        stats.trackEjaculationSpeed();
        stats.trackEjaculation();
        AchievementManager.checkAchievements(player, stats);
    }

    // Vagina-specific methods (squirt = same stats as cum)
    public static void incrementSquirtOnOthers(Player player, Player target) {
        incrementCumOnOthers(player, target);
    }
    
    public static void incrementSquirtOnOthers(Player player) {
        incrementCumOnOthers(player);
    }
    
    /**
     * Track a solo ejaculation (when not cumming on anyone).
     * This is called when the player finishes without someone nearby.
     */
    public static void trackSoloEjaculation(Player player) {
        PlayerStats stats = getStats(player);
        stats.trackEjaculationSpeed();
        stats.trackEjaculation();
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

    // Vagina-specific methods (squirted on = same stats as cummed on)
    public static void incrementGotSquirtedOn(Player player, Player source) {
        incrementGotCummedOn(player, source);
    }
    
    public static void incrementGotSquirtedOn(Player player) {
        incrementGotCummedOn(player);
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

    // ===== Exposure Timer (Gender Neutral) =====

    public static void startExposureTimer(Player player) {
        PlayerStats stats = getStats(player);
        stats.startExposureTimer();
    }

    public static void stopExposureTimer(Player player) {
        PlayerStats stats = getStats(player);
        stats.stopExposureTimer();
        AchievementManager.checkAchievements(player, stats);
    }

    /** @deprecated Use startExposureTimer instead */
    @Deprecated
    public static void startPenisOutTimer(Player player) {
        startExposureTimer(player);
    }

    /** @deprecated Use stopExposureTimer instead */
    @Deprecated
    public static void stopPenisOutTimer(Player player) {
        stopExposureTimer(player);
    }

    // ===== New Bodily Function Stats =====

    public static void incrementFartCount(Player player) {
        PlayerStats stats = getStats(player);
        stats.fartCount++;
        lastFartTime.put(player.getUniqueId(), System.currentTimeMillis());
        
        // Award XP and show progress
        awardXpWithProgress(player, stats, 1, "Farting");
        
        // Track nearby players for social achievements
        trackNearbyPlayers(player, stats.uniquePlayersFartedNear);
        
        AchievementManager.checkAchievements(player, stats);
    }

    public static void incrementPoopCount(Player player) {
        PlayerStats stats = getStats(player);
        stats.poopCount++;
        
        // Award XP and show progress
        awardXpWithProgress(player, stats, 1, "Pooping");
        
        // Check for shart achievement (fart within 5 seconds before poop)
        Long lastFart = lastFartTime.get(player.getUniqueId());
        if (lastFart != null && System.currentTimeMillis() - lastFart < 5000) {
            AchievementManager.tryUnlockById(player, "shart");
        }
        
        AchievementManager.checkAchievements(player, stats);
    }

    public static void incrementPissCount(Player player) {
        PlayerStats stats = getStats(player);
        stats.pissCount++;
        
        // Award XP and show progress
        awardXpWithProgress(player, stats, 1, "Pissing");
        
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
        AchievementManager.tryUnlockById(player, "gender_other");
    }

    // ===== Danger Stats =====

    public static void incrementDamageWhileGooning(Player player) {
        PlayerStats stats = getStats(player);
        stats.damageWhileGooning++;
        AchievementManager.checkAchievements(player, stats);
    }

    /** @deprecated Use incrementDamageWhileGooning instead */
    @Deprecated
    public static void incrementDamageWhileFapping(Player player) {
        incrementDamageWhileGooning(player);
    }

    public static void incrementDeathWhileExposed(Player player) {
        PlayerStats stats = getStats(player);
        stats.deathsWhileExposed++;
        AchievementManager.checkAchievements(player, stats);
    }

    public static void incrementCreeperDeathWhileExposed(Player player) {
        PlayerStats stats = getStats(player);
        stats.creeperDeathsWhileExposed++;
        AchievementManager.tryUnlockById(player, "creeper_death");
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

    // ===== XP and Progress Bar Methods =====
    
    /**
     * Award XP to a player and show progress bar briefly.
     */
    public static void awardXpWithProgress(Player player, PlayerStats stats, long amount, String action) {
        long oldXp = stats.experience;
        stats.addExperience(amount);
        
        // Show progress bar (with cooldown to avoid spam)
        showProgressBar(player, stats, action);
        
        // Check for rank up
        AchievementManager.checkRankUpAfterXp(player, oldXp);
    }
    
    /**
     * Show XP progress bar in action bar.
     */
    private static void showProgressBar(Player player, PlayerStats stats, String action) {
        long now = System.currentTimeMillis();
        Long lastTime = lastProgressBarTime.get(player.getUniqueId());
        
        // Rate limit progress bar updates
        if (lastTime != null && now - lastTime < PROGRESS_BAR_COOLDOWN) {
            return;
        }
        lastProgressBarTime.put(player.getUniqueId(), now);
        
        // Get current rank info
        BaseRank currentRank = RankManager.getRank(player);
        BaseRank nextRank = RankManager.getNextRank(currentRank);
        double progress = RankManager.getProgressToNextRank(player);
        long currentXp = stats.experience;
        
        // Build progress bar
        String progressBar = RankManager.createColoredProgressBar(progress, 20);
        
        String message;
        if (nextRank == null) {
            // Max rank
            message = currentRank.getColor() + currentRank.getIcon() + " " + currentRank.getDisplayName() + " §7| §6MAX RANK §7| §e" + currentXp + " XP";
        } else {
            long xpToNext = RankManager.getXpToNextRank(player);
            message = currentRank.getColor() + currentRank.getIcon() + " " + progressBar + " §7" + xpToNext + " XP to " + nextRank.getDisplayName();
        }
        
        // Send to action bar
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        
        // Schedule hiding the progress bar after 2 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                // Only clear if no new progress bar was shown
                Long currentTime = lastProgressBarTime.get(player.getUniqueId());
                if (currentTime != null && System.currentTimeMillis() - currentTime >= 2000) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                }
            }
        }.runTaskLater(Plugin.instance, 40L); // 2 seconds = 40 ticks
    }

    // ===== Helper Methods =====

    private static void trackNearbyPlayers(Player player, Set<UUID> trackingSet) {
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby != player && nearby.getLocation().distance(player.getLocation()) < 5) {
                trackingSet.add(nearby.getUniqueId());
            }
        }
    }

    private static void checkGoonLocation(Player player, PlayerStats stats) {
        World world = player.getWorld();
        World.Environment env = world.getEnvironment();
        
        // Check dimension
        if (env == World.Environment.NETHER) {
            stats.goonedInNether = true;
        } else if (env == World.Environment.THE_END) {
            stats.goonedInEnd = true;
        }
        
        // Check underwater
        if (player.isInWater() || player.getEyeLocation().getBlock().isLiquid()) {
            stats.goonedUnderwater = true;
        }
        
        // Check altitude
        if (player.getLocation().getY() > 200) {
            stats.goonedHighAltitude = true;
        }
        
        // Check biome
        Biome biome = player.getLocation().getBlock().getBiome();
        String biomeName = biome.name().toLowerCase();
        
        if (biomeName.contains("desert") || biomeName.contains("badlands")) {
            stats.goonedInDesert = true;
        }
        
        if (biomeName.contains("snow") || biomeName.contains("ice") || 
            biomeName.contains("frozen") || biomeName.contains("cold") ||
            biomeName.contains("taiga")) {
            stats.goonedInSnow = true;
        }
    }

    private static void checkGoonDanger(Player player, PlayerStats stats) {
        // Check if on fire
        if (player.getFireTicks() > 0) {
            stats.goonsWhileOnFire++;
            AchievementManager.tryUnlockById(player, "goon_on_fire");
        }
        
        // Check if falling (velocity Y is negative and not on ground)
        if (!player.isOnGround() && player.getVelocity().getY() < -0.5) {
            stats.goonsWhileFalling++;
            AchievementManager.tryUnlockById(player, "goon_falling");
        }
    }

    // ===== Event Handlers for Danger Achievements =====

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        // Check if player is gooning (has any genital model active)
        if (GenderManager.hasActiveGenitals(player)) {
            incrementDamageWhileGooning(player);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Check if player had genitals out
        if (GenderManager.hasActiveGenitals(player)) {
            incrementDeathWhileExposed(player);
            
            // Check if killed by creeper
            if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent entityEvent) {
                if (entityEvent.getDamager().getType().name().contains("CREEPER")) {
                    incrementCreeperDeathWhileExposed(player);
                }
            }
        }
    }

}
