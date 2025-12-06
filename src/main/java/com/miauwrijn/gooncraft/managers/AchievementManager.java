package com.miauwrijn.gooncraft.managers;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.data.PlayerStats;
import com.miauwrijn.gooncraft.storage.PlayerData;
import com.miauwrijn.gooncraft.storage.StorageManager;

/**
 * Manages achievements for players.
 * Uses StorageManager for persistence (supports file and database storage).
 */
public class AchievementManager {

    public enum Achievement {
        // ===== GOON ACHIEVEMENTS (6) - Gender Neutral =====
        FIRST_GOON("First Timer", "Goon for the first time", "goon", 1),
        GOON_10("Getting Started", "Goon 10 times", "goon", 10),
        GOON_50("Chronic Masturbator", "Goon 50 times", "goon", 50),
        GOON_100("Coomer", "Goon 100 times", "goon", 100),
        GOON_500("Professional Gooner", "Goon 500 times", "goon", 500),
        GOON_1000("Legendary Gooner", "Goon 1000 times", "goon", 1000),
        
        // ===== CUM ON OTHERS ACHIEVEMENTS (4) =====
        CUM_ON_1("Oops!", "Cum on someone for the first time", "cum_on", 1),
        CUM_ON_10("Spray and Pray", "Cum on others 10 times", "cum_on", 10),
        CUM_ON_50("Human Sprinkler", "Cum on others 50 times", "cum_on", 50),
        CUM_ON_100("Bukakke Master", "Cum on others 100 times", "cum_on", 100),
        
        // ===== GOT CUMMED ON ACHIEVEMENTS (3) =====
        GOT_CUMMED_1("Victim", "Get cummed on for the first time", "got_cummed", 1),
        GOT_CUMMED_10("Easy Target", "Get cummed on 10 times", "got_cummed", 10),
        GOT_CUMMED_50("Cum Magnet", "Get cummed on 50 times", "got_cummed", 50),
        
        // ===== EXPOSURE TIME ACHIEVEMENTS (5) - Gender Neutral =====
        TIME_OUT_60("Quick Flash", "Be exposed for 1 minute total", "time_out", 60),
        TIME_OUT_600("Exhibitionist", "Be exposed for 10 minutes total", "time_out", 600),
        TIME_OUT_3600("Nudist", "Be exposed for 1 hour total", "time_out", 3600),
        TIME_OUT_36000("Public Menace", "Be exposed for 10 hours total", "time_out", 36000),
        TIME_OUT_360000("Exhibitionist Prime", "Be exposed for 100 hours total", "time_out", 360000),
        
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
        
        // ===== LOCATION ACHIEVEMENTS (6) - Gender Neutral =====
        GOON_NETHER("Hot & Bothered", "Goon in the Nether", "location", 1, false, "nether"),
        GOON_END("End Game", "Beat the dragon... differently", "location", 1, false, "end"),
        GOON_UNDERWATER("Deep Diver", "Aquatic activities", "location", 1, false, "underwater"),
        GOON_DESERT("Dry Rub", "Sandy situation", "location", 1, false, "desert"),
        GOON_SNOW("Shrinkage", "It's cold!", "location", 1, false, "snow"),
        GOON_HIGH("Mile High Club", "Get freaky above Y=200", "location", 1, false, "high"),
        
        // ===== DANGER ACHIEVEMENTS (5) - Gender Neutral =====
        DAMAGE_WHILE_GOON("Caught Red-Handed", "Take damage while gooning", "damage_goon", 1),
        DEATH_EXPOSED("Didn't See That Coming", "Die while exposed", "death_exposed", 1),
        GOON_ON_FIRE("Too Hot To Handle", "Goon while on fire", "goon_fire", 1),
        GOON_FALLING("Terminal Velocity", "Falling with style", "goon_falling", 1),
        CREEPER_DEATH("Creeper's Delight", "Get blown up while exposed", "creeper_death", 1),
        
