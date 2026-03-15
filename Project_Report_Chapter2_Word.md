CHAPTER 2

DESIGN OF RELATIONAL SCHEMAS, CREATION OF DATABASE AND TABLES FOR AI ACCIDENT DETECTOR AND AMBULANCE DISPATCHER

2.1 Relational Schema for AI Accident Detector & Ambulance Dispatcher

The relational schema represents the logical structure of the database. Below are the key relations where the primary keys are underlined and foreign keys are denoted with asterisks (*).

1.  **Users** (<u>id</u>, username, password, role, full_name, email, phone, created_at)
2.  **Accidents** (<u>accident_id</u>, location, latitude, longitude, vehicle_id, severity, description, reported_by, status, accident_time)
3.  **Hospitals** (<u>hospital_id</u>, name, capacity, available_beds, icu_beds, available_icu_beds, max_severity, specialty, phone, x, y, latitude, longitude)
4.  **Ambulances** (<u>ambulance_id</u>, vehicle_number, current_x, current_y, latitude, longitude, status, fuel_level, *driver_id*, *assigned_accident_id*, *assigned_hospital_id*, target_x, target_y, last_maintenance)
5.  **Messages** (<u>id</u>, *sender_id*, *receiver_id*, message, priority, sent_at, read_status)
6.  **Audit_Logs** (<u>id</u>, *user_id*, action, details, ip_address, timestamp)
7.  **Drivers** (<u>driver_id</u>, name, license_number, phone, email, shift_start, shift_end, status, *ambulance_id*, total_trips, rating, hire_date, created_at)
8.  **Patients** (<u>patient_id</u>, *accident_id*, name, age, gender, blood_type, phone, emergency_contact, emergency_phone, injury_type, injury_severity, vitals_bp, vitals_pulse, vitals_oxygen, status, *hospital_id*, *ambulance_id*, admitted_at, discharged_at, notes, created_at)
9.  **Vehicle_Maintenance** (<u>maintenance_id</u>, *ambulance_id*, maintenance_type, description, cost, mileage, service_date, next_service_date, performed_by, status, created_at)
10. **Incident_Reports** (<u>report_id</u>, *accident_id*, *ambulance_id*, *driver_id*, *patient_id*, *hospital_id*, dispatch_time, arrival_time, scene_departure_time, hospital_arrival_time, response_time_minutes, total_time_minutes, distance_km, outcome, weather_conditions, traffic_conditions, complications, notes, *created_by*, created_at)
11. **Response_Times** (<u>id</u>, *accident_id*, *ambulance_id*, dispatch_time, arrival_time, response_time_seconds, distance_meters, severity, location, created_at)

2.2 Description of Tables

Below is the detailed description of each table, following the required format for documentation.

Chapter 2. Table 2.1: Users Table Description
Purpose: Stores authentication and profile information for all system users including administrators, dispatchers, hospital staff, and viewers.

| Attribute | Data Type | Size/Details | Constraints |
| :--- | :--- | :--- | :--- |
| id | INT | - | Primary Key, Auto Increment |
| username | VARCHAR | 50 | Unique, Not Null |
| password | VARCHAR | 255 | Not Null |
| role | ENUM | ('ADMIN', 'DISPATCHER', 'HOSPITAL_STAFF', 'VIEWER') | Default: 'VIEWER' |
| full_name | VARCHAR | 100 | - |
| email | VARCHAR | 100 | - |
| phone | VARCHAR | 20 | - |
| created_at | TIMESTAMP | - | Default: Current Timestamp |

Chapter 2. Table 2.2: Accidents Table Description
Purpose: Main record for reported accidents, storing location data, severity assessment, and current processing status.

| Attribute | Data Type | Size/Details | Constraints |
| :--- | :--- | :--- | :--- |
| accident_id | INT | - | Primary Key, Auto Increment |
| location | VARCHAR | 100 | Not Null |
| latitude | DECIMAL | (10,8) | Default: 13.0827 |
| longitude | DECIMAL | (11,8) | Default: 80.2707 |
| vehicle_id | VARCHAR | 50 | Not Null |
| severity | ENUM | ('Low', 'Medium', 'High', 'Critical') | Not Null |
| description | TEXT | - | - |
| reported_by | VARCHAR | 100 | Not Null |
| status | ENUM | ('Reported', 'Dispatched', 'Responding', 'Resolved') | Default: 'Reported' |
| accident_time | TIMESTAMP | - | Default: Current Timestamp |

