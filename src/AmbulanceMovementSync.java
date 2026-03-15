import javax.swing.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * AmbulanceMovementSync - Central synchronization manager for ambulance
 * movements
 * Handles automatic dispatching, movement simulation, and status updates
 */
public class AmbulanceMovementSync {
    private DatabaseManager dbManager;
    private Timer syncTimer;
    private NotificationManager notificationManager;
    private boolean isRunning = false;
    private Set<Integer> assignedAccidents = new HashSet<>();

    public AmbulanceMovementSync() {
        this.dbManager = DatabaseManager.getInstance();
        this.notificationManager = NotificationManager.getInstance();
        loadAssignedAccidents();
    }

    /**
     * Load already assigned accidents from database
     */
    private void loadAssignedAccidents() {
        try {
            String sql = "SELECT assigned_accident_id FROM ambulances WHERE assigned_accident_id IS NOT NULL";
            ResultSet rs = dbManager.executeQuery(sql);
            while (rs.next()) {
                assignedAccidents.add(rs.getInt("assigned_accident_id"));
            }
        } catch (SQLException e) {
            System.err.println("[AmbulanceMovementSync] Error loading assigned accidents: " + e.getMessage());
        }
    }

    /**
     * Start automatic synchronization
     */
    public void start() {
        if (isRunning) {
            return;
        }

        isRunning = true;
        System.out.println("[AmbulanceMovementSync] Starting synchronization...");

        // Run sync every 2 seconds
        syncTimer = new Timer(2000, e -> performSync());
        syncTimer.start();
    }

    /**
     * Stop synchronization
     */
    public void stop() {
        if (syncTimer != null) {
            syncTimer.stop();
            isRunning = false;
            System.out.println("[AmbulanceMovementSync] Synchronization stopped");
        }
    }

    /**
     * Perform synchronization cycle
     */
    private void performSync() {
        try {
            checkForNewAccidents();
            moveAmbulances();
            updateAmbulanceStatus();
        } catch (Exception e) {
            System.err.println("[AmbulanceMovementSync] Sync error: " + e.getMessage());
        }
    }

