-- Complete Database Setup Script
-- Run this in MySQL Workbench to ensure all tables exist

USE accident_alert_system;

-- Create Users Table (for authentication)
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) DEFAULT 'VIEWER',
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- Create Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(100),
    details TEXT,
    ip_address VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Messages Table (for communication)
CREATE TABLE IF NOT EXISTS messages (
    message_id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT,
    receiver_id INT,
    accident_id INT,
    message_text TEXT,
    priority VARCHAR(20),
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Surgery Rooms Table
CREATE TABLE IF NOT EXISTS surgery_rooms (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    hospital_id INT,
    room_number VARCHAR(20),
    status VARCHAR(20) DEFAULT 'Available',
    current_patient VARCHAR(100),
    scheduled_until TIMESTAMP NULL
);

-- Insert default admin user (if not exists)
INSERT INTO users (username, password_hash, full_name, role, email) 
VALUES ('admin', 'admin123', 'System Administrator', 'ADMIN', 'admin@system.com')
ON DUPLICATE KEY UPDATE username=username;

-- Verify all tables
SHOW TABLES;

-- Show user accounts
SELECT username, role, full_name, created_at FROM users;

SELECT 'Database setup complete!' AS Status;
