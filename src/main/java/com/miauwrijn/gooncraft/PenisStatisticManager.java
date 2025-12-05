package com.miauwrijn.gooncraft;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.miauwrijn.gooncraft.data.PenisStatistics;
import com.miauwrijn.gooncraft.models.PenisModel;

public class PenisStatisticManager implements Listener {

    private static final Map<UUID, PenisStatistics> statistics = new ConcurrentHashMap<>();
    private final File dataFolder;

    public PenisStatisticManager() {
        this.dataFolder = new File(Plugin.instance.getDataFolder(), "players");
        ensureDataFolderExists();
        syncOnlinePlayers();
        
        Bukkit.getPluginManager().registerEvents(this, Plugin.instance);
    }

    public static PenisStatistics getStatistics(Player player) {
        return statistics.get(player.getUniqueId());
    }

    public static void setViagraBoost(Player player, int boost) {
        PenisStatistics stats = statistics.get(player.getUniqueId());
        if (stats != null) {
            stats.viagraBoost = boost;
            if (stats.penisModel != null) {
                stats.penisModel.reload(stats);
            }
        }
    }

    public static void setPenisSize(Player player, int size) {
        PenisStatistics stats = statistics.get(player.getUniqueId());
        if (stats != null) {
            stats.size = size;
            savePlayerData(player, stats);
            if (stats.penisModel != null) {
                stats.penisModel.reload(stats);
            }
        }
    }

    public static void setPenisGirth(Player player, int girth) {
        PenisStatistics stats = statistics.get(player.getUniqueId());
        if (stats != null) {
            stats.girth = girth;
            savePlayerData(player, stats);
            if (stats.penisModel != null) {
                stats.penisModel.reload(stats);
            }
        }
    }

    public static void setPenisBbc(Player player, boolean bbc) {
        PenisStatistics stats = statistics.get(player.getUniqueId());
        if (stats != null) {
            stats.bbc = bbc;
            savePlayerData(player, stats);
            if (stats.penisModel != null) {
                stats.penisModel.reload(stats);
            }
        }
    }

    public static void setActivePenis(Player player, PenisModel model, int taskId) {
        PenisStatistics stats = statistics.get(player.getUniqueId());
        if (stats != null) {
            stats.penisModel = model;
            stats.runnableTaskId = taskId;
        }
    }

    public static void clearActivePenis(Player player) {
        PenisStatistics stats = statistics.get(player.getUniqueId());
        if (stats != null && stats.penisModel != null) {
            stats.penisModel.discard();
            Bukkit.getScheduler().cancelTask(stats.runnableTaskId);
            stats.penisModel = null;
            stats.runnableTaskId = 0;
        }
    }

    private void ensureDataFolderExists() {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            Plugin.instance.getLogger().warning("Failed to create data folder: " + dataFolder.getAbsolutePath());
        }
    }

    private void syncOnlinePlayers() {
        // Clear any floating models from previous session
        cleanupFloatingModels();
        
        // Re-register all online players
        for (Player player : Plugin.instance.getServer().getOnlinePlayers()) {
            clearActivePenis(player);
            statistics.remove(player.getUniqueId());
            loadOrCreatePlayerData(player);
        }
    }

    private void cleanupFloatingModels() {
        for (World world : Plugin.instance.getServer().getWorlds()) {
            world.getEntities().stream()
                .filter(e -> e.getType() == EntityType.BLOCK_DISPLAY)
                .filter(e -> "GOONCRAFT".equalsIgnoreCase(e.getName()))
                .forEach(org.bukkit.entity.Entity::remove);
        }
    }

    private void loadOrCreatePlayerData(Player player) {
        File file = new File(dataFolder, player.getUniqueId() + ".yml");
        
        if (file.exists()) {
            loadPlayerData(player, file);
        } else {
            createNewPlayerData(player, file);
        }
    }

    private void loadPlayerData(Player player, File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        int size = config.getInt("Penis.Size", -1);
        int girth = config.getInt("Penis.Girth", -1);
        boolean bbc = config.getBoolean("Penis.BBC", false);
        
        boolean needsSave = false;
        
        if (size == -1) {
            size = PenisModel.getRandomSize();
            config.set("Penis.Size", size);
            needsSave = true;
        }
        
        if (girth == -1) {
            girth = PenisModel.getRandomGirth();
            config.set("Penis.Girth", girth);
            needsSave = true;
        }
        
        if (needsSave) {
            try {
                config.save(file);
            } catch (IOException e) {
                Plugin.instance.getLogger().log(Level.WARNING, "Failed to save player data", e);
            }
        }
        
        statistics.put(player.getUniqueId(), new PenisStatistics(size, girth, bbc));
    }

    private void createNewPlayerData(Player player, File file) {
        int size = PenisModel.getRandomSize();
        int girth = PenisModel.getRandomGirth();
        boolean bbc = PenisModel.getRandomBbc();
        
        try {
            if (file.createNewFile()) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                config.set("Penis.Size", size);
                config.set("Penis.Girth", girth);
                config.set("Penis.BBC", bbc);
                config.save(file);
            }
        } catch (IOException e) {
            Plugin.instance.getLogger().log(Level.WARNING, "Failed to create player data", e);
        }
        
        statistics.put(player.getUniqueId(), new PenisStatistics(size, girth, bbc));
    }

    private static void savePlayerData(Player player, PenisStatistics stats) {
        File file = new File(new File(Plugin.instance.getDataFolder(), "players"), 
                           player.getUniqueId() + ".yml");
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.set("Penis.Size", stats.size);
            config.set("Penis.Girth", stats.girth);
            config.set("Penis.BBC", stats.bbc);
            config.save(file);
        } catch (IOException e) {
            Plugin.instance.getLogger().log(Level.WARNING, "Failed to save player data", e);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadOrCreatePlayerData(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        clearActivePenis(event.getPlayer());
    }

    @EventHandler
    public void onPlayerSwing(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
            return;
        }
        
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            return;
        }
        
        PenisStatistics stats = getStatistics(player);
        if (stats != null && stats.penisModel != null) {
            stats.penisModel.cum();
        }
    }
}