Chapter 2. Table 2.3: Hospitals Table Description
Purpose: Stores information about medical facilities including location coordinates, bed capacity, specialties, and maximum treatable severity level.

| Attribute | Data Type | Size/Details | Constraints |
| :--- | :--- | :--- | :--- |
| hospital_id | INT | - | Primary Key, Auto Increment |
| name | VARCHAR | 100 | Not Null |
| capacity | INT | - | Not Null |
| available_beds | INT | - | Not Null |
| icu_beds | INT | - | Default: 10 |
| available_icu_beds | INT | - | Default: 10 |
| max_severity | ENUM | ('Low', 'Medium', 'High', 'Critical') | Not Null, Default: 'Medium' |
| specialty | VARCHAR | 100 | Default: 'General' |
| phone | VARCHAR | 20 | - |
| x | INT | - | Not Null (Grid coordinate) |
| y | INT | - | Not Null (Grid coordinate) |
| latitude | DECIMAL | (10,8) | Default: 13.0827 |
| longitude | DECIMAL | (11,8) | Default: 80.2707 |

Chapter 2. Table 2.4: Ambulances Table Description
Purpose: Tracks the current status, real-time position, fuel level, and assignment details of all emergency response vehicles in the fleet.

| Attribute | Data Type | Size/Details | Constraints |
| :--- | :--- | :--- | :--- |
| ambulance_id | INT | - | Primary Key, Auto Increment |
| vehicle_number | VARCHAR | 20 | - |
| current_x | INT | - | Not Null (Grid position) |
| current_y | INT | - | Not Null (Grid position) |
| latitude | DECIMAL | (10,8) | Default: 13.0827 |
| longitude | DECIMAL | (11,8) | Default: 80.2707 |
| status | ENUM | ('green', 'yellow', 'red') | Default: 'green' |
| fuel_level | INT | - | Default: 100 |
| driver_id | INT | - | Foreign Key (Drivers) |
| assigned_accident_id | INT | - | Foreign Key (Accidents), Nullable |
| assigned_hospital_id | INT | - | Foreign Key (Hospitals), Nullable |
| target_x | INT | - | Default: 0 |
| target_y | INT | - | Default: 0 |
| last_maintenance | DATE | - | - |

Chapter 2. Table 2.5: Messages Table Description
Purpose: Stores inter-user communication with priority levels and read status tracking for the dispatch communication center.

| Attribute | Data Type | Size/Details | Constraints |
| :--- | :--- | :--- | :--- |
| id | INT | - | Primary Key, Auto Increment |
| sender_id | INT | - | Foreign Key (Users) |
| receiver_id | INT | - | Foreign Key (Users) |
| message | TEXT | - | - |
| priority | ENUM | ('LOW', 'MEDIUM', 'HIGH', 'URGENT') | Default: 'MEDIUM' |
| sent_at | TIMESTAMP | - | Default: Current Timestamp |
| read_status | BOOLEAN | - | Default: FALSE |

Chapter 2. Table 2.6: Audit Logs Table Description
Purpose: Maintains a comprehensive audit trail of all system actions performed by users for security and accountability.

| Attribute | Data Type | Size/Details | Constraints |
| :--- | :--- | :--- | :--- |
| id | INT | - | Primary Key, Auto Increment |
| user_id | INT | - | Foreign Key (Users) |
| action | VARCHAR | 100 | - |
| details | TEXT | - | - |
| ip_address | VARCHAR | 50 | - |
| timestamp | TIMESTAMP | - | Default: Current Timestamp |

Chapter 2. Table 2.7: Drivers Table Description
Purpose: Stores driver profiles, shift schedules, performance ratings, and vehicle assignment for emergency response personnel.

