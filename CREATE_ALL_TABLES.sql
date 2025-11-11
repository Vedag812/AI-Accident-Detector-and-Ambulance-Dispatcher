-- ============================================
-- ACCIDENT ALERT SYSTEM - COMPLETE DATABASE SETUP
-- ============================================
-- Run this script in MySQL Workbench if needed
-- Or just run the Java application - it will auto-create tables!
-- ============================================

USE accident_alert_system;

-- Drop existing tables (CAREFUL! This deletes all data)
-- Uncomment below if you want a fresh start
-- DROP TABLE IF EXISTS surgery_rooms;
-- DROP TABLE IF EXISTS messages;
-- DROP TABLE IF EXISTS audit_logs;
-- DROP TABLE IF EXISTS ambulance_requests;
-- DROP TABLE IF EXISTS equipment_inventory;
-- DROP TABLE IF EXISTS hospital_staff;
-- DROP TABLE IF EXISTS ambulances;
-- DROP TABLE IF EXISTS hospitals;
-- DROP TABLE IF EXISTS accidents;
-- DROP TABLE IF EXISTS users;

-- ============================================
-- 1. USERS TABLE (Authentication)
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'DISPATCHER', 'HOSPITAL_STAFF', 'VIEWER') DEFAULT 'VIEWER',
    full_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default admin user
INSERT IGNORE INTO users (username, password, full_name, role, email) 
VALUES ('admin', 'admin123', 'System Administrator', 'ADMIN', 'admin@system.com');

