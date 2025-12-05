package com.miauwrijn.gooncraft;

import java.util.HashMap;

import org.bukkit.entity.Player;

public class CooldownManager {
    private static final HashMap<String, Long> cooldowns = new HashMap<>();

    public static boolean hasCooldown(Player player, String action, long cooldownTime) {
        if (cooldowns.containsKey(player.getName() + action)) {
            long secondsLeft = ((cooldowns.get(player.getName() + action) / 1000) + cooldownTime)
                    - (System.currentTimeMillis() / 1000);
            return secondsLeft > 0;
        }
        return false;
    }

    public static void setCooldown(Player player, String action) {
        cooldowns.put(player.getName() + action, System.currentTimeMillis());
    }
}

