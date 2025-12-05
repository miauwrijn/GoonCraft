package com.miauwrijn.gooncraft.managers;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.data.PlayerStats;

/**
 * Manages achievements for players.
 * Data is stored in the players folder alongside other player data.
 */
public class AchievementManager implements Listener {

    public enum Achievement {
        // ===== FAP ACHIEVEMENTS (6) =====
        FIRST_FAP("First Timer", "Fap for the first time", "fap", 1),
        FAP_10("Getting Started", "Fap 10 times", "fap", 10),
        FAP_50("Chronic Masturbator", "Fap 50 times", "fap", 50),
        FAP_100("Coomer", "Fap 100 times", "fap", 100),
        FAP_500("Professional Gooner", "Fap 500 times", "fap", 500),
        FAP_1000("Legendary Gooner", "Fap 1000 times", "fap", 1000),
        
        // ===== CUM ON OTHERS ACHIEVEMENTS (4) =====
        CUM_ON_1("Oops!", "Cum on someone for the first time", "cum_on", 1),
        CUM_ON_10("Spray and Pray", "Cum on others 10 times", "cum_on", 10),
        CUM_ON_50("Human Sprinkler", "Cum on others 50 times", "cum_on", 50),
        CUM_ON_100("Bukakke Master", "Cum on others 100 times", "cum_on", 100),
        
        // ===== GOT CUMMED ON ACHIEVEMENTS (3) =====
        GOT_CUMMED_1("Victim", "Get cummed on for the first time", "got_cummed", 1),
        GOT_CUMMED_10("Easy Target", "Get cummed on 10 times", "got_cummed", 10),
        GOT_CUMMED_50("Cum Magnet", "Get cummed on 50 times", "got_cummed", 50),
        
        // ===== EXPOSURE TIME ACHIEVEMENTS (5) =====
        TIME_OUT_60("Quick Flash", "Have your penis out for 1 minute total", "time_out", 60),
        TIME_OUT_600("Exhibitionist", "Have your penis out for 10 minutes total", "time_out", 600),
        TIME_OUT_3600("Nudist", "Have your penis out for 1 hour total", "time_out", 3600),
        TIME_OUT_36000("Public Menace", "Have your penis out for 10 hours total", "time_out", 36000),
        TIME_OUT_360000("Exhibitionist Prime", "Have your penis out for 100 hours total", "time_out", 360000),
        
        // ===== BUTTFINGER ACHIEVEMENTS (5) =====
        BUTTFINGER_1("Probing", "Buttfinger someone for the first time", "bf_given", 1),
        BUTTFINGER_10("Proctologist", "Buttfinger 10 people", "bf_given", 10),
        BUTTFINGER_50("Master Fingerer", "Buttfinger 50 people", "bf_given", 50),
        GOT_BF_1("Surprised!", "Get buttfingered for the first time", "bf_received", 1),
        GOT_BF_10("Loose", "Get buttfingered 10 times", "bf_received", 10),
        
        // ===== VIAGRA ACHIEVEMENTS (3) =====
        VIAGRA_1("Performance Issues", "Use your first Viagra", "viagra", 1),
        VIAGRA_10("Pill Popper", "Use 10 Viagras", "viagra", 10),
        VIAGRA_50("Pharmacist's Best Friend", "Use 50 Viagras", "viagra", 50),
        
        // ===== FART ACHIEVEMENTS (3) =====
        FART_1("Wind Breaker", "Your first toot", "fart", 1),
        FART_50("Crop Duster", "Spread the love - fart 50 times", "fart", 50),
        FART_500("Taco Bell Survivor", "You should see a doctor - fart 500 times", "fart", 500),
        
        // ===== POOP ACHIEVEMENTS (3) =====
        POOP_1("First Drop", "Nature calls", "poop", 1),
        POOP_50("Regular Schedule", "Fiber is important - poop 50 times", "poop", 50),
        POOP_500("IBS Warrior", "Your poor toilet - poop 500 times", "poop", 500),
        
        // ===== PISS ACHIEVEMENTS (3) =====
        PISS_1("Marking Territory", "Claim what's yours", "piss", 1),
        PISS_50("Fire Hydrant", "Like a dog - piss 50 times", "piss", 50),
        PISS_500("Niagara Falls", "Endless stream - piss 500 times", "piss", 500),
        
        // ===== BOOB ACHIEVEMENTS (4) =====
        BOOB_TOGGLE_1("Flasher", "Show 'em off", "boob_toggle", 1),
        BOOB_TOGGLE_50("Mardi Gras", "Free the nipple - flash 50 times", "boob_toggle", 50),
        JIGGLE_10("Jello Jigglers", "Boing boing - jiggle 10 times", "jiggle", 10),
        JIGGLE_100("Earthquake", "8.0 on the Richter scale - jiggle 100 times", "jiggle", 100),
        
