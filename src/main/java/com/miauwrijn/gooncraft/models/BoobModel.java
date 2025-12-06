package com.miauwrijn.gooncraft.models;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import com.miauwrijn.gooncraft.managers.CooldownManager;

/**
 * 3D boob model using BlockDisplay entities with nipples!
 * Nipples become stiffer (more erect) in cold areas.
 */
public class BoobModel implements Runnable {

    public static final int minSize = 1;
    public static final int maxSize = 10;
    public static final int minPerkiness = 1;
    public static final int maxPerkiness = 10;
    
    // Starting ranges for new players (minimum 5, random 5-10)
    private static final int START_SIZE_MIN = 5;
    private static final int START_SIZE_MAX = 10;
    private static final int START_PERKINESS_MIN = 5;
    private static final int START_PERKINESS_MAX = 10;

    // Cup size names for display
    private static final String[] CUP_SIZES = {
        "AA", "A", "B", "C", "D", "DD", "E", "F", "G", "H"
    };

    private static final int JIGGLE_DURATION = 15;
    
    // Nipple stiffness modifiers (cold = stiffer nipples)
    private static final float MIN_NIPPLE_STIFFNESS = 1.0f;  // Normal nipples
    private static final float MAX_NIPPLE_STIFFNESS = 2.0f;  // Very erect nipples
    private static final float STIFFNESS_TRANSITION_SPEED = 0.05f;

    private int size;
    private int perkiness;
    private final Player owner;

    private BlockDisplay leftBoob;
    private BlockDisplay rightBoob;
    private BlockDisplay leftNipple;
    private BlockDisplay rightNipple;

    private boolean isJiggling = false;
    private int framesSinceJiggleStart = 0;
    private float jiggleOffset = 0;
    
    // Temperature-based nipple stiffness (1.0 = normal, 2.0 = very erect)
    private float currentNippleStiffness = MIN_NIPPLE_STIFFNESS;
    private int coldCheckCounter = 0;

    public BoobModel(Player owner, int size, int perkiness) {
        this.owner = owner;
        this.size = clamp(size, minSize, maxSize);
        this.perkiness = clamp(perkiness, minPerkiness, maxPerkiness);
        buildModel();
    }

    /**
     * Get random starting size for new players (5-10 range).
     */
    public static int getRandomSize() {
        return ThreadLocalRandom.current().nextInt(START_SIZE_MIN, START_SIZE_MAX + 1);
    }

    /**
     * Get random starting perkiness for new players (5-10 range).
     */
    public static int getRandomPerkiness() {
        return ThreadLocalRandom.current().nextInt(START_PERKINESS_MIN, START_PERKINESS_MAX + 1);
    }

    /**
     * Convert numeric size (1-10) to cup size string (AA-H).
     */
    public static String getCupSize(int size) {
        int index = clamp(size, minSize, maxSize) - 1;
        return CUP_SIZES[index];
    }

    public void reload(int newSize, int newPerkiness) {
        this.size = clamp(newSize, minSize, maxSize);
        this.perkiness = clamp(newPerkiness, minPerkiness, maxPerkiness);
        discard();
        buildModel();
    }

    @Override
    public void run() {
        if (isJiggling) {
            animateJiggle();
        }
        
        // Check temperature and update nipple stiffness every 20 ticks (1 second)
        coldCheckCounter++;
        if (coldCheckCounter >= 20) {
            coldCheckCounter = 0;
            updateNippleStiffness();
        }
        
        updatePosition();
    }
    
    /**
     * Check if player is in a cold area and update nipple stiffness accordingly.
     * Cold areas cause nipples to become stiffer (more erect).
     */
    private void updateNippleStiffness() {
        boolean isCold = isPlayerInCold();
        float targetStiffness = isCold ? MAX_NIPPLE_STIFFNESS : MIN_NIPPLE_STIFFNESS;
        
        // Gradually transition stiffness
        if (Math.abs(currentNippleStiffness - targetStiffness) > 0.01f) {
            if (currentNippleStiffness < targetStiffness) {
                currentNippleStiffness = Math.min(currentNippleStiffness + STIFFNESS_TRANSITION_SPEED, targetStiffness);
            } else {
                currentNippleStiffness = Math.max(currentNippleStiffness - STIFFNESS_TRANSITION_SPEED, targetStiffness);
            }
            
            // Update nipple transformations with new stiffness
            updateTransformations();
        }
    }
    
