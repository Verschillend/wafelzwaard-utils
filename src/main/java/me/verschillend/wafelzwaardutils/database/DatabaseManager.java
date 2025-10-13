package me.verschillend.wafelzwaardutils.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        setupDatabase();
        createTables();
        updateTables();
    }

    private void setupDatabase() {
        try {
            HikariConfig config = new HikariConfig();

            String host = plugin.getConfig().getString("database.host", "192.168.1.242");
            int port = plugin.getConfig().getInt("database.port", 3306);
            String database = plugin.getConfig().getString("database.name", "wafelz");
            String username = plugin.getConfig().getString("database.username", "luckperms");
            String password = plugin.getConfig().getString("database.password", "");

            // Log the configuration
            plugin.getLogger().info("Connecting to database:");
            plugin.getLogger().info("Host: " + host);
            plugin.getLogger().info("Port: " + port);
            plugin.getLogger().info("Database: " + database);
            plugin.getLogger().info("Username: " + username);
            plugin.getLogger().info("Password: " + (password.isEmpty() ? "NO" : "YES"));

            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true");
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to setup database connection: " + e.getMessage());
            throw e;
        }
    }

    private void createTables() {
        String playerDataSql = """
        CREATE TABLE IF NOT EXISTS player_data (
            uuid VARCHAR(36) PRIMARY KEY,
            player_name VARCHAR(16) NOT NULL,
            color CHAR DEFAULT 7,
            gems DOUBLE DEFAULT 0.0,
            referral_code VARCHAR(8) UNIQUE,
            referred_by VARCHAR(36),
            referral_count INT DEFAULT 0,
            last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        )
        """;

        String referralsSql = """
        CREATE TABLE IF NOT EXISTS referrals (
            id INT AUTO_INCREMENT PRIMARY KEY,
            referrer_uuid VARCHAR(36) NOT NULL,
            referred_uuid VARCHAR(36) NOT NULL,
            referral_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (referrer_uuid) REFERENCES player_data(uuid),
            FOREIGN KEY (referred_uuid) REFERENCES player_data(uuid),
            UNIQUE(referred_uuid)
        )
        """;

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(playerDataSql)) {
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(referralsSql)) {
                stmt.executeUpdate();
            }
            plugin.getLogger().info("Database tables created successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create database tables: " + e.getMessage());
        }
    }

    private void updateTables() {
        // Add missing columns if they don't exist
        String[] alterStatements = {
                "ALTER TABLE player_data ADD COLUMN IF NOT EXISTS gems DOUBLE DEFAULT 0.0",
                "ALTER TABLE player_data ADD COLUMN IF NOT EXISTS referral_code VARCHAR(8) UNIQUE",
                "ALTER TABLE player_data ADD COLUMN IF NOT EXISTS referred_by VARCHAR(36)",
                "ALTER TABLE player_data ADD COLUMN IF NOT EXISTS referral_count INT DEFAULT 0"
        };

        try (Connection conn = dataSource.getConnection()) {
            for (String sql : alterStatements) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.executeUpdate();
                    plugin.getLogger().info("Updated table structure: " + sql);
                } catch (SQLException e) {
                    // Column might already exist, this is usually fine
                    if (!e.getMessage().contains("Duplicate column")) {
                        plugin.getLogger().warning("Failed to update table: " + e.getMessage());
                    }
                }
            }
            plugin.getLogger().info("Table structure update completed!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update tables: " + e.getMessage());
        }
    }

    //async method to save player data
    public CompletableFuture<Void> savePlayerData(UUID uuid, String playerName, char color, double gems) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
            INSERT INTO player_data (uuid, player_name, color, gems) 
            VALUES (?, ?, ?, ?) 
            ON DUPLICATE KEY UPDATE player_name = VALUES(player_name), color = VALUES(color), gems = VALUES(gems)
            """;

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, playerName);
                stmt.setString(3, String.valueOf(color));
                stmt.setDouble(4, gems);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Character> getPlayerColor(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT color FROM player_data WHERE uuid = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String colorStr = rs.getString("color");
                        return (colorStr != null && !colorStr.isEmpty()) ? colorStr.charAt(0) : '7';
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get player color: " + e.getMessage());
            }
            return '7'; // Default color
        });
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public CompletableFuture<Double> getPlayerGems(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT gems FROM player_data WHERE uuid = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("gems");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get player gems: " + e.getMessage());
            }
            return 0.0;
        });
    }

    public CompletableFuture<Void> setPlayerGems(UUID uuid, String playerName, double gems) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
            INSERT INTO player_data (uuid, player_name, gems) 
            VALUES (?, ?, ?) 
            ON DUPLICATE KEY UPDATE player_name = VALUES(player_name), gems = VALUES(gems)
            """;

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, playerName);
                stmt.setDouble(3, gems);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to set player gems: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Double> addPlayerGems(UUID uuid, String playerName, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
            INSERT INTO player_data (uuid, player_name, gems) 
            VALUES (?, ?, ?) 
            ON DUPLICATE KEY UPDATE player_name = VALUES(player_name), gems = gems + VALUES(gems)
            """;

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, playerName);
                stmt.setDouble(3, amount);
                stmt.executeUpdate();

                //return the new amount
                return getPlayerGems(uuid).join();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to add player gems: " + e.getMessage());
            }
            return 0.0;
        });
    }

    public CompletableFuture<Double> removePlayerGems(UUID uuid, String playerName, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
            INSERT INTO player_data (uuid, player_name, gems) 
            VALUES (?, ?, ?) 
            ON DUPLICATE KEY UPDATE player_name = VALUES(player_name), gems = GREATEST(0, gems - VALUES(gems))
            """;

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, playerName);
                stmt.setDouble(3, amount);
                stmt.executeUpdate();

                //return the new amount
                return getPlayerGems(uuid).join();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to remove player gems: " + e.getMessage());
            }
            return 0.0;
        });
    }

    public CompletableFuture<Boolean> hasEnoughGems(UUID uuid, double amount) {
        return getPlayerGems(uuid).thenApply(gems -> gems >= amount);
    }

    public CompletableFuture<String> generateReferralCode(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String code;
            int attempts = 0;
            do {
                code = generateRandomCode();
                attempts++;
                if (attempts > 100) {
                    plugin.getLogger().severe("Failed to generate unique referral code after 100 attempts");
                    return null;
                }
            } while (referralCodeExists(code));

            //update player's referral code
            String sql = "UPDATE player_data SET referral_code = ? WHERE uuid = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, code);
                stmt.setString(2, playerUuid.toString());
                stmt.executeUpdate();
                return code;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to set referral code: " + e.getMessage());
                return null;
            }
        });
    }

    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    private boolean referralCodeExists(String code) {
        String sql = "SELECT 1 FROM player_data WHERE referral_code = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to check referral code: " + e.getMessage());
            return true; // Assume it exists to be safe
        }
    }

    public CompletableFuture<String> getPlayerReferralCode(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT referral_code FROM player_data WHERE uuid = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("referral_code");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get referral code: " + e.getMessage());
            }
            return null;
        });
    }

    public CompletableFuture<String> getPlayerByReferralCode(String code) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT uuid FROM player_data WHERE referral_code = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, code.toUpperCase());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("uuid");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get player by referral code: " + e.getMessage());
            }
            return null;
        });
    }

    public CompletableFuture<Boolean> wasPlayerReferred(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT referred_by FROM player_data WHERE uuid = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("referred_by") != null;
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to check if player was referred: " + e.getMessage());
            }
            return false;
        });
    }

    public CompletableFuture<Boolean> processReferral(UUID referredUuid, String referralCode) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                conn.setAutoCommit(false);

                // Get referrer UUID
                String getReferrerSql = "SELECT uuid FROM player_data WHERE referral_code = ?";
                String referrerUuid = null;
                try (PreparedStatement stmt = conn.prepareStatement(getReferrerSql)) {
                    stmt.setString(1, referralCode.toUpperCase());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            referrerUuid = rs.getString("uuid");
                        }
                    }
                }

                if (referrerUuid == null) {
                    conn.rollback();
                    return false; // Invalid referral code
                }

                if (referrerUuid.equals(referredUuid.toString())) {
                    conn.rollback();
                    return false; // Can't refer yourself
                }

                // Check if referred player was already referred
                String checkReferredSql = "SELECT referred_by FROM player_data WHERE uuid = ?";
                try (PreparedStatement stmt = conn.prepareStatement(checkReferredSql)) {
                    stmt.setString(1, referredUuid.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next() && rs.getString("referred_by") != null) {
                            conn.rollback();
                            return false; // Already referred
                        }
                    }
                }

                // Update referred player
                String updateReferredSql = "UPDATE player_data SET referred_by = ? WHERE uuid = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateReferredSql)) {
                    stmt.setString(1, referrerUuid);
                    stmt.setString(2, referredUuid.toString());
                    stmt.executeUpdate();
                }

                // Insert referral record
                String insertReferralSql = "INSERT INTO referrals (referrer_uuid, referred_uuid) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertReferralSql)) {
                    stmt.setString(1, referrerUuid);
                    stmt.setString(2, referredUuid.toString());
                    stmt.executeUpdate();
                }

                // Update referrer count
                String updateCountSql = "UPDATE player_data SET referral_count = referral_count + 1 WHERE uuid = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateCountSql)) {
                    stmt.setString(1, referrerUuid);
                    stmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to process referral: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Integer> getReferralCount(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT referral_count FROM player_data WHERE uuid = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("referral_count");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get referral count: " + e.getMessage());
            }
            return 0;
        });
    }

    public CompletableFuture<Boolean> hasReferredSomeone(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) FROM referrals WHERE referrer_uuid = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to check if player has referred someone: " + e.getMessage());
            }
            return false;
        });
    }
}