package com.miauwrijn.gooncraft.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.gui.GenderSelectionGUI;
import com.miauwrijn.gooncraft.models.BoobModel;
import com.miauwrijn.gooncraft.models.VaginaModel;
import com.miauwrijn.gooncraft.storage.PlayerData;
import com.miauwrijn.gooncraft.storage.StorageManager;

/**
 * Manages player gender selection, boob models, and vagina models.
 * Uses StorageManager for persistence (supports file and database storage).
 */
public class GenderManager implements Listener {

    public enum Gender {
        MALE,      // Has penis
        FEMALE,    // Has boobs + vagina
        OTHER      // Has penis + boobs
    }

    // Runtime-only: active models (not persisted)
    private static final Map<UUID, BoobModel> activeBoobModels = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> boobTaskIds = new ConcurrentHashMap<>();
    private static final Map<UUID, VaginaModel> activeVaginaModels = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> vaginaTaskIds = new ConcurrentHashMap<>();
    
    // Runtime-only: rank perk boosts for boobs (not persisted, reset on logout)
    private static final Map<UUID, Integer> rankBoobBoosts = new ConcurrentHashMap<>();

    public GenderManager() {
        Bukkit.getPluginManager().registerEvents(this, Plugin.instance);
    }

    public static Gender getGender(Player player) {
        PlayerData data = StorageManager.getPlayerData(player);
        return data.gender;
    }

    public static boolean hasSelectedGender(Player player) {
        PlayerData data = StorageManager.getPlayerData(player);
        return data.gender != null;
    }

    public static void setGender(Player player, Gender gender) {
        PlayerData data = StorageManager.getPlayerData(player);
        data.gender = gender;
        
        // Generate boob stats if female or other
        if ((gender == Gender.FEMALE || gender == Gender.OTHER)) {
            if (data.boobSize <= 0) {
                data.boobSize = BoobModel.getRandomSize();
            }
            if (data.boobPerkiness <= 0) {
                data.boobPerkiness = BoobModel.getRandomPerkiness();
            }
        }
        
        StorageManager.savePlayerData(player.getUniqueId());
    }

    public static int getBoobSize(Player player) {
        PlayerData data = StorageManager.getPlayerData(player);
        if (data.boobSize <= 0) {
            data.boobSize = BoobModel.getRandomSize();
            StorageManager.savePlayerData(player.getUniqueId());
        }
        return data.boobSize;
    }
    
    /**
     * Get effective boob size including rank boosts.
     */
    public static int getEffectiveBoobSize(Player player) {
        int baseSize = getBoobSize(player);
        int rankBoost = rankBoobBoosts.getOrDefault(player.getUniqueId(), 0);
        return Math.min(BoobModel.maxSize, baseSize + rankBoost);
    }

    public static void setBoobSize(Player player, int size) {
        PlayerData data = StorageManager.getPlayerData(player);
        data.boobSize = size;
        StorageManager.savePlayerData(player.getUniqueId());
        
        // Reload active model if exists (use effective size including rank boosts)
        BoobModel model = activeBoobModels.get(player.getUniqueId());
        if (model != null) {
            model.reload(getEffectiveBoobSize(player), getBoobPerkiness(player));
        }
    }

    public static int getBoobPerkiness(Player player) {
        PlayerData data = StorageManager.getPlayerData(player);
        if (data.boobPerkiness <= 0) {
            data.boobPerkiness = BoobModel.getRandomPerkiness();
            StorageManager.savePlayerData(player.getUniqueId());
        }
        return data.boobPerkiness;
    }

    public static void setBoobPerkiness(Player player, int perkiness) {
        PlayerData data = StorageManager.getPlayerData(player);
        data.boobPerkiness = perkiness;
        StorageManager.savePlayerData(player.getUniqueId());
        
        // Reload active model if exists
        BoobModel model = activeBoobModels.get(player.getUniqueId());
        if (model != null) {
            model.reload(getBoobSize(player), perkiness);
        }
    }

