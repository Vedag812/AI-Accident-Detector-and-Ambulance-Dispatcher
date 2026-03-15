# 2.4 Insertion of tuples into the table – DML commands

### Insert SQL (for all tables)

**Example:**

```sql
INSERT INTO users (username, password, full_name, role, email, phone) 
VALUES ('admin', 'admin123', 'System Administrator', 'ADMIN', 'admin@system.com', '9876543210'),
       ('dispatcher01', 'disp123', 'John Doe', 'DISPATCHER', 'john@system.com', '9876543211'),
       ('hospital01', 'hosp123', 'Medical Staff', 'HOSPITAL_STAFF', 'staff@apollo.com', '9876543212');
```

### Users table:

| id | username | password | full_name | role | email | phone |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | admin | admin123 | System Administrator | ADMIN | admin@system.com | 9876543210 |
| **2** | dispatcher01 | disp123 | John Doe | DISPATCHER | john@system.com | 9876543211 |
| **3** | hospital01 | hosp123 | Medical Staff | HOSPITAL_STAFF | staff@apollo.com| 9876543212 |

---

### Accidents table:

| accident_id | location | severity | vehicle_id | reported_by | status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **101** | Mount Road, Chennai | High | TN-01-AX-9999 | Sensor A1 | Reported |
| **102** | OMR, Chennai | Medium | TN-02-BY-8888 | Citizen App | In Transit |
| **103** | Adyar, Chennai | Critical | TN-01-CZ-7777 | Traffic Cam 4 | Dispatched|

---

### Hospitals table:

| hospital_id | name | latitude | longitude | available_beds | contact_number |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | Apollo Hospital | 13.0358 | 80.2464 | 15 | 044-28296000 |
| **2** | MIOT Hospital | 13.0890 | 80.2100 | 20 | 044-42002000 |
| **3** | Fortis Malar | 13.0569 | 80.2540 | 12 | 044-42899000 |

---

### Ambulances table:

| ambulance_id | vehicle_number | status | driver_name | driver_contact |
| :--- | :--- | :--- | :--- | :--- |
| **1** | TN01-AB-1234 | green | Rajesh Kumar | 9876543210 |
| **2** | TN01-CD-5678 | green | Suresh Babu | 9876543211 |
| **3** | TN01-EF-9012 | green | Venkat Raman | 9876543212 |