        // ===== GENDER ACHIEVEMENTS (3) =====
        GENDER_OTHER("Best of Both Worlds", "Embrace it all - select Other gender", "gender_other", 1),
        GENDER_CHANGE_5("Identity Crisis", "Can't decide - change gender 5 times", "gender_changes", 5),
        GENDER_CHANGE_20("Fluid", "Go with the flow - change gender 20 times", "gender_changes", 20),
        
        // ===== LOCATION ACHIEVEMENTS (6) =====
        FAP_NETHER("Hot & Bothered", "Fap in the Nether", "location", 1, false, "nether"),
        FAP_END("End Game", "Beat the dragon... differently", "location", 1, false, "end"),
        FAP_UNDERWATER("Deep Diver", "Aquatic activities", "location", 1, false, "underwater"),
        FAP_DESERT("Dry Rub", "Sandy situation", "location", 1, false, "desert"),
        FAP_SNOW("Shrinkage", "It's cold!", "location", 1, false, "snow"),
        FAP_HIGH("Mile High Club", "Get freaky above Y=200", "location", 1, false, "high"),
        
        // ===== DANGER ACHIEVEMENTS (5) =====
        DAMAGE_WHILE_FAP("Caught Red-Handed", "Take damage while fapping", "damage_fap", 1),
        DEATH_EXPOSED("Didn't See That Coming", "Die while exposed", "death_exposed", 1),
        FAP_ON_FIRE("Too Hot To Handle", "Fap while on fire", "fap_fire", 1),
        FAP_FALLING("Terminal Velocity", "Falling with style", "fap_falling", 1),
        CREEPER_DEATH("Creeper's Delight", "Get blown up while exposed", "creeper_death", 1),
        
        // ===== SOCIAL ACHIEVEMENTS (6) =====
        CUM_VARIETY_10("Sharing is Caring", "Cum on 10 different players", "unique_cum", 10),
        GOT_CUM_VARIETY_10("Popular Target", "Get cummed on by 10 different players", "unique_got_cum", 10),
        PISS_VARIETY_10("Golden Shower Hour", "Piss near 10 different players", "unique_piss", 10),
        FART_VARIETY_10("Stink Bomb", "Fart near 10 different players", "unique_fart", 10),
        BF_VARIETY_10("Explorer", "Buttfinger 10 different players", "unique_bf", 10),
        
        // ===== SPEED ACHIEVEMENTS (3) =====
        SPEED_FAP_10("Speed Runner", "Fap 10 times in 60 seconds", "speed_fap", 10),
        RAPID_FIRE_3("Rapid Fire", "Ejaculate 3 times in 30 seconds", "rapid_fire", 3),
        
        // ===== ANIMAL EASTER EGGS - SHEEP (3 hidden) =====
        YELLOW_SHEEP("Golden Fleece", "Dye a sheep yellow in an... unconventional way", "hidden", 1, true),
        BROWN_SHEEP("Chocolate Wool", "Give a sheep a brown makeover... naturally", "hidden", 1, true),
        WHITE_SHEEP("Glazed & Confused", "Cover a sheep in your... essence", "hidden", 1, true),
        
        // ===== ANIMAL EASTER EGGS - CHICKEN (3 hidden) =====
        YELLOW_CHICKEN("Lemon Chicken", "Make a chicken golden... with your stream", "hidden", 1, true),
        BROWN_CHICKEN("Nugget Maker", "Drop something on a chicken", "hidden", 1, true),
        WHITE_CHICKEN("Cream Chicken", "Give a chicken a special coating", "hidden", 1, true),
        
        // ===== ANIMAL EASTER EGGS - OTHER ANIMALS (4 hidden) =====
        WHITE_PIG("Pork Glazing", "Give a pig a makeover", "hidden", 1, true),
        WHITE_COW("Holy Cow", "Milk isn't the only white fluid", "hidden", 1, true),
        YELLOW_WOLF("Wet Wolf", "Golden retriever", "hidden", 1, true),
        WHITE_CAT("Cat Got Your... Cream?", "Curious cat", "hidden", 1, true),
        
        // ===== COMBO ACHIEVEMENT (1 hidden) =====
        SHART("Shart Attack", "Fart and poop within 5 seconds", "hidden", 1, true);

        public final String name;
        public final String description;
        public final String category;
        public final long threshold;
        public final boolean hidden;
        public final String locationTag; // For location-based achievements

        Achievement(String name, String description, String category, long threshold) {
            this(name, description, category, threshold, false, null);
        }

        Achievement(String name, String description, String category, long threshold, boolean hidden) {
            this(name, description, category, threshold, hidden, null);
        }

        Achievement(String name, String description, String category, long threshold, boolean hidden, String locationTag) {
            this.name = name;
            this.description = description;
            this.category = category;
            this.threshold = threshold;
            this.hidden = hidden;
            this.locationTag = locationTag;
        }
    }