| Attribute | Data Type | Size/Details | Constraints |
| :--- | :--- | :--- | :--- |
| driver_id | INT | - | Primary Key, Auto Increment |
| name | VARCHAR | 100 | Not Null |
| license_number | VARCHAR | 50 | Unique |
| phone | VARCHAR | 20 | - |
| email | VARCHAR | 100 | - |
| shift_start | TIME | - | Default: '08:00:00' |
| shift_end | TIME | - | Default: '20:00:00' |
| status | ENUM | ('available', 'on_duty', 'off_duty', 'on_leave') | Default: 'available' |
| ambulance_id | INT | - | Foreign Key (Ambulances) |
| total_trips | INT | - | Default: 0 |
| rating | DECIMAL | (2,1) | Default: 5.0 |
| hire_date | DATE | - | - |
| created_at | TIMESTAMP | - | Default: Current Timestamp |

Chapter 2. Table 2.8: Patients Table Description
Purpose: Records patient information from accident scenes, including medical vitals, injury details, and current treatment status.

| Attribute | Data Type | Size/Details | Constraints |
| :--- | :--- | :--- | :--- |
| patient_id | INT | - | Primary Key, Auto Increment |
| accident_id | INT | - | Foreign Key (Accidents) |
| name | VARCHAR | 100 | - |
| age | INT | - | - |
| gender | ENUM | ('Male', 'Female', 'Other') | - |
| blood_type | VARCHAR | 5 | - |
| phone | VARCHAR | 20 | - |
| emergency_contact | VARCHAR | 100 | - |
| emergency_phone | VARCHAR | 20 | - |
| injury_type | VARCHAR | 100 | - |
| injury_severity | ENUM | ('Minor', 'Moderate', 'Severe', 'Critical') | Default: 'Moderate' |
| vitals_bp | VARCHAR | 20 | - |
| vitals_pulse | INT | - | - |
| vitals_oxygen | INT | - | - |
| status | ENUM | ('At Scene', 'In Transit', 'Admitted', 'Discharged', 'Deceased') | Default: 'At Scene' |
| hospital_id | INT | - | Foreign Key (Hospitals) |
| ambulance_id | INT | - | Foreign Key (Ambulances) |
| admitted_at | TIMESTAMP | - | Nullable |
| discharged_at | TIMESTAMP | - | Nullable |
| notes | TEXT | - | - |
| created_at | TIMESTAMP | - | Default: Current Timestamp |

Chapter 2. Table 2.9: Vehicle Maintenance Table Description
Purpose: Tracks scheduled and completed maintenance activities for fleet ambulances to ensure vehicle readiness.

| Attribute | Data Type | Size/Details | Constraints |
| :--- | :--- | :--- | :--- |
| maintenance_id | INT | - | Primary Key, Auto Increment |
| ambulance_id | INT | - | Foreign Key (Ambulances), Not Null |
| maintenance_type | ENUM | ('Oil Change', 'Tire Rotation', 'Full Service', 'Repair', 'Inspection') | Not Null |
| description | TEXT | - | - |
| cost | DECIMAL | (10,2) | - |
| mileage | INT | - | - |
| service_date | DATE | - | - |
| next_service_date | DATE | - | - |
| performed_by | VARCHAR | 100 | - |
| status | ENUM | ('Scheduled', 'In Progress', 'Completed', 'Cancelled') | Default: 'Scheduled' |
| created_at | TIMESTAMP | - | Default: Current Timestamp |

Chapter 2. Table 2.10: Incident Reports Table Description
Purpose: Comprehensive post-incident documentation linking accidents, ambulances, drivers, patients, and hospitals with timing and outcome data.

| Attribute | Data Type | Size/Details | Constraints |
| :--- | :--- | :--- | :--- |
| report_id | INT | - | Primary Key, Auto Increment |
| accident_id | INT | - | Foreign Key (Accidents), Not Null |
| ambulance_id | INT | - | Foreign Key (Ambulances) |
| driver_id | INT | - | Foreign Key (Drivers) |
| patient_id | INT | - | Foreign Key (Patients) |
| hospital_id | INT | - | Foreign Key (Hospitals) |
| dispatch_time | TIMESTAMP | - | - |
| arrival_time | TIMESTAMP | - | - |
| scene_departure_time | TIMESTAMP | - | - |
| hospital_arrival_time | TIMESTAMP | - | - |
| response_time_minutes | INT | - | - |
| total_time_minutes | INT | - | - |
| distance_km | DECIMAL | (6,2) | - |
| outcome | ENUM | ('Patient Stabilized', 'Patient Admitted', 'Patient Deceased', 'False Alarm', 'Other') | Default: 'Patient Admitted' |
| weather_conditions | VARCHAR | 50 | - |
| traffic_conditions | ENUM | ('Light', 'Moderate', 'Heavy') | Default: 'Moderate' |
| complications | TEXT | - | - |
| notes | TEXT | - | - |
| created_by | INT | - | Foreign Key (Users) |
| created_at | TIMESTAMP | - | Default: Current Timestamp |