        // ===== SOCIAL ACHIEVEMENTS (6) =====
        CUM_VARIETY_10("Sharing is Caring", "Cum on 10 different players", "unique_cum", 10),
        GOT_CUM_VARIETY_10("Popular Target", "Get cummed on by 10 different players", "unique_got_cum", 10),
        PISS_VARIETY_10("Golden Shower Hour", "Piss near 10 different players", "unique_piss", 10),
        FART_VARIETY_10("Stink Bomb", "Fart near 10 different players", "unique_fart", 10),
        BF_VARIETY_10("Explorer", "Buttfinger 10 different players", "unique_bf", 10),
        
        // ===== SPEED ACHIEVEMENTS (2) - Gender Neutral =====
        SPEED_GOON_10("Speed Runner", "Goon 10 times in 60 seconds", "speed_goon", 10),
        RAPID_FIRE_3("Rapid Fire", "Finish 3 times in 30 seconds", "rapid_fire", 3),
        
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
        SHART("Shart Attack", "Fart and poop within 5 seconds", "hidden", 1, true),
        
        // ===== GROUP GOONING ACHIEVEMENTS (4) - Gender Neutral =====
        CIRCLE_GOON("Circle Goon", "Goon with 3+ players at the same time", "group_goon", 1),
        THREESOME("Threesome", "Three's company - goon with 2 others", "group_goon_3", 1),
        ORGY("Orgy", "Goon party - 5+ players together", "group_goon_5", 1),
        GANGBANG("Gangbang", "Seven's a crowd - goon with 6+ others", "group_goon_7", 1),
        
        // ===== MOB PROXIMITY ACHIEVEMENTS (20+) - Gender Neutral =====
        GOON_NEAR_ZOMBIE("Zombie Encounter", "Goon near a zombie", "mob_proximity", 1, false, "zombie"),
        GOON_NEAR_SKELETON("Bone Zone", "Goon near a skeleton", "mob_proximity", 1, false, "skeleton"),
        GOON_NEAR_CREEPER("Dangerous Liaison", "Goon near a creeper", "mob_proximity", 1, false, "creeper"),
        GOON_NEAR_SPIDER("Web of Intrigue", "Goon near a spider", "mob_proximity", 1, false, "spider"),
        GOON_NEAR_ENDERMAN("Tall Dark Stranger", "Goon near an enderman", "mob_proximity", 1, false, "enderman"),
        GOON_NEAR_PIG("Piggy Play", "Goon near a pig", "mob_proximity", 1, false, "pig"),
        GOON_NEAR_COW("Bovine Beauty", "Goon near a cow", "mob_proximity", 1, false, "cow"),
        GOON_NEAR_SHEEP("Sheepish", "Goon near a sheep", "mob_proximity", 1, false, "sheep"),
        GOON_NEAR_CHICKEN("Fowl Play", "Goon near a chicken", "mob_proximity", 1, false, "chicken"),
        GOON_NEAR_HORSE("Stallion Session", "Goon near a horse", "mob_proximity", 1, false, "horse"),
        GOON_NEAR_WOLF("Pack Mentality", "Goon near a wolf", "mob_proximity", 1, false, "wolf"),
        GOON_NEAR_CAT("Feline Friend", "Goon near a cat", "mob_proximity", 1, false, "cat"),
        GOON_NEAR_VILLAGER("Villager Voyeur", "Goon near a villager", "mob_proximity", 1, false, "villager"),
        GOON_NEAR_IRON_GOLEM("Metal Attraction", "Goon near an iron golem", "mob_proximity", 1, false, "iron_golem"),
        GOON_NEAR_LLAMA("Alpaca Adventure", "Goon near a llama", "mob_proximity", 1, false, "llama"),
        GOON_NEAR_PANDA("Bamboo Breeze", "Goon near a panda", "mob_proximity", 1, false, "panda"),
        GOON_NEAR_FOX("Sly Fox", "Goon near a fox", "mob_proximity", 1, false, "fox"),
        GOON_NEAR_BEE("Honey Pot", "Goon near a bee", "mob_proximity", 1, false, "bee"),
        GOON_NEAR_OCELOT("Jungle Fever", "Goon near an ocelot", "mob_proximity", 1, false, "ocelot"),
        GOON_NEAR_DOLPHIN("Dolphin Dive", "Goon near a dolphin", "mob_proximity", 1, false, "dolphin"),
        GOON_NEAR_TURTLE("Turtle Time", "Goon near a turtle", "mob_proximity", 1, false, "turtle"),
        GOON_NEAR_SQUID("Inkredible", "Goon near a squid", "mob_proximity", 1, false, "squid"),
        GOON_NEAR_GOAT("Mountain Goat", "Goon near a goat", "mob_proximity", 1, false, "goat"),
        GOON_NEAR_ALLAY("Allay Play", "Goon near an allay", "mob_proximity", 1, false, "allay"),
        GOON_NEAR_AXOLOTL("Axolotl Adventure", "Goon near an axolotl", "mob_proximity", 1, false, "axolotl");

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

