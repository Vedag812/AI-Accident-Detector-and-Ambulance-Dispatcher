-- PostgreSQL Schema for Render.com deployment
-- Equivalent to CREATE_ALL_TABLES.sql but PostgreSQL-compatible

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) DEFAULT 'VIEWER',
    full_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username, password, full_name, role, email)
VALUES ('admin', 'admin123', 'System Administrator', 'ADMIN', 'admin@system.com')
ON CONFLICT (username) DO NOTHING;

CREATE TABLE IF NOT EXISTS accidents (
    accident_id SERIAL PRIMARY KEY,
    location VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION DEFAULT 0.0,
    longitude DOUBLE PRECISION DEFAULT 0.0,
    vehicle_id VARCHAR(100),
    severity VARCHAR(20) DEFAULT 'Medium',
    description TEXT,
    reported_by VARCHAR(100),
    weather_condition VARCHAR(50),
    traffic_density INT DEFAULT 50,
    casualties INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'Reported',
    accident_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hospitals (
    hospital_id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    x INT DEFAULT 0,
    y INT DEFAULT 0,
    latitude DOUBLE PRECISION DEFAULT 0.0,
    longitude DOUBLE PRECISION DEFAULT 0.0,
    available_beds INT DEFAULT 10,
    capacity INT DEFAULT 50,
    max_severity VARCHAR(50) DEFAULT 'Critical',
    contact_number VARCHAR(20),
    phone VARCHAR(20),
    specialty VARCHAR(100),
    available_icu_beds INT DEFAULT 0,
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO hospitals (hospital_id, name, x, y, latitude, longitude, available_beds, capacity, contact_number, address)
VALUES
    (1, 'Apollo Hospital', 200, 150, 13.0358, 80.2464, 15, 100, '044-28296000', 'Greams Road, Chennai'),
    (2, 'MIOT Hospital', 600, 400, 13.0890, 80.2100, 20, 120, '044-42002000', 'Manapakkam, Chennai'),
    (3, 'Fortis Malar', 350, 250, 13.0569, 80.2540, 12, 80, '044-42899000', 'Adyar, Chennai')
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS drivers (
    driver_id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    phone VARCHAR(20),
    license_no VARCHAR(50),
    status VARCHAR(20) DEFAULT 'active',
    rating DECIMAL(3,1) DEFAULT 4.5
);

INSERT INTO drivers (driver_id, name, phone, license_no, status, rating)
VALUES
    (1, 'Rajesh Kumar', '9876543210', 'TN-DL-001', 'active', 4.8),
    (2, 'Suresh Babu', '9876543211', 'TN-DL-002', 'active', 4.5),
    (3, 'Venkat Raman', '9876543212', 'TN-DL-003', 'active', 4.7)
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS ambulances (
    ambulance_id SERIAL PRIMARY KEY,
    vehicle_number VARCHAR(50),
    current_x INT DEFAULT 0,
    current_y INT DEFAULT 0,
    latitude DOUBLE PRECISION DEFAULT 0.0,
    longitude DOUBLE PRECISION DEFAULT 0.0,
    status VARCHAR(50) DEFAULT 'green',
    assigned_accident_id INT REFERENCES accidents(accident_id) ON DELETE SET NULL,
    assigned_hospital_id INT REFERENCES hospitals(hospital_id) ON DELETE SET NULL,
    driver_id INT REFERENCES drivers(driver_id) ON DELETE SET NULL,
    driver_name VARCHAR(100),
    driver_contact VARCHAR(20),
    fuel_level INT DEFAULT 100,
    target_x DOUBLE PRECISION,
    target_y DOUBLE PRECISION,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hospital_staff (
    staff_id SERIAL PRIMARY KEY,
    hospital_id INT REFERENCES hospitals(hospital_id) ON DELETE CASCADE,
    name VARCHAR(100),
    role VARCHAR(50),
    specialization VARCHAR(100),
    contact VARCHAR(20),
    shift VARCHAR(20),
    availability VARCHAR(20) DEFAULT 'Available'
);

CREATE TABLE IF NOT EXISTS equipment_inventory (
    equipment_id SERIAL PRIMARY KEY,
    hospital_id INT REFERENCES hospitals(hospital_id) ON DELETE CASCADE,
    item_name VARCHAR(100),
    category VARCHAR(50),
    quantity INT,
    min_threshold INT,
    status VARCHAR(50),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ambulance_requests (
    request_id SERIAL PRIMARY KEY,
    accident_id INT REFERENCES accidents(accident_id) ON DELETE CASCADE,
    ambulance_id INT REFERENCES ambulances(ambulance_id) ON DELETE SET NULL,
    hospital_id INT REFERENCES hospitals(hospital_id) ON DELETE SET NULL,
    status VARCHAR(50),
    estimated_time INT,
    actual_time INT,
    request_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    pickup_time TIMESTAMP,
    delivery_time TIMESTAMP,
    route_data TEXT
);

CREATE TABLE IF NOT EXISTS audit_logs (
    log_id SERIAL PRIMARY KEY,
    user_id INT,
    action VARCHAR(100),
    details TEXT,
    ip_address VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS messages (
    message_id SERIAL PRIMARY KEY,
    sender_id INT,
    content TEXT,
    channel VARCHAR(50) DEFAULT 'general',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS surgery_rooms (
    room_id SERIAL PRIMARY KEY,
    hospital_id INT REFERENCES hospitals(hospital_id) ON DELETE CASCADE,
    room_number VARCHAR(20),
    room_type VARCHAR(50),
    status VARCHAR(30) DEFAULT 'Available',
    current_patient VARCHAR(100)
);