    private static final Map<UUID, Set<Achievement>> unlockedAchievements = new ConcurrentHashMap<>();
    private static File dataFolder;

    public AchievementManager() {
        dataFolder = new File(Plugin.instance.getDataFolder(), "players");
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
            // Skip location-based and hidden achievements - they're handled separately
            if (achievement.locationTag != null || achievement.hidden) continue;
            
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
            case "fart" -> stats.fartCount;
            case "poop" -> stats.poopCount;
            case "piss" -> stats.pissCount;
            case "jiggle" -> stats.jiggleCount;
            case "boob_toggle" -> stats.boobToggleCount;
            case "gender_changes" -> stats.genderChanges;
            case "gender_other" -> stats.genderChanges > 0 ? 1 : 0; // Handled specially
            case "unique_cum" -> stats.uniquePlayersCummedOn.size();
            case "unique_got_cum" -> stats.uniquePlayersGotCummedBy.size();
            case "unique_piss" -> stats.uniquePlayersPissedNear.size();
            case "unique_fart" -> stats.uniquePlayersFartedNear.size();
            case "unique_bf" -> stats.uniquePlayersButtfingered.size();
            case "damage_fap" -> stats.damageWhileFapping;
            case "death_exposed" -> stats.deathsWhileExposed;
            case "fap_fire" -> stats.fapsWhileOnFire;
            case "fap_falling" -> stats.fapsWhileFalling;
            case "creeper_death" -> stats.creeperDeathsWhileExposed;
            case "speed_fap" -> stats.maxFapsInMinute;
            case "rapid_fire" -> stats.ejaculationsIn30Seconds;
            default -> 0;
        };
    }

    /**
     * Check location-based achievements.
     */
    public static void checkLocationAchievements(Player player, PlayerStats stats) {
        Set<Achievement> unlocked = getUnlocked(player);
        
        if (stats.fappedInNether && !unlocked.contains(Achievement.FAP_NETHER)) {
            unlockAchievement(player, Achievement.FAP_NETHER);
        }
        if (stats.fappedInEnd && !unlocked.contains(Achievement.FAP_END)) {
            unlockAchievement(player, Achievement.FAP_END);
        }
        if (stats.fappedUnderwater && !unlocked.contains(Achievement.FAP_UNDERWATER)) {
            unlockAchievement(player, Achievement.FAP_UNDERWATER);
        }
        if (stats.fappedInDesert && !unlocked.contains(Achievement.FAP_DESERT)) {
            unlockAchievement(player, Achievement.FAP_DESERT);
        }
        if (stats.fappedInSnow && !unlocked.contains(Achievement.FAP_SNOW)) {
            unlockAchievement(player, Achievement.FAP_SNOW);
        }
        if (stats.fappedHighAltitude && !unlocked.contains(Achievement.FAP_HIGH)) {
            unlockAchievement(player, Achievement.FAP_HIGH);
        }
    }

    private static void unlockAchievement(Player player, Achievement achievement) {
        Set<Achievement> unlocked = getUnlocked(player);
        if (unlocked.contains(achievement)) return; // Double check
        
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

    /**
     * Get total non-hidden achievements.
     */
    public static int getVisibleAchievements() {
        int count = 0;
        for (Achievement a : Achievement.values()) {
            if (!a.hidden) count++;
        }
        return count;
    }

    /**
     * Manually unlock an achievement (for easter eggs).
     * Returns true if newly unlocked, false if already had it.
     */
    public static boolean tryUnlock(Player player, Achievement achievement) {
        Set<Achievement> unlocked = getUnlocked(player);
        if (unlocked.contains(achievement)) {
            return false;
        }
        unlockAchievement(player, achievement);
        return true;
    }

    /**
     * Check if an achievement is hidden (shows as ??? until unlocked).
     */
    public static boolean isHidden(Achievement achievement) {
        return achievement.hidden;
    }

    /**
     * Get all achievements by category for display.
     */
    public static List<Achievement> getAchievementsByCategory(String category) {
        return java.util.Arrays.stream(Achievement.values())
            .filter(a -> a.category.equals(category) || 
                        (category.equals("hidden") && a.hidden) ||
                        (category.equals("location") && a.locationTag != null))
            .toList();
    }

    // ===== Persistence =====

    private void ensureDataFolderExists() {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            Plugin.instance.getLogger().warning("Failed to create players folder: " + dataFolder.getAbsolutePath());
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
                if (config.getBoolean("Achievements." + achievement.name(), false)) {
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
                config.set("Achievements." + achievement.name(), unlocked.contains(achievement));
            }
            
            config.save(file);
        } catch (IOException e) {
            Plugin.instance.getLogger().log(Level.WARNING, "Failed to save player achievements", e);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadPlayerAchievements(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        savePlayerAchievements(event.getPlayer());
        unlockedAchievements.remove(event.getPlayer().getUniqueId());
    }
}
