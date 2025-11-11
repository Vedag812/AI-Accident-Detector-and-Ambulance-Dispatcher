# 🚀 ACCIDENT ALERT SYSTEM - QUICK START GUIDE

## ✅ What's New (Auto-Setup Edition)

### **NO MANUAL DATABASE SETUP NEEDED!**
- All 10 tables are created automatically when you run the app
- Sample data is inserted (hospitals, ambulances, admin user)
- No SQL scripts to run manually!

---

## 🎯 How to Run

### **Option 1: Using Batch File (Easiest)**
```
Double-click: RUN-WITH-GOOGLE-MAPS.bat
```

### **Option 2: Manual Commands**
```powershell
cd "c:\Users\VEDANT\Downloads\AOOP PROJECT\TestJdbc"
javac -encoding UTF-8 -cp ".;lib\mysql-connector-j-9.4.0.jar" -d bin src\*.java
java -cp ".;bin;lib\mysql-connector-j-9.4.0.jar" Main
```

---

## 🔐 Login Credentials

**Username:** `admin`  
**Password:** `admin123`

---

## 🎨 New Features

### **1. Auto-Database Setup**
- ✅ Creates all tables automatically on first run
- ✅ Inserts 3 sample hospitals in Chennai
- ✅ Inserts 3 sample ambulances
- ✅ Creates admin user

### **2. Modern Tabbed Interface**
- 🗺️ **Map View Tab** - Interactive map + accident list
- 📋 **Table View Tab** - Full-screen accident table
- 💬 **Communications Tab** - Messaging system

### **3. Scrollable UI**
- Smooth scrolling throughout the application
- Floating **⬆️ Scroll to Top** button (appears after scrolling)
- Better organization for long lists

### **4. Real-Time Google Maps**
- Click **"🗺️ Google Maps"** button
- Opens in your default browser
- Click **"Auto-Refresh: OFF"** to enable real-time updates (every 10 seconds)
- Markers with emoji icons:
  - 🚨 Accidents (color-coded by severity)
  - 🏥 Hospitals (with bed counts)
  - 🚑 Ambulances (with status)

---

## 📊 Database Tables Created

| # | Table Name | Purpose |
|---|------------|---------|
| 1 | `users` | Authentication & user management |
| 2 | `accidents` | Main accident records |
| 3 | `hospitals` | Hospital information & bed availability |
| 4 | `ambulances` | Ambulance tracking & status |
| 5 | `hospital_staff` | Staff management |
| 6 | `equipment_inventory` | Medical equipment tracking |
| 7 | `ambulance_requests` | Dispatch requests & routing |
| 8 | `audit_logs` | System activity logs |
| 9 | `messages` | Communication system |
| 10 | `surgery_rooms` | Operating room availability |

---

## 🎮 How to Use

1. **Run the application** (batch file or manual)
2. **Login** with admin/admin123
3. **Report accidents** using the form
4. **View on map**:
   - Click **"🗺️ Google Maps"** button
   - Browser opens automatically
   - Enable **Auto-Refresh** for live updates
5. **Switch tabs** to see different views
6. **Use scroll-to-top** button (⬆️) when needed

---

## 🔧 Troubleshooting

### **Error: "Table doesn't exist"**
**Solution:** The application creates tables automatically. If you still see this:
1. Make sure MySQL is running
2. Database `accident_alert_system` exists
3. Run the app again - tables will be created

### **Error: "Cannot connect to database"**
**Solution:**
1. Check MySQL is running
2. Verify credentials in `config.properties`:
   ```
   db.url=jdbc:mysql://localhost:3306/accident_alert_system
   db.user=root
   db.password=Vedant@039
   ```

### **Error: "ClassNotFoundException: com.mysql.cj.jdbc.Driver"**
**Solution:**
- Ensure `mysql-connector-j-9.4.0.jar` is in the `lib` folder
- Use the provided batch file to run

### **Application opens but login window doesn't show**
**Solution:**
1. Close all Java processes
2. Recompile: `javac -encoding UTF-8 -cp ".;lib\mysql-connector-j-9.4.0.jar" -d bin src\*.java`
3. Run again

### **Google Maps not showing markers**
**Solution:**
1. Report some accidents first
2. Click **"🗺️ Google Maps"** button
3. Wait for browser to load
4. Check the stats panel at top of map for counts

### **Scroll-to-top button not appearing**
**Solution:**
- Scroll down more than 100 pixels
- Button appears automatically in bottom-right corner

---

## 📝 Manual SQL Script (Backup)

If automatic setup fails, run `CREATE_ALL_TABLES.sql` in MySQL Workbench:

```sql
-- Run this in MySQL Workbench
USE accident_alert_system;
SOURCE CREATE_ALL_TABLES.sql;
```

---

## 🆘 Quick MySQL Commands

### **Check if tables exist:**
```sql
USE accident_alert_system;
SHOW TABLES;
```

### **Check table row counts:**
```sql
SELECT 'users' AS table_name, COUNT(*) AS rows FROM users
UNION ALL SELECT 'accidents', COUNT(*) FROM accidents
UNION ALL SELECT 'hospitals', COUNT(*) FROM hospitals
UNION ALL SELECT 'ambulances', COUNT(*) FROM ambulances;
```

### **Reset database (CAREFUL - deletes all data):**
```sql
DROP DATABASE IF EXISTS accident_alert_system;
CREATE DATABASE accident_alert_system;
USE accident_alert_system;
-- Then run the application - tables will be created automatically
```

---

## 📦 Project Structure

```
TestJdbc/
├── src/
│   ├── Main.java                    (Main application)
│   ├── ConfigManager.java           (Config handler)
│   ├── DatabaseManager.java         (Auto-creates tables!)
│   ├── GoogleMapsPanel.java         (Maps integration)
│   ├── RouteOptimizer.java          (Route planning)
│   ├── WeatherService.java          (Weather simulation)
│   ├── NotificationManager.java     (Notifications)
│   ├── CommunicationPanel.java      (Messaging)
│   ├── LoginDialog.java             (Authentication)
│   ├── HospitalPortalDialog.java    (Hospital management)
│   ├── AnalyticsDashboard.java      (Charts & stats)
│   └── MapPanel.java                (Swing map)
├── bin/                             (Compiled classes)
├── lib/
│   └── mysql-connector-j-9.4.0.jar  (MySQL driver)
├── config.properties                (Configuration)
├── RUN-WITH-GOOGLE-MAPS.bat         (Quick start)
└── CREATE_ALL_TABLES.sql            (Backup SQL script)
```

---

## 🎯 Key Features Summary

✅ **Automatic database setup** - No manual SQL needed  
✅ **Google Maps integration** - Real-time accident visualization  
✅ **Auto-refresh** - Updates every 10 seconds when enabled  
✅ **Tabbed interface** - Better organization  
✅ **Smooth scrolling** - Enhanced UX  
✅ **Communication system** - Built-in messaging  
✅ **Hospital portal** - Bed management  
✅ **Analytics dashboard** - Charts and statistics  
✅ **Weather integration** - Response time calculations  
✅ **Route optimization** - Haversine distance algorithm  

---

## 📞 Support

If you encounter any issues:
1. Check the console output (terminal) for detailed error messages
2. Verify MySQL is running
3. Ensure all files are in correct folders
4. Try recompiling and running again

**All tables are created automatically - just run and login!** 🎉
