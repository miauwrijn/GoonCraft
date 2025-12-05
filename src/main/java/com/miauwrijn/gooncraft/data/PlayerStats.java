package com.miauwrijn.gooncraft.data;

/**
 * Tracks player statistics for achievements and scoreboards.
 */
public class PlayerStats {
    
    public int fapCount;
    public int cumOnOthersCount;
    public int gotCummedOnCount;
    public long totalTimeWithPenisOut; // in seconds
    public int buttfingersGiven;
    public int buttfingersReceived;
    public int viagraUsed;
    
    // Session tracking (not persisted)
    public transient long penisOutStartTime;
    public transient boolean isPenisOut;

    public PlayerStats() {
        this.fapCount = 0;
        this.cumOnOthersCount = 0;
        this.gotCummedOnCount = 0;
        this.totalTimeWithPenisOut = 0;
        this.buttfingersGiven = 0;
        this.buttfingersReceived = 0;
        this.viagraUsed = 0;
        this.penisOutStartTime = 0;
        this.isPenisOut = false;
    }

    public PlayerStats(int fapCount, int cumOnOthersCount, int gotCummedOnCount, 
                       long totalTimeWithPenisOut, int buttfingersGiven, 
                       int buttfingersReceived, int viagraUsed) {
        this.fapCount = fapCount;
        this.cumOnOthersCount = cumOnOthersCount;
        this.gotCummedOnCount = gotCummedOnCount;
        this.totalTimeWithPenisOut = totalTimeWithPenisOut;
        this.buttfingersGiven = buttfingersGiven;
        this.buttfingersReceived = buttfingersReceived;
        this.viagraUsed = viagraUsed;
        this.penisOutStartTime = 0;
        this.isPenisOut = false;
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