-- ============================================
-- 2. ACCIDENTS TABLE (Main Data)
-- ============================================
CREATE TABLE IF NOT EXISTS accidents (
    accident_id INT PRIMARY KEY AUTO_INCREMENT,
    location VARCHAR(255) NOT NULL,
    latitude DOUBLE DEFAULT 0.0,
    longitude DOUBLE DEFAULT 0.0,
    vehicle_id VARCHAR(100),
    severity ENUM('Low', 'Medium', 'High', 'Critical') DEFAULT 'Medium',
    description TEXT,
    reported_by VARCHAR(100),
    weather_condition VARCHAR(50),
    traffic_density INT DEFAULT 50,
    casualties INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'Reported',
    accident_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_severity (severity),
    INDEX idx_time (accident_time),
    INDEX idx_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 3. HOSPITALS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS hospitals (
    hospital_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    x INT DEFAULT 0,
    y INT DEFAULT 0,
    latitude DOUBLE DEFAULT 0.0,
    longitude DOUBLE DEFAULT 0.0,
    available_beds INT DEFAULT 10,
    capacity INT DEFAULT 50,
    max_severity VARCHAR(50) DEFAULT 'Critical',
    contact_number VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_beds (available_beds)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample hospitals
INSERT IGNORE INTO hospitals (hospital_id, name, x, y, latitude, longitude, available_beds, capacity, contact_number, address) 
VALUES 
    (1, 'Apollo Hospital', 200, 150, 13.0358, 80.2464, 15, 100, '044-28296000', 'Greams Road, Chennai'),
    (2, 'MIOT Hospital', 600, 400, 13.0890, 80.2100, 20, 120, '044-42002000', 'Manapakkam, Chennai'),
    (3, 'Fortis Malar', 350, 250, 13.0569, 80.2540, 12, 80, '044-42899000', 'Adyar, Chennai');

-- ============================================
-- 4. AMBULANCES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS ambulances (
    ambulance_id INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_number VARCHAR(50),
    current_x INT DEFAULT 0,
    current_y INT DEFAULT 0,
    latitude DOUBLE DEFAULT 0.0,
    longitude DOUBLE DEFAULT 0.0,
    status VARCHAR(50) DEFAULT 'green',
    assigned_accident_id INT,
    assigned_hospital_id INT,
    driver_name VARCHAR(100),
    driver_contact VARCHAR(20),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    FOREIGN KEY (assigned_accident_id) REFERENCES accidents(accident_id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_hospital_id) REFERENCES hospitals(hospital_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample ambulances
INSERT IGNORE INTO ambulances (ambulance_id, vehicle_number, current_x, current_y, latitude, longitude, status, driver_name, driver_contact) 
VALUES 
    (1, 'TN01-AB-1234', 300, 200, 13.0500, 80.2500, 'green', 'Rajesh Kumar', '9876543210'),
    (2, 'TN01-CD-5678', 450, 350, 13.0700, 80.2600, 'green', 'Suresh Babu', '9876543211'),
    (3, 'TN01-EF-9012', 150, 100, 13.0300, 80.2400, 'green', 'Venkat Raman', '9876543212');

-- ============================================
-- 5. HOSPITAL STAFF TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS hospital_staff (
    staff_id INT PRIMARY KEY AUTO_INCREMENT,
    hospital_id INT,
    name VARCHAR(100),
    role VARCHAR(50),
    specialization VARCHAR(100),
    contact VARCHAR(20),
    shift VARCHAR(20),
    availability VARCHAR(20) DEFAULT 'Available',
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 6. EQUIPMENT INVENTORY TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS equipment_inventory (
    equipment_id INT PRIMARY KEY AUTO_INCREMENT,
    hospital_id INT,
    item_name VARCHAR(100),
    category VARCHAR(50),
    quantity INT,
    min_threshold INT,
    status VARCHAR(50),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE,
    INDEX idx_hospital (hospital_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 7. AMBULANCE REQUESTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS ambulance_requests (
    request_id INT PRIMARY KEY AUTO_INCREMENT,
    accident_id INT,
    ambulance_id INT,
    hospital_id INT,
    status VARCHAR(50),
    estimated_time INT,
    actual_time INT,
    request_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    pickup_time TIMESTAMP NULL,
    delivery_time TIMESTAMP NULL,
    route_data TEXT,
    FOREIGN KEY (accident_id) REFERENCES accidents(accident_id) ON DELETE CASCADE,
    FOREIGN KEY (ambulance_id) REFERENCES ambulances(ambulance_id) ON DELETE SET NULL,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE SET NULL,
    INDEX idx_status (status),
    INDEX idx_accident (accident_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 8. AUDIT LOGS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(100),
    details TEXT,
    ip_address VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user (user_id),
    INDEX idx_time (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 9. MESSAGES TABLE (Communication)
-- ============================================
CREATE TABLE IF NOT EXISTS messages (
    message_id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT,
    receiver_id INT,
    accident_id INT,
    message_text TEXT,
    priority VARCHAR(20),
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (accident_id) REFERENCES accidents(accident_id) ON DELETE CASCADE,
    INDEX idx_sender (sender_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 10. SURGERY ROOMS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS surgery_rooms (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    hospital_id INT,
    room_number VARCHAR(20),
    status VARCHAR(20) DEFAULT 'Available',
    current_patient VARCHAR(100),
    scheduled_until TIMESTAMP NULL,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE,
    INDEX idx_hospital (hospital_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- VERIFICATION QUERIES
-- ============================================
-- Check all tables
SELECT 'users' AS table_name, COUNT(*) AS row_count FROM users
UNION ALL
SELECT 'accidents', COUNT(*) FROM accidents
UNION ALL
SELECT 'hospitals', COUNT(*) FROM hospitals
UNION ALL
SELECT 'ambulances', COUNT(*) FROM ambulances
UNION ALL
SELECT 'hospital_staff', COUNT(*) FROM hospital_staff
UNION ALL
SELECT 'equipment_inventory', COUNT(*) FROM equipment_inventory
UNION ALL
SELECT 'ambulance_requests', COUNT(*) FROM ambulance_requests
UNION ALL
SELECT 'audit_logs', COUNT(*) FROM audit_logs
UNION ALL
SELECT 'messages', COUNT(*) FROM messages
UNION ALL
SELECT 'surgery_rooms', COUNT(*) FROM surgery_rooms;

-- ============================================
-- SUCCESS MESSAGE
-- ============================================
SELECT '✅ ALL TABLES CREATED SUCCESSFULLY!' AS status;
SELECT '👤 Login with: admin / admin123' AS credentials;
SELECT '🚀 You can now run the Java application!' AS next_step;
