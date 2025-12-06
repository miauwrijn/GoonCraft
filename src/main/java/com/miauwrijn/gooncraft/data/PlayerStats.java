package com.miauwrijn.gooncraft.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks player statistics for achievements and scoreboards.
 * Uses gender-neutral terminology (goon = masturbate for any gender).
 */
public class PlayerStats {
    
    // Experience points for ranking
    public long experience;
    
    // Core stats (gender neutral)
    public int goonCount; // Masturbation count (was fapCount)
    public int cumOnOthersCount; // Also counts squirting for females
    public int gotCummedOnCount; // Also counts getting squirted on
    public long totalExposureTime; // in seconds (was totalTimeWithPenisOut)
    public int buttfingersGiven;
    public int buttfingersReceived;
    public int viagraUsed;
    
    // Bodily function stats
    public int fartCount;
    public int poopCount;
    public int pissCount;
    
    // Mob proximity goon tracking (mob_type -> count)
    public Map<String, Integer> mobGoonCounts;
    
    // Boob stats
    public int jiggleCount;
    public int boobToggleCount;
    
    // Gender stats
    public int genderChanges;
    
    // Unique player tracking (for variety achievements)
    public Set<UUID> uniquePlayersCummedOn;
    public Set<UUID> uniquePlayersGotCummedBy;
    public Set<UUID> uniquePlayersButtfingered;
    public Set<UUID> uniquePlayersPissedNear;
    public Set<UUID> uniquePlayersFartedNear;
    
    // Danger stats (gender neutral)
    public int deathsWhileExposed;
    public int damageWhileGooning; // was damageWhileFapping
    public int goonsWhileFalling; // was fapsWhileFalling
    public int goonsWhileOnFire; // was fapsWhileOnFire
    public int creeperDeathsWhileExposed;
    
    // Location stats (gender neutral)
    public boolean goonedInNether;
    public boolean goonedInEnd;
    public boolean goonedUnderwater;
    public boolean goonedInDesert;
    public boolean goonedInSnow;
    public boolean goonedHighAltitude;
    
    // Combo/Speed stats
    public int maxGoonsInMinute; // was maxFapsInMinute
    public int ejaculationsIn30Seconds;
    public long lastEjaculationTime;
    public int currentEjaculationStreak;
    
    // Animal stats (for more animal achievements)
    public int pigsAffected;
    public int cowsAffected;
    public int wolvesAffected;
    public int catsAffected;
    
    // Detailed goon stats
    public int totalEjaculations; // Total times finished/orgasmed
    
    // Goon streak stats (consecutive Minecraft days)
    public int currentGoonStreak; // Current consecutive MC days
    public int longestGoonStreak; // Best streak ever
    public long lastGoonMinecraftDay; // Last MC day number they gooned
    
    // Session tracking (not persisted)
    public transient long exposureStartTime;
    public transient boolean isExposed;
    public transient int goonsThisMinute;
    public transient long minuteStartTime;

    public PlayerStats() {
        this.experience = 0;
        this.goonCount = 0;
        this.cumOnOthersCount = 0;
        this.gotCummedOnCount = 0;
        this.totalExposureTime = 0;
        this.buttfingersGiven = 0;
        this.buttfingersReceived = 0;
        this.viagraUsed = 0;
        this.fartCount = 0;
        this.poopCount = 0;
        this.pissCount = 0;
        this.jiggleCount = 0;
        this.boobToggleCount = 0;
        this.genderChanges = 0;
        this.mobGoonCounts = new HashMap<>();
        this.uniquePlayersCummedOn = new HashSet<>();
        this.uniquePlayersGotCummedBy = new HashSet<>();
        this.uniquePlayersButtfingered = new HashSet<>();
        this.uniquePlayersPissedNear = new HashSet<>();
        this.uniquePlayersFartedNear = new HashSet<>();
        this.deathsWhileExposed = 0;
        this.damageWhileGooning = 0;
        this.goonsWhileFalling = 0;
        this.goonsWhileOnFire = 0;
        this.creeperDeathsWhileExposed = 0;
        this.goonedInNether = false;
        this.goonedInEnd = false;
        this.goonedUnderwater = false;
        this.goonedInDesert = false;
        this.goonedInSnow = false;
        this.goonedHighAltitude = false;
        this.maxGoonsInMinute = 0;
        this.ejaculationsIn30Seconds = 0;
        this.lastEjaculationTime = 0;
        this.currentEjaculationStreak = 0;
        this.pigsAffected = 0;
        this.cowsAffected = 0;
        this.wolvesAffected = 0;
        this.catsAffected = 0;
        this.totalEjaculations = 0;
        this.currentGoonStreak = 0;
        this.longestGoonStreak = 0;
        this.lastGoonMinecraftDay = 0;
        this.exposureStartTime = 0;
        this.isExposed = false;
        this.goonsThisMinute = 0;
        this.minuteStartTime = 0;
    }
    