Chapter 2. Table 2.11: Response Times Table Description
Purpose: Records dispatch-to-arrival performance metrics for each emergency response, used for analytics and system optimization.

| Attribute | Data Type | Size/Details | Constraints |
| :--- | :--- | :--- | :--- |
| id | INT | - | Primary Key, Auto Increment |
| accident_id | INT | - | Foreign Key (Accidents), Not Null |
| ambulance_id | INT | - | Foreign Key (Ambulances) |
| dispatch_time | TIMESTAMP | - | - |
| arrival_time | TIMESTAMP | - | - |
| response_time_seconds | INT | - | - |
| distance_meters | INT | - | - |
| severity | VARCHAR | 20 | - |
| location | VARCHAR | 100 | - |
| created_at | TIMESTAMP | - | Default: Current Timestamp |

[Place ER Diagram Image Here]

Figure 2.1: ER diagram of AI Accident Detector and Ambulance Dispatcher System

2.3 Creation of Database and Tables – DDL Commands

The following SQL commands create the database structure for the system.

```sql
CREATE DATABASE IF NOT EXISTS accident_alert_system;
USE accident_alert_system;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'DISPATCHER', 'HOSPITAL_STAFF', 'VIEWER') DEFAULT 'VIEWER',
    full_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Accidents Table
CREATE TABLE IF NOT EXISTS accidents (
    accident_id INT AUTO_INCREMENT PRIMARY KEY,
    location VARCHAR(100) NOT NULL,
    latitude DECIMAL(10,8) DEFAULT 13.0827,
    longitude DECIMAL(11,8) DEFAULT 80.2707,
    vehicle_id VARCHAR(50) NOT NULL,
    severity ENUM('Low', 'Medium', 'High', 'Critical') NOT NULL,
    description TEXT,
    reported_by VARCHAR(100) NOT NULL,
    status ENUM('Reported', 'Dispatched', 'Responding', 'Resolved') DEFAULT 'Reported',
    accident_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_severity (severity),
    INDEX idx_time (accident_time),
    INDEX idx_status (status)
);

-- 3. Hospitals Table
CREATE TABLE IF NOT EXISTS hospitals (
    hospital_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    available_beds INT NOT NULL,
    icu_beds INT DEFAULT 10,
    available_icu_beds INT DEFAULT 10,
    max_severity ENUM('Low', 'Medium', 'High', 'Critical') NOT NULL DEFAULT 'Medium',
    specialty VARCHAR(100) DEFAULT 'General',
    phone VARCHAR(20),
    x INT NOT NULL,
    y INT NOT NULL,
    latitude DECIMAL(10,8) DEFAULT 13.0827,
    longitude DECIMAL(11,8) DEFAULT 80.2707,
    INDEX idx_beds (available_beds),
    INDEX idx_specialty (specialty)
);

-- 4. Ambulances Table
CREATE TABLE IF NOT EXISTS ambulances (
    ambulance_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(20),
    current_x INT NOT NULL,
    current_y INT NOT NULL,
    latitude DECIMAL(10,8) DEFAULT 13.0827,
    longitude DECIMAL(11,8) DEFAULT 80.2707,
    status ENUM('green', 'yellow', 'red') DEFAULT 'green',
    fuel_level INT DEFAULT 100,
    driver_id INT,
    assigned_accident_id INT NULL,
    assigned_hospital_id INT NULL,
    target_x INT DEFAULT 0,
    target_y INT DEFAULT 0,
    last_maintenance DATE,
    INDEX idx_status (status),
    INDEX idx_driver (driver_id),
    INDEX idx_assigned_accident (assigned_accident_id)
);

-- 5. Messages Table
CREATE TABLE IF NOT EXISTS messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT,
    receiver_id INT,
    message TEXT,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_status BOOLEAN DEFAULT FALSE
);

-- 6. Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action VARCHAR(100),
    details TEXT,
    ip_address VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. Drivers Table
CREATE TABLE IF NOT EXISTS drivers (
    driver_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) UNIQUE,
    phone VARCHAR(20),
    email VARCHAR(100),
    shift_start TIME DEFAULT '08:00:00',
    shift_end TIME DEFAULT '20:00:00',
    status ENUM('available', 'on_duty', 'off_duty', 'on_leave') DEFAULT 'available',
    ambulance_id INT,
    total_trips INT DEFAULT 0,
    rating DECIMAL(2,1) DEFAULT 5.0,
    hire_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_ambulance (ambulance_id)
);

-- 8. Patients Table
CREATE TABLE IF NOT EXISTS patients (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    accident_id INT,
    name VARCHAR(100),
    age INT,
    gender ENUM('Male', 'Female', 'Other'),
    blood_type VARCHAR(5),
    phone VARCHAR(20),
    emergency_contact VARCHAR(100),
    emergency_phone VARCHAR(20),
    injury_type VARCHAR(100),
    injury_severity ENUM('Minor', 'Moderate', 'Severe', 'Critical') DEFAULT 'Moderate',
    vitals_bp VARCHAR(20),
    vitals_pulse INT,
    vitals_oxygen INT,
    status ENUM('At Scene', 'In Transit', 'Admitted', 'Discharged', 'Deceased') DEFAULT 'At Scene',
    hospital_id INT,
    ambulance_id INT,
    admitted_at TIMESTAMP NULL,
    discharged_at TIMESTAMP NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_accident (accident_id),
    INDEX idx_hospital (hospital_id),
    INDEX idx_status (status)
);

-- 9. Vehicle Maintenance Table
CREATE TABLE IF NOT EXISTS vehicle_maintenance (
    maintenance_id INT AUTO_INCREMENT PRIMARY KEY,
    ambulance_id INT NOT NULL,
    maintenance_type ENUM('Oil Change', 'Tire Rotation', 'Full Service', 'Repair', 'Inspection') NOT NULL,
    description TEXT,
    cost DECIMAL(10,2),
    mileage INT,
    service_date DATE,
    next_service_date DATE,
    performed_by VARCHAR(100),
    status ENUM('Scheduled', 'In Progress', 'Completed', 'Cancelled') DEFAULT 'Scheduled',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ambulance (ambulance_id),
    INDEX idx_status (status),
    INDEX idx_service_date (service_date)
);

-- 10. Incident Reports Table
CREATE TABLE IF NOT EXISTS incident_reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    accident_id INT NOT NULL,
    ambulance_id INT,
    driver_id INT,
    patient_id INT,
    hospital_id INT,
    dispatch_time TIMESTAMP,
    arrival_time TIMESTAMP,
    scene_departure_time TIMESTAMP,
    hospital_arrival_time TIMESTAMP,
    response_time_minutes INT,
    total_time_minutes INT,
    distance_km DECIMAL(6,2),
    outcome ENUM('Patient Stabilized', 'Patient Admitted', 'Patient Deceased', 'False Alarm', 'Other') DEFAULT 'Patient Admitted',
    weather_conditions VARCHAR(50),
    traffic_conditions ENUM('Light', 'Moderate', 'Heavy') DEFAULT 'Moderate',
    complications TEXT,
    notes TEXT,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_accident (accident_id),
    INDEX idx_ambulance (ambulance_id),
    INDEX idx_outcome (outcome)
);

-- 11. Response Times Table
CREATE TABLE IF NOT EXISTS response_times (
    id INT AUTO_INCREMENT PRIMARY KEY,
    accident_id INT NOT NULL,
    ambulance_id INT,
    dispatch_time TIMESTAMP,
    arrival_time TIMESTAMP,
    response_time_seconds INT,
    distance_meters INT,
    severity VARCHAR(20),
    location VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_accident (accident_id),
    INDEX idx_date (created_at)
);
```