    public AchievementManager() {
        // No initialization needed - StorageManager handles data
    }

    /**
     * Get unlocked achievements for a player from StorageManager.
     */
    public static Set<Achievement> getUnlocked(Player player) {
        PlayerData data = StorageManager.getPlayerData(player);
        if (data.unlockedAchievements == null) {
            data.unlockedAchievements = EnumSet.noneOf(Achievement.class);
        }
        return data.unlockedAchievements;
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
            case "gender_other" -> stats.genderChanges > 0 ? 1 : 0; // Handled specially
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

    /**
     * Check location-based achievements.
     */
    public static void checkLocationAchievements(Player player, PlayerStats stats) {
        Set<Achievement> unlocked = getUnlocked(player);
        
        if (stats.goonedInNether && !unlocked.contains(Achievement.GOON_NETHER)) {
            unlockAchievement(player, Achievement.GOON_NETHER);
        }
        if (stats.goonedInEnd && !unlocked.contains(Achievement.GOON_END)) {
            unlockAchievement(player, Achievement.GOON_END);
        }
        if (stats.goonedUnderwater && !unlocked.contains(Achievement.GOON_UNDERWATER)) {
            unlockAchievement(player, Achievement.GOON_UNDERWATER);
        }
        if (stats.goonedInDesert && !unlocked.contains(Achievement.GOON_DESERT)) {
            unlockAchievement(player, Achievement.GOON_DESERT);
        }
        if (stats.goonedInSnow && !unlocked.contains(Achievement.GOON_SNOW)) {
            unlockAchievement(player, Achievement.GOON_SNOW);
        }
        if (stats.goonedHighAltitude && !unlocked.contains(Achievement.GOON_HIGH)) {
            unlockAchievement(player, Achievement.GOON_HIGH);
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
        
        // Save immediately via StorageManager
        StorageManager.savePlayerData(player.getUniqueId());
        
        // Check for rank up and award skill points
        checkRankUp(player);
    }
    
    /**
     * Check if player ranked up and award skill points.
     */
    private static void checkRankUp(Player player) {
        com.miauwrijn.gooncraft.ranks.BaseRank oldRank = RankManager.getRankForAchievements(getUnlockedCount(player) - 1);
        com.miauwrijn.gooncraft.ranks.BaseRank newRank = RankManager.getRank(player);
        
        if (oldRank != newRank) {
            // Player ranked up!
            com.miauwrijn.gooncraft.handlers.SkillPointsHandler.awardSkillPointsOnRankUp(player, newRank);
            
            // Apply rank perks (they'll be applied by applyAllRankPerks which checks up to current rank)
            RankPerkManager.applyAllRankPerks(player);
            
            // Notify about rank up
            player.sendMessage("");
            player.sendMessage("§6§l✨ RANK UP! ✨");
            player.sendMessage("§7You are now: " + newRank.getDisplayName());
            if (!newRank.getDescription().isEmpty()) {
                player.sendMessage("§8" + newRank.getDescription());
            }
            if (!newRank.getPerkDescriptions().isEmpty()) {
                player.sendMessage("§aPerks:");
                for (String perk : newRank.getPerkDescriptions()) {
                    player.sendMessage("§a  • " + perk);
                }
            }
            player.sendMessage("");
        }
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

}
