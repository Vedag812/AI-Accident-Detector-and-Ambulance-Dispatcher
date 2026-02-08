import java.sql.*;
import java.util.*;

/**
 * AmbulanceDriverManager - Manage driver assignments, shifts, and availability
 * Renamed from DriverManager to avoid conflict with java.sql.DriverManager
 */
public class AmbulanceDriverManager {
    private static AmbulanceDriverManager instance;
    private DatabaseManager dbManager;

    private AmbulanceDriverManager() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public static AmbulanceDriverManager getInstance() {
        if (instance == null) {
            instance = new AmbulanceDriverManager();
        }
        return instance;
    }

    /**
     * Add a new driver
     */
    public int addDriver(String name, String licenseNumber, String phone, String email) {
        try {
            String sql = "INSERT INTO drivers (name, license_number, phone, email, status, hire_date) " +
                    "VALUES (?, ?, ?, ?, 'available', CURDATE())";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, name);
            pstmt.setString(2, licenseNumber);
            pstmt.setString(3, phone);
            pstmt.setString(4, email);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[DriverManager] Error adding driver: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Assign driver to ambulance
     */
    public boolean assignToAmbulance(int driverId, int ambulanceId) {
        try {
            // Update driver
            String sql = "UPDATE drivers SET ambulance_id = ?, status = 'on_duty' WHERE driver_id = ?";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, ambulanceId);
            pstmt.setInt(2, driverId);
            pstmt.executeUpdate();

            // Update ambulance
            sql = "UPDATE ambulances SET driver_id = ? WHERE ambulance_id = ?";
            pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, driverId);
            pstmt.setInt(2, ambulanceId);
            pstmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            System.err.println("[DriverManager] Error assigning driver: " + e.getMessage());
        }
        return false;
    }

    /**
     * Unassign driver from ambulance
     */
    public boolean unassignFromAmbulance(int driverId) {
        try {
            // Get current ambulance
            String sql = "SELECT ambulance_id FROM drivers WHERE driver_id = ?";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, driverId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int ambulanceId = rs.getInt("ambulance_id");

                // Update ambulance
                dbManager.executeUpdate("UPDATE ambulances SET driver_id = NULL WHERE ambulance_id = " + ambulanceId);
            }

            // Update driver
            sql = "UPDATE drivers SET ambulance_id = NULL, status = 'available' WHERE driver_id = ?";
            pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, driverId);
            pstmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            System.err.println("[DriverManager] Error unassigning driver: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update driver status
     */
    public void updateStatus(int driverId, String status) {
        try {
            String sql = "UPDATE drivers SET status = ? WHERE driver_id = ?";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, driverId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DriverManager] Error updating status: " + e.getMessage());
        }
    }

    /**
     * Update driver shift times
     */
    public void updateShift(int driverId, String shiftStart, String shiftEnd) {
        try {
            String sql = "UPDATE drivers SET shift_start = ?, shift_end = ? WHERE driver_id = ?";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, shiftStart);
            pstmt.setString(2, shiftEnd);
            pstmt.setInt(3, driverId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DriverManager] Error updating shift: " + e.getMessage());
        }
    }

    /**
     * Increment trip count after completed trip
     */
    public void incrementTrips(int driverId) {
        try {
            dbManager.executeUpdate("UPDATE drivers SET total_trips = total_trips + 1 WHERE driver_id = " + driverId);
        } catch (SQLException e) {
            System.err.println("[DriverManager] Error incrementing trips: " + e.getMessage());
        }
    }

    /**
     * Get driver by ID
     */
    public Map<String, Object> getDriver(int driverId) {
        Map<String, Object> driver = new HashMap<>();
        try {
            String sql = "SELECT d.*, a.vehicle_number FROM drivers d " +
                    "LEFT JOIN ambulances a ON d.ambulance_id = a.ambulance_id " +
                    "WHERE d.driver_id = ?";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, driverId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                driver.put("driver_id", rs.getInt("driver_id"));
                driver.put("name", rs.getString("name"));
                driver.put("license_number", rs.getString("license_number"));
                driver.put("phone", rs.getString("phone"));
                driver.put("status", rs.getString("status"));
                driver.put("ambulance_id", rs.getInt("ambulance_id"));
                driver.put("vehicle_number", rs.getString("vehicle_number"));
                driver.put("total_trips", rs.getInt("total_trips"));
                driver.put("rating", rs.getDouble("rating"));
            }
        } catch (SQLException e) {
            System.err.println("[DriverManager] Error getting driver: " + e.getMessage());
        }
        return driver;
    }

    /**
     * Get all drivers
     */
    public List<Map<String, Object>> getAllDrivers() {
        List<Map<String, Object>> drivers = new ArrayList<>();
        try {
            String sql = "SELECT d.*, a.vehicle_number FROM drivers d " +
                    "LEFT JOIN ambulances a ON d.ambulance_id = a.ambulance_id " +
                    "ORDER BY d.name";
            ResultSet rs = dbManager.executeQuery(sql);

            while (rs.next()) {
                Map<String, Object> driver = new HashMap<>();
                driver.put("driver_id", rs.getInt("driver_id"));
                driver.put("name", rs.getString("name"));
                driver.put("license_number", rs.getString("license_number"));
                driver.put("phone", rs.getString("phone"));
                driver.put("status", rs.getString("status"));
                driver.put("ambulance_id", rs.getInt("ambulance_id"));
                driver.put("vehicle_number", rs.getString("vehicle_number"));
                driver.put("total_trips", rs.getInt("total_trips"));
                driver.put("rating", rs.getDouble("rating"));
                driver.put("shift_start", rs.getString("shift_start"));
                driver.put("shift_end", rs.getString("shift_end"));
                drivers.add(driver);
            }
        } catch (SQLException e) {
            System.err.println("[DriverManager] Error getting drivers: " + e.getMessage());
        }
        return drivers;
    }

    /**
     * Get available drivers
     */
    public List<Map<String, Object>> getAvailableDrivers() {
        List<Map<String, Object>> drivers = new ArrayList<>();
        try {
            String sql = "SELECT * FROM drivers WHERE status = 'available' AND ambulance_id IS NULL";
            ResultSet rs = dbManager.executeQuery(sql);

            while (rs.next()) {
                Map<String, Object> driver = new HashMap<>();
                driver.put("driver_id", rs.getInt("driver_id"));
                driver.put("name", rs.getString("name"));
                driver.put("phone", rs.getString("phone"));
                driver.put("rating", rs.getDouble("rating"));
                drivers.add(driver);
            }
        } catch (SQLException e) {
            System.err.println("[DriverManager] Error getting available drivers: " + e.getMessage());
        }
        return drivers;
    }

    /**
     * Get driver statistics
     */
    public Map<String, Integer> getDriverStats() {
        Map<String, Integer> stats = new HashMap<>();
        try {
            ResultSet rs = dbManager.executeQuery("SELECT status, COUNT(*) as count FROM drivers GROUP BY status");
            while (rs.next()) {
                stats.put(rs.getString("status"), rs.getInt("count"));
            }

            rs = dbManager.executeQuery("SELECT COUNT(*) as total FROM drivers");
            if (rs.next()) {
                stats.put("total", rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.err.println("[DriverManager] Error getting stats: " + e.getMessage());
        }
        return stats;
    }
}

