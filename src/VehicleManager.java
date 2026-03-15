import java.sql.*;
import java.util.*;

/**
 * VehicleManager - Manages ambulance fleet operations including status, fuel,
 * and maintenance
 */
public class VehicleManager {
    private static VehicleManager instance;
    private DatabaseManager dbManager;

    private VehicleManager() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public static VehicleManager getInstance() {
        if (instance == null) {
            instance = new VehicleManager();
        }
        return instance;
    }

    /**
     * Get statistics about the vehicle fleet
     */
    public Map<String, Object> getVehicleStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            // Total count
            ResultSet rs = dbManager.executeQuery("SELECT COUNT(*) as total FROM ambulances");
            if (rs.next())
                stats.put("total", rs.getInt("total"));

            // Available count
            rs = dbManager.executeQuery("SELECT COUNT(*) as available FROM ambulances WHERE status = 'green'");
            if (rs.next())
                stats.put("available", rs.getInt("available"));

            // Low fuel count (below 30%)
            rs = dbManager.executeQuery("SELECT COUNT(*) as low_fuel FROM ambulances WHERE fuel_level < 30");
            if (rs.next())
                stats.put("low_fuel", rs.getInt("low_fuel"));

            // Average fuel level
            rs = dbManager.executeQuery("SELECT AVG(fuel_level) as avg_fuel FROM ambulances");
            if (rs.next())
                stats.put("avg_fuel", rs.getDouble("avg_fuel"));

            // Pending maintenance count
            rs = dbManager
                    .executeQuery("SELECT COUNT(*) as pending FROM vehicle_maintenance WHERE status = 'Scheduled'");
            if (rs.next())
                stats.put("pending_maintenance", rs.getInt("pending"));

        } catch (SQLException e) {
            System.err.println("[VehicleManager] Error getting stats: " + e.getMessage());
        }
        return stats;
    }

    /**
     * Get all ambulances with their details
     */
    public List<Map<String, Object>> getAllAmbulances() {
        List<Map<String, Object>> ambulances = new ArrayList<>();
        try {
            String sql = "SELECT a.*, d.name as driver_name FROM ambulances a " +
                    "LEFT JOIN drivers d ON a.driver_id = d.driver_id ORDER BY a.ambulance_id";
            ResultSet rs = dbManager.executeQuery(sql);
            while (rs.next()) {
                Map<String, Object> amb = new HashMap<>();
                amb.put("ambulance_id", rs.getInt("ambulance_id"));
                amb.put("vehicle_number", rs.getString("vehicle_number"));
                amb.put("current_x", rs.getInt("current_x"));
                amb.put("current_y", rs.getInt("current_y"));
                amb.put("status", rs.getString("status"));
                amb.put("fuel_level", rs.getInt("fuel_level"));
                amb.put("driver_name", rs.getString("driver_name"));
                ambulances.add(amb);
            }
        } catch (SQLException e) {
            System.err.println("[VehicleManager] Error getting ambulances: " + e.getMessage());
        }
        return ambulances;
    }

    /**
     * Get scheduled maintenance records
     */
    public List<Map<String, Object>> getScheduledMaintenance() {
        List<Map<String, Object>> records = new ArrayList<>();
        try {
            String sql = "SELECT vm.*, a.vehicle_number FROM vehicle_maintenance vm " +
                    "JOIN ambulances a ON vm.ambulance_id = a.ambulance_id " +
                    "WHERE vm.status IN ('Scheduled', 'In Progress') ORDER BY vm.service_date";
            ResultSet rs = dbManager.executeQuery(sql);
            while (rs.next()) {
                Map<String, Object> record = new HashMap<>();
                record.put("maintenance_id", rs.getInt("maintenance_id"));
                record.put("vehicle_number", rs.getString("vehicle_number"));
                record.put("maintenance_type", rs.getString("maintenance_type"));
                record.put("service_date", rs.getDate("service_date"));
                record.put("status", rs.getString("status"));
                records.add(record);
            }
        } catch (SQLException e) {
            System.err.println("[VehicleManager] Error getting maintenance: " + e.getMessage());
        }
        return records;
    }

    /**
     * Refuel an ambulance to 100%
     */
    public void refuel(int ambulanceId) {
        try {
            dbManager.executeUpdate("UPDATE ambulances SET fuel_level = 100 WHERE ambulance_id = " + ambulanceId);
        } catch (SQLException e) {
            System.err.println("[VehicleManager] Error refueling: " + e.getMessage());
        }
    }

    /**
     * Schedule maintenance for an ambulance
     */
    public void scheduleMaintenance(int ambulanceId, String type, String description, String date) {
        try {
            String sql = "INSERT INTO vehicle_maintenance (ambulance_id, maintenance_type, description, service_date, status) "
                    +
                    "VALUES (" + ambulanceId + ", '" + type + "', '" + description + "', '" + date + "', 'Scheduled')";
            dbManager.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("[VehicleManager] Error scheduling maintenance: " + e.getMessage());
        }
    }

    /**
     * Update maintenance record status
     */
    public void updateMaintenanceStatus(int maintenanceId, String status) {
        try {
            dbManager.executeUpdate(
                    "UPDATE vehicle_maintenance SET status = '" + status + "' WHERE maintenance_id = " + maintenanceId);
        } catch (SQLException e) {
            System.err.println("[VehicleManager] Error updating maintenance: " + e.getMessage());
        }
    }
}
