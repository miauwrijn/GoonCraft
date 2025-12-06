package com.miauwrijn.gooncraft.util;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;

import com.miauwrijn.gooncraft.managers.AchievementManager;
import com.miauwrijn.gooncraft.managers.ConfigManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;

/**
 * Handles interactions between players' bodily functions and animals.
 * Extracted from BodilyFunctionsHandler for better organization.
 */
public class AnimalInteractionHandler {

    /**
     * Check for nearby animals and affect them based on the color/type of bodily function.
     * @param player The player performing the action
     * @param color "yellow" for piss, "brown" for poop, "white" for cum
     */
    public static void checkForAnimals(Player player, String color) {
        try {
            if (player == null || !player.isOnline() || color == null) {
                return;
            }
            
            org.bukkit.Location loc = player.getLocation();
            if (loc == null || loc.getWorld() == null) {
                return;
            }
            
            for (Entity entity : loc.getWorld().getNearbyEntities(loc, 3, 3, 3)) {
                if (entity instanceof LivingEntity livingEntity) {
                    handleMob(livingEntity, color, player);
                }
            }
        } catch (Exception e) {
            // Silently fail to prevent breaking model rendering
            // Errors here should not affect genital model display
        }
    }

    private static void handleMob(LivingEntity entity, String color, Player player) {
        String mobType = entity.getType().name().toLowerCase();
        
        // Get mob config
        ConfigurationSection mobConfig = ConfigManager.getConfig().getConfigurationSection("animals.mobs." + mobType);
        if (mobConfig == null) {
            return; // Mob not configured
        }
        
        String customName = mobConfig.getString(color, "");
        if (customName == null || customName.isEmpty()) {
            return; // This color interaction is disabled for this mob
        }
        
        // Handle sheep color change separately
        if (entity instanceof Sheep sheep && ConfigManager.getConfig().getBoolean("animals.change-sheep-color", true)) {
            DyeColor dyeColor = switch (color) {
                case "yellow" -> DyeColor.YELLOW;
                case "brown" -> DyeColor.BROWN;
                case "white" -> DyeColor.WHITE;
                default -> null;
            };
            
            if (dyeColor != null && sheep.getColor() != dyeColor) {
                sheep.setColor(dyeColor);
            }
        }
        
        // Set custom name
        if (!customName.equals(entity.getCustomName())) {
            entity.setCustomName(ConfigManager.colorize(customName));
            entity.setCustomNameVisible(true);
        }
        
        // Spawn particles
        Color particleColor = switch (color) {
            case "yellow" -> Color.YELLOW;
            case "brown" -> Color.fromRGB(139, 69, 19);
            case "white" -> Color.WHITE;
            default -> Color.WHITE;
        };
        
        DustOptions dust = new DustOptions(particleColor, 0.8f);
        entity.getWorld().spawnParticle(Particle.DUST, entity.getLocation().add(0, entity.getHeight() * 0.5, 0), 
            15, 0.3, 0.3, 0.3, 0, dust);
        
        // Play sound (try common sound patterns for different mob types)
        try {
            Sound sound = null;
            String typeName = entity.getType().name().toUpperCase();
            try {
                sound = Sound.valueOf("ENTITY_" + typeName + "_AMBIENT");
            } catch (IllegalArgumentException e1) {
                try {
                    sound = Sound.valueOf("ENTITY_" + typeName + "_HURT");
                } catch (IllegalArgumentException e2) {
                    // No sound available for this mob type
                }
            }
            if (sound != null) {
                entity.getWorld().playSound(entity.getLocation(), sound, 1.0f, 1.5f);
            }
        } catch (Exception e) {
            // Sound might not exist for all mob types, that's okay
        }
        
        // Unlock achievement if it exists (based on color and mob type)
        String achievementId = switch (color) {
            case "yellow" -> "yellow_" + mobType;
            case "brown" -> "brown_" + mobType;
            case "white" -> "white_" + mobType;
            default -> null;
        };
        
        if (achievementId != null) {
            AchievementManager.tryUnlockById(player, achievementId);
        }
        
        // Update stats for specific mobs
        switch (mobType) {
            case "pig":
                StatisticsManager.incrementPigsAffected(player);
                break;
            case "cow":
                StatisticsManager.incrementCowsAffected(player);
                break;
            case "wolf":
                StatisticsManager.incrementWolvesAffected(player);
                break;
            case "cat":
                StatisticsManager.incrementCatsAffected(player);
                break;
        }
    }
}