    /**
     * Check for new accidents that need ambulance dispatch
     */
    private void checkForNewAccidents() {
        try {
            // Find all accidents - we'll track assignment via our Set
            String sql = "SELECT accident_id, location, severity FROM accidents " +
                    "ORDER BY accident_time DESC LIMIT 20";

            ResultSet rs = dbManager.executeQuery(sql);

            while (rs.next()) {
                int accidentId = rs.getInt("accident_id");

                // Skip if already assigned
                if (assignedAccidents.contains(accidentId)) {
                    continue;
                }

                String location = rs.getString("location");
                String severity = rs.getString("severity");

                // Try to dispatch ambulance
                if (dispatchNearestAmbulance(accidentId, location, severity)) {
                    assignedAccidents.add(accidentId);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AmbulanceMovementSync] Error checking accidents: " + e.getMessage());
        }
    }

    /**
     * Dispatch nearest available ambulance to accident
     */
    private boolean dispatchNearestAmbulance(int accidentId, String location, String severity) {
        try {
            // Get accident coordinates first
            String accSql = "SELECT latitude, longitude FROM accidents WHERE accident_id = ?";
            PreparedStatement accPstmt = dbManager.getConnection().prepareStatement(accSql);
            accPstmt.setInt(1, accidentId);
            ResultSet accRs = accPstmt.executeQuery();

            double targetLat = 13.0827; // Default Chennai
            double targetLng = 80.2707;
            int targetX = 200, targetY = 200;

            if (accRs.next()) {
                targetLat = accRs.getDouble("latitude");
                targetLng = accRs.getDouble("longitude");
                // Convert to grid coordinates (0-600 range)
                // Chennai spans roughly: lat 12.9-13.2, lng 80.0-80.4
                targetX = (int) ((targetLat - 12.9) / 0.3 * 600);
                targetY = (int) ((targetLng - 80.0) / 0.4 * 600);
                // Clamp to valid range
                targetX = Math.max(50, Math.min(550, targetX));
                targetY = Math.max(50, Math.min(550, targetY));
            }

            // Find nearest green (available) ambulance
            String sql = "SELECT ambulance_id, current_x, current_y FROM ambulances " +
                    "WHERE status = 'green' ORDER BY ambulance_id LIMIT 1";

            ResultSet rs = dbManager.executeQuery(sql);

            if (rs.next()) {
                int ambulanceId = rs.getInt("ambulance_id");

                // Update ambulance status to yellow (dispatched) AND set target coordinates
                String updateAmb = "UPDATE ambulances SET status = 'yellow', " +
                        "assigned_accident_id = ?, target_x = ?, target_y = ? WHERE ambulance_id = ?";
                PreparedStatement pstmt = dbManager.getConnection().prepareStatement(updateAmb);
                pstmt.setInt(1, accidentId);
                pstmt.setInt(2, targetX);
                pstmt.setInt(3, targetY);
                pstmt.setInt(4, ambulanceId);
                pstmt.executeUpdate();

                // Notify
                notificationManager.showWarning("Ambulance Dispatched",
                        String.format("Ambulance #%d dispatched to %s (%s severity)",
                                ambulanceId, location, severity));

                System.out.println(
                        String.format("[AmbulanceMovementSync] Dispatched ambulance %d to accident %d at (%d,%d)",
                                ambulanceId, accidentId, targetX, targetY));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[AmbulanceMovementSync] Error dispatching ambulance: " + e.getMessage());
        }
        return false;
    }

    /**
     * Move ambulances towards their targets
     */
    private void moveAmbulances() {
        try {
            // Get all yellow (dispatched) ambulances with their targets
            String sql = "SELECT ambulance_id, current_x, current_y, target_x, target_y, assigned_accident_id " +
                    "FROM ambulances WHERE status = 'yellow' AND assigned_accident_id IS NOT NULL";

            ResultSet rs = dbManager.executeQuery(sql);

            while (rs.next()) {
                int ambulanceId = rs.getInt("ambulance_id");
                int currentX = rs.getInt("current_x");
                int currentY = rs.getInt("current_y");
                int targetX = rs.getInt("target_x");
                int targetY = rs.getInt("target_y");
                int accidentId = rs.getInt("assigned_accident_id");

                // Calculate direction toward target
                int dx = targetX - currentX;
                int dy = targetY - currentY;
                double distance = Math.sqrt(dx * dx + dy * dy);

                // Move speed (pixels per update)
                int speed = 15;

                int newX, newY;

                if (distance <= speed) {
                    // Close enough - arrived!
                    newX = targetX;
                    newY = targetY;
                    markArrived(ambulanceId, accidentId);
                } else {
                    // Move toward target
                    double ratio = speed / distance;
                    newX = currentX + (int) (dx * ratio);
                    newY = currentY + (int) (dy * ratio);

                    // Add small random variation for realism
                    newX += (int) (Math.random() * 4 - 2);
                    newY += (int) (Math.random() * 4 - 2);

                    // Clamp to reasonable bounds
                    newX = Math.max(0, Math.min(600, newX));
                    newY = Math.max(0, Math.min(600, newY));
                }

                // Update ambulance position and GPS coordinates
                double newLat = 13.0827 + (newX - 200) * 0.0005;
                double newLng = 80.2707 + (newY - 200) * 0.0005;

                String update = "UPDATE ambulances SET current_x = ?, current_y = ?, latitude = ?, longitude = ? " +
                        "WHERE ambulance_id = ?";
                PreparedStatement pstmt = dbManager.getConnection().prepareStatement(update);
                pstmt.setInt(1, newX);
                pstmt.setInt(2, newY);
                pstmt.setDouble(3, newLat);
                pstmt.setDouble(4, newLng);
                pstmt.setInt(5, ambulanceId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[AmbulanceMovementSync] Error moving ambulances: " + e.getMessage());
        }
    }

    /**
     * Mark ambulance as arrived and change status to red
     */
    private void markArrived(int ambulanceId, int accidentId) {
        try {
            // Update ambulance to red (at scene)
            String update = "UPDATE ambulances SET status = 'red' WHERE ambulance_id = ?";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(update);
            pstmt.setInt(1, ambulanceId);
            pstmt.executeUpdate();

            notificationManager.showUrgent("Ambulance Arrived",
                    String.format("Ambulance #%d has arrived at accident scene #%d",
                            ambulanceId, accidentId));

            System.out.println(String.format("[AmbulanceMovementSync] Ambulance %d arrived at accident %d",
                    ambulanceId, accidentId));
        } catch (SQLException e) {
            System.err.println("[AmbulanceMovementSync] Error marking arrival: " + e.getMessage());
        }
    }

    /**
     * Update ambulance status and handle hospital transport
     */
    private void updateAmbulanceStatus() {
        try {
            // Handle red ambulances (at scene) - move them to nearest hospital
            String sql = "SELECT a.ambulance_id, a.assigned_accident_id, a.current_x, a.current_y, " +
                    "a.assigned_hospital_id FROM ambulances a WHERE a.status = 'red'";
            ResultSet rs = dbManager.executeQuery(sql);

            while (rs.next()) {
                int ambulanceId = rs.getInt("ambulance_id");
                int accidentId = rs.getInt("assigned_accident_id");
                int hospitalId = rs.getInt("assigned_hospital_id");
                int currentX = rs.getInt("current_x");
                int currentY = rs.getInt("current_y");

                if (hospitalId == 0) {
                    // Find nearest hospital and start transport
                    String hospSql = "SELECT hospital_id, x, y, name FROM hospitals ORDER BY " +
                            "SQRT(POW(x - ?, 2) + POW(y - ?, 2)) LIMIT 1";
                    PreparedStatement hospPstmt = dbManager.getConnection().prepareStatement(hospSql);
                    hospPstmt.setInt(1, currentX);
                    hospPstmt.setInt(2, currentY);
                    ResultSet hospRs = hospPstmt.executeQuery();

                    if (hospRs.next()) {
                        int nearestHospId = hospRs.getInt("hospital_id");
                        int hospX = hospRs.getInt("x");
                        int hospY = hospRs.getInt("y");
                        String hospName = hospRs.getString("name");

                        // Assign hospital and set target
                        String update = "UPDATE ambulances SET assigned_hospital_id = ?, target_x = ?, target_y = ? " +
                                "WHERE ambulance_id = ?";
                        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(update);
                        pstmt.setInt(1, nearestHospId);
                        pstmt.setInt(2, hospX);
                        pstmt.setInt(3, hospY);
                        pstmt.setInt(4, ambulanceId);
                        pstmt.executeUpdate();

                        notificationManager.showInfo("Patient Pickup",
                                String.format("Ambulance #%d transporting patient to %s", ambulanceId, hospName));
                        System.out.println(
                                String.format("[AmbulanceMovementSync] Ambulance %d transporting to hospital %s",
                                        ambulanceId, hospName));
                    }
                } else {
                    // Already has hospital assigned - check if arrived at hospital
                    String hospSql = "SELECT x, y FROM hospitals WHERE hospital_id = ?";
                    PreparedStatement hospPstmt = dbManager.getConnection().prepareStatement(hospSql);
                    hospPstmt.setInt(1, hospitalId);
                    ResultSet hospRs = hospPstmt.executeQuery();

                    if (hospRs.next()) {
                        int hospX = hospRs.getInt("x");
                        int hospY = hospRs.getInt("y");
                        double distance = Math.sqrt(Math.pow(hospX - currentX, 2) + Math.pow(hospY - currentY, 2));

                        if (distance <= 15) {
                            // Arrived at hospital - complete mission
                            String update = "UPDATE ambulances SET status = 'green', " +
                                    "assigned_accident_id = NULL, assigned_hospital_id = NULL " +
                                    "WHERE ambulance_id = ?";
                            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(update);
                            pstmt.setInt(1, ambulanceId);
                            pstmt.executeUpdate();

                            assignedAccidents.remove(accidentId);

                            notificationManager.showInfo("Patient Delivered",
                                    String.format("Ambulance #%d delivered patient to hospital", ambulanceId));
                            System.out.println(String.format(
                                    "[AmbulanceMovementSync] Ambulance %d completed mission (delivered to hospital)",
                                    ambulanceId));
                        } else {
                            // Still moving to hospital - update position
                            int dx = hospX - currentX;
                            int dy = hospY - currentY;
                            int speed = 15;
                            double ratio = speed / distance;
                            int newX = currentX + (int) (dx * ratio);
                            int newY = currentY + (int) (dy * ratio);

                            double newLat = 13.0827 + (newX - 200) * 0.0005;
                            double newLng = 80.2707 + (newY - 200) * 0.0005;

                            String update = "UPDATE ambulances SET current_x = ?, current_y = ?, latitude = ?, longitude = ? "
                                    +
                                    "WHERE ambulance_id = ?";
                            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(update);
                            pstmt.setInt(1, newX);
                            pstmt.setInt(2, newY);
                            pstmt.setDouble(3, newLat);
                            pstmt.setDouble(4, newLng);
                            pstmt.setInt(5, ambulanceId);
                            pstmt.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[AmbulanceMovementSync] Error updating status: " + e.getMessage());
        }
    }

    /**
     * Manually dispatch ambulance to accident
     */
    public void manualDispatch(int ambulanceId, int accidentId) {
        try {
            String updateAmb = "UPDATE ambulances SET status = 'yellow', " +
                    "assigned_accident_id = ? WHERE ambulance_id = ?";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(updateAmb);
            pstmt.setInt(1, accidentId);
            pstmt.setInt(2, ambulanceId);
            pstmt.executeUpdate();

            assignedAccidents.add(accidentId);

            notificationManager.showInfo("Manual Dispatch",
                    String.format("Ambulance #%d manually dispatched", ambulanceId));
        } catch (SQLException e) {
            System.err.println("[AmbulanceMovementSync] Error in manual dispatch: " + e.getMessage());
        }
    }

    /**
     * Check if sync is running
     */
    public boolean isRunning() {
        return isRunning;
    }
}
