package com.miauwrijn.gooncraft.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.miauwrijn.gooncraft.managers.SkillPointsManager;

/**
 * GUI for spending skill points on funny perks.
 */
public class SkillPointsGUI extends GUI {

    private final Player target;
    private int page = 0;

    public SkillPointsGUI(Player viewer, Player target) {
        super(viewer, "§6§l✦ Skill Points Shop ✦", 6);
        this.target = target;
        render();
    }

    private void render() {
        inventory.clear();
        clickHandlers.clear();
        
        int skillPoints = SkillPointsManager.getSkillPoints(target);
        
        // Top border
        fillBorder(ItemBuilder.filler(Material.ORANGE_STAINED_GLASS_PANE));
        
        // Info header
        setItem(slot(0, 4), new ItemBuilder(Material.EMERALD)
                .name("§a§lYour Skill Points: §e" + skillPoints)
                .lore(
                    "",
                    "§7Earn skill points by ranking up!",
                    "§7Spend them on hilarious perks below.",
                    "",
                    "§8Click perks to purchase"
                )
                .build());
        
        // Display perks
        int slotIndex = 10;
        Perk[] allPerks = Perk.values();
        int startIndex = page * 28;
        
        for (int i = startIndex; i < Math.min(startIndex + 28, allPerks.length); i++) {
            Perk perk = allPerks[i];
            boolean hasPerk = SkillPointsManager.hasPerk(target, perk.id);
            boolean canAfford = skillPoints >= perk.cost;
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7" + perk.description);
            lore.add("");
            
            if (hasPerk) {
                lore.add("§a§l✓ PURCHASED");
            } else {
                lore.add("§7Cost: §e" + perk.cost + " Skill Point" + (perk.cost > 1 ? "s" : ""));
                if (!canAfford) {
                    lore.add("§cYou need " + (perk.cost - skillPoints) + " more point" + ((perk.cost - skillPoints) > 1 ? "s" : ""));
                }
            }
            
            lore.add("");
            if (!perk.effects.isEmpty()) {
                lore.add("§aEffects:");
                for (String effect : perk.effects) {
                    lore.add("§a  • " + effect);
                }
            }
            
            Material material = hasPerk ? Material.EMERALD_BLOCK : (canAfford ? Material.GOLD_INGOT : Material.IRON_INGOT);
            
            ItemBuilder builder = new ItemBuilder(material)
                    .name(perk.icon + " §f§l" + perk.name)
                    .lore(lore);
            
            if (hasPerk) {
                builder.glow();
            }
            
            if (!hasPerk && canAfford) {
                final Perk finalPerk = perk;
                setItem(slotIndex, builder.build(), event -> {
                    if (SkillPointsManager.purchasePerk(target, finalPerk.id, finalPerk.cost)) {
                        viewer.sendMessage("§a§l✓ Purchased: " + finalPerk.name);
                        render(); // Refresh GUI
                    } else {
                        viewer.sendMessage("§cFailed to purchase perk!");
                    }
                });
            } else {
                setItem(slotIndex, builder.build());
            }
            
            slotIndex++;
            // Skip border slots
            if ((slotIndex + 1) % 9 == 0) {
                slotIndex += 2;
            }
            if (slotIndex >= 44) break;
        }
        
        // Fill empty slots
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 7; col++) {
                int s = slot(row, col);
                if (inventory.getItem(s) == null) {
                    setItem(s, ItemBuilder.filler(Material.BLACK_STAINED_GLASS_PANE));
                }
            }
        }
        
        // Bottom navigation
        int totalPages = (int) Math.ceil(allPerks.length / 28.0);
        
        // Back button
        setItem(slot(5, 0), new ItemBuilder(Material.ARROW)
                .name("§c§l← Back")
                .lore("§7Return to stats")
                .build(),
                event -> new StatsGUI(viewer, target).open());
        
        // Previous page
        if (page > 0) {
            setItem(slot(5, 3), new ItemBuilder(Material.ARROW)
                    .name("§e§lPrevious Page")
                    .lore("§7Page " + page + "/" + (totalPages - 1))
                    .build(),
                    event -> {
                        page--;
                        render();
                    });
        }
        
        // Page indicator
        setItem(slot(5, 4), new ItemBuilder(Material.BOOK)
                .name("§6§lPage " + (page + 1) + "/" + Math.max(1, totalPages))
                .lore(
                    "",
                    "§7You have: §e" + skillPoints + " Skill Point" + (skillPoints != 1 ? "s" : ""),
                    "",
                    "§8Earn more by ranking up!"
                )
                .build());
        
        // Next page
        if ((page + 1) * 28 < allPerks.length) {
            setItem(slot(5, 5), new ItemBuilder(Material.ARROW)
                    .name("§e§lNext Page")
                    .lore("§7Page " + (page + 2) + "/" + totalPages)
                    .build(),
                    event -> {
                        page++;
                        render();
                    });
        }
        
        // Close button
        setItem(slot(5, 8), new ItemBuilder(Material.BARRIER)
                .name("§c§lClose")
                .lore("§7Click to close")
                .build(),
                event -> viewer.closeInventory());
    }
    
    /**
     * Available perks that can be purchased with skill points.
     */
    public enum Perk {
        // Size & Enhancement Perks
        GROWTH_SPURT("growth_spurt", "Growth Spurt", "💪", 2,
            "Permanently increase your size by 2cm!",
            new String[]{"+2cm permanent size boost"}),
        
        GIRTH_MASTER("girth_master", "Girth Master", "🍆", 3,
            "Add some extra thickness! Permanent +1cm girth.",
            new String[]{"+1cm permanent girth boost"}),
        
        BBC_UPGRADE("bbc_upgrade", "BBC Upgrade", "⭐", 5,
            "Unlock the legendary Big Block Construct status!",
            new String[]{"Unlocks BBC status"}),
        
        // Cooldown & Speed Perks
        FAST_HANDS("fast_hands", "Fast Hands", "⚡", 2,
            "Reduce all cooldowns by 20%!",
            new String[]{"20% faster cooldowns"}),
        
        RAPID_FIRE("rapid_fire", "Rapid Fire", "🔥", 4,
            "Goon 50% faster! More actions, more fun!",
            new String[]{"50% faster goon speed"}),
        
        // Ejaculation Perks
        CUM_BOOST("cum_boost", "Cum Boost", "💦", 3,
            "15% higher chance to ejaculate!",
            new String[]{"+15% ejaculation chance"}),
        
        FIRE_HOSE("fire_hose", "Fire Hose", "🚿", 5,
            "When you cum, you REALLY cum! Bigger particle effects!",
            new String[]{"3x bigger cum particles"}),
        
        // Special Effect Perks
        GLOWING("glowing", "Glowing", "✨", 2,
            "Glow in the dark when exposed! Show off that body!",
            new String[]{"Glow effect when genitals shown"}),
        
        RAINBOW_MODE("rainbow_mode", "Rainbow Mode", "🌈", 4,
            "Your equipment cycles through rainbow colors!",
            new String[]{"Rainbow color effect"}),
        
        // Boob Perks (for applicable genders)
        BOOB_UPGRADE("boob_upgrade", "Boob Upgrade", "🍈", 3,
            "Permanently increase boob size by one cup!",
            new String[]{"+1 cup size permanent boost"}),
        
        PERKY_BOOST("perky_boost", "Perky Boost", "📈", 2,
            "Increase perkiness by 2 points!",
            new String[]{"+2 permanent perkiness"}),
        
        // Bodily Function Perks
        GAS_GUZZLER("gas_guzzler", "Gas Guzzler", "💨", 1,
            "Fart 50% more often! The stink is real!",
            new String[]{"50% faster fart cooldown"}),
        
        PLUMBING_MASTER("plumbing_master", "Plumbing Master", "🚰", 2,
            "Piss with 2x the range! Mark your territory!",
            new String[]{"2x piss range and duration"}),
        
        // Social Perks
        MAGNET("magnet", "Cum Magnet", "🧲", 3,
            "Others are 25% more likely to cum on you!",
            new String[]{"+25% chance to get cummed on"}),
        
        SPRAY_MASTER("spray_master", "Spray Master", "🎯", 4,
            "Your cum reaches 2x further! Hit targets from afar!",
            new String[]{"2x cum range"}),
        
        // Exhibitionist Perks
        EXHIBITIONIST("exhibitionist", "Exhibitionist", "👁️", 3,
            "Gain double exposure time! Show off more!",
            new String[]{"2x exposure time multiplier"}),
        
        PUBLIC_DISPLAY("public_display", "Public Display", "📺", 5,
            "When exposed, all nearby players get a notification!",
            new String[]{"Broadcast when you expose yourself"}),
        
        // Special Perks
        GENDER_FLUID("gender_fluid", "Gender Fluid", "🌊", 2,
            "Change gender with no cooldown!",
            new String[]{"Remove gender change cooldown"}),
        
        VIAGRA_PLUS("viagra_plus", "Viagra Plus", "💊", 3,
            "Viagra effects last 2x longer!",
            new String[]{"2x viagra duration"}),
        
        BUTTFINGER_PRO("buttfinger_pro", "Buttfinger Pro", "👆", 2,
            "Buttfinger cooldown reduced by 50%!",
            new String[]{"50% faster buttfinger cooldown"}),
        
        // Ultimate Perks
        GOD_MODE("god_mode", "God Mode", "👑", 10,
            "ALL cooldowns reduced by 30%! You're unstoppable!",
            new String[]{"30% faster ALL cooldowns", "+10% ejaculation chance", "Glow effect"}),
        
        LEGENDARY("legendary", "Legendary Status", "🌟", 15,
            "The ultimate perk! Permanent +5cm size, +2cm girth, and ALL bonuses!",
            new String[]{"+5cm permanent size", "+2cm permanent girth", "All other perks active"});
        
        public final String id;
        public final String name;
        public final String icon;
        public final int cost;
        public final String description;
        public final List<String> effects;
        
        Perk(String id, String name, String icon, int cost, String description, String[] effects) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.cost = cost;
            this.description = description;
            this.effects = java.util.Arrays.asList(effects);
        }
    }
}
