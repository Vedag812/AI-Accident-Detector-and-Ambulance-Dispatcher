-- Create Database
CREATE DATABASE IF NOT EXISTS accident_alert_system;
USE accident_alert_system;

-- Accident Table
DROP TABLE IF EXISTS accidents;
CREATE TABLE accidents (
    accident_id INT AUTO_INCREMENT PRIMARY KEY,
    location VARCHAR(100) NOT NULL,
    vehicle_id VARCHAR(50) NOT NULL,
    severity ENUM('Low','Medium','High','Critical') NOT NULL,
    description TEXT,
    reported_by VARCHAR(100) NOT NULL,
    accident_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_severity (severity),
    INDEX idx_time (accident_time)
);

-- Hospital Table
DROP TABLE IF EXISTS hospitals;
CREATE TABLE hospitals (
    hospital_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    available_beds INT NOT NULL,
    max_severity ENUM('Low','Medium','High','Critical') NOT NULL DEFAULT 'Medium',
    x INT NOT NULL,
    y INT NOT NULL,
    INDEX idx_beds (available_beds)
);

-- Ambulance Table
DROP TABLE IF EXISTS ambulances;
CREATE TABLE ambulances (
    ambulance_id INT AUTO_INCREMENT PRIMARY KEY,
    current_x INT NOT NULL,
    current_y INT NOT NULL,
    status ENUM('green','yellow','red') DEFAULT 'green',
    assigned_accident_id INT NULL,
    assigned_hospital_id INT NULL,
    FOREIGN KEY (assigned_accident_id) REFERENCES accidents(accident_id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_hospital_id) REFERENCES hospitals(hospital_id) ON DELETE SET NULL,
    INDEX idx_status (status),
    INDEX idx_assigned_accident (assigned_accident_id)
);

-- Insert Sample Hospitals (Top 5 Chennai)
INSERT INTO hospitals (name, capacity, available_beds, x, y, max_severity) VALUES
('Apollo Hospital', 50, 50, 100, 100, 'Critical'),
('MIOT International', 40, 40, 300, 150, 'High'),
('Fortis Malar Hospital', 30, 30, 500, 200, 'Medium'),
('SRM Hospital', 35, 35, 200, 300, 'High'),
('Global Hospital', 25, 25, 400, 350, 'Critical');

-- Insert Sample Ambulances (3 moving units)
INSERT INTO ambulances (current_x, current_y, status) VALUES
(50, 50, 'green'),
(60, 60, 'green'),
(70, 70, 'green');
