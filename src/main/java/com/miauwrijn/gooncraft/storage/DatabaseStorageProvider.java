package com.miauwrijn.gooncraft.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.miauwrijn.gooncraft.Plugin;
import com.miauwrijn.gooncraft.data.PlayerStats;
import com.miauwrijn.gooncraft.managers.GenderManager.Gender;
import com.miauwrijn.gooncraft.models.BoobModel;
import com.miauwrijn.gooncraft.models.PenisModel;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Database-based storage provider using MySQL or PostgreSQL.
 * Uses HikariCP for connection pooling.
 * Great for BungeeCord/Velocity networks where data needs to be shared.
 */
public class DatabaseStorageProvider implements StorageProvider {

    public enum DatabaseType {
        MYSQL,
        POSTGRESQL
    }

    private final DatabaseType type;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String tablePrefix;

    private HikariDataSource dataSource;
    private boolean initialized = false;

    public DatabaseStorageProvider(DatabaseType type, String host, int port, 
                                   String database, String username, String password, 
                                   String tablePrefix) {
        this.type = type;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.tablePrefix = tablePrefix != null ? tablePrefix : "gooncraft_";
    }

    @Override
    public boolean initialize() {
        try {
            HikariConfig config = new HikariConfig();
            
            if (type == DatabaseType.MYSQL) {
                config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + 
                    "?useSSL=false&allowPublicKeyRetrieval=true&autoReconnect=true");
            } else {
                config.setDriverClassName("org.postgresql.Driver");
                config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + database);
            }
            
            config.setUsername(username);
            config.setPassword(password);
            
            // Connection pool settings
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(300000); // 5 minutes
            config.setConnectionTimeout(10000); // 10 seconds
            config.setMaxLifetime(600000); // 10 minutes
            config.setPoolName("GoonCraft-Pool");
            
            // Performance settings
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            
            dataSource = new HikariDataSource(config);
            
            // Create tables
            createTables();
            