    /**
     * Check if player is in a cold environment (snowy biome or cold weather).
     */
    private boolean isPlayerInCold() {
        Location loc = owner.getLocation();
        World world = loc.getWorld();
        
        // Check biome
        Biome biome = loc.getBlock().getBiome();
        String biomeName = biome.name().toLowerCase();
        
        boolean coldBiome = biomeName.contains("snow") || 
                           biomeName.contains("ice") || 
                           biomeName.contains("frozen") || 
                           biomeName.contains("cold") ||
                           biomeName.contains("taiga") ||
                           biomeName.contains("grove") ||
                           biomeName.contains("peaks");
        
        // Check if it's snowing/raining in a cold biome
        boolean coldWeather = world.hasStorm() && loc.getBlock().getTemperature() < 0.15;
        
        return coldBiome || coldWeather;
    }
    
    /**
     * Get current nipple stiffness modifier (for external use).
     */
    public float getNippleStiffness() {
        return currentNippleStiffness;
    }
    
    /**
     * Check if the nipples are currently affected by cold (erect).
     */
    public boolean areNipplesErect() {
        return currentNippleStiffness > MIN_NIPPLE_STIFFNESS + 0.1f;
    }

    public void discard() {
        if (leftBoob != null) leftBoob.remove();
        if (rightBoob != null) rightBoob.remove();
        if (leftNipple != null) leftNipple.remove();
        if (rightNipple != null) rightNipple.remove();
    }

    public void jiggle() {
        if (isJiggling) {
            return;
        }

        int cooldown = 1;
        if (CooldownManager.hasCooldown(owner, "jiggle", cooldown)) {
            return;
        }

        isJiggling = true;
        CooldownManager.setCooldown(owner, "jiggle");

        // Play bounce sound
        owner.getWorld().playSound(owner.getLocation(), Sound.BLOCK_SLIME_BLOCK_PLACE, 0.5f, 1.2f);
    }

    private void animateJiggle() {
        if (framesSinceJiggleStart >= JIGGLE_DURATION) {
            isJiggling = false;
            jiggleOffset = 0;
            framesSinceJiggleStart = 0;
            updateTransformations();
            return;
        }

        // Sine wave bounce effect
        float progress = (float) framesSinceJiggleStart / JIGGLE_DURATION;
        jiggleOffset = (float) Math.sin(progress * Math.PI * 3) * 0.02f * (1 - progress);

        updateTransformations();
        framesSinceJiggleStart++;
    }

