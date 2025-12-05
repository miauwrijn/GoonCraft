package com.miauwrijn.gooncraft.managers;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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

/**
 * Manages player gender selection, boob models, and vagina models.
 * Data is stored in the players folder alongside penis stats.
 */
public class GenderManager implements Listener {

    public enum Gender {
        MALE,      // Has penis
        FEMALE,    // Has boobs + vagina
        OTHER      // Has penis + boobs
    }

    private static final Map<UUID, Gender> playerGenders = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> boobSizes = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> boobPerkiness = new ConcurrentHashMap<>();
    private static final Map<UUID, BoobModel> activeBoobModels = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> boobTaskIds = new ConcurrentHashMap<>();
    private static final Map<UUID, VaginaModel> activeVaginaModels = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> vaginaTaskIds = new ConcurrentHashMap<>();
    private static File dataFolder;

    public GenderManager() {
        dataFolder = new File(Plugin.instance.getDataFolder(), "players");
        ensureDataFolderExists();
        loadOnlinePlayers();
        
        Bukkit.getPluginManager().registerEvents(this, Plugin.instance);
    }

    public static Gender getGender(Player player) {
        return playerGenders.get(player.getUniqueId());
    }

    public static boolean hasSelectedGender(Player player) {
        return playerGenders.containsKey(player.getUniqueId());
    }

    public static void setGender(Player player, Gender gender) {
        playerGenders.put(player.getUniqueId(), gender);
        
        // Generate boob stats if female or other
        if ((gender == Gender.FEMALE || gender == Gender.OTHER)) {
            if (!boobSizes.containsKey(player.getUniqueId())) {
                boobSizes.put(player.getUniqueId(), BoobModel.getRandomSize());
            }
            if (!boobPerkiness.containsKey(player.getUniqueId())) {
                boobPerkiness.put(player.getUniqueId(), BoobModel.getRandomPerkiness());
            }
        }
        
        savePlayerData(player);
    }

    public static int getBoobSize(Player player) {
        return boobSizes.getOrDefault(player.getUniqueId(), BoobModel.getRandomSize());
    }

    public static void setBoobSize(Player player, int size) {
        boobSizes.put(player.getUniqueId(), size);
        savePlayerData(player);
        
        // Reload active model if exists
        BoobModel model = activeBoobModels.get(player.getUniqueId());
        if (model != null) {
            model.reload(size, getBoobPerkiness(player));
        }
    }

    public static int getBoobPerkiness(Player player) {
        return boobPerkiness.getOrDefault(player.getUniqueId(), BoobModel.getRandomPerkiness());
    }

    public static void setBoobPerkiness(Player player, int perkiness) {
        boobPerkiness.put(player.getUniqueId(), perkiness);
        savePlayerData(player);
        
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

    // ===== Persistence =====

    private void ensureDataFolderExists() {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            Plugin.instance.getLogger().warning("Failed to create players folder: " + dataFolder.getAbsolutePath());
        }
    }

    private void loadOnlinePlayers() {
        for (Player player : Plugin.instance.getServer().getOnlinePlayers()) {
            loadPlayerData(player);
        }
    }

    private void loadPlayerData(Player player) {
        File file = new File(dataFolder, player.getUniqueId() + ".yml");
        
        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            
            String genderStr = config.getString("Gender", null);
            if (genderStr != null) {
                try {
                    Gender gender = Gender.valueOf(genderStr);
                    playerGenders.put(player.getUniqueId(), gender);
                } catch (IllegalArgumentException ignored) {}
            }
            
            int boobSize = config.getInt("Boobs.Size", -1);
            if (boobSize > 0) {
                boobSizes.put(player.getUniqueId(), boobSize);
            }
            
            int perkiness = config.getInt("Boobs.Perkiness", -1);
            if (perkiness > 0) {
                boobPerkiness.put(player.getUniqueId(), perkiness);
            }
        }
    }

    private static void savePlayerData(Player player) {
        File file = new File(dataFolder, player.getUniqueId() + ".yml");
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            
            Gender gender = playerGenders.get(player.getUniqueId());
            if (gender != null) {
                config.set("Gender", gender.name());
            }
            
            Integer boobSize = boobSizes.get(player.getUniqueId());
            if (boobSize != null) {
                config.set("Boobs.Size", boobSize);
            }
            
            Integer perkiness = boobPerkiness.get(player.getUniqueId());
            if (perkiness != null) {
                config.set("Boobs.Perkiness", perkiness);
            }
            
            config.save(file);
        } catch (IOException e) {
            Plugin.instance.getLogger().log(Level.WARNING, "Failed to save gender data", e);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loadPlayerData(player);
        
        // Show gender selection GUI if not selected yet
        if (!hasSelectedGender(player)) {
            // Delay slightly so player fully loads in
            Bukkit.getScheduler().runTaskLater(Plugin.instance, () -> {
                if (player.isOnline()) {
                    new GenderSelectionGUI(player).open();
                }
            }, 20L); // 1 second delay
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        clearActiveBoobModel(event.getPlayer());
        clearActiveVaginaModel(event.getPlayer());
    }
}
