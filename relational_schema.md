# Relational Schema - AI Accident Detector & Ambulance Dispatcher

This document details the relational schema for the project's database, including table structures, data types, and constraints.

## Visual Schema (ER Diagram)

```mermaid
erDiagram
    USERS ||--o{ AUDIT_LOGS : "logs"
    USERS ||--o{ MESSAGES : "sends/receives"
    HOSPITALS ||--o{ AMBULANCES : "manages"
    HOSPITALS ||--o{ HOSPITAL_STAFF : "employs"
    HOSPITALS ||--o{ EQUIPMENT_INVENTORY : "stocks"
    HOSPITALS ||--o{ SURGERY_ROOMS : "contains"
    ACCIDENTS ||--o{ AMBULANCE_REQUESTS : "triggers"
    AMBULANCES ||--o{ AMBULANCE_REQUESTS : "fulfills"
    HOSPITALS ||--o{ AMBULANCE_REQUESTS : "receives_patient"
    ACCIDENTS ||--o{ MESSAGES : "relates_to"

    USERS {
        int id PK
        varchar username
        varchar password
        enum role
        varchar full_name
        varchar email
        varchar phone
        timestamp created_at
    }

    ACCIDENTS {
        int accident_id PK
        varchar location
        double latitude
        double longitude
        varchar vehicle_id
        enum severity
        text description
        varchar reported_by
        varchar weather_condition
        int traffic_density
        int casualties
        varchar status
        timestamp accident_time
    }

    HOSPITALS {
        int hospital_id PK
        varchar name
        int x
        int y
        double latitude
        double longitude
        int available_beds
        int capacity
        varchar max_severity
        varchar contact_number
        text address
        timestamp created_at
    }

    AMBULANCES {
        int ambulance_id PK
        varchar vehicle_number
        int current_x
        int current_y
        double latitude
        double longitude
        varchar status
        int assigned_accident_id FK
        int assigned_hospital_id FK
        varchar driver_name
        varchar driver_contact
        timestamp last_updated
    }

    HOSPITAL_STAFF {
        int staff_id PK
        int hospital_id FK
        varchar name
        varchar role
        varchar specialization
        varchar contact
        varchar shift
        varchar availability
    }

    EQUIPMENT_INVENTORY {
        int equipment_id PK
        int hospital_id FK
        varchar item_name
        varchar category
        int quantity
        int min_threshold
        varchar status
        timestamp last_updated
    }

    AMBULANCE_REQUESTS {
        int request_id PK
        int accident_id FK
        int ambulance_id FK
        int hospital_id FK
        varchar status
        int estimated_time
        int actual_time
        timestamp request_time
        timestamp pickup_time
        timestamp delivery_time
        text route_data
    }

    AUDIT_LOGS {
        int log_id PK
        int user_id FK
        varchar action
        text details
        varchar ip_address
        timestamp timestamp
    }

    MESSAGES {
        int message_id PK
        int sender_id FK
        int receiver_id FK
        int accident_id FK
        text message_text
        varchar priority
        boolean is_read
        timestamp sent_at
    }

    SURGERY_ROOMS {
        int room_id PK
        int hospital_id FK
        varchar room_number
        varchar status
        varchar current_patient
        timestamp scheduled_until
    }
```

## Table Specifications

### 1. USERS
| Attribute | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | PK, AUTO_INCREMENT | Unique user identifier |
| `username` | VARCHAR(50) | UNIQUE, NOT NULL | Login name |
| `password` | VARCHAR(255) | NOT NULL | Encrypted password |
| `role` | ENUM | ADMIN, DISPATCHER, etc. | System access level |
| `full_name` | VARCHAR(100) | | User's full name |
| `email` | VARCHAR(100) | | Email address |
| `phone` | VARCHAR(20) | | Contact number |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Account creation time |

### 2. ACCIDENTS
| Attribute | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `accident_id` | INT | PK, AUTO_INCREMENT | Unique accident identifier |
| `location` | VARCHAR(255) | NOT NULL | Location description |
| `latitude` | DOUBLE | DEFAULT 0.0 | GPS Latitude |
| `longitude` | DOUBLE | DEFAULT 0.0 | GPS Longitude |
| `severity` | ENUM | Low, Medium, High, Critical | Urgency level |
| `status` | VARCHAR(50) | DEFAULT 'Reported' | Current lifecycle state |

### 3. AMBULANCES
| Attribute | Data Type | Constraints | Description |
| :--- | :--- | : :--- | :--- |
| `ambulance_id` | INT | PK, AUTO_INCREMENT | Unique ambulance identifier |
| `vehicle_number` | VARCHAR(50) | | License plate number |
| `status` | VARCHAR(50) | DEFAULT 'green' | Availability/Alert status |
| `assigned_accident_id` | INT | FK -> ACCIDENTS | Current task |
| `assigned_hospital_id`| INT | FK -> HOSPITALS | Home/Current base |

### 4. HOSPITALS
| Attribute | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `hospital_id` | INT | PK, AUTO_INCREMENT | Unique hospital identifier |
| `name` | VARCHAR(200) | NOT NULL | Hospital name |
| `available_beds` | INT | | Currently free beds |
| `capacity` | INT | | Total bed capacity |

### 5. AMBULANCE_REQUESTS
| Attribute | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `request_id` | INT | PK, AUTO_INCREMENT | Unique request identifier |
| `accident_id` | INT | FK -> ACCIDENTS | Linked accident |
| `ambulance_id` | INT | FK -> AMBULANCES | Assigned unit |
| `hospital_id` | INT | FK -> HOSPITALS | Destination hospital |
| `status` | VARCHAR(50) | | Pickup, In Transit, Delivered |