2.4 Insertion of Tuples into the Table – DML Commands

Sample SQL commands to populate the system with initial data.

```sql
-- Inserting Default Admin User
INSERT INTO users (username, password, role, full_name, email)
VALUES ('admin', 'admin123', 'ADMIN', 'System Administrator', 'admin@system.com');

-- Inserting Hospitals
INSERT INTO hospitals (name, capacity, available_beds, icu_beds, available_icu_beds, x, y, latitude, longitude, specialty, phone, max_severity) VALUES
    ('Apollo Hospital', 200, 180, 30, 25, 100, 100, 13.0827, 80.2707, 'Trauma,Cardiac,General', '044-28291000', 'Critical'),
    ('MIOT International', 150, 130, 25, 20, 300, 150, 13.0067, 80.2206, 'Orthopedic,Trauma', '044-42001000', 'Critical'),
    ('Fortis Malar Hospital', 100, 85, 15, 12, 500, 200, 13.0358, 80.2415, 'Cardiac,General', '044-42892222', 'High'),
    ('SRM Hospital', 120, 100, 20, 18, 200, 300, 12.8231, 80.0442, 'General,Burns', '044-27456789', 'High'),
    ('Global Hospital', 180, 160, 28, 22, 400, 350, 13.0569, 80.2425, 'Neuro,Trauma,Cardiac', '044-44777000', 'Critical');

-- Inserting Ambulances
INSERT INTO ambulances (vehicle_number, current_x, current_y, latitude, longitude, status, fuel_level) VALUES
    ('TN01AB1234', 50, 50, 13.0827, 80.2707, 'green', 85),
    ('TN01CD5678', 150, 80, 13.0600, 80.2500, 'green', 92),
    ('TN01EF9012', 250, 120, 13.0400, 80.2300, 'green', 78),
    ('TN01GH3456', 350, 200, 13.0200, 80.2100, 'green', 95),
    ('TN01IJ7890', 450, 280, 13.0000, 80.1900, 'green', 88),
    ('TN01KL1234', 100, 350, 13.0900, 80.2800, 'green', 80),
    ('TN01MN5678', 550, 150, 13.0500, 80.2400, 'green', 90),
    ('TN01OP9012', 400, 50, 13.1100, 80.2900, 'green', 75),
    ('TN01QR3456', 50, 550, 13.0100, 80.2000, 'green', 85),
    ('TN01ST7890', 300, 400, 13.0700, 80.2600, 'green', 95),
    ('TN01UV1234', 200, 100, 13.0300, 80.2200, 'green', 82),
    ('TN01WX5678', 450, 450, 13.0950, 80.2850, 'green', 88),
    ('TN01YZ9012', 150, 500, 13.0250, 80.2150, 'green', 91),
    ('TN01AA3456', 250, 60, 13.1050, 80.2950, 'green', 79),
    ('TN01BB7890', 350, 350, 13.0550, 80.2450, 'green', 84);

-- Inserting Drivers
INSERT INTO drivers (name, license_number, phone, status, ambulance_id, total_trips, rating, hire_date) VALUES
    ('Rajesh Kumar', 'TN0120190012345', '9876543210', 'available', 1, 150, 4.8, '2020-01-15'),
    ('Suresh Babu', 'TN0120180054321', '9876543211', 'available', 2, 230, 4.9, '2019-06-20'),
    ('Kumar Shankar', 'TN0120200098765', '9876543212', 'available', 3, 85, 4.5, '2021-03-10'),
    ('Venkat Raman', 'TN0120170011111', '9876543213', 'on_leave', NULL, 320, 4.7, '2018-09-05'),
    ('Arun Prasad', 'TN0120210022222', '9876543214', 'available', 4, 45, 4.6, '2022-01-20');

-- Inserting a Sample Accident
INSERT INTO accidents (location, latitude, longitude, vehicle_id, severity, description, reported_by)
VALUES ('Mount Road, Chennai', 13.0400, 80.2400, 'TN01AB1234', 'High', 'Multi-vehicle collision near signal', 'AI System');
```

***

