package com.miauwrijn.gooncraft.data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks player statistics for achievements and scoreboards.
 */
public class PlayerStats {
    
    // Original stats
    public int fapCount;
    public int cumOnOthersCount;
    public int gotCummedOnCount;
    public long totalTimeWithPenisOut; // in seconds
    public int buttfingersGiven;
    public int buttfingersReceived;
    public int viagraUsed;
    
    // Bodily function stats
    public int fartCount;
    public int poopCount;
    public int pissCount;
    
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
    
    // Danger stats
    public int deathsWhileExposed;
    public int damageWhileFapping;
    public int fapsWhileFalling;
    public int fapsWhileOnFire;
    public int creeperDeathsWhileExposed;
    
    // Location stats
    public boolean fappedInNether;
    public boolean fappedInEnd;
    public boolean fappedUnderwater;
    public boolean fappedInDesert;
    public boolean fappedInSnow;
    public boolean fappedHighAltitude;
    
    // Combo/Speed stats
    public int maxFapsInMinute;
    public int ejaculationsIn30Seconds;
    public long lastEjaculationTime;
    public int currentEjaculationStreak;
    
    // Animal stats (for more animal achievements)
    public int pigsAffected;
    public int cowsAffected;
    public int wolvesAffected;
    public int catsAffected;
    
    // Session tracking (not persisted)
    public transient long penisOutStartTime;
    public transient boolean isPenisOut;
    public transient int fapsThisMinute;
    public transient long minuteStartTime;

    public PlayerStats() {
        this.fapCount = 0;
        this.cumOnOthersCount = 0;
        this.gotCummedOnCount = 0;
        this.totalTimeWithPenisOut = 0;
        this.buttfingersGiven = 0;
        this.buttfingersReceived = 0;
        this.viagraUsed = 0;
        this.fartCount = 0;
        this.poopCount = 0;
        this.pissCount = 0;
        this.jiggleCount = 0;
        this.boobToggleCount = 0;
        this.genderChanges = 0;
        this.uniquePlayersCummedOn = new HashSet<>();
        this.uniquePlayersGotCummedBy = new HashSet<>();
        this.uniquePlayersButtfingered = new HashSet<>();
        this.uniquePlayersPissedNear = new HashSet<>();
        this.uniquePlayersFartedNear = new HashSet<>();
        this.deathsWhileExposed = 0;
        this.damageWhileFapping = 0;
        this.fapsWhileFalling = 0;
        this.fapsWhileOnFire = 0;
        this.creeperDeathsWhileExposed = 0;
        this.fappedInNether = false;
        this.fappedInEnd = false;
        this.fappedUnderwater = false;
        this.fappedInDesert = false;
        this.fappedInSnow = false;
        this.fappedHighAltitude = false;
        this.maxFapsInMinute = 0;
        this.ejaculationsIn30Seconds = 0;
        this.lastEjaculationTime = 0;
        this.currentEjaculationStreak = 0;
        this.pigsAffected = 0;
        this.cowsAffected = 0;
        this.wolvesAffected = 0;
        this.catsAffected = 0;
        this.penisOutStartTime = 0;
        this.isPenisOut = false;
        this.fapsThisMinute = 0;
        this.minuteStartTime = 0;
    }

    public void startPenisOutTimer() {
        if (!isPenisOut) {
            penisOutStartTime = System.currentTimeMillis();
            isPenisOut = true;
        }
    }

    public void stopPenisOutTimer() {
        if (isPenisOut) {
            long elapsed = (System.currentTimeMillis() - penisOutStartTime) / 1000;
            totalTimeWithPenisOut += elapsed;
            isPenisOut = false;
            penisOutStartTime = 0;
        }
    }

    public long getCurrentTotalTime() {
        if (isPenisOut) {
            long elapsed = (System.currentTimeMillis() - penisOutStartTime) / 1000;
            return totalTimeWithPenisOut + elapsed;
        }
        return totalTimeWithPenisOut;
    }

    /**
     * Track fap for speed achievements.
     * @return the current faps in the current minute
     */
    public int trackFapSpeed() {
        long now = System.currentTimeMillis();
        
        // Reset if more than a minute has passed
        if (now - minuteStartTime > 60000) {
            // Update max if current streak was higher
            if (fapsThisMinute > maxFapsInMinute) {
                maxFapsInMinute = fapsThisMinute;
            }
            fapsThisMinute = 0;
            minuteStartTime = now;
        }
        
        fapsThisMinute++;
        
        // Update max in real-time too
        if (fapsThisMinute > maxFapsInMinute) {
            maxFapsInMinute = fapsThisMinute;
        }
        
        return fapsThisMinute;
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
}
