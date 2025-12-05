package com.miauwrijn.gooncraft;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

public class CooldownManager {
    
    private static final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    public static boolean hasCooldown(Player player, String action, long cooldownSeconds) {
        String key = createKey(player.getUniqueId(), action);
        Long lastUsed = cooldowns.get(key);
        
        if (lastUsed == null) {
            return false;
        }
        
        long elapsedSeconds = (System.currentTimeMillis() - lastUsed) / 1000;
        return elapsedSeconds < cooldownSeconds;
    }

    public static void setCooldown(Player player, String action) {
        String key = createKey(player.getUniqueId(), action);
        cooldowns.put(key, System.currentTimeMillis());
    }

    public static void clearCooldown(Player player, String action) {
        String key = createKey(player.getUniqueId(), action);
        cooldowns.remove(key);
    }

    private static String createKey(UUID playerId, String action) {
        return playerId.toString() + ":" + action;
    }
}
