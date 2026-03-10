# AI Accident Detector & Ambulance Dispatcher

A desktop application for managing accident response in a city. Reports an accident, finds the nearest hospital with available beds, dispatches the closest ambulance, and tracks everything on a live Google Maps view.

Built this as a college project (AOOP course) but went way beyond the requirements - added real Google Maps integration, weather-aware routing, analytics dashboards, and a full communication system between dispatchers and hospitals.

## How it works

1. An accident is reported with location, severity, and vehicle details
2. The system queries the database for the nearest hospital with available beds
3. It finds the closest available ambulance and calculates the route
4. The dispatcher gets a notification with all the details
5. Once dispatched, the ambulance location updates in real-time on the map
6. The hospital gets notified to prepare for the incoming patient

Everything runs through a MySQL database with proper foreign keys, audit logs, and role-based access (Admin, Dispatcher, Hospital Staff, Viewer).

## Architecture

Java Swing frontend with 16 custom panels, each handling a different part of the workflow:

```
Main.java                    → Main window, navigation, panel management
├── LoginDialog              → Auth with role-based access control
├── IncidentReportPanel      → Report accidents with severity classification
├── GoogleMapsPanel          → Live map with accident/hospital/ambulance markers
├── AmbulanceMovementSync    → Real-time ambulance position tracking
├── AmbulanceDriverManager   → Assign drivers, manage availability
├── FleetManagementPanel     → Vehicle fleet overview and status
├── HospitalManagementPanel  → Bed availability, contact info
├── AnalyticsDashboard       → Charts for response times, severity trends
├── CommunicationPanel       → Messaging between dispatchers and hospitals
├── WeatherService           → Weather data for routing decisions
├── NotificationManager      → Desktop notifications for new incidents
├── DatabaseManager          → All SQL operations, connection pooling
├── ConfigManager            → External config (API keys, DB credentials)
└── UITheme                  → Custom Swing look and feel
```

## Tech

- **Language**: Java (Swing for GUI)
- **Database**: MySQL with 6 tables (users, accidents, hospitals, ambulances, messages, audit_logs)
- **Maps**: Google Maps JavaScript API embedded in a JEditorPane
- **Weather**: OpenWeatherMap API for weather-aware dispatch decisions

## Database schema

| Table | Purpose |
|:------|:--------|
| users | Login credentials, roles (ADMIN/DISPATCHER/HOSPITAL_STAFF/VIEWER) |
| accidents | Location, severity, status, assigned ambulance and hospital |
| hospitals | Name, coordinates, bed capacity, contact numbers |
| ambulances | Vehicle number, driver, GPS coordinates, availability status |
| messages | Priority-based messaging between system users |
| audit_logs | Every action logged for accountability |

## Setup

**Prerequisites**: Java 8+, MySQL 8.0+, Google Maps API key

```bash
# 1. Create the database
mysql -u root -p -e "CREATE DATABASE accident_alert_system;"

# 2. Run the schema
mysql -u root -p accident_alert_system < CREATE_ALL_TABLES.sql

# 3. Update config.properties with your credentials
#    db.url, db.user, db.password, google.maps.api.key

# 4. Compile and run
javac -cp ".;lib/mysql-connector-j-8.2.0.jar" -d bin src/*.java
java -cp ".;bin;lib/mysql-connector-j-8.2.0.jar" Main
```

Or use the one-click launcher: `RUN-WITH-GOOGLE-MAPS.bat`

## What I'd improve

- Replace Swing with JavaFX or move to a web stack entirely
- Add actual accident detection from CCTV feeds using a YOLO model
- Push notifications via SMS/WhatsApp instead of just desktop
- Containerize the whole thing with Docker for easier deployment
