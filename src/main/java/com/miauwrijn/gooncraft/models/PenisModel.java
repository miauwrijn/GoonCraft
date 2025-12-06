package com.miauwrijn.gooncraft.models;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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

import com.miauwrijn.gooncraft.data.PenisStatistics;
import com.miauwrijn.gooncraft.util.AnimalInteractionHandler;
import com.miauwrijn.gooncraft.managers.ConfigManager;
import com.miauwrijn.gooncraft.managers.CooldownManager;
import com.miauwrijn.gooncraft.managers.RankManager;
import com.miauwrijn.gooncraft.managers.StatisticsManager;

public class PenisModel implements Runnable {

    public static final int minSize = 5;
    public static final int maxSize = 30;
    public static final int minGirth = 5;
    public static final int maxGirth = 15;
    
    // Starting ranges for new players (minimum 5, random 5-10)
    private static final int START_SIZE_MIN = 5;
    private static final int START_SIZE_MAX = 10;
    private static final int START_GIRTH_MIN = 5;
    private static final int START_GIRTH_MAX = 10;

    private static final int CUM_ANIMATION_DURATION = 10;
    private static final int BASE_EJACULATE_CHANCE = 50; // 1 in 50 for rank 0
    
    // Cold/stiffness modifiers
    private static final float MIN_STIFFNESS = 0.5f; // 50% size when very cold
    private static final float MAX_STIFFNESS = 1.0f; // 100% size when warm
    private static final float STIFFNESS_TRANSITION_SPEED = 0.02f; // How fast stiffness changes

    private int size;
    private int girth;
    private int viagraBoost;
    private boolean bbc;
    private final Player owner;

    private BlockDisplay leftBall;
    private BlockDisplay rightBall;
    private BlockDisplay shaft;
    private BlockDisplay head;

    private boolean isCumming = false;
    private int framesSinceCumStart = 0;
    private float stretch = 0;
    
    // Temperature-based stiffness (1.0 = full size, 0.5 = shrinkage)
    private float currentStiffness = MAX_STIFFNESS;
    private int coldCheckCounter = 0;

    public PenisModel(Player owner, boolean bbc, int size, int girth, int viagraBoost) {
        this.owner = owner;
        this.bbc = bbc;
        this.size = clamp(size, minSize, maxSize);
        this.girth = clamp(girth, minGirth, maxGirth);
        this.viagraBoost = viagraBoost;
        buildModel();
    }

    /**
     * Get random starting size for new players (5-10 range).
     */
    public static int getRandomSize() {
        return ThreadLocalRandom.current().nextInt(START_SIZE_MIN, START_SIZE_MAX + 1);
    }

    /**
     * Get random starting girth for new players (5-10 range).
     */
    public static int getRandomGirth() {
        return ThreadLocalRandom.current().nextInt(START_GIRTH_MIN, START_GIRTH_MAX + 1);
    }

    public static boolean getRandomBbc() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    public void reload(PenisStatistics stats) {
        this.size = clamp(stats.size, minSize, maxSize);
        this.girth = clamp(stats.girth, minGirth, maxGirth);
        this.bbc = stats.bbc;
        this.viagraBoost = stats.viagraBoost;
        // Rank boosts are applied in setBlockTransformation via effective size
        discard();
        buildModel();
    }

    @Override
    public void run() {
        if (isCumming) {
            animateFap();
        }
        
        // Check temperature and update stiffness every 20 ticks (1 second)
        coldCheckCounter++;
        if (coldCheckCounter >= 20) {
            coldCheckCounter = 0;
            updateStiffness();
        }
        
        updatePosition();
    }
    
