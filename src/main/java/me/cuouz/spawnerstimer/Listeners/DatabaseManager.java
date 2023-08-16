package me.cuouz.spawnerstimer.Listeners;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.*;

import java.io.*;
import java.sql.*;
import java.util.Date;
import java.util.*;

public class DatabaseManager {

    private Connection connection;
    private Plugin plugin;

    public DatabaseManager(String databasePath, Plugin plugin) {
        this.plugin = plugin;
        try {
            Class.forName("org.sqlite.JDBC");

            File databaseFile = new File(databasePath);
            if (!databaseFile.exists()) {
                databaseFile.getParentFile().mkdirs();
                databaseFile.createNewFile();
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            createTables();
        } catch (ClassNotFoundException | SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        createSpawnerLocationsTable();
        createSpawnerExpirationsTable();
    }

    private void createSpawnerLocationsTable() {
        try {
            String query = "CREATE TABLE IF NOT EXISTS spawner_locations (player_uuid TEXT, location_x INT, location_y INT, location_z INT)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createSpawnerExpirationsTable() {
        try {
            String query = "CREATE TABLE IF NOT EXISTS spawner_expirations (player_uuid TEXT, location_x INT, location_y INT, location_z INT, expiration_date BIGINT, entity_type TEXT)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveSpawnerLocation(UUID playerUUID, Location location) {
        try {
            String query = "INSERT INTO spawner_locations (player_uuid, location_x, location_y, location_z) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, playerUUID.toString());
            statement.setInt(2, location.getBlockX());
            statement.setInt(3, location.getBlockY());
            statement.setInt(4, location.getBlockZ());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Location> getSpawnerLocations(UUID playerUUID) {
        List<Location> spawnerLocations = new ArrayList<>();
        try {
            String query = "SELECT location_x, location_y, location_z FROM spawner_locations WHERE player_uuid = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, playerUUID.toString());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int x = resultSet.getInt("location_x");
                int y = resultSet.getInt("location_y");
                int z = resultSet.getInt("location_z");
                World world = plugin.getServer().getWorlds().get(0);
                Location location = new Location(world, x, y, z);
                spawnerLocations.add(location);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return spawnerLocations;
    }

    public void clearPlayerSpawnerLocations(UUID playerUUID) {
        try {
            String query = "DELETE FROM spawner_locations WHERE player_uuid = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Date getSpawnerExpiration(Location location) {
        try {
            String query = "SELECT expiration_date FROM spawner_expirations WHERE location_x = ? AND location_y = ? AND location_z = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, location.getBlockX());
            statement.setInt(2, location.getBlockY());
            statement.setInt(3, location.getBlockZ());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                long expirationDateMillis = resultSet.getLong("expiration_date");
                return new Date(expirationDateMillis);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void removeSpawnerExpiration(Location location) {
        try {
            String query = "DELETE FROM spawner_expirations WHERE location_x = ? AND location_y = ? AND location_z = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, location.getBlockX());
            statement.setInt(2, location.getBlockY());
            statement.setInt(3, location.getBlockZ());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void givePlayerSpawner(UUID playerUUID, long durationMillis, EntityType entityType) {
        try {
            long expirationDateMillis = System.currentTimeMillis() + durationMillis;
            String query = "INSERT INTO spawner_expirations (player_uuid, location_x, location_y, location_z, expiration_date, entity_type) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, playerUUID.toString());
            statement.setInt(2, 0);  // In 1.8.8, we don't have location data for spawners
            statement.setInt(3, 0);  // In 1.8.8, we don't have location data for spawners
            statement.setInt(4, 0);  // In 1.8.8, we don't have location data for spawners
            statement.setLong(5, expirationDateMillis);
            statement.setString(6, entityType.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
