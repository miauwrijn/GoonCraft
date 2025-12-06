package com.miauwrijn.gooncraft.handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.data.PenisStatistics;
import com.miauwrijn.gooncraft.managers.ConfigManager;
import com.miauwrijn.gooncraft.managers.GenderManager;
import com.miauwrijn.gooncraft.managers.GenderManager.Gender;
import com.miauwrijn.gooncraft.managers.PenisStatisticManager;
import com.miauwrijn.gooncraft.managers.RankPerkManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;
import com.miauwrijn.gooncraft.models.BoobModel;
import com.miauwrijn.gooncraft.models.PenisModel;
import com.miauwrijn.gooncraft.models.VaginaModel;

/**
 * Handles /genitals command for unified toggle of body parts based on gender.
 * - MALE: Toggles penis
 * - FEMALE: Toggles vagina + boobs
 * - OTHER: Toggles penis + boobs
 */
public class GenitalsCommandHandler implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigManager.getMessage("only-players"));
            return true;
        }

        Gender gender = GenderManager.getGender(player);
        if (gender == null) {
            player.sendMessage(ConfigManager.getMessage("genitals.no-gender"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("toggle")) {
            return handleToggle(player, gender);
        }

        player.sendMessage(ConfigManager.getMessage("genitals.usage"));
        return true;
    }

    private boolean handleToggle(Player player, Gender gender) {
        boolean isCurrentlyShowing = isShowingGenitals(player, gender);

        if (isCurrentlyShowing) {
            // Hide everything
            hideGenitals(player, gender);
            player.sendMessage(ConfigManager.getMessage("genitals.hidden"));
            
            // Stop animal following when hidden
            RankPerkManager.checkAnimalFollowing(player);
        } else {
            // Show based on gender
            showGenitals(player, gender);
            player.sendMessage(ConfigManager.getMessage("genitals.shown"));
            
            // Track exposure time start
            StatisticsManager.startExposureTimer(player);
            
            // Check animal following when shown
            RankPerkManager.checkAnimalFollowing(player);
        }

        return true;
    }

    private boolean isShowingGenitals(Player player, Gender gender) {
        return switch (gender) {
            case MALE -> {
                PenisStatistics stats = PenisStatisticManager.getStatistics(player);
                yield stats != null && stats.penisModel != null;
            }
            case FEMALE -> GenderManager.getActiveVaginaModel(player) != null;
            case OTHER -> {
                PenisStatistics stats = PenisStatisticManager.getStatistics(player);
                yield stats != null && stats.penisModel != null;
            }
        };
    }

    private void showGenitals(Player player, Gender gender) {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

        switch (gender) {
            case MALE -> showPenis(player, scheduler);
            case FEMALE -> {
                showVagina(player, scheduler);
                showBoobs(player, scheduler);
            }
            case OTHER -> {
                showPenis(player, scheduler);
                showBoobs(player, scheduler);
            }
        }
    }

    private void hideGenitals(Player player, Gender gender) {
        // Stop exposure timer
        StatisticsManager.stopExposureTimer(player);

        switch (gender) {
            case MALE -> PenisStatisticManager.clearActivePenis(player);
            case FEMALE -> {
                GenderManager.clearActiveVaginaModel(player);
                GenderManager.clearActiveBoobModel(player);
            }
            case OTHER -> {
                PenisStatisticManager.clearActivePenis(player);
                GenderManager.clearActiveBoobModel(player);
            }
        }
    }

    private void showPenis(Player player, BukkitScheduler scheduler) {
        PenisStatistics stats = PenisStatisticManager.getStatistics(player);
        if (stats.penisModel != null) return; // Already showing

        // Create model - size/girth boosts are applied via getEffectiveSize() in setBlockTransformation
        PenisModel model = new PenisModel(player, stats.bbc, stats.size, stats.girth, stats.viagraBoost);
        stats.penisModel = model;
        int taskId = scheduler.scheduleSyncRepeatingTask(Plugin.instance, model, 0, 1L);
        stats.runnableTaskId = taskId;
        
        // Reload model to ensure rank boosts are applied
        if (stats.rankSizeBoost > 0 || stats.rankGirthBoost > 0) {
            model.reload(stats);
        }
    }

    private void showBoobs(Player player, BukkitScheduler scheduler) {
        if (GenderManager.getActiveBoobModel(player) != null) return; // Already showing

        // Use effective size including rank boosts
        int size = GenderManager.getEffectiveBoobSize(player);
        int perkiness = GenderManager.getBoobPerkiness(player);
        BoobModel boobs = new BoobModel(player, size, perkiness);
        int taskId = scheduler.scheduleSyncRepeatingTask(Plugin.instance, boobs, 0, 1L);
        GenderManager.setActiveBoobModel(player, boobs, taskId);
    }

    private void showVagina(Player player, BukkitScheduler scheduler) {
        if (GenderManager.getActiveVaginaModel(player) != null) return; // Already showing

        VaginaModel vagina = new VaginaModel(player);
        int taskId = scheduler.scheduleSyncRepeatingTask(Plugin.instance, vagina, 0, 1L);
        GenderManager.setActiveVaginaModel(player, vagina, taskId);
    }
}
