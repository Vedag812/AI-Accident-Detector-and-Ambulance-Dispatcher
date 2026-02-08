# AI Accident Detector & Ambulance Dispatcher - ER Model

This document provides the Entity Relationship (ER) model for the project's database schema.

## ER Diagram

```mermaid
erDiagram
    USER ||--o{ AUDIT_LOG : "performs"
    USER ||--o{ MESSAGE : "sends/receives"
    HOSPITAL ||--o{ AMBULANCE : "manages"
    HOSPITAL ||--o{ SURGERY_ROOM : "has"
    ACCIDENT ||--o{ AMBULANCE : "requires"
    ACCIDENT ||--o{ MESSAGE : "related to"

    USER {
        int id PK
        string username
        string password
        string role
        string full_name
        string email
        timestamp created_at
    }

    ACCIDENT {
        int accident_id PK
        string location
        string vehicle_id
        string severity
        text description
        string reported_by
        timestamp accident_time
    }

    HOSPITAL {
        int hospital_id PK
        string name
        int capacity
        int available_beds
        string max_severity
        int x
        int y
    }

    AMBULANCE {
        int ambulance_id PK
        int current_x
        int current_y
        string status
        int assigned_accident_id FK
        int assigned_hospital_id FK
    }

    MESSAGE {
        int id PK
        int sender_id FK
        int receiver_id FK
        int accident_id FK
        text message
        string priority
        timestamp sent_at
        boolean is_read
    }

    AUDIT_LOG {
        int id PK
        int user_id FK
        string action
        text details
        string ip_address
        timestamp timestamp
    }

    SURGERY_ROOM {
        int room_id PK
        int hospital_id FK
        string room_number
        string status
        string current_patient
        timestamp scheduled_until
    }
```

## Entity Descriptions

### 1. User
Stores authentication and profile information for system users (Admin, Dispatcher, Operator, etc.).

### 2. Accident
Main record for reported accidents, including location, severity, and reporting details.

### 3. Hospital
Information about hospitals, their locations, and bed capacities.

### 4. Ambulance
Tracks the fleet of ambulances, their current positions, and their dispatch status relative to accidents and hospitals.

### 5. Message
Handles internal communication between different system roles, often related to specific accidents.

### 6. Audit Log
A security feature tracking user actions and system changes for accountability.

### 7. Surgery Room
Tracks the availability and status of operating rooms within hospitals.
