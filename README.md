# 🚨 Accident Alert System with Google Maps Integrationcompile-and-run.bat# AI-Assisted Accident Detection & Ambulance Simulator - Enhanced Edition



## 📋 Table of Contents## 🚀 Features Implemented

1. [Overview](#overview)

2. [Quick Start](#quick-start)### 1. Real-Time Traffic & Route Optimization ✅

3. [Features](#features)- Dynamic traffic density simulation

4. [Complete Setup Guide](#complete-setup-guide)- A* pathfinding algorithm for optimal routes

5. [Database Setup](#database-setup)- ETA calculations based on traffic and weather

6. [Running the Application](#running-the-application)- Traffic heatmap visualization

7. [Google Maps Integration](#google-maps-integration)- Multiple route options with distance calculations

8. [User Guide](#user-guide)

9. [Troubleshooting](#troubleshooting)### 2. Communication System ✅

10. [Technical Details](#technical-details)- Real-time messaging between dispatchers, ambulances, and hospitals

- Priority-based messaging (Normal, High, Critical)

---- Message history with read/unread status

- Auto-refresh message feed

## 🎯 Overview- Toast notifications for new messages



A comprehensive **Accident Detection & Ambulance Management System** with real-time Google Maps integration, built with Java Swing and MySQL.### 3. Enhanced Hospital Management ✅

- Bed availability tracking and management

### Key Highlights:- Surgery room status monitoring

- ✅ Real-time accident reporting and tracking- Equipment inventory management

- ✅ Intelligent ambulance dispatch system- Staff availability tracking

- ✅ **Google Maps integration** with color-coded markers- Resource allocation system

- ✅ Hospital management with bed availability

- ✅ Weather-aware routing### 4. Analytics & Reporting ✅

- ✅ Traffic optimization- Real-time statistics dashboard

- ✅ Communication center for messaging- Severity distribution pie charts

- ✅ Analytics dashboard with charts- Status distribution bar charts

- ✅ User authentication and role-based access- Hospital bed availability charts

- Response time analysis

---- Export to CSV functionality



## 🚀 Quick Start### 5. User Authentication & Management ✅

- Role-based access control (Admin, Dispatcher, Hospital Staff, Viewer)

### One-Click Launch (Recommended)- Secure login system

```- Audit logging for all actions

Double-click: RUN-WITH-GOOGLE-MAPS.bat- User session management

```- Multi-user support



### Login Credentials:### 6. Weather Integration ✅

- **Username**: `admin`- Real-time weather data integration (simulated)

- **Password**: `admin123`- Weather impact on ambulance speed

- Visual weather indicators

That's it! The application will compile and launch automatically.- Weather-based route adjustments

- Support for OpenWeatherMap API

---

### 7. Advanced Simulation Features ✅

## ✨ Features- Multi-casualty accidents

- Disaster mode simulation

### 1. 🚨 Accident Management- Configurable AI accident generation

- Report accidents with location, vehicle ID, severity- Real-time ambulance tracking

- AI-based severity prediction from description- Dynamic hospital selection based on availability

- Real-time status tracking (Pending → Dispatched → En Route → Resolved)

- Priority-based handling### 8. Google Maps Integration 🔄

- Export to CSV- Ready for Google Maps JavaScript API integration

- Latitude/Longitude coordinate system

### 2. 🗺️ Google Maps Integration- Real-world address support

- **Interactive Google Maps** showing all accidents, hospitals, and ambulances- Distance calculation using Haversine formula

- **Color-coded markers**:

  - 🔴 **Critical** - Red### 9. Modern UI/UX ✅

  - 🟠 **High** - Orange- Professional color scheme

  - 🟡 **Medium** - Yellow- Material Design inspired components

  - 🟢 **Low** - Green- Toast notifications

  - 🔵 **Hospitals** - Blue (with bed availability)- Smooth animations

  - 🟣 **Ambulances** - Purple (with vehicle status)- Responsive layouts

- **Traffic layer** for real-time traffic conditions- Dark mode support

- **Interactive legend** and controls- Interactive charts and visualizations

- **One-click generation** - opens in browser

## 📋 Prerequisites

### 3. 🚑 Smart Ambulance Dispatch

- Automatic ambulance assignment based on:1. **Java Development Kit (JDK) 17 or higher**

  - Distance to accident   - Download from: https://www.oracle.com/java/technologies/downloads/

  - Hospital capacity

  - Ambulance availability2. **MySQL Server 8.0 or higher**

  - Traffic conditions   - Download from: https://dev.mysql.com/downloads/mysql/

  - Weather impact

- Real-time ambulance tracking on map3. **MySQL Connector/J (JDBC Driver)**

- ETA calculation   - Download from: https://dev.mysql.com/downloads/connector/j/

   - Or use Maven/Gradle dependency

### 4. 🏥 Hospital Management

- Track 5 Chennai hospitals:4. **IDE (Choose one)**

  - Apollo Hospital   - IntelliJ IDEA (Recommended): https://www.jetbrains.com/idea/download/

  - Fortis Malar Hospital   - Visual Studio Code with Java Extension Pack

  - MIOT International

  - Sri Ramachandra Medical Centre## 🛠️ Setup Instructions

  - Gleneagles Global Health City

- Real-time bed availability### Step 1: Database Setup

- Update bed counts through portal

- Capacity-based patient routing1. Start MySQL Server

2. Create the database:

### 5. 🌤️ Weather Integration```sql

- Simulated weather conditionsCREATE DATABASE accident_alert_system;

- Speed impact calculations```

- Weather-aware route optimization

3. Update database credentials in `config.properties`:

### 6. 🚦 Traffic Optimization```properties

- Traffic level simulation (Light, Moderate, Heavy, Severe)db.url=jdbc:mysql://localhost:3306/accident_alert_system?useSSL=false&serverTimezone=UTC

- Haversine distance calculationsdb.user=root

- Optimal route findingdb.password=YOUR_MYSQL_PASSWORD

- ETA adjustments```



### 7. 💬 Communication Center4. The application will automatically create all required tables on first run.

- Message system between users

- Priority levels (Low, Medium, High, Urgent)### Step 2: Download MySQL JDBC Driver

- Real-time message delivery

**Option A: Manual Download**

### 8. 📊 Analytics Dashboard1. Download `mysql-connector-j-8.2.0.jar` from https://dev.mysql.com/downloads/connector/j/

- Real-time statistics2. Place it in the project `lib` folder (create if doesn't exist)

- Severity distribution pie chart

- Status breakdown bar chart**Option B: Maven (if using Maven)**

- Response time analysisAdd to `pom.xml`:

```xml

### 9. 🔐 Authentication & Security<dependency>

- User login system    <groupId>com.mysql</groupId>

- Role-based access (Admin, Dispatcher, Hospital Staff, Viewer)    <artifactId>mysql-connector-j</artifactId>

- Audit logging    <version>8.2.0</version>

</dependency>

### 10. 🔔 Notification System```

- Toast notifications

- Color-coded alerts### Step 3: Google Maps API Setup (Optional)

- Sound alerts for critical events

1. Go to https://console.cloud.google.com/google/maps-apis

---2. Create a new project or select existing

3. Enable "Maps JavaScript API"

## 🔧 Complete Setup Guide4. Create credentials (API Key)

5. Add API key to `config.properties`:

### Step 1: Prerequisites```properties

google.maps.api.key=YOUR_GOOGLE_MAPS_API_KEY

#### Install Java JDK 17+```

```powershell

# Check if Java is installed### Step 4: Weather API Setup (Optional)

java -version

javac -version1. Sign up at https://openweathermap.org/api

```2. Get free API key

3. Add to `config.properties`:

If not installed, download from: https://www.oracle.com/java/technologies/downloads/```properties

weather.api.key=YOUR_WEATHER_API_KEY

#### Install MySQL 8.0+```

```powershell

# Check if MySQL is installed## 🖥️ Running in IntelliJ IDEA

mysql --version

```### Method 1: Import as Project

1. Open IntelliJ IDEA

If not installed, download from: https://dev.mysql.com/downloads/mysql/2. File → Open → Select project folder

3. Wait for indexing to complete

### Step 2: Verify Project Structure4. Right-click `Main.java` → Run 'Main.main()'

```

TestJdbc/### Method 2: Add Library Dependencies

├── src/                          # Java source files1. File → Project Structure (Ctrl+Alt+Shift+S)

│   ├── Main.java2. Libraries → + → Java

│   ├── GoogleMapsPanel.java3. Select `mysql-connector-j-8.2.0.jar`

│   ├── DatabaseManager.java4. Apply → OK

│   ├── ConfigManager.java5. Right-click `Main.java` → Run

│   └── ... (other Java files)

├── lib/                          # External libraries### Method 3: Using Run Configuration

│   └── mysql-connector-j-9.4.0.jar1. Run → Edit Configurations

├── config.properties             # Configuration2. + → Application

└── RUN-WITH-GOOGLE-MAPS.bat      # Launch script3. Main class: `Main`

```4. Working directory: `[Your project path]`

5. Classpath: Select your module

### Step 3: Configure Database6. OK → Run



Edit `config.properties`:## 💻 Running in VS Code

```properties

# Database Configuration### Setup Java in VS Code

db.url=jdbc:mysql://localhost:3306/accident_alert_system1. Install Extension Pack for Java

db.user=root2. Install MySQL extension (optional)

db.password=Vedant@0393. Open project folder in VS Code



# Google Maps API### Configure Classpath

google.maps.api.key=AIzaSyAhJyy219zxBaaR-n6Ai6RMS5jOGxuk5p8Create `.vscode/settings.json`:

```json

# Other settings{

weather.update.interval=300000    "java.project.sourcePaths": ["src"],

notification.sound.enabled=true    "java.project.outputPath": "bin",

notification.display.duration=5000    "java.project.referencedLibraries": [

```        "lib/**/*.jar"

    ]

---}

```

## 🗄️ Database Setup

### Run the Application

### IMPORTANT: Run This First!**Method 1: Using Code Runner**

1. Install "Code Runner" extension

Open PowerShell and run these commands:2. Open `Main.java`

3. Press F5 or click Run button

```powershell

# Create database**Method 2: Using Terminal**

mysql -u root -pVedant@039 -e "CREATE DATABASE IF NOT EXISTS accident_alert_system;"```powershell

# Navigate to project folder

# Create users tablecd "c:\Users\VEDANT\Downloads\AOOP PROJECT\TestJdbc"

mysql -u root -pVedant@039 accident_alert_system -e "CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL, role ENUM('ADMIN', 'DISPATCHER', 'HOSPITAL_STAFF', 'VIEWER') DEFAULT 'VIEWER', full_name VARCHAR(100), email VARCHAR(100), phone VARCHAR(20), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);"

# Compile (with JDBC driver in classpath)

# Insert admin userjavac -cp ".;lib\mysql-connector-j-8.2.0.jar" -d bin src\*.java

mysql -u root -pVedant@039 accident_alert_system -e "INSERT INTO users (username, password, role, full_name, email) VALUES ('admin', 'admin123', 'ADMIN', 'System Administrator', 'admin@system.com') ON DUPLICATE KEY UPDATE username=username;"

# Run

# Create accidents tablejava -cp ".;bin;lib\mysql-connector-j-8.2.0.jar" Main

mysql -u root -pVedant@039 accident_alert_system -e "CREATE TABLE IF NOT EXISTS accidents (id INT AUTO_INCREMENT PRIMARY KEY, location VARCHAR(255) NOT NULL, vehicle_id VARCHAR(50), severity ENUM('Low', 'Medium', 'High', 'Critical') NOT NULL, description TEXT, reported_by VARCHAR(100), accident_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, status ENUM('Pending', 'Dispatched', 'En Route', 'Resolved') DEFAULT 'Pending', assigned_ambulance_id INT, assigned_hospital_id INT);"```



# Create hospitals table**Method 3: Using launch.json**

mysql -u root -pVedant@039 accident_alert_system -e "CREATE TABLE IF NOT EXISTS hospitals (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, address VARCHAR(255), latitude DOUBLE NOT NULL, longitude DOUBLE NOT NULL, total_beds INT DEFAULT 100, available_beds INT DEFAULT 50, phone VARCHAR(20), emergency_contact VARCHAR(20));"Create `.vscode/launch.json`:

```json

# Create ambulances table{

mysql -u root -pVedant@039 accident_alert_system -e "CREATE TABLE IF NOT EXISTS ambulances (id INT AUTO_INCREMENT PRIMARY KEY, vehicle_number VARCHAR(50) UNIQUE NOT NULL, driver_name VARCHAR(100), driver_phone VARCHAR(20), status ENUM('Available', 'Dispatched', 'En Route', 'At Hospital') DEFAULT 'Available', current_latitude DOUBLE DEFAULT 13.0827, current_longitude DOUBLE DEFAULT 80.2707, last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);"    "version": "0.2.0",

    "configurations": [

# Create messages table        {

mysql -u root -pVedant@039 accident_alert_system -e "CREATE TABLE IF NOT EXISTS messages (id INT AUTO_INCREMENT PRIMARY KEY, sender_id INT, receiver_id INT, message TEXT, priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM', sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, read_status BOOLEAN DEFAULT FALSE, FOREIGN KEY (sender_id) REFERENCES users(id), FOREIGN KEY (receiver_id) REFERENCES users(id));"            "type": "java",

            "name": "Launch Main",

# Create audit_logs table            "request": "launch",

mysql -u root -pVedant@039 accident_alert_system -e "CREATE TABLE IF NOT EXISTS audit_logs (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT, action VARCHAR(100), details TEXT, ip_address VARCHAR(50), timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (user_id) REFERENCES users(id));"            "mainClass": "Main",

            "projectName": "TestJdbc",

# Insert hospitals            "classPaths": [

mysql -u root -pVedant@039 accident_alert_system -e "INSERT INTO hospitals (name, address, latitude, longitude, total_beds, available_beds, phone) VALUES ('Apollo Hospital', 'Greams Road, Chennai', 13.0569, 80.2557, 150, 45, '044-28296000'), ('Fortis Malar Hospital', 'Adyar, Chennai', 13.0067, 80.2560, 120, 38, '044-42892222'), ('MIOT International', 'Manapakkam, Chennai', 13.0199, 80.1680, 200, 67, '044-42002000'), ('Sri Ramachandra Medical Centre', 'Porur, Chennai', 13.0358, 80.1560, 180, 52, '044-45928453'), ('Gleneagles Global Health City', 'Perumbakkam, Chennai', 12.9127, 80.2265, 250, 89, '044-44777000') ON DUPLICATE KEY UPDATE name=name;"                "${workspaceFolder}/bin",

                "${workspaceFolder}/lib/*"

# Insert ambulances            ]

mysql -u root -pVedant@039 accident_alert_system -e "INSERT INTO ambulances (vehicle_number, driver_name, driver_phone, status, current_latitude, current_longitude) VALUES ('TN01AB1234', 'Rajesh Kumar', '9876543210', 'Available', 13.0827, 80.2707), ('TN01CD5678', 'Suresh Babu', '9876543211', 'Available', 13.0450, 80.2350), ('TN01EF9012', 'Vijay Anand', '9876543212', 'Available', 13.1200, 80.2900) ON DUPLICATE KEY UPDATE vehicle_number=vehicle_number;"        }

    ]

# Verify setup}

mysql -u root -pVedant@039 accident_alert_system -e "SHOW TABLES;"```

mysql -u root -pVedant@039 accident_alert_system -e "SELECT username, role FROM users;"

```## 📁 Project Structure



### Quick Database Setup Script```

TestJdbc/

Or save this as `setup-database.bat` and run it:├── src/

│   ├── Main.java                      # Main application entry point

```batch│   ├── ConfigManager.java             # Configuration management

@echo off│   ├── DatabaseManager.java           # Database operations

echo Setting up database...│   ├── RouteOptimizer.java            # Traffic & routing logic

mysql -u root -pVedant@039 -e "CREATE DATABASE IF NOT EXISTS accident_alert_system;"│   ├── WeatherService.java            # Weather integration

mysql -u root -pVedant@039 accident_alert_system < database-schema.sql│   ├── NotificationManager.java       # Notification system

echo Database setup complete!│   └── CommunicationPanel.java        # Messaging system

pause├── lib/

```│   └── mysql-connector-j-8.2.0.jar   # MySQL JDBC driver

├── config.properties                  # Application configuration

---└── README.md                          # This file

```

## ▶️ Running the Application

## 🎯 Default Login Credentials

### Method 1: Batch File (Easiest)

```- **Username:** admin

1. Double-click: RUN-WITH-GOOGLE-MAPS.bat- **Password:** admin123

2. Wait for compilation (10-15 seconds)- **Role:** Administrator

3. Application launches automatically

4. Login with admin/admin123## 🚀 Quick Start Guide

```

1. **Start MySQL Server**

### Method 2: Command Line2. **Update config.properties** with your database password

```powershell3. **Compile and run Main.java**

cd "c:\Users\VEDANT\Downloads\AOOP PROJECT\TestJdbc"4. **The application will:**

   - Create database tables automatically

# Compile   - Load with default data

javac -cp ".;lib\mysql-connector-j-9.4.0.jar" -d . src\*.java   - Start AI accident simulation

   - Display interactive map and dashboard

# Run

java -cp ".;lib\mysql-connector-j-9.4.0.jar" Main## 🎨 UI Features

```

### Dashboard

### Method 3: IntelliJ IDEA- Real-time statistics

```- Active ambulance count

1. File → Open → Select TestJdbc folder- Severity distribution

2. File → Project Structure → Libraries → Add mysql-connector-j-9.4.0.jar- Average response time

3. Right-click Main.java → Run 'Main.main()'

```### Interactive Map

- Click and drag to pan

### Method 4: VS Code- Double-click accident to center

```- Real-time ambulance movement

1. File → Open Folder → Select TestJdbc- Hospital locations with bed availability

2. Install "Extension Pack for Java" (if not installed)- Traffic heatmap overlay

3. Open Main.java

4. Press F5 or click Run### Accident Management

```- Report new accidents

- Edit severity levels

---- Delete accidents

- Export to CSV

## 🗺️ Google Maps Integration Guide- Search and filter



### How to Use:### Hospital Portal

- Manage bed availability

#### Step 1: Launch Application- Track surgery rooms

```- Equipment inventory

- Run: RUN-WITH-GOOGLE-MAPS.bat- Staff scheduling

- Login: admin/admin123

```### Communication Center

- Send priority messages

#### Step 2: Report Some Accidents (Optional)- Real-time chat

```- Message history

1. Fill Location: "Chennai, T Nagar"- Auto-refresh

2. Fill Vehicle ID: "TN01AB1234"

3. Fill Reporter: Your name### Analytics Dashboard

4. Select Severity or leave blank- Pie charts for severity distribution

5. Click "🚨 Report Accident"- Bar charts for status tracking

```- Hospital bed availability

- Summary statistics

#### Step 3: Open Google Maps

```## ⚙️ Configuration Options

1. Click the "🗺️ Google Maps" button in toolbar

2. Wait 2-3 seconds for map generationEdit `config.properties` to customize:

3. Browser opens automatically

4. Interactive map displays!```properties

```# AI accident generation interval (seconds)

app.ai.interval=20

#### Step 4: Interact with Map

```# Default map center (Chennai, India)

- Click markers to see detailsapp.default.latitude=13.0827

- Zoom in/out with mouse wheelapp.default.longitude=80.2707

- Pan by dragging the mapapp.default.city=Chennai

- Toggle traffic layer

- View legend for marker types# Enable/disable notifications

```notification.sound.enabled=true

notification.toast.enabled=true

### Marker Types:```



| Type | Color | Icon | Description |## 🐛 Troubleshooting

|------|-------|------|-------------|

| **Critical Accident** | 🔴 Red | 🚨 | Immediate attention |### Database Connection Error

| **High Accident** | 🟠 Orange | 🚨 | Serious accident |- Ensure MySQL Server is running

| **Medium Accident** | 🟡 Yellow | 🚨 | Moderate accident |- Check username/password in config.properties

| **Low Accident** | 🟢 Green | 🚨 | Minor accident |- Verify database exists: `accident_alert_system`

| **Hospital** | 🔵 Blue | 🏥 | Available beds shown |

| **Ambulance** | 🟣 Purple | 🚑 | Vehicle status shown |### ClassNotFoundException: com.mysql.cj.jdbc.Driver

- Add MySQL Connector/J to classpath

### Map Features:- In IntelliJ: File → Project Structure → Libraries → Add JAR

- ✅ Real-time traffic layer- In VS Code: Add to `.vscode/settings.json` referencedLibraries

- ✅ Interactive markers with popups

- ✅ Color-coded by severity### Cannot find symbol errors

- ✅ Zoom and pan controls- Ensure all .java files are in the `src` folder

- ✅ Legend display- Clean and rebuild project

- ✅ Responsive design- Check Java version (requires JDK 17+)



---### UI Not Displaying Properly

- Check screen resolution (minimum 1280x720)

## 📖 User Guide- Update graphics drivers

- Try running with `-Dsun.java2d.uiScale=1.0` VM option

### 1. Reporting an Accident

## 📊 Database Schema

```

FORM FIELDS:The application creates these tables:

├── Location: e.g., "Chennai, Anna Nagar"- `accidents` - Accident records

├── Vehicle ID: e.g., "TN01XY5678"- `users` - User accounts and authentication

├── Reported By: Your name- `hospital_staff` - Hospital personnel

├── Severity: Select or leave blank for AI- `equipment_inventory` - Hospital equipment tracking

└── Description: Details about accident- `ambulance_requests` - Ambulance dispatch records

- `audit_logs` - System audit trail

CLICK: 🚨 Report Accident- `messages` - Communication messages

- `surgery_rooms` - Surgery room availability

RESULT:

├── Accident saved to database## 🔐 Security Notes

├── Ambulance dispatched automatically

├── Appears on map- Default passwords should be changed in production

└── Notification sent- Password hashing should be implemented (currently plain text for demo)

```- SQL injection prevention using PreparedStatements

- Audit logging for all critical actions

### 2. Viewing Google Maps

## 📈 Performance Tips

```

ACTION: Click "🗺️ Google Maps"- Adjust AI interval for better performance (increase value)

- Limit map animation updates if CPU usage is high

SYSTEM DOES:- Use database indexes for large datasets

├── Load all accidents- Clear old accidents periodically

├── Load all hospitals

├── Load all ambulances## 🆘 Support

├── Generate HTML map

└── Open in browserFor issues or questions:

1. Check troubleshooting section above

YOU SEE:2. Review console logs for error messages

├── Interactive Google Maps3. Verify all prerequisites are installed

├── Color-coded markers4. Ensure config.properties is correctly set up

├── Traffic layer toggle

└── Legend## 📝 License

```

This project is for educational purposes.

### 3. Managing Hospitals

## 🙏 Acknowledgments

```

ACTION: Click "🏥 Hospital Portal"- MySQL for database management

- Java Swing for UI framework

STEPS:- OpenWeatherMap for weather data (optional)

1. Select hospital from dropdown- Google Maps for mapping (optional)

2. Enter new available bed count
3. Click "Update Bed Availability"
4. Changes reflect immediately
```

### 4. Viewing Analytics

```
ACTION: Click "📈 Charts"

YOU SEE:
├── Severity distribution (pie chart)
├── Status breakdown (bar chart)
├── Total accidents count
├── Average response time
└── Trend analysis
```

### 5. Sending Messages

```
MENU: Tools → Communication Center

STEPS:
1. Select recipient
2. Choose priority (Low/Medium/High/Urgent)
3. Type message
4. Click "Send Message"
5. Recipient gets notification
```

### 6. Searching & Filtering

```
SEARCH BOX: Type anything
├── Location: "Chennai"
├── Vehicle: "TN01"
├── Severity: "High"
└── Reporter: Name

Table filters automatically!
Click "Clear" to reset.
```

### 7. Editing Accidents

```
EDIT SEVERITY:
1. Select row in table
2. Click "✏️ Edit Severity"
3. Choose new severity
4. Click OK

DELETE:
├── Delete Selected: Select row + Click ❌
└── Delete All: Click 🗑️ (requires confirmation)
```

### 8. Exporting Data

```
ACTION: Click "📄 Export CSV"

RESULT:
├── Choose save location
├── File saved with timestamp
├── Format: accidents_YYYYMMDD_HHMMSS.csv
└── Open in Excel
```

---

## 🔧 Troubleshooting

### ❌ Error: "Table 'users' doesn't exist"

This is the most common issue! **Fix it now:**

```powershell
mysql -u root -pVedant@039 accident_alert_system -e "CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL, role ENUM('ADMIN', 'DISPATCHER', 'HOSPITAL_STAFF', 'VIEWER') DEFAULT 'VIEWER', full_name VARCHAR(100), email VARCHAR(100), phone VARCHAR(20), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP); INSERT INTO users (username, password, role, full_name, email) VALUES ('admin', 'admin123', 'ADMIN', 'System Administrator', 'admin@system.com') ON DUPLICATE KEY UPDATE username=username;"
```

**Verify fix:**
```powershell
mysql -u root -pVedant@039 accident_alert_system -e "SELECT username, role FROM users;"
```

Should show:
```
+----------+-------+
| username | role  |
+----------+-------+
| admin    | ADMIN |
+----------+-------+
```

### ❌ Error: "Cannot connect to database"

**Check MySQL is running:**
```powershell
net start MySQL80
```

**Test connection:**
```powershell
mysql -u root -pVedant@039 -e "SELECT 1;"
```

**Verify database exists:**
```powershell
mysql -u root -pVedant@039 -e "SHOW DATABASES;" | findstr accident_alert_system
```

**Check config.properties:**
```properties
db.url=jdbc:mysql://localhost:3306/accident_alert_system
db.user=root
db.password=Vedant@039
```

### ❌ Error: "Class not found: com.mysql.cj.jdbc.Driver"

**Check jar file exists:**
```powershell
ls lib\mysql-connector-j-9.4.0.jar
```

**Recompile with correct classpath:**
```powershell
javac -cp ".;lib\mysql-connector-j-9.4.0.jar" -d . src\*.java
```

### ❌ Google Maps not showing

**Check API key:**
```
Open: config.properties
Find: google.maps.api.key=AIzaSyAhJyy219zxBaaR-n6Ai6RMS5jOGxuk5p8
```

**Manual open:**
```
Look for: accident_map.html
Double-click to open in browser
```

**Check system log:**
```
Look at bottom panel in application for errors
```

### ❌ Compilation Errors

**Clean and rebuild:**
```powershell
cd "c:\Users\VEDANT\Downloads\AOOP PROJECT\TestJdbc"
Remove-Item *.class -Recurse -ErrorAction SilentlyContinue
javac -encoding UTF-8 -cp ".;lib\mysql-connector-j-9.4.0.jar" -d . src\*.java
```

**Check Java version:**
```powershell
java -version
```
Must be 17 or higher!

### ❌ Login Failed

**Reset admin password:**
```powershell
mysql -u root -pVedant@039 accident_alert_system -e "UPDATE users SET password='admin123' WHERE username='admin';"
```

**Verify:**
```powershell
mysql -u root -pVedant@039 accident_alert_system -e "SELECT username, password, role FROM users WHERE username='admin';"
```

---

## 🔬 Technical Details

### Architecture
```
┌─────────────────────────────────────┐
│     Presentation Layer (Swing)      │
│  Main.java, GoogleMapsPanel.java   │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│     Business Logic Layer            │
│  RouteOptimizer, WeatherService,    │
│  NotificationManager                │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│     Data Access Layer               │
│  DatabaseManager, ConfigManager     │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│     MySQL Database                  │
│  accident_alert_system              │
└─────────────────────────────────────┘
```

### Design Patterns
- **Singleton**: DatabaseManager, ConfigManager, WeatherService, NotificationManager
- **Observer**: Event listeners for UI updates
- **Factory**: Dialog creation
- **MVC**: Separation of concerns

### Key Technologies
- **Java 17**: Core language
- **Swing**: GUI framework
- **JDBC**: Database connectivity
- **MySQL 8.0**: Relational database
- **Google Maps JavaScript API v3**: Map integration

### Database Schema
```
users (id, username, password, role, full_name, email, phone, created_at)
├── Primary Key: id
└── Unique: username

accidents (id, location, vehicle_id, severity, description, reported_by, accident_time, status, assigned_ambulance_id, assigned_hospital_id)
├── Primary Key: id
└── Foreign Keys: assigned_ambulance_id, assigned_hospital_id

hospitals (id, name, address, latitude, longitude, total_beds, available_beds, phone, emergency_contact)
└── Primary Key: id

ambulances (id, vehicle_number, driver_name, driver_phone, status, current_latitude, current_longitude, last_updated)
├── Primary Key: id
└── Unique: vehicle_number

messages (id, sender_id, receiver_id, message, priority, sent_at, read_status)
├── Primary Key: id
└── Foreign Keys: sender_id, receiver_id

audit_logs (id, user_id, action, details, ip_address, timestamp)
├── Primary Key: id
└── Foreign Key: user_id
```

### Performance Optimizations
- Connection pooling for database
- Lazy loading of components
- Efficient SQL queries with prepared statements
- Configuration caching
- Asynchronous map generation

---

## 🎯 Complete Feature List

| # | Feature | Status | Description |
|---|---------|--------|-------------|
| 1 | Accident Reporting | ✅ | Report with location, vehicle, severity |
| 2 | AI Severity Prediction | ✅ | Predicts severity from description |
| 3 | Google Maps View | ✅ | Interactive map with all data |
| 4 | Color-Coded Markers | ✅ | Severity-based colors |
| 5 | Ambulance Dispatch | ✅ | Smart automatic dispatch |
| 6 | Hospital Management | ✅ | Bed availability tracking |
| 7 | Weather Integration | ✅ | Weather-aware routing |
| 8 | Traffic Optimization | ✅ | Traffic-based ETA |
| 9 | Communication Center | ✅ | Message system |
| 10 | Analytics Dashboard | ✅ | Charts and statistics |
| 11 | User Authentication | ✅ | Login and roles |
| 12 | Notification System | ✅ | Toast alerts |
| 13 | Export to CSV | ✅ | Data export |
| 14 | Real-time Updates | ✅ | Live status tracking |
| 15 | Search & Filter | ✅ | Table filtering |
| 16 | Edit & Delete | ✅ | Accident management |
| 17 | Audit Logging | ✅ | Activity tracking |
| 18 | Traffic Layer | ✅ | Google Maps traffic |

---

## 📊 System Requirements

### Minimum Requirements:
- **OS**: Windows 10 / macOS 10.15 / Ubuntu 20.04
- **RAM**: 4 GB
- **Storage**: 500 MB
- **Java**: JDK 17
- **MySQL**: 8.0
- **Browser**: Chrome, Firefox, or Edge

### Recommended Requirements:
- **OS**: Windows 11 / macOS 12+ / Ubuntu 22.04
- **RAM**: 8 GB
- **Storage**: 1 GB
- **Java**: JDK 21
- **MySQL**: 8.0.35+
- **Browser**: Latest Chrome or Edge

---

## 🚀 Quick Commands Reference

### Database Commands:
```powershell
# Connect to MySQL
mysql -u root -pVedant@039

# Use database
mysql -u root -pVedant@039 accident_alert_system

# Show all tables
mysql -u root -pVedant@039 accident_alert_system -e "SHOW TABLES;"

# Check users
mysql -u root -pVedant@039 accident_alert_system -e "SELECT * FROM users;"

# Check accidents
mysql -u root -pVedant@039 accident_alert_system -e "SELECT id, location, severity, status FROM accidents;"

# Check hospitals
mysql -u root -pVedant@039 accident_alert_system -e "SELECT name, available_beds FROM hospitals;"

# Check ambulances
mysql -u root -pVedant@039 accident_alert_system -e "SELECT vehicle_number, status FROM ambulances;"
```

### Compilation Commands:
```powershell
# Navigate to project
cd "c:\Users\VEDANT\Downloads\AOOP PROJECT\TestJdbc"

# Compile all Java files
javac -cp ".;lib\mysql-connector-j-9.4.0.jar" -d . src\*.java

# Compile specific file
javac -cp ".;lib\mysql-connector-j-9.4.0.jar" src\Main.java

# Run application
java -cp ".;lib\mysql-connector-j-9.4.0.jar" Main

# Clean compiled files
Remove-Item *.class -Recurse
```

### Testing Commands:
```powershell
# Check Java version
java -version
javac -version

# Check MySQL version
mysql --version

# Test MySQL connection
mysql -u root -pVedant@039 -e "SELECT 1;"

# Verify jar file
ls lib\mysql-connector-j-9.4.0.jar

# Check config file
cat config.properties
```

---

## 🎉 Success Checklist

Before running the application, verify:

- [ ] **Java JDK 17+** installed (`java -version`)
- [ ] **MySQL 8.0+** installed and running (`mysql --version`)
- [ ] **Database created** (`accident_alert_system`)
- [ ] **Users table exists** (run create users command)
- [ ] **Admin user created** (username: admin, password: admin123)
- [ ] **All tables created** (accidents, hospitals, ambulances, messages, audit_logs)
- [ ] **Hospitals inserted** (5 Chennai hospitals)
- [ ] **Ambulances inserted** (3 vehicles)
- [ ] **JDBC driver present** (`lib\mysql-connector-j-9.4.0.jar`)
- [ ] **Config file configured** (`config.properties` with correct password)
- [ ] **Google Maps API key present** (already configured)
- [ ] **Application compiles** (no errors)
- [ ] **Can login successfully** (admin/admin123)

---

## 📞 Support & Help

### If you encounter any issues:

1. **Check the system log** (bottom panel in application)
2. **Run database verification** commands above
3. **Review error messages** carefully
4. **Check this README** troubleshooting section
5. **Verify all tables exist** in MySQL

### Common Error Solutions:

| Error | Solution |
|-------|----------|
| "Table 'users' doesn't exist" | Run users table creation command |
| "Cannot connect to database" | Start MySQL: `net start MySQL80` |
| "Class not found" | Verify jar file in lib folder |
| "Login failed" | Reset admin password with SQL command |
| "Compilation error" | Clean and rebuild |
| "Google Maps not showing" | Check API key in config.properties |

---

## 🏆 Credits

**Project**: Advanced Object-Oriented Programming
**Technologies**: Java 17, MySQL 8.0, Google Maps API, Swing
**Version**: 2.0 with Google Maps Integration
**Date**: November 2025

---

## 📄 License

This project is for educational purposes as part of an Advanced Object-Oriented Programming course.

---

## 🎊 Final Notes

### You're All Set!

1. **Run database setup** commands (copy-paste from Database Setup section)
2. **Double-click** `RUN-WITH-GOOGLE-MAPS.bat`
3. **Login** with admin/admin123
4. **Click 🗺️ Google Maps** button
5. **Enjoy!** 🎉

### Need Help?
- Check troubleshooting section
- Review system log in application
- Verify database with test commands

---

**🎉 Enjoy using the Accident Alert System with Google Maps! 🗺️**

*For any issues, refer to the troubleshooting section above.*