    /**
     * Increment goon count near a specific mob type.
     */
    public int incrementMobGoonCount(String mobType) {
        if (mobGoonCounts == null) {
            mobGoonCounts = new HashMap<>();
        }
        int current = mobGoonCounts.getOrDefault(mobType, 0);
        mobGoonCounts.put(mobType, current + 1);
        return current + 1;
    }
    
    /**
     * Get goon count near a specific mob type.
     */
    public int getMobGoonCount(String mobType) {
        if (mobGoonCounts == null) {
            return 0;
        }
        return mobGoonCounts.getOrDefault(mobType, 0);
    }
    
    /**
     * Add experience points.
     */
    public void addExperience(long amount) {
        this.experience += amount;
    }

    public void startExposureTimer() {
        if (!isExposed) {
            exposureStartTime = System.currentTimeMillis();
            isExposed = true;
        }
    }

    public void stopExposureTimer() {
        if (isExposed) {
            long elapsed = (System.currentTimeMillis() - exposureStartTime) / 1000;
            totalExposureTime += elapsed;
            isExposed = false;
            exposureStartTime = 0;
        }
    }

    /** @deprecated Use startExposureTimer instead */
    @Deprecated
    public void startPenisOutTimer() {
        startExposureTimer();
    }

    /** @deprecated Use stopExposureTimer instead */
    @Deprecated
    public void stopPenisOutTimer() {
        stopExposureTimer();
    }

    public long getCurrentTotalTime() {
        if (isExposed) {
            long elapsed = (System.currentTimeMillis() - exposureStartTime) / 1000;
            return totalExposureTime + elapsed;
        }
        return totalExposureTime;
    }

    /**
     * Track goon for speed achievements.
     * @return the current goons in the current minute
     */
    public int trackGoonSpeed() {
        long now = System.currentTimeMillis();
        
        // Reset if more than a minute has passed
        if (now - minuteStartTime > 60000) {
            // Update max if current streak was higher
            if (goonsThisMinute > maxGoonsInMinute) {
                maxGoonsInMinute = goonsThisMinute;
            }
            goonsThisMinute = 0;
            minuteStartTime = now;
        }
        
        goonsThisMinute++;
        
        // Update max in real-time too
        if (goonsThisMinute > maxGoonsInMinute) {
            maxGoonsInMinute = goonsThisMinute;
        }
        
        return goonsThisMinute;
    }

    /** @deprecated Use trackGoonSpeed instead */
    @Deprecated
    public int trackFapSpeed() {
        return trackGoonSpeed();
    }

    /**
     * Track ejaculation for rapid fire achievements.
     * @return the current ejaculations in 30 seconds
     */
    public int trackEjaculationSpeed() {
        long now = System.currentTimeMillis();
        
        // Reset streak if more than 30 seconds since last
        if (now - lastEjaculationTime > 30000) {
            currentEjaculationStreak = 0;
        }
        
        currentEjaculationStreak++;
        lastEjaculationTime = now;
        
        // Update max
        if (currentEjaculationStreak > ejaculationsIn30Seconds) {
            ejaculationsIn30Seconds = currentEjaculationStreak;
        }
        
        return currentEjaculationStreak;
    }

    public String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }
    
    /**
     * Track a goon and update streak.
     * Call this every time the player goons.
     * @param currentMinecraftDay The current Minecraft day (world.getFullTime() / 24000)
     */
    public void trackGoonStreak(long currentMinecraftDay) {
        // Check streak
        if (lastGoonMinecraftDay == 0) {
            // First time gooning
            currentGoonStreak = 1;
            longestGoonStreak = 1;
        } else if (currentMinecraftDay == lastGoonMinecraftDay) {
            // Same day - no streak change needed
        } else if (currentMinecraftDay == lastGoonMinecraftDay + 1) {
            // Consecutive day! Increase streak
            currentGoonStreak++;
            if (currentGoonStreak > longestGoonStreak) {
                longestGoonStreak = currentGoonStreak;
            }
        } else {
            // Streak broken - reset to 1 (today counts)
            currentGoonStreak = 1;
        }
        
        lastGoonMinecraftDay = currentMinecraftDay;
    }
    
    /**
     * Track an ejaculation/orgasm.
     */
    public void trackEjaculation() {
        totalEjaculations++;
    }
    
    /**
     * Get display string for streak.
     */
    public String getStreakDisplay() {
        if (currentGoonStreak <= 0) {
            return "No streak";
        } else if (currentGoonStreak == 1) {
            return "1 day";
        } else {
            return currentGoonStreak + " days";
        }
    }
}