    private void updatePosition() {
        Location location = owner.getLocation();
        
        // Only use yaw (horizontal rotation), ignore pitch (head up/down)
        float yaw = location.getYaw();
        
        float heightOffset = owner.isSneaking() ? 1.1f : 1.2f;
        // Perkiness affects vertical position (higher perkiness = higher boobs)
        float perkinessOffset = (perkiness - 5) * 0.008f;
        Location center = location.clone().add(0, heightOffset + perkinessOffset, 0);
        
        // Calculate direction from yaw only (no pitch)
        double yawRad = Math.toRadians(yaw);
        Vector direction = new Vector(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();

        // Position in front of chest
        Vector perpVector = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
        Location chestCenter = center.clone().add(direction.clone().multiply(0.15));

        float boobSpacing = 0.08f + (size * 0.005f);
        
        Location leftBoobPos = chestCenter.clone().add(perpVector.clone().multiply(boobSpacing));
        Location rightBoobPos = chestCenter.clone().subtract(perpVector.clone().multiply(boobSpacing));
        
        // Set the yaw for proper rotation (face the direction player is looking)
        leftBoobPos.setYaw(yaw);
        rightBoobPos.setYaw(yaw);
        
        leftBoob.teleport(leftBoobPos);
        rightBoob.teleport(rightBoobPos);
        
        // Nipples positioned slightly in front of boobs
        float nippleOffset = 0.06f + (size * 0.008f);
        // Perkiness affects nipple angle (higher = pointing more up/forward)
        float nippleVerticalOffset = (perkiness - 5) * 0.003f;
        
        Location leftNipplePos = leftBoobPos.clone()
            .add(direction.clone().multiply(nippleOffset))
            .add(0, nippleVerticalOffset, 0);
        Location rightNipplePos = rightBoobPos.clone()
            .add(direction.clone().multiply(nippleOffset))
            .add(0, nippleVerticalOffset, 0);
        
        leftNipplePos.setYaw(yaw);
        rightNipplePos.setYaw(yaw);
        
        leftNipple.teleport(leftNipplePos);
        rightNipple.teleport(rightNipplePos);
    }

    private void buildModel() {
        Location location = owner.getLocation();
        World world = owner.getWorld();

        BlockData boobBlock = Material.PINK_TERRACOTTA.createBlockData();
        BlockData nippleBlock = Material.BROWN_TERRACOTTA.createBlockData();

        leftBoob = createBlockDisplay(location, boobBlock, world);
        rightBoob = createBlockDisplay(location, boobBlock, world);
        leftNipple = createBlockDisplay(location, nippleBlock, world);
        rightNipple = createBlockDisplay(location, nippleBlock, world);

        updateTransformations();
        updatePosition();
    }

    private BlockDisplay createBlockDisplay(Location location, BlockData block, World world) {
        BlockDisplay display = (BlockDisplay) world.spawnEntity(location, EntityType.BLOCK_DISPLAY);
        display.setBlock(block);
        display.setDisplayHeight(0.1f);
        display.setDisplayWidth(0.1f);
        display.setGravity(false);
        display.setInvulnerable(true);
        display.setSilent(true);
        display.setCustomName("GOONCRAFT");
        display.setCustomNameVisible(false);

        return display;
    }

    private void updateTransformations() {
        // Boob transformations - size affects overall size
        float baseSize = 0.06f + (size * 0.012f);
        float yScale = baseSize * 0.8f;
        float zScale = baseSize * 0.6f;

        Vector3f boobScale = new Vector3f(baseSize, yScale + jiggleOffset, zScale);
        Vector3f boobTranslation = new Vector3f(-boobScale.x * 0.5f, -boobScale.y * 0.5f, 0f);
        AxisAngle4f rotation = new AxisAngle4f(0, 0, 1, 0);

        Transformation boobTransform = new Transformation(boobTranslation, rotation, boobScale, rotation);
        
        if (leftBoob != null) leftBoob.setTransformation(boobTransform);
        if (rightBoob != null) rightBoob.setTransformation(boobTransform);
        
        // Nipple transformations - perkiness and stiffness affect nipple size
        // Stiffness makes nipples larger/more prominent when cold
        float nippleBaseSize = 0.015f + (perkiness * 0.002f);
        float stiffnessBonus = (currentNippleStiffness - 1.0f) * 0.01f; // Extra size when cold
        float nippleSize = nippleBaseSize + stiffnessBonus;
        
        // Z scale (length) increases more with stiffness for that erect look
        float nippleYScale = nippleSize * (0.8f + jiggleOffset * 2);
        float nippleZScale = nippleSize * (1.2f + (currentNippleStiffness - 1.0f) * 0.8f);
        
        Vector3f nippleScale = new Vector3f(nippleSize, nippleYScale, nippleZScale);
        Vector3f nippleTranslation = new Vector3f(-nippleScale.x * 0.5f, -nippleScale.y * 0.5f, 0f);
        
        Transformation nippleTransform = new Transformation(nippleTranslation, rotation, nippleScale, rotation);
        
        if (leftNipple != null) leftNipple.setTransformation(nippleTransform);
        if (rightNipple != null) rightNipple.setTransformation(nippleTransform);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