            initialized = true;
            Plugin.instance.getLogger().info("Database storage provider initialized (" + type.name() + ")");
            return true;
            
        } catch (Exception e) {
            Plugin.instance.getLogger().log(Level.SEVERE, "Failed to initialize database connection!", e);
            return false;
        }
    }

    @Override
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        initialized = false;
    }

    @Override
    public boolean isConnected() {
        return initialized && dataSource != null && !dataSource.isClosed();
    }

    @Override
    public PlayerData loadPlayerData(UUID uuid) {
        PlayerData data = new PlayerData(uuid);
        
        String sql = "SELECT * FROM " + tablePrefix + "players WHERE uuid = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Penis data
                // If value is 0, initialize with random starting size
                int penisSize = rs.getInt("penis_size");
                data.penisSize = penisSize > 0 ? penisSize : PenisModel.getRandomSize();
                
                int penisGirth = rs.getInt("penis_girth");
                data.penisGirth = penisGirth > 0 ? penisGirth : PenisModel.getRandomGirth();
                
                data.bbc = rs.getBoolean("bbc");
                data.viagraBoost = rs.getInt("viagra_boost");
                
                // Gender data
                String genderStr = rs.getString("gender");
                if (genderStr != null && !genderStr.isEmpty()) {
                    try {
                        data.gender = Gender.valueOf(genderStr);
                    } catch (IllegalArgumentException ignored) {}
                }
                
                // Boob data
                // If value is 0, initialize with random starting size
                int boobSize = rs.getInt("boob_size");
                data.boobSize = boobSize > 0 ? boobSize : BoobModel.getRandomSize();
                
                int boobPerkiness = rs.getInt("boob_perkiness");
                data.boobPerkiness = boobPerkiness > 0 ? boobPerkiness : BoobModel.getRandomPerkiness();
                
                // Load stats
                data.stats = loadStatsFromRow(rs);
                
                // Load achievements
                String achievementsStr = rs.getString("achievements");
                data.unlockedAchievements = parseAchievements(achievementsStr);
            } else {
                // New player - set defaults
                data.penisSize = PenisModel.getRandomSize();
                data.penisGirth = PenisModel.getRandomGirth();
                data.bbc = PenisModel.getRandomBbc();
                data.boobSize = BoobModel.getRandomSize();
                data.boobPerkiness = BoobModel.getRandomPerkiness();
            }
            
        } catch (SQLException e) {
            Plugin.instance.getLogger().log(Level.WARNING, "Failed to load player data for " + uuid, e);
        }
        
        return data;
    }

    @Override
    public CompletableFuture<PlayerData> loadPlayerDataAsync(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> loadPlayerData(uuid));
    }

    @Override
    public boolean savePlayerData(PlayerData data) {
        if (data == null || data.uuid == null) return false;
        
        // Stop exposure timer if active
        if (data.stats != null && data.stats.isExposed) {
            data.stats.stopExposureTimer();
        }
        
        String sql = type == DatabaseType.MYSQL ? getMySQLUpsert() : getPostgreSQLUpsert();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int i = 1;
            stmt.setString(i++, data.uuid.toString());
            stmt.setInt(i++, data.penisSize);
            stmt.setInt(i++, data.penisGirth);
            stmt.setBoolean(i++, data.bbc);
            stmt.setInt(i++, data.viagraBoost);
            stmt.setString(i++, data.gender != null ? data.gender.name() : null);
            stmt.setInt(i++, data.boobSize);
            stmt.setInt(i++, data.boobPerkiness);
            
            // Stats
            PlayerStats stats = data.stats != null ? data.stats : new PlayerStats();
            stmt.setLong(i++, stats.experience);
            stmt.setInt(i++, stats.goonCount);
            stmt.setInt(i++, stats.cumOnOthersCount);
            stmt.setInt(i++, stats.gotCummedOnCount);
            stmt.setLong(i++, stats.totalExposureTime);
            stmt.setInt(i++, stats.buttfingersGiven);
            stmt.setInt(i++, stats.buttfingersReceived);
            stmt.setInt(i++, stats.viagraUsed);
            stmt.setInt(i++, stats.fartCount);
            stmt.setInt(i++, stats.poopCount);
            stmt.setInt(i++, stats.pissCount);
            stmt.setInt(i++, stats.jiggleCount);
            stmt.setInt(i++, stats.boobToggleCount);
            stmt.setInt(i++, stats.genderChanges);
            stmt.setInt(i++, stats.deathsWhileExposed);
            stmt.setInt(i++, stats.damageWhileGooning);
            stmt.setInt(i++, stats.goonsWhileFalling);
            stmt.setInt(i++, stats.goonsWhileOnFire);
            stmt.setInt(i++, stats.creeperDeathsWhileExposed);
            stmt.setBoolean(i++, stats.goonedInNether);
            stmt.setBoolean(i++, stats.goonedInEnd);
            stmt.setBoolean(i++, stats.goonedUnderwater);
            stmt.setBoolean(i++, stats.goonedInDesert);
            stmt.setBoolean(i++, stats.goonedInSnow);
            stmt.setBoolean(i++, stats.goonedHighAltitude);
            stmt.setInt(i++, stats.maxGoonsInMinute);
            stmt.setInt(i++, stats.ejaculationsIn30Seconds);
            stmt.setInt(i++, stats.pigsAffected);
            stmt.setInt(i++, stats.cowsAffected);
            stmt.setInt(i++, stats.wolvesAffected);
            stmt.setInt(i++, stats.catsAffected);
            
            // Unique player sets (stored as comma-separated UUIDs)
            stmt.setString(i++, serializeUUIDSet(stats.uniquePlayersCummedOn));
            stmt.setString(i++, serializeUUIDSet(stats.uniquePlayersGotCummedBy));
            stmt.setString(i++, serializeUUIDSet(stats.uniquePlayersButtfingered));
            stmt.setString(i++, serializeUUIDSet(stats.uniquePlayersPissedNear));
            stmt.setString(i++, serializeUUIDSet(stats.uniquePlayersFartedNear));
            
            // Mob goon counts
            stmt.setString(i++, serializeMobGoonCounts(stats.mobGoonCounts));
            
            // Achievements
            stmt.setString(i++, serializeAchievements(data.unlockedAchievements));
            
            stmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            Plugin.instance.getLogger().log(Level.WARNING, "Failed to save player data for " + data.uuid, e);
            return false;
        }
    }

    @Override
    public CompletableFuture<Boolean> savePlayerDataAsync(PlayerData data) {
        return CompletableFuture.supplyAsync(() -> savePlayerData(data));
    }

    @Override
    public boolean deletePlayerData(UUID uuid) {
        String sql = "DELETE FROM " + tablePrefix + "players WHERE uuid = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            Plugin.instance.getLogger().log(Level.WARNING, "Failed to delete player data for " + uuid, e);
            return false;
        }
    }

    @Override
    public boolean hasPlayerData(UUID uuid) {
        String sql = "SELECT 1 FROM " + tablePrefix + "players WHERE uuid = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
            
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return type == DatabaseType.MYSQL ? "MySQL" : "PostgreSQL";
    }

    // ===== Private Helper Methods =====

    private void createTables() throws SQLException {
        String createTable = """
            CREATE TABLE IF NOT EXISTS %splayers (
                uuid VARCHAR(36) PRIMARY KEY,
                penis_size INT DEFAULT 15,
                penis_girth INT DEFAULT 8,
                bbc BOOLEAN DEFAULT FALSE,
                viagra_boost INT DEFAULT 0,
                gender VARCHAR(10),
                boob_size INT DEFAULT 5,
                boob_perkiness INT DEFAULT 5,
                experience BIGINT DEFAULT 0,
                goon_count INT DEFAULT 0,
                cum_on_others INT DEFAULT 0,
                got_cummed_on INT DEFAULT 0,
                exposure_time BIGINT DEFAULT 0,
                buttfingers_given INT DEFAULT 0,
                buttfingers_received INT DEFAULT 0,
                viagra_used INT DEFAULT 0,
                fart_count INT DEFAULT 0,
                poop_count INT DEFAULT 0,
                piss_count INT DEFAULT 0,
                jiggle_count INT DEFAULT 0,
                boob_toggle_count INT DEFAULT 0,
                gender_changes INT DEFAULT 0,
                deaths_exposed INT DEFAULT 0,
                damage_gooning INT DEFAULT 0,
                goons_falling INT DEFAULT 0,
                goons_on_fire INT DEFAULT 0,
                creeper_deaths INT DEFAULT 0,
                gooned_nether BOOLEAN DEFAULT FALSE,
                gooned_end BOOLEAN DEFAULT FALSE,
                gooned_underwater BOOLEAN DEFAULT FALSE,
                gooned_desert BOOLEAN DEFAULT FALSE,
                gooned_snow BOOLEAN DEFAULT FALSE,
                gooned_altitude BOOLEAN DEFAULT FALSE,
                max_goons_minute INT DEFAULT 0,
                max_ejaculations_30s INT DEFAULT 0,
                pigs_affected INT DEFAULT 0,
                cows_affected INT DEFAULT 0,
                wolves_affected INT DEFAULT 0,
                cats_affected INT DEFAULT 0,
                unique_cummed_on TEXT,
                unique_got_cummed_by TEXT,
                unique_buttfingered TEXT,
                unique_pissed_near TEXT,
                unique_farted_near TEXT,
                mob_goon_counts TEXT,
                achievements TEXT,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """.formatted(tablePrefix);
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(createTable)) {
            stmt.executeUpdate();
        }
        
        // Add new columns if they don't exist (for existing databases)
        addColumnIfNotExists("experience", "BIGINT DEFAULT 0");
        addColumnIfNotExists("mob_goon_counts", "TEXT");
    }
    
    private void addColumnIfNotExists(String columnName, String columnDef) {
        String checkSql;
        if (type == DatabaseType.MYSQL) {
            checkSql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = '" + 
                       tablePrefix + "players' AND column_name = '" + columnName + "'";
        } else {
            checkSql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = '" + 
                       tablePrefix + "players' AND column_name = '" + columnName + "'";
        }
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             ResultSet rs = checkStmt.executeQuery()) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                // Column doesn't exist, add it
                String alterSql = "ALTER TABLE " + tablePrefix + "players ADD COLUMN " + columnName + " " + columnDef;
                try (PreparedStatement alterStmt = conn.prepareStatement(alterSql)) {
                    alterStmt.executeUpdate();
                    Plugin.instance.getLogger().info("Added new column '" + columnName + "' to database.");
                }
            }
        } catch (SQLException e) {
            // Silently ignore - column might already exist
        }
    }

    private String getMySQLUpsert() {
        return """
            INSERT INTO %splayers (
                uuid, penis_size, penis_girth, bbc, viagra_boost, gender, boob_size, boob_perkiness,
                experience, goon_count, cum_on_others, got_cummed_on, exposure_time,
                buttfingers_given, buttfingers_received, viagra_used,
                fart_count, poop_count, piss_count, jiggle_count, boob_toggle_count, gender_changes,
                deaths_exposed, damage_gooning, goons_falling, goons_on_fire, creeper_deaths,
                gooned_nether, gooned_end, gooned_underwater, gooned_desert, gooned_snow, gooned_altitude,
                max_goons_minute, max_ejaculations_30s, pigs_affected, cows_affected, wolves_affected, cats_affected,
                unique_cummed_on, unique_got_cummed_by, unique_buttfingered, unique_pissed_near, unique_farted_near,
                mob_goon_counts, achievements
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                penis_size = VALUES(penis_size), penis_girth = VALUES(penis_girth), bbc = VALUES(bbc),
                viagra_boost = VALUES(viagra_boost), gender = VALUES(gender), boob_size = VALUES(boob_size),
                boob_perkiness = VALUES(boob_perkiness), experience = VALUES(experience), goon_count = VALUES(goon_count),
                cum_on_others = VALUES(cum_on_others), got_cummed_on = VALUES(got_cummed_on),
                exposure_time = VALUES(exposure_time), buttfingers_given = VALUES(buttfingers_given),
                buttfingers_received = VALUES(buttfingers_received), viagra_used = VALUES(viagra_used),
                fart_count = VALUES(fart_count), poop_count = VALUES(poop_count), piss_count = VALUES(piss_count),
                jiggle_count = VALUES(jiggle_count), boob_toggle_count = VALUES(boob_toggle_count),
                gender_changes = VALUES(gender_changes), deaths_exposed = VALUES(deaths_exposed),
                damage_gooning = VALUES(damage_gooning), goons_falling = VALUES(goons_falling),
                goons_on_fire = VALUES(goons_on_fire), creeper_deaths = VALUES(creeper_deaths),
                gooned_nether = VALUES(gooned_nether), gooned_end = VALUES(gooned_end),
                gooned_underwater = VALUES(gooned_underwater), gooned_desert = VALUES(gooned_desert),
                gooned_snow = VALUES(gooned_snow), gooned_altitude = VALUES(gooned_altitude),
                max_goons_minute = VALUES(max_goons_minute), max_ejaculations_30s = VALUES(max_ejaculations_30s),
                pigs_affected = VALUES(pigs_affected), cows_affected = VALUES(cows_affected),
                wolves_affected = VALUES(wolves_affected), cats_affected = VALUES(cats_affected),
                unique_cummed_on = VALUES(unique_cummed_on), unique_got_cummed_by = VALUES(unique_got_cummed_by),
                unique_buttfingered = VALUES(unique_buttfingered), unique_pissed_near = VALUES(unique_pissed_near),
                unique_farted_near = VALUES(unique_farted_near), mob_goon_counts = VALUES(mob_goon_counts),
                achievements = VALUES(achievements), updated_at = CURRENT_TIMESTAMP
            """.formatted(tablePrefix);
    }

    private String getPostgreSQLUpsert() {
        return """
            INSERT INTO %splayers (
                uuid, penis_size, penis_girth, bbc, viagra_boost, gender, boob_size, boob_perkiness,
                experience, goon_count, cum_on_others, got_cummed_on, exposure_time,
                buttfingers_given, buttfingers_received, viagra_used,
                fart_count, poop_count, piss_count, jiggle_count, boob_toggle_count, gender_changes,
                deaths_exposed, damage_gooning, goons_falling, goons_on_fire, creeper_deaths,
                gooned_nether, gooned_end, gooned_underwater, gooned_desert, gooned_snow, gooned_altitude,
                max_goons_minute, max_ejaculations_30s, pigs_affected, cows_affected, wolves_affected, cats_affected,
                unique_cummed_on, unique_got_cummed_by, unique_buttfingered, unique_pissed_near, unique_farted_near,
                mob_goon_counts, achievements
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (uuid) DO UPDATE SET
                penis_size = EXCLUDED.penis_size, penis_girth = EXCLUDED.penis_girth, bbc = EXCLUDED.bbc,
                viagra_boost = EXCLUDED.viagra_boost, gender = EXCLUDED.gender, boob_size = EXCLUDED.boob_size,
                boob_perkiness = EXCLUDED.boob_perkiness, experience = EXCLUDED.experience, goon_count = EXCLUDED.goon_count,
                cum_on_others = EXCLUDED.cum_on_others, got_cummed_on = EXCLUDED.got_cummed_on,
                exposure_time = EXCLUDED.exposure_time, buttfingers_given = EXCLUDED.buttfingers_given,
                buttfingers_received = EXCLUDED.buttfingers_received, viagra_used = EXCLUDED.viagra_used,
                fart_count = EXCLUDED.fart_count, poop_count = EXCLUDED.poop_count, piss_count = EXCLUDED.piss_count,
                jiggle_count = EXCLUDED.jiggle_count, boob_toggle_count = EXCLUDED.boob_toggle_count,
                gender_changes = EXCLUDED.gender_changes, deaths_exposed = EXCLUDED.deaths_exposed,
                damage_gooning = EXCLUDED.damage_gooning, goons_falling = EXCLUDED.goons_falling,
                goons_on_fire = EXCLUDED.goons_on_fire, creeper_deaths = EXCLUDED.creeper_deaths,
                gooned_nether = EXCLUDED.gooned_nether, gooned_end = EXCLUDED.gooned_end,
                gooned_underwater = EXCLUDED.gooned_underwater, gooned_desert = EXCLUDED.gooned_desert,
                gooned_snow = EXCLUDED.gooned_snow, gooned_altitude = EXCLUDED.gooned_altitude,
                max_goons_minute = EXCLUDED.max_goons_minute, max_ejaculations_30s = EXCLUDED.max_ejaculations_30s,
                pigs_affected = EXCLUDED.pigs_affected, cows_affected = EXCLUDED.cows_affected,
                wolves_affected = EXCLUDED.wolves_affected, cats_affected = EXCLUDED.cats_affected,
                unique_cummed_on = EXCLUDED.unique_cummed_on, unique_got_cummed_by = EXCLUDED.unique_got_cummed_by,
                unique_buttfingered = EXCLUDED.unique_buttfingered, unique_pissed_near = EXCLUDED.unique_pissed_near,
                unique_farted_near = EXCLUDED.unique_farted_near, mob_goon_counts = EXCLUDED.mob_goon_counts,
                achievements = EXCLUDED.achievements, updated_at = CURRENT_TIMESTAMP
            """.formatted(tablePrefix);
    }

    private PlayerStats loadStatsFromRow(ResultSet rs) throws SQLException {
        PlayerStats stats = new PlayerStats();
        
        // Experience points
        try {
            stats.experience = rs.getLong("experience");
        } catch (SQLException e) {
            stats.experience = 0; // Column might not exist yet
        }
        
        stats.goonCount = rs.getInt("goon_count");
        stats.cumOnOthersCount = rs.getInt("cum_on_others");
        stats.gotCummedOnCount = rs.getInt("got_cummed_on");
        stats.totalExposureTime = rs.getLong("exposure_time");
        stats.buttfingersGiven = rs.getInt("buttfingers_given");
        stats.buttfingersReceived = rs.getInt("buttfingers_received");
        stats.viagraUsed = rs.getInt("viagra_used");
        stats.fartCount = rs.getInt("fart_count");
        stats.poopCount = rs.getInt("poop_count");
        stats.pissCount = rs.getInt("piss_count");
        stats.jiggleCount = rs.getInt("jiggle_count");
        stats.boobToggleCount = rs.getInt("boob_toggle_count");
        stats.genderChanges = rs.getInt("gender_changes");
        stats.deathsWhileExposed = rs.getInt("deaths_exposed");
        stats.damageWhileGooning = rs.getInt("damage_gooning");
        stats.goonsWhileFalling = rs.getInt("goons_falling");
        stats.goonsWhileOnFire = rs.getInt("goons_on_fire");
        stats.creeperDeathsWhileExposed = rs.getInt("creeper_deaths");
        stats.goonedInNether = rs.getBoolean("gooned_nether");
        stats.goonedInEnd = rs.getBoolean("gooned_end");
        stats.goonedUnderwater = rs.getBoolean("gooned_underwater");
        stats.goonedInDesert = rs.getBoolean("gooned_desert");
        stats.goonedInSnow = rs.getBoolean("gooned_snow");
        stats.goonedHighAltitude = rs.getBoolean("gooned_altitude");
        stats.maxGoonsInMinute = rs.getInt("max_goons_minute");
        stats.ejaculationsIn30Seconds = rs.getInt("max_ejaculations_30s");
        stats.pigsAffected = rs.getInt("pigs_affected");
        stats.cowsAffected = rs.getInt("cows_affected");
        stats.wolvesAffected = rs.getInt("wolves_affected");
        stats.catsAffected = rs.getInt("cats_affected");
        
        // Parse UUID sets
        stats.uniquePlayersCummedOn = parseUUIDSet(rs.getString("unique_cummed_on"));
        stats.uniquePlayersGotCummedBy = parseUUIDSet(rs.getString("unique_got_cummed_by"));
        stats.uniquePlayersButtfingered = parseUUIDSet(rs.getString("unique_buttfingered"));
        stats.uniquePlayersPissedNear = parseUUIDSet(rs.getString("unique_pissed_near"));
        stats.uniquePlayersFartedNear = parseUUIDSet(rs.getString("unique_farted_near"));
        
        // Parse mob goon counts
        try {
            String mobGoonStr = rs.getString("mob_goon_counts");
            stats.mobGoonCounts = parseMobGoonCounts(mobGoonStr);
        } catch (SQLException e) {
            // Column might not exist yet
        }
        
        return stats;
    }
    
    private java.util.Map<String, Integer> parseMobGoonCounts(String str) {
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        if (str == null || str.isEmpty()) return map;
        
        // Format: "mob_type:count,mob_type:count,..."
        for (String entry : str.split(",")) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                try {
                    map.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                } catch (NumberFormatException ignored) {}
            }
        }
        return map;
    }
    
    private String serializeMobGoonCounts(java.util.Map<String, Integer> map) {
        if (map == null || map.isEmpty()) return "";
        
        StringBuilder sb = new StringBuilder();
        for (var entry : map.entrySet()) {
            if (sb.length() > 0) sb.append(",");
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        return sb.toString();
    }

    private String serializeUUIDSet(Set<UUID> set) {
        if (set == null || set.isEmpty()) return "";
        return set.stream().map(UUID::toString).collect(Collectors.joining(","));
    }

    private Set<UUID> parseUUIDSet(String str) {
        Set<UUID> set = new HashSet<>();
        if (str == null || str.isEmpty()) return set;
        
        for (String uuidStr : str.split(",")) {
            try {
                set.add(UUID.fromString(uuidStr.trim()));
            } catch (IllegalArgumentException ignored) {}
        }
        return set;
    }

    private String serializeAchievements(Set<String> achievements) {
        if (achievements == null || achievements.isEmpty()) return "";
        // Store as lowercase achievement IDs (matching file format)
        return achievements.stream()
            .map(String::toLowerCase)
            .collect(Collectors.joining(","));
    }

    private Set<String> parseAchievements(String str) {
        Set<String> set = new HashSet<>();
        if (str == null || str.isEmpty()) return set;
        
        for (String id : str.split(",")) {
            String trimmed = id.trim().toLowerCase();
            if (!trimmed.isEmpty()) {
                set.add(trimmed);
            }
        }
        return set;
    }
}
