package ru.florestdev.interactivePets;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseManager {
    private final HikariDataSource dataSource;
    private final InteractivePets plugin;

    public DatabaseManager(InteractivePets plugin) {
        this.plugin = plugin;
        HikariConfig config = new HikariConfig();

        String host = plugin.getConfig().getString("database.host", "localhost");
        int port = plugin.getConfig().getInt("database.port", 3306);
        String database = plugin.getConfig().getString("database.database", "pets");
        String username = plugin.getConfig().getString("database.username", "root");
        String password = plugin.getConfig().getString("database.password", "password");
        int poolSize = plugin.getConfig().getInt("database.pool-size", 10);

        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(poolSize);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        this.dataSource = new HikariDataSource(config);
        createTables();
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS player_pets (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "player_uuid VARCHAR(36) NOT NULL," +
                "pet_type VARCHAR(20) NOT NULL," +
                "pet_name VARCHAR(50)," +
                "purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE KEY unique_pet (player_uuid, pet_type)" +
                ")";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            plugin.getLogger().info("Database table created/checked successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create database table: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public boolean hasPet(UUID playerUuid, String petType) {
        String sql = "SELECT 1 FROM player_pets WHERE player_uuid = ? AND pet_type = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, petType);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error checking pet ownership: " + e.getMessage());
            return false;
        }
    }

    public void addPet(UUID playerUuid, String petType, String petName) {
        String sql = "INSERT INTO player_pets (player_uuid, pet_type, pet_name) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, petType.toLowerCase());
            stmt.setString(3, petName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error adding pet: " + e.getMessage());
        }
    }

    public String getPetName(UUID playerUuid, String petType) {
        String sql = "SELECT pet_name FROM player_pets WHERE player_uuid = ? AND pet_type = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, petType.toLowerCase());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("pet_name");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting pet name: " + e.getMessage());
        }
        return null;
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}