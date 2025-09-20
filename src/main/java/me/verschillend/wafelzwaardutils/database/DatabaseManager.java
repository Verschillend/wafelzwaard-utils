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
    }

    private void setupDatabase() {
        try {
            HikariConfig config = new HikariConfig();

            String host = plugin.getConfig().getString("database.host", "192.168.1.242");
            int port = plugin.getConfig().getInt("database.port", 3306);
            String database = plugin.getConfig().getString("database.name", "wafelz");
            String username = plugin.getConfig().getString("database.username", "luckperms");
            String password = plugin.getConfig().getString("database.password", "");

            // Log the configuration (without password for security)
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
        String sql = """
            CREATE TABLE IF NOT EXISTS player_data (
                uuid VARCHAR(36) PRIMARY KEY,
                player_name VARCHAR(16) NOT NULL,
                color CHAR DEFAULT 7,
                last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            plugin.getLogger().info("Database tables created successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create database tables: " + e.getMessage());
        }
    }

    //async method to save player data
    public CompletableFuture<Void> savePlayerData(UUID uuid, String playerName, char color) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO player_data (uuid, player_name, color) 
                VALUES (?, ?, ?) 
                ON DUPLICATE KEY UPDATE player_name = VALUES(player_name), color = VALUES(color)
                """;

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, playerName);
                stmt.setInt(3, color);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Integer> getPlayerColor(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT color FROM player_data WHERE uuid = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("color");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get player color: " + e.getMessage());
            }
            return 0;
        });
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}