package com.miauwrijn.gooncraft.util;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wolf;

import com.miauwrijn.gooncraft.managers.AchievementManager;
import com.miauwrijn.gooncraft.managers.AchievementManager.Achievement;
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
                if (entity instanceof Sheep sheep) {
                handleSheep(sheep, color, player);
                } else if (entity instanceof Chicken chicken) {
                    handleChicken(chicken, color, player);
                } else if (entity instanceof Pig pig && color.equals("white")) {
                    handlePig(pig, player);
                } else if (entity instanceof Cow cow && color.equals("white")) {
                    handleCow(cow, player);
                } else if (entity instanceof Wolf wolf && color.equals("yellow")) {
                    handleWolf(wolf, player);
                } else if (entity instanceof Cat cat && color.equals("white")) {
                    handleCat(cat, player);
                }
            }
        } catch (Exception e) {
            // Silently fail to prevent breaking model rendering
            // Errors here should not affect genital model display
        }
    }

    private static void handleSheep(Sheep sheep, String color, Player player) {
        DyeColor dyeColor = switch (color) {
            case "yellow" -> DyeColor.YELLOW;
            case "brown" -> DyeColor.BROWN;
            case "white" -> DyeColor.WHITE;
            default -> null;
        };
        
        if (dyeColor != null && sheep.getColor() != dyeColor) {
            sheep.setColor(dyeColor);
            
            DustOptions dust = new DustOptions(
                color.equals("yellow") ? Color.YELLOW : 
                color.equals("brown") ? Color.fromRGB(139, 69, 19) : Color.WHITE, 
                1.0f
            );
            sheep.getWorld().spawnParticle(Particle.DUST, sheep.getLocation().add(0, 1, 0), 
                20, 0.5, 0.5, 0.5, 0, dust);
            sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_SHEEP_AMBIENT, 1.0f, 1.5f);
            
            Achievement achievement = switch (color) {
                case "yellow" -> Achievement.YELLOW_SHEEP;
                case "brown" -> Achievement.BROWN_SHEEP;
                case "white" -> Achievement.WHITE_SHEEP;
                default -> null;
            };
            if (achievement != null) {
                AchievementManager.tryUnlock(player, achievement);
            }
        }
    }

    private static void handleChicken(Chicken chicken, String color, Player player) {
        DustOptions dust = new DustOptions(
            color.equals("yellow") ? Color.YELLOW : 
            color.equals("brown") ? Color.fromRGB(139, 69, 19) : Color.WHITE, 
            0.8f
        );
        
        chicken.getWorld().spawnParticle(Particle.DUST, chicken.getLocation().add(0, 0.5, 0), 
            15, 0.3, 0.3, 0.3, 0, dust);
        chicken.getWorld().playSound(chicken.getLocation(), Sound.ENTITY_CHICKEN_HURT, 1.0f, 1.2f);
        
        String chickenName = switch (color) {
            case "yellow" -> "§e§lLemon Chicken";
            case "brown" -> "§6§lChocolate Nugget";
            case "white" -> "§f§lCreamed Chicken";
            default -> null;
        };
        if (chickenName != null && !chickenName.equals(chicken.getCustomName())) {
            chicken.setCustomName(chickenName);
            chicken.setCustomNameVisible(true);
            
            Achievement achievement = switch (color) {
                case "yellow" -> Achievement.YELLOW_CHICKEN;
                case "brown" -> Achievement.BROWN_CHICKEN;
                case "white" -> Achievement.WHITE_CHICKEN;
                default -> null;
            };
            if (achievement != null) {
                AchievementManager.tryUnlock(player, achievement);
            }
        }
    }

    private static void handlePig(Pig pig, Player player) {
        DustOptions dust = new DustOptions(Color.WHITE, 1.0f);
        pig.getWorld().spawnParticle(Particle.DUST, pig.getLocation().add(0, 0.5, 0), 
            20, 0.4, 0.4, 0.4, 0, dust);
        pig.getWorld().playSound(pig.getLocation(), Sound.ENTITY_PIG_AMBIENT, 1.0f, 1.5f);
        
        if (pig.getCustomName() == null || !pig.getCustomName().contains("Glazed")) {
            pig.setCustomName("§f§lGlazed Ham");
            pig.setCustomNameVisible(true);
            AchievementManager.tryUnlock(player, Achievement.WHITE_PIG);
            StatisticsManager.incrementPigsAffected(player);
        }
    }

    private static void handleCow(Cow cow, Player player) {
        DustOptions dust = new DustOptions(Color.WHITE, 1.0f);
        cow.getWorld().spawnParticle(Particle.DUST, cow.getLocation().add(0, 1, 0), 
            25, 0.5, 0.5, 0.5, 0, dust);
        cow.getWorld().playSound(cow.getLocation(), Sound.ENTITY_COW_AMBIENT, 1.0f, 1.5f);
        
        if (cow.getCustomName() == null || !cow.getCustomName().contains("Cream")) {
            cow.setCustomName("§f§lCream Cow");
            cow.setCustomNameVisible(true);
            AchievementManager.tryUnlock(player, Achievement.WHITE_COW);
            StatisticsManager.incrementCowsAffected(player);
        }
    }

    private static void handleWolf(Wolf wolf, Player player) {
        DustOptions dust = new DustOptions(Color.YELLOW, 0.8f);
        wolf.getWorld().spawnParticle(Particle.DUST, wolf.getLocation().add(0, 0.7, 0), 
            20, 0.4, 0.4, 0.4, 0, dust);
        wolf.getWorld().playSound(wolf.getLocation(), Sound.ENTITY_WOLF_WHINE, 1.0f, 1.2f);
        
        if (wolf.getCustomName() == null || !wolf.getCustomName().contains("Golden")) {
            wolf.setCustomName("§e§lGolden Retriever");
            wolf.setCustomNameVisible(true);
            AchievementManager.tryUnlock(player, Achievement.YELLOW_WOLF);
            StatisticsManager.incrementWolvesAffected(player);
        }
    }

    private static void handleCat(Cat cat, Player player) {
        DustOptions dust = new DustOptions(Color.WHITE, 0.6f);
        cat.getWorld().spawnParticle(Particle.DUST, cat.getLocation().add(0, 0.4, 0), 
            15, 0.3, 0.3, 0.3, 0, dust);
        cat.getWorld().playSound(cat.getLocation(), Sound.ENTITY_CAT_HISS, 1.0f, 1.5f);
        
        if (cat.getCustomName() == null || !cat.getCustomName().contains("Cream")) {
            cat.setCustomName("§f§lCream Puff");
            cat.setCustomNameVisible(true);
            AchievementManager.tryUnlock(player, Achievement.WHITE_CAT);
            StatisticsManager.incrementCatsAffected(player);
        }
    }
}

