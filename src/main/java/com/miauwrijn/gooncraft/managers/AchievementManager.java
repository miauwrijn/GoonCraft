package com.miauwrijn.gooncraft.managers;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.data.PlayerStats;

/**
 * Manages achievements for players.
 */
public class AchievementManager implements Listener {

    public enum Achievement {
        // Fap achievements
        FIRST_FAP("First Timer", "Fap for the first time", "fap", 1),
        FAP_10("Getting Started", "Fap 10 times", "fap", 10),
        FAP_50("Chronic Masturbator", "Fap 50 times", "fap", 50),
        FAP_100("Coomer", "Fap 100 times", "fap", 100),
        FAP_500("Professional Gooner", "Fap 500 times", "fap", 500),
        FAP_1000("Legendary Gooner", "Fap 1000 times", "fap", 1000),
        
        // Cum on others achievements
        CUM_ON_1("Oops!", "Cum on someone for the first time", "cum_on", 1),
        CUM_ON_10("Spray and Pray", "Cum on others 10 times", "cum_on", 10),
        CUM_ON_50("Human Sprinkler", "Cum on others 50 times", "cum_on", 50),
        CUM_ON_100("Bukakke Master", "Cum on others 100 times", "cum_on", 100),
        
        // Got cummed on achievements
        GOT_CUMMED_1("Victim", "Get cummed on for the first time", "got_cummed", 1),
        GOT_CUMMED_10("Easy Target", "Get cummed on 10 times", "got_cummed", 10),
        GOT_CUMMED_50("Cum Magnet", "Get cummed on 50 times", "got_cummed", 50),
        
        // Penis out time achievements
        TIME_OUT_60("Quick Flash", "Have your penis out for 1 minute total", "time_out", 60),
        TIME_OUT_600("Exhibitionist", "Have your penis out for 10 minutes total", "time_out", 600),
        TIME_OUT_3600("Nudist", "Have your penis out for 1 hour total", "time_out", 3600),
        TIME_OUT_36000("Public Menace", "Have your penis out for 10 hours total", "time_out", 36000),
        
        // Buttfinger achievements
        BUTTFINGER_1("Probing", "Buttfinger someone for the first time", "bf_given", 1),
        BUTTFINGER_10("Proctologist", "Buttfinger 10 people", "bf_given", 10),
        BUTTFINGER_50("Master Fingerer", "Buttfinger 50 people", "bf_given", 50),
        
        // Got buttfingered achievements
        GOT_BF_1("Surprised!", "Get buttfingered for the first time", "bf_received", 1),
        GOT_BF_10("Loose", "Get buttfingered 10 times", "bf_received", 10),
        
        // Viagra achievements
        VIAGRA_1("Performance Issues", "Use your first Viagra", "viagra", 1),
        VIAGRA_10("Pill Popper", "Use 10 Viagras", "viagra", 10),
        VIAGRA_50("Pharmacist's Best Friend", "Use 50 Viagras", "viagra", 50);

        public final String name;
        public final String description;
        public final String category;
        public final long threshold;

        Achievement(String name, String description, String category, long threshold) {
            this.name = name;
            this.description = description;
            this.category = category;
            this.threshold = threshold;
        }
    }

    private static final Map<UUID, Set<Achievement>> unlockedAchievements = new ConcurrentHashMap<>();
    private static File dataFolder;

    public AchievementManager() {
        dataFolder = new File(Plugin.instance.getDataFolder(), "achievements");
        ensureDataFolderExists();
        loadOnlinePlayers();
        
        Bukkit.getPluginManager().registerEvents(this, Plugin.instance);
    }

    public static Set<Achievement> getUnlocked(Player player) {
        return unlockedAchievements.computeIfAbsent(player.getUniqueId(), k -> EnumSet.noneOf(Achievement.class));
    }

    public static void checkAchievements(Player player, PlayerStats stats) {
        Set<Achievement> unlocked = getUnlocked(player);
        
        for (Achievement achievement : Achievement.values()) {
            if (unlocked.contains(achievement)) continue;
            
            long currentValue = getStatForCategory(stats, achievement.category);
            
            if (currentValue >= achievement.threshold) {
                unlockAchievement(player, achievement);
            }
        }
    }

    private static long getStatForCategory(PlayerStats stats, String category) {
        return switch (category) {
            case "fap" -> stats.fapCount;
            case "cum_on" -> stats.cumOnOthersCount;
            case "got_cummed" -> stats.gotCummedOnCount;
            case "time_out" -> stats.getCurrentTotalTime();
            case "bf_given" -> stats.buttfingersGiven;
            case "bf_received" -> stats.buttfingersReceived;
            case "viagra" -> stats.viagraUsed;
            default -> 0;
        };
    }

    private static void unlockAchievement(Player player, Achievement achievement) {
        Set<Achievement> unlocked = getUnlocked(player);
        unlocked.add(achievement);
        
        // Notify player
        player.sendMessage("");
        player.sendMessage(ConfigManager.getMessage("achievement.unlocked-title"));
        player.sendMessage(ConfigManager.getMessage("achievement.unlocked-name", "{name}", achievement.name));
        player.sendMessage(ConfigManager.getMessage("achievement.unlocked-description", "{description}", achievement.description));
        player.sendMessage("");
        
        // Sound effect
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        
        // Broadcast to server
        String broadcast = ConfigManager.getMessage("achievement.broadcast",
            "{player}", player.getName(),
            "{name}", achievement.name);
        Bukkit.broadcastMessage(broadcast);
        
        // Save immediately
        savePlayerAchievements(player);
    }

    public static int getUnlockedCount(Player player) {
        return getUnlocked(player).size();
    }

    public static int getTotalAchievements() {
        return Achievement.values().length;
    }

    // ===== Persistence =====

    private void ensureDataFolderExists() {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            Plugin.instance.getLogger().warning("Failed to create achievements folder: " + dataFolder.getAbsolutePath());
        }
    }

    private void loadOnlinePlayers() {
        for (Player player : Plugin.instance.getServer().getOnlinePlayers()) {
            loadPlayerAchievements(player);
        }
    }

    private void loadPlayerAchievements(Player player) {
        File file = new File(dataFolder, player.getUniqueId() + ".yml");
        Set<Achievement> unlocked = EnumSet.noneOf(Achievement.class);
        
        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            
            for (Achievement achievement : Achievement.values()) {
                if (config.getBoolean(achievement.name(), false)) {
                    unlocked.add(achievement);
                }
            }
        }
        
        unlockedAchievements.put(player.getUniqueId(), unlocked);
    }

    private static void savePlayerAchievements(Player player) {
        Set<Achievement> unlocked = unlockedAchievements.get(player.getUniqueId());
        if (unlocked == null) return;
        
        File file = new File(dataFolder, player.getUniqueId() + ".yml");
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            
            for (Achievement achievement : Achievement.values()) {
                config.set(achievement.name(), unlocked.contains(achievement));
            }
            
            config.save(file);
        } catch (IOException e) {
            Plugin.instance.getLogger().log(Level.WARNING, "Failed to save player achievements", e);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadPlayerAchievements(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        savePlayerAchievements(event.getPlayer());
        unlockedAchievements.remove(event.getPlayer().getUniqueId());
    }
}
