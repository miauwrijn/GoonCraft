package com.miauwrijn.gooncraft.models;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
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
 */
public class BoobModel implements Runnable {

    public static final int minSize = 1;
    public static final int maxSize = 10;
    public static final int minPerkiness = 1;
    public static final int maxPerkiness = 10;

    private static final int JIGGLE_DURATION = 15;

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

    public BoobModel(Player owner, int size, int perkiness) {
        this.owner = owner;
        this.size = clamp(size, minSize, maxSize);
        this.perkiness = clamp(perkiness, minPerkiness, maxPerkiness);
        buildModel();
    }

    public static int getRandomSize() {
        return ThreadLocalRandom.current().nextInt(minSize, maxSize + 1);
    }

    public static int getRandomPerkiness() {
        return ThreadLocalRandom.current().nextInt(minPerkiness, maxPerkiness + 1);
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
        updatePosition();
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
        
        float heightOffset = owner.isSneaking() ? 1.1f : 1.2f;
        // Perkiness affects vertical position (higher perkiness = higher boobs)
        float perkinessOffset = (perkiness - 5) * 0.008f;
        Location center = location.clone().add(0, heightOffset + perkinessOffset, 0);
        Vector direction = center.getDirection();
        direction.setY(0).normalize();

        // Position in front of chest
        Vector perpVector = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
        Location chestCenter = center.clone().add(direction.clone().multiply(0.15));

        float boobSpacing = 0.08f + (size * 0.005f);
        
        Location leftBoobPos = chestCenter.clone().add(perpVector.clone().multiply(boobSpacing));
        Location rightBoobPos = chestCenter.clone().subtract(perpVector.clone().multiply(boobSpacing));
        
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
        
        // Nipple transformations - perkiness affects nipple size slightly
        float nippleSize = 0.015f + (perkiness * 0.002f);
        float nippleYScale = nippleSize * (0.8f + jiggleOffset * 2);
        
        Vector3f nippleScale = new Vector3f(nippleSize, nippleYScale, nippleSize * 1.2f);
        Vector3f nippleTranslation = new Vector3f(-nippleScale.x * 0.5f, -nippleScale.y * 0.5f, 0f);
        
        Transformation nippleTransform = new Transformation(nippleTranslation, rotation, nippleScale, rotation);
        
        if (leftNipple != null) leftNipple.setTransformation(nippleTransform);
        if (rightNipple != null) rightNipple.setTransformation(nippleTransform);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
