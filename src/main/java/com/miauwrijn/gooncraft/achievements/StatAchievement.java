package com.miauwrijn.gooncraft.achievements;

import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.data.PlayerStats;

/**
 * Achievement based on a stat value from PlayerStats.
 */
public class StatAchievement extends BaseAchievement {
    
    private final String statCategory;
    
    public StatAchievement(String id, String name, String description, 
                          String category, long threshold, boolean hidden, String statCategory) {
        super(id, name, description, category, threshold, hidden);
        this.statCategory = statCategory;
    }
    
    @Override
    public boolean checkCondition(Player player, PlayerStats stats) {
        long currentValue = getStatForCategory(stats, statCategory);
        return currentValue >= threshold;
    }
    
    @Override
    public long getCurrentProgress(Player player, PlayerStats stats) {
        return getStatForCategory(stats, statCategory);
    }
    
    private long getStatForCategory(PlayerStats stats, String category) {
        return switch (category) {
            case "goon" -> stats.goonCount;
            case "cum_on" -> stats.cumOnOthersCount;
            case "got_cummed" -> stats.gotCummedOnCount;
            case "time_out" -> stats.getCurrentTotalTime();
            case "bf_given" -> stats.buttfingersGiven;
            case "bf_received" -> stats.buttfingersReceived;
            case "viagra" -> stats.viagraUsed;
            case "fart" -> stats.fartCount;
            case "poop" -> stats.poopCount;
            case "piss" -> stats.pissCount;
            case "jiggle" -> stats.jiggleCount;
            case "boob_toggle" -> stats.boobToggleCount;
            case "gender_changes" -> stats.genderChanges;
            case "unique_cum" -> stats.uniquePlayersCummedOn.size();
            case "unique_got_cum" -> stats.uniquePlayersGotCummedBy.size();
            case "unique_piss" -> stats.uniquePlayersPissedNear.size();
            case "unique_fart" -> stats.uniquePlayersFartedNear.size();
            case "unique_bf" -> stats.uniquePlayersButtfingered.size();
            case "damage_goon" -> stats.damageWhileGooning;
            case "death_exposed" -> stats.deathsWhileExposed;
            case "goon_fire" -> stats.goonsWhileOnFire;
            case "goon_falling" -> stats.goonsWhileFalling;
            case "creeper_death" -> stats.creeperDeathsWhileExposed;
            case "speed_goon" -> stats.maxGoonsInMinute;
            case "rapid_fire" -> stats.ejaculationsIn30Seconds;
            default -> 0;
        };
    }
}