    public static boolean hasBoobs(Player player) {
        Gender gender = getGender(player);
        return gender == Gender.FEMALE || gender == Gender.OTHER;
    }

    public static boolean hasPenis(Player player) {
        Gender gender = getGender(player);
        return gender == Gender.MALE || gender == Gender.OTHER || gender == null;
    }

    public static boolean hasVagina(Player player) {
        Gender gender = getGender(player);
        return gender == Gender.FEMALE;
    }

    // ===== Boob Model Management =====

    public static BoobModel getActiveBoobModel(Player player) {
        return activeBoobModels.get(player.getUniqueId());
    }

    public static void setActiveBoobModel(Player player, BoobModel model, int taskId) {
        activeBoobModels.put(player.getUniqueId(), model);
        boobTaskIds.put(player.getUniqueId(), taskId);
    }

    public static void clearActiveBoobModel(Player player) {
        BoobModel model = activeBoobModels.remove(player.getUniqueId());
        Integer taskId = boobTaskIds.remove(player.getUniqueId());
        
        if (model != null) {
            model.discard();
        }
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    public static void clearAllActiveBoobModels() {
        for (Player player : Plugin.instance.getServer().getOnlinePlayers()) {
            clearActiveBoobModel(player);
        }
    }

    // ===== Vagina Model Management =====

    public static VaginaModel getActiveVaginaModel(Player player) {
        return activeVaginaModels.get(player.getUniqueId());
    }

    public static void setActiveVaginaModel(Player player, VaginaModel model, int taskId) {
        activeVaginaModels.put(player.getUniqueId(), model);
        vaginaTaskIds.put(player.getUniqueId(), taskId);
    }

    public static void clearActiveVaginaModel(Player player) {
        VaginaModel model = activeVaginaModels.remove(player.getUniqueId());
        Integer taskId = vaginaTaskIds.remove(player.getUniqueId());
        
        if (model != null) {
            model.discard();
        }
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    public static void clearAllActiveVaginaModels() {
        for (Player player : Plugin.instance.getServer().getOnlinePlayers()) {
            clearActiveVaginaModel(player);
        }
    }

    // ===== Check if any genital model is active =====

    public static boolean hasActiveGenitals(Player player) {
        return activeBoobModels.containsKey(player.getUniqueId()) ||
               activeVaginaModels.containsKey(player.getUniqueId()) ||
               PenisStatisticManager.getStatistics(player) != null && 
               PenisStatisticManager.getStatistics(player).penisModel != null;
    }

    // ===== Event Handlers =====

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Show gender selection GUI if not selected yet
        // Delayed to ensure StorageManager has loaded player data
        Bukkit.getScheduler().runTaskLater(Plugin.instance, () -> {
            if (player.isOnline() && !hasSelectedGender(player)) {
                new GenderSelectionGUI(player).open();
            }
        }, 40L); // 2 second delay
    }

    /**
     * Add rank boob boost (cumulative).
     */
    public static void addRankBoobBoost(Player player, int boost) {
        UUID uuid = player.getUniqueId();
        rankBoobBoosts.put(uuid, rankBoobBoosts.getOrDefault(uuid, 0) + boost);
        
        // Reload active model if exists
        BoobModel model = activeBoobModels.get(uuid);
        if (model != null) {
            model.reload(getEffectiveBoobSize(player), getBoobPerkiness(player));
        }
    }
    
    /**
     * Remove rank boob boost.
     */
    public static void removeRankBoobBoost(Player player, int boost) {
        UUID uuid = player.getUniqueId();
        int current = rankBoobBoosts.getOrDefault(uuid, 0);
        rankBoobBoosts.put(uuid, Math.max(0, current - boost));
        
        // Reload active model if exists
        BoobModel model = activeBoobModels.get(uuid);
        if (model != null) {
            model.reload(getEffectiveBoobSize(player), getBoobPerkiness(player));
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        clearActiveBoobModel(event.getPlayer());
        clearActiveVaginaModel(event.getPlayer());
        rankBoobBoosts.remove(uuid);
    }
}
