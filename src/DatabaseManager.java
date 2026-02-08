import java.sql.*;
import java.util.*;

/**
 * DatabaseManager - Handles all database operations including connection
 * management,
 * automatic table creation, and CRUD operations for the accident alert system.
 * Follows Singleton pattern.
 * 
 * UPDATED: Added tables for patients, drivers, vehicle maintenance, incident
 * reports
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private ConfigManager config;

    private DatabaseManager() {
        config = ConfigManager.getInstance();
        connect();
        initializeTables();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    config.getDbUrl(),
                    config.getDbUser(),
                    config.getDbPassword());
            System.out.println("[DatabaseManager] Connected to database successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseManager] MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Database connection failed: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error checking connection: " + e.getMessage());
        }
        return connection;
    }

    private void initializeTables() {
        try {
            // Core tables
            createUsersTable();
            createAccidentsTable();
            createHospitalsTable();
            createAmbulancesTable();
            createMessagesTable();
            createAuditLogsTable();

            // NEW: Advanced feature tables
            createDriversTable();
            createPatientsTable();
            createVehicleMaintenanceTable();
            createIncidentReportsTable();
            createResponseTimesTable();

            // Migrate existing tables with new columns
            migrateExistingDatabase();

            insertDefaultData();
            System.out.println("[DatabaseManager] All tables initialized successfully");
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error initializing tables: " + e.getMessage());
        }
    }

    /**
     * Migrate existing database tables to add new columns if they don't exist
     */
    private void migrateExistingDatabase() {
        // Add columns to ambulances table if they don't exist
        safeAddColumn("ambulances", "latitude", "DECIMAL(10,8) DEFAULT 13.0827");
        safeAddColumn("ambulances", "longitude", "DECIMAL(11,8) DEFAULT 80.2707");
        safeAddColumn("ambulances", "fuel_level", "INT DEFAULT 100");
        safeAddColumn("ambulances", "driver_id", "INT");
        safeAddColumn("ambulances", "last_maintenance", "DATE");
        safeAddColumn("ambulances", "target_x", "INT DEFAULT 0");
        safeAddColumn("ambulances", "target_y", "INT DEFAULT 0");

        // Add GPS columns to accidents table if they don't exist
        safeAddColumn("accidents", "latitude", "DECIMAL(10,8) DEFAULT 13.0827");
        safeAddColumn("accidents", "longitude", "DECIMAL(11,8) DEFAULT 80.2707");

        // Add columns to hospitals table if they don't exist
        safeAddColumn("hospitals", "latitude", "DECIMAL(10,8) DEFAULT 13.0827");
        safeAddColumn("hospitals", "longitude", "DECIMAL(11,8) DEFAULT 80.2707");
        safeAddColumn("hospitals", "specialty", "VARCHAR(100) DEFAULT 'General'");
        safeAddColumn("hospitals", "icu_beds", "INT DEFAULT 10");
        safeAddColumn("hospitals", "available_icu_beds", "INT DEFAULT 10");
        safeAddColumn("hospitals", "phone", "VARCHAR(20)");
        safeAddColumn("hospitals", "max_severity", "ENUM('Low','Medium','High','Critical') DEFAULT 'Medium'");

        System.out.println("[DatabaseManager] Database migration completed");
    }

    /**
     * Safely add a column to a table if it doesn't exist
     */
    private void safeAddColumn(String table, String column, String definition) {
        try {
            String sql = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition;
            executeUpdate(sql);
        } catch (SQLException e) {
            // Column likely already exists - ignore error
            if (!e.getMessage().contains("Duplicate column")) {
                // Only log if it's not a duplicate column error
                if (!e.getMessage().toLowerCase().contains("duplicate") &&
                        !e.getMessage().toLowerCase().contains("already exists")) {
                    System.err.println(
                            "[DatabaseManager] Migration note for " + table + "." + column + ": " + e.getMessage());
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CORE TABLES
    // ═══════════════════════════════════════════════════════════════════════════

    private void createUsersTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "role ENUM('ADMIN', 'DISPATCHER', 'HOSPITAL_STAFF', 'VIEWER') DEFAULT 'VIEWER', " +
                "full_name VARCHAR(100), " +
                "email VARCHAR(100), " +
                "phone VARCHAR(20), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        executeUpdate(sql);
    }

    private void createAccidentsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS accidents (" +
                "accident_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "location VARCHAR(100) NOT NULL, " +
                "latitude DECIMAL(10,8) DEFAULT 13.0827, " +
                "longitude DECIMAL(11,8) DEFAULT 80.2707, " +
                "vehicle_id VARCHAR(50) NOT NULL, " +
                "severity ENUM('Low','Medium','High','Critical') NOT NULL, " +
                "description TEXT, " +
                "reported_by VARCHAR(100) NOT NULL, " +
                "accident_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "status ENUM('Reported','Dispatched','Responding','Resolved') DEFAULT 'Reported', " +
                "INDEX idx_severity (severity), " +
                "INDEX idx_time (accident_time), " +
                "INDEX idx_status (status))";
        executeUpdate(sql);
    }

    private void createHospitalsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS hospitals (" +
                "hospital_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "capacity INT NOT NULL, " +
                "available_beds INT NOT NULL, " +
                "icu_beds INT DEFAULT 10, " +
                "available_icu_beds INT DEFAULT 10, " +
                "max_severity ENUM('Low','Medium','High','Critical') NOT NULL DEFAULT 'Medium', " +
                "specialty VARCHAR(100) DEFAULT 'General', " +
                "phone VARCHAR(20), " +
                "x INT NOT NULL, " +
                "y INT NOT NULL, " +
                "latitude DECIMAL(10,8) DEFAULT 13.0827, " +
                "longitude DECIMAL(11,8) DEFAULT 80.2707, " +
                "INDEX idx_beds (available_beds), " +
                "INDEX idx_specialty (specialty))";
        executeUpdate(sql);
    }

    private void createAmbulancesTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS ambulances (" +
                "ambulance_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "vehicle_number VARCHAR(20), " +
                "current_x INT NOT NULL, " +
                "current_y INT NOT NULL, " +
                "latitude DECIMAL(10,8) DEFAULT 13.0827, " +
                "longitude DECIMAL(11,8) DEFAULT 80.2707, " +
                "status ENUM('green','yellow','red') DEFAULT 'green', " +
                "fuel_level INT DEFAULT 100, " +
                "driver_id INT, " +
                "assigned_accident_id INT NULL, " +
                "assigned_hospital_id INT NULL, " +
                "last_maintenance DATE, " +
                "INDEX idx_status (status), " +
                "INDEX idx_driver (driver_id), " +
                "INDEX idx_assigned_accident (assigned_accident_id))";
        executeUpdate(sql);
    }

    private void createMessagesTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS messages (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "sender_id INT, " +
                "receiver_id INT, " +
                "message TEXT, " +
                "priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM', " +
                "sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "read_status BOOLEAN DEFAULT FALSE)";
        executeUpdate(sql);
    }

    private void createAuditLogsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS audit_logs (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT, " +
                "action VARCHAR(100), " +
                "details TEXT, " +
                "ip_address VARCHAR(50), " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        executeUpdate(sql);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // NEW ADVANCED FEATURE TABLES
    // ═══════════════════════════════════════════════════════════════════════════

    private void createDriversTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS drivers (" +
                "driver_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "license_number VARCHAR(50) UNIQUE, " +
                "phone VARCHAR(20), " +
                "email VARCHAR(100), " +
                "shift_start TIME DEFAULT '08:00:00', " +
                "shift_end TIME DEFAULT '20:00:00', " +
                "status ENUM('available','on_duty','off_duty','on_leave') DEFAULT 'available', " +
                "ambulance_id INT, " +
                "total_trips INT DEFAULT 0, " +
                "rating DECIMAL(2,1) DEFAULT 5.0, " +
                "hire_date DATE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_status (status), " +
                "INDEX idx_ambulance (ambulance_id))";
        executeUpdate(sql);
    }

    private void createPatientsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS patients (" +
                "patient_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "accident_id INT, " +
                "name VARCHAR(100), " +
                "age INT, " +
                "gender ENUM('Male','Female','Other'), " +
                "blood_type VARCHAR(5), " +
                "phone VARCHAR(20), " +
                "emergency_contact VARCHAR(100), " +
                "emergency_phone VARCHAR(20), " +
                "injury_type VARCHAR(100), " +
                "injury_severity ENUM('Minor','Moderate','Severe','Critical') DEFAULT 'Moderate', " +
                "vitals_bp VARCHAR(20), " +
                "vitals_pulse INT, " +
                "vitals_oxygen INT, " +
                "status ENUM('At Scene','In Transit','Admitted','Discharged','Deceased') DEFAULT 'At Scene', " +
                "hospital_id INT, " +
                "ambulance_id INT, " +
                "admitted_at TIMESTAMP NULL, " +
                "discharged_at TIMESTAMP NULL, " +
                "notes TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_accident (accident_id), " +
                "INDEX idx_hospital (hospital_id), " +
                "INDEX idx_status (status))";
        executeUpdate(sql);
    }

    private void createVehicleMaintenanceTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS vehicle_maintenance (" +
                "maintenance_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "ambulance_id INT NOT NULL, " +
                "maintenance_type ENUM('Oil Change','Tire Rotation','Full Service','Repair','Inspection') NOT NULL, " +
                "description TEXT, " +
                "cost DECIMAL(10,2), " +
                "mileage INT, " +
                "service_date DATE, " +
                "next_service_date DATE, " +
                "performed_by VARCHAR(100), " +
                "status ENUM('Scheduled','In Progress','Completed','Cancelled') DEFAULT 'Scheduled', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_ambulance (ambulance_id), " +
                "INDEX idx_status (status), " +
                "INDEX idx_service_date (service_date))";
        executeUpdate(sql);
    }

    private void createIncidentReportsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS incident_reports (" +
                "report_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "accident_id INT NOT NULL, " +
                "ambulance_id INT, " +
                "driver_id INT, " +
                "patient_id INT, " +
                "hospital_id INT, " +
                "dispatch_time TIMESTAMP, " +
                "arrival_time TIMESTAMP, " +
                "scene_departure_time TIMESTAMP, " +
                "hospital_arrival_time TIMESTAMP, " +
                "response_time_minutes INT, " +
                "total_time_minutes INT, " +
                "distance_km DECIMAL(6,2), " +
                "outcome ENUM('Patient Stabilized','Patient Admitted','Patient Deceased','False Alarm','Other') DEFAULT 'Patient Admitted', "
                +
                "weather_conditions VARCHAR(50), " +
                "traffic_conditions ENUM('Light','Moderate','Heavy') DEFAULT 'Moderate', " +
                "complications TEXT, " +
                "notes TEXT, " +
                "created_by INT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_accident (accident_id), " +
                "INDEX idx_ambulance (ambulance_id), " +
                "INDEX idx_outcome (outcome))";
        executeUpdate(sql);
    }

    private void createResponseTimesTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS response_times (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "accident_id INT NOT NULL, " +
                "ambulance_id INT, " +
                "dispatch_time TIMESTAMP, " +
                "arrival_time TIMESTAMP, " +
                "response_time_seconds INT, " +
                "distance_meters INT, " +
                "severity VARCHAR(20), " +
                "location VARCHAR(100), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_accident (accident_id), " +
                "INDEX idx_date (created_at))";
        executeUpdate(sql);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DEFAULT DATA INSERTION
    // ═══════════════════════════════════════════════════════════════════════════

    private void insertDefaultData() throws SQLException {
        insertDefaultUsers();
        insertDefaultHospitals();
        insertDefaultAmbulances();
        insertDefaultDrivers();
    }

    private void insertDefaultUsers() throws SQLException {
        String checkUser = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
        ResultSet rs = executeQuery(checkUser);
        if (rs.next() && rs.getInt(1) == 0) {
            String insertAdmin = "INSERT INTO users (username, password, role, full_name, email) " +
                    "VALUES ('admin', 'admin123', 'ADMIN', 'System Administrator', 'admin@system.com')";
            executeUpdate(insertAdmin);
        }
    }

    private void insertDefaultHospitals() throws SQLException {
        String check = "SELECT COUNT(*) FROM hospitals";
        ResultSet rs = executeQuery(check);
        if (rs.next() && rs.getInt(1) == 0) {
            String[] hospitals = {
                    "INSERT INTO hospitals (name, capacity, available_beds, icu_beds, available_icu_beds, x, y, latitude, longitude, specialty, phone, max_severity) VALUES "
                            +
                            "('Apollo Hospital', 200, 180, 30, 25, 100, 100, 13.0827, 80.2707, 'Trauma,Cardiac,General', '044-28291000', 'Critical')",
                    "INSERT INTO hospitals (name, capacity, available_beds, icu_beds, available_icu_beds, x, y, latitude, longitude, specialty, phone, max_severity) VALUES "
                            +
                            "('MIOT International', 150, 130, 25, 20, 300, 150, 13.0067, 80.2206, 'Orthopedic,Trauma', '044-42001000', 'Critical')",
                    "INSERT INTO hospitals (name, capacity, available_beds, icu_beds, available_icu_beds, x, y, latitude, longitude, specialty, phone, max_severity) VALUES "
                            +
                            "('Fortis Malar Hospital', 100, 85, 15, 12, 500, 200, 13.0358, 80.2415, 'Cardiac,General', '044-42892222', 'High')",
                    "INSERT INTO hospitals (name, capacity, available_beds, icu_beds, available_icu_beds, x, y, latitude, longitude, specialty, phone, max_severity) VALUES "
                            +
                            "('SRM Hospital', 120, 100, 20, 18, 200, 300, 12.8231, 80.0442, 'General,Burns', '044-27456789', 'High')",
                    "INSERT INTO hospitals (name, capacity, available_beds, icu_beds, available_icu_beds, x, y, latitude, longitude, specialty, phone, max_severity) VALUES "
                            +
                            "('Global Hospital', 180, 160, 28, 22, 400, 350, 13.0569, 80.2425, 'Neuro,Trauma,Cardiac', '044-44777000', 'Critical')"
            };
            for (String sql : hospitals) {
                executeUpdate(sql);
            }
        }
    }

    private void insertDefaultAmbulances() throws SQLException {
        String check = "SELECT COUNT(*) FROM ambulances";
        ResultSet rs = executeQuery(check);
        if (rs.next() && rs.getInt(1) == 0) {
            String[] ambulances = {
                    "INSERT INTO ambulances (vehicle_number, current_x, current_y, latitude, longitude, status, fuel_level) VALUES ('TN01AB1234', 50, 50, 13.0827, 80.2707, 'green', 85)",
                    "INSERT INTO ambulances (vehicle_number, current_x, current_y, latitude, longitude, status, fuel_level) VALUES ('TN01CD5678', 150, 80, 13.0600, 80.2500, 'green', 92)",
                    "INSERT INTO ambulances (vehicle_number, current_x, current_y, latitude, longitude, status, fuel_level) VALUES ('TN01EF9012', 250, 120, 13.0400, 80.2300, 'green', 78)",
                    "INSERT INTO ambulances (vehicle_number, current_x, current_y, latitude, longitude, status, fuel_level) VALUES ('TN01GH3456', 350, 200, 13.0200, 80.2100, 'green', 95)",
                    "INSERT INTO ambulances (vehicle_number, current_x, current_y, latitude, longitude, status, fuel_level) VALUES ('TN01IJ7890', 450, 280, 13.0000, 80.1900, 'green', 88)",
                    "INSERT INTO ambulances (vehicle_number, current_x, current_y, latitude, longitude, status, fuel_level) VALUES ('TN01KL1234', 100, 350, 13.0900, 80.2800, 'green', 80)",
                    "INSERT INTO ambulances (vehicle_number, current_x, current_y, latitude, longitude, status, fuel_level) VALUES ('TN01MN5678', 550, 150, 13.0500, 80.2400, 'green', 90)",
                    "INSERT INTO ambulances (vehicle_number, current_x, current_y, latitude, longitude, status, fuel_level) VALUES ('TN01OP9012', 400, 50, 13.1100, 80.2900, 'green', 75)",
                    "INSERT INTO ambulances (vehicle_number, current_x, current_y, latitude, longitude, status, fuel_level) VALUES ('TN01QR3456', 50, 550, 13.0100, 80.2000, 'green', 85)",
                    "INSERT INTO ambulances (vehicle_number, current_x, current_y, latitude, longitude, status, fuel_level) VALUES ('TN01ST7890', 300, 400, 13.0700, 80.2600, 'green', 95)",
                    "INSERT INTO ambulances (vehicle_number, current_x, current_y, latitude, longitude, status, fuel_level) VALUES ('TN01UV1234', 200, 100, 13.0300, 80.2200, 'green', 82)",
                    "INSERT INTO ambulances (vehicle_number, current_x, current_y, latitude, longitude, status, fuel_level) VALUES ('TN01WX5678', 450, 450, 13.0950, 80.2850, 'green', 88)",
                    "INSERT INTO ambulances (vehicle_number, current_x, current_y, latitude, longitude, status, fuel_level) VALUES ('TN01YZ9012', 150, 500, 13.0250, 80.2150, 'green', 91)",
                    "INSERT INTO ambulances (vehicle_number, current_x, current_y, latitude, longitude, status, fuel_level) VALUES ('TN01AA3456', 250, 60, 13.1050, 80.2950, 'green', 79)",
                    "INSERT INTO ambulances (vehicle_number, current_x, current_y, latitude, longitude, status, fuel_level) VALUES ('TN01BB7890', 350, 350, 13.0550, 80.2450, 'green', 84)"
            };
            for (String sql : ambulances) {
                executeUpdate(sql);
            }
        }
    }

    private void insertDefaultDrivers() throws SQLException {
        String check = "SELECT COUNT(*) FROM drivers";
        ResultSet rs = executeQuery(check);
        if (rs.next() && rs.getInt(1) == 0) {
            String[] drivers = {
                    "INSERT INTO drivers (name, license_number, phone, status, ambulance_id, total_trips, rating, hire_date) VALUES "
                            +
                            "('Rajesh Kumar', 'TN0120190012345', '9876543210', 'available', 1, 150, 4.8, '2020-01-15')",
                    "INSERT INTO drivers (name, license_number, phone, status, ambulance_id, total_trips, rating, hire_date) VALUES "
                            +
                            "('Suresh Babu', 'TN0120180054321', '9876543211', 'available', 2, 230, 4.9, '2019-06-20')",
                    "INSERT INTO drivers (name, license_number, phone, status, ambulance_id, total_trips, rating, hire_date) VALUES "
                            +
                            "('Kumar Shankar', 'TN0120200098765', '9876543212', 'available', 3, 85, 4.5, '2021-03-10')",
                    "INSERT INTO drivers (name, license_number, phone, status, ambulance_id, total_trips, rating, hire_date) VALUES "
                            +
                            "('Venkat Raman', 'TN0120170011111', '9876543213', 'on_leave', NULL, 320, 4.7, '2018-09-05')",
                    "INSERT INTO drivers (name, license_number, phone, status, ambulance_id, total_trips, rating, hire_date) VALUES "
                            +
                            "('Arun Prasad', 'TN0120210022222', '9876543214', 'available', 4, 45, 4.6, '2022-01-20')"
            };
            for (String sql : drivers) {
                executeUpdate(sql);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // QUERY METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    public int executeUpdate(String sql) throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(sql);
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DatabaseManager] Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error closing connection: " + e.getMessage());
        }
    }
}