    /**
     * Check if player is in a cold area and update stiffness accordingly.
     * Cold areas cause shrinkage (lower stiffness).
     */
    private void updateStiffness() {
        boolean isCold = isPlayerInCold();
        float targetStiffness = isCold ? MIN_STIFFNESS : MAX_STIFFNESS;
        
        // Gradually transition stiffness
        if (Math.abs(currentStiffness - targetStiffness) > 0.01f) {
            if (currentStiffness < targetStiffness) {
                currentStiffness = Math.min(currentStiffness + STIFFNESS_TRANSITION_SPEED, targetStiffness);
            } else {
                currentStiffness = Math.max(currentStiffness - STIFFNESS_TRANSITION_SPEED, targetStiffness);
            }
            
            // Update model with new stiffness
            setBlockTransformation(leftBall, false, false);
            setBlockTransformation(rightBall, false, false);
            setBlockTransformation(shaft, true, false);
            setBlockTransformation(head, false, true);
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
     * Get current stiffness modifier (for external use).
     */
    public float getStiffness() {
        return currentStiffness;
    }
    
    /**
     * Check if the penis is currently affected by cold.
     */
    public boolean isCold() {
        return currentStiffness < MAX_STIFFNESS;
    }

    public void discard() {
        if (leftBall != null) leftBall.remove();
        if (rightBall != null) rightBall.remove();
        if (shaft != null) shaft.remove();
        if (head != null) head.remove();
    }

    public void cum() {
        if (!owner.isSneaking() || isCumming) {
            return;
        }

        int cooldown = ThreadLocalRandom.current().nextInt(2);
        if (CooldownManager.hasCooldown(owner, "goon", cooldown)) {
            return;
        }

        isCumming = true;
        CooldownManager.setCooldown(owner, "goon");

        // Track goon statistic
        StatisticsManager.incrementGoonCount(owner);

        Location location = owner.getLocation();
        World world = location.getWorld();
        List<Player> nearbyPlayers = world.getPlayers();
        
        // Higher rank = more frequent ejaculation
        // Rank 0 (Innocent Virgin) = 1 in 50 chance
        // Rank 11 (Ultimate Degenerate) = 1 in 5 chance
        com.miauwrijn.gooncraft.ranks.BaseRank rank = RankManager.getRank(owner);
        int rankBonus = rank.getOrdinal() * 4; // Each rank reduces the divisor by 4
        int ejaculateChance = Math.max(5, BASE_EJACULATE_CHANCE - rankBonus);
        int ejaculateRoll = ThreadLocalRandom.current().nextInt(ejaculateChance);
        boolean isEjaculating = ejaculateRoll == 0;

        boolean cummedOnSomeone = false;
        for (Player player : nearbyPlayers) {
            double distance = player.getLocation().distance(location);
            if (distance < 10) {
                sendCumMessage(player, distance, isEjaculating);
                // Track if we cummed on someone (within 2 blocks)
                if (isEjaculating && distance < 2 && player != owner) {
                    cummedOnSomeone = true;
                }
            }
        }

        // Only spawn cum particles when ejaculating
        if (isEjaculating) {
            spawnCumParticles();
            // Easter egg: Check for nearby sheep/chickens to cover in white
            AnimalInteractionHandler.checkForAnimals(owner, "white");
            
            // Track solo ejaculation if we didn't cum on anyone
            if (!cummedOnSomeone) {
                StatisticsManager.trackSoloEjaculation(owner);
            }
        }
    }

    private void animateFap() {
        float maxStretch = (size * 0.15f) * 0.03f;
        
        if (framesSinceCumStart >= CUM_ANIMATION_DURATION) {
            isCumming = false;
            stretch = 0;
            framesSinceCumStart = 0;
            return;
        }

        boolean extending = framesSinceCumStart <= CUM_ANIMATION_DURATION / 2;
        float delta = maxStretch / (CUM_ANIMATION_DURATION / 2f);
        stretch += extending ? delta : -delta;

        setBlockTransformation(shaft, true, false);
        setBlockTransformation(head, false, true);
        framesSinceCumStart++;
    }

    private void updatePosition() {
        Location location = owner.getLocation();
        location.setPitch(clamp(location.getPitch(), -10f, 10f));

        float heightOffset = owner.isSneaking() ? 0.55f : 0.65f;
        Location center = location.clone().add(0, heightOffset, 0);
        Vector direction = center.getDirection();

        if (owner.isSneaking()) {
            center = center.subtract(direction.clone().normalize().multiply(0.25));
            direction = center.getDirection();
        }

        Vector perpVector = new Vector(direction.getZ(), 0, -direction.getX());
        center = center.add(direction.clone().multiply(0.075));
        Location ballCenter = center.clone().subtract(new Vector(0, 0.075, 0));

        leftBall.teleport(ballCenter.clone().add(perpVector.clone().multiply(0.075)));
        rightBall.teleport(ballCenter.clone().subtract(perpVector.clone().multiply(0.075)));
        shaft.teleport(center.clone().add(direction.clone().multiply(0.075)));
        head.teleport(center.clone().add(direction.clone().multiply(0.09)));
    }

    private void buildModel() {
        Location location = owner.getLocation();
        World world = owner.getWorld();

        BlockData bodyBlock = (bbc ? Material.BROWN_TERRACOTTA : Material.WHITE_TERRACOTTA).createBlockData();
        BlockData headBlock = Material.TERRACOTTA.createBlockData();

        leftBall = createBlockDisplay(location, bodyBlock, world, false, false);
        rightBall = createBlockDisplay(location, bodyBlock, world, false, false);
        shaft = createBlockDisplay(location, bodyBlock, world, true, false);
        head = createBlockDisplay(location, headBlock, world, false, true);

        updatePosition();
    }

    private BlockDisplay createBlockDisplay(Location location, BlockData block, World world, 
                                            boolean isShaft, boolean isHead) {
        BlockDisplay display = (BlockDisplay) world.spawnEntity(location, EntityType.BLOCK_DISPLAY);
        display.setBlock(block);
        display.setDisplayHeight(0.1f);
        display.setDisplayWidth(0.1f);
        display.setGravity(false);
        display.setInvulnerable(true);
        display.setSilent(true);
        display.setCustomName("GOONCRAFT");
        display.setCustomNameVisible(false);

        setBlockTransformation(display, isShaft, isHead);
        return display;
    }

    private void setBlockTransformation(BlockDisplay block, boolean isShaft, boolean isHead) {
        // Apply stiffness modifier (cold = smaller)
        float stiffnessModifier = currentStiffness;
        
        // Get effective size/girth including all temporary boosts (viagra + rank)
        int effectiveSizeInt = size + viagraBoost;
        int effectiveGirthInt = girth;
        
        if (owner != null) {
            com.miauwrijn.gooncraft.data.PenisStatistics stats = 
                com.miauwrijn.gooncraft.managers.PenisStatisticManager.getStatistics(owner);
            if (stats != null) {
                effectiveSizeInt = stats.getEffectiveSize();
                effectiveGirthInt = stats.getEffectiveGirth();
            }
        }
        
        float g = effectiveGirthInt * 0.01f * stiffnessModifier;
        float effectiveSize = effectiveSizeInt * stiffnessModifier;

        Vector3f scale;
        if (isShaft) {
            scale = new Vector3f(g, g, effectiveSize * 0.03f + stretch);
        } else if (isHead) {
            scale = new Vector3f(g * 0.75f, g * 0.75f, (effectiveSize + 1) * 0.03f + stretch);
        } else {
            // Balls also shrink in cold
            scale = new Vector3f(g, g, g);
        }

        Vector3f translation = new Vector3f(-scale.x * 0.5f, -scale.y * 0.5f, 0f);
        AxisAngle4f rotation = new AxisAngle4f(0, 0, 1, 0);
        
        block.setTransformation(new Transformation(translation, rotation, scale, rotation));
    }

    private void sendCumMessage(Player player, double distance, boolean isEjaculating) {
        if (isEjaculating) {
            if (ConfigManager.showEjaculateMessages()) {
                player.sendMessage("<" + owner.getName() + "> " + ConfigManager.getMessage("goon.orgasm"));
            }
        } else if (distance < 2 && player != owner) {
            if (ConfigManager.showCummedOnMessages()) {
                player.sendMessage(ConfigManager.getMessage("goon.cummed-on", "{player}", owner.getName()));
            }
            // Track statistic for getting cummed on (with unique player tracking)
            StatisticsManager.incrementGotCummedOn(player, owner);
            StatisticsManager.incrementCumOnOthers(owner, player);
        } else {
            if (ConfigManager.showGoonMessages()) {
                player.sendMessage("<" + owner.getName() + "> " + ConfigManager.getMessage("goon.goon"));
            }
        }
    }

    private void spawnCumParticles() {
        Location location = owner.getLocation();
        location.setPitch(clamp(location.getPitch(), -10f, 10f));
        
        Location center = location.clone().add(0, 0.65, 0);
        Vector direction = center.getDirection();
        Location headLocation = head.getLocation().add(direction.multiply((size + 2) * 0.03f));

        World world = owner.getWorld();
        world.spawnParticle(Particle.CLOUD, headLocation, 20, 0.001, 0.001, 0.001, 0.1);
        world.playSound(owner.getLocation(), Sound.ENTITY_LLAMA_SPIT, 1.0f, 2.0f);
        world.playSound(owner.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1.0f, 0.00001f);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
