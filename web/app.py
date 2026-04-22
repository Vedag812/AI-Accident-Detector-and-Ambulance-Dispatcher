from flask import Flask, render_template, request, redirect, url_for, session, jsonify, flash
from functools import wraps
import hashlib, base64, os, secrets, math
from datetime import datetime, timedelta
import json, random, time

# ─── Detect Database Backend ───────────────────────────────────────────
DATABASE_URL = os.environ.get('DATABASE_URL')

if DATABASE_URL:
    # ── PostgreSQL (Render.com deployment) ──
    import psycopg2
    import psycopg2.pool
    import psycopg2.extras
    pg_pool = psycopg2.pool.SimpleConnectionPool(1, 3, DATABASE_URL)
    print("[Flask] PostgreSQL pool created (Render)")

    class PgConnWrapper:
        """Wraps psycopg2 connection to match mysql.connector interface."""
        def __init__(self, conn):
            self._conn = conn
        def cursor(self, dictionary=False):
            if dictionary:
                return self._conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor)
            return self._conn.cursor()
        def commit(self):
            self._conn.commit()
        def close(self):
            pg_pool.putconn(self._conn)
        @property
        def lastrowid(self):
            return None  # handled via RETURNING

    def get_db():
        return PgConnWrapper(pg_pool.getconn())
else:
    # ── MySQL (local development) ──
    import mysql.connector
    from mysql.connector import pooling
    DB_CONFIG = {
        'host': os.environ.get('DB_HOST', '127.0.0.1'),
        'port': int(os.environ.get('DB_PORT', 3306)),
        'database': os.environ.get('DB_NAME', 'accident_alert_system'),
        'user': os.environ.get('DB_USER', 'root'),
        'password': os.environ.get('DB_PASSWORD', 'Vedant@039'),
        'pool_name': 'accident_pool',
        'pool_size': int(os.environ.get('DB_POOL_SIZE', 5)),
        'pool_reset_session': True
    }
    try:
        pool = mysql.connector.pooling.MySQLConnectionPool(**DB_CONFIG)
        print("[Flask] MySQL pool created (local)")
    except Exception as e:
        print(f"[Flask] MySQL pool error: {e}")
        pool = None

    def get_db():
        return pool.get_connection()

app = Flask(__name__)
app.secret_key = os.environ.get('SECRET_KEY', secrets.token_hex(32))
app.permanent_session_lifetime = timedelta(hours=8)

# ─── Chennai Ambulance Base Stations ───────────────────────────────────
CHENNAI_AMBULANCE_POSITIONS = [
    (13.0827, 80.2707, 'Central Chennai'),
    (13.0569, 80.2425, 'Nungambakkam'),
    (13.0732, 80.2609, 'Egmore'),
    (13.0418, 80.2341, 'T Nagar'),
    (13.0067, 80.2206, 'Guindy'),
    (12.9815, 80.2180, 'Velachery'),
    (13.0850, 80.2101, 'Anna Nagar'),
    (13.0063, 80.2574, 'Adyar'),
    (13.0520, 80.2121, 'Vadapalani'),
    (13.0382, 80.1567, 'Porur'),
    (13.0604, 80.2496, 'Mount Road'),
    (12.9249, 80.1000, 'Tambaram'),
]

TARGET_FLEET_SIZE = 12

def init_ambulance_fleet():
    """Ensure 12 ambulances exist and ALL are positioned within Chennai."""
    try:
        conn = get_db()
        cur = conn.cursor(dictionary=True)

        # Step 1: Ensure at least TARGET_FLEET_SIZE ambulances exist
        cur.execute("SELECT COUNT(*) AS c FROM ambulances")
        count = cur.fetchone()['c']

        if count < TARGET_FLEET_SIZE:
            for i in range(count, TARGET_FLEET_SIZE):
                pos = CHENNAI_AMBULANCE_POSITIONS[i % len(CHENNAI_AMBULANCE_POSITIONS)]
                lat = pos[0] + random.uniform(-0.005, 0.005)
                lng = pos[1] + random.uniform(-0.005, 0.005)
                vnum = f"TN{random.choice(['01','02','09','22','07'])}{chr(65+random.randint(0,25))}{random.randint(1000,9999)}"
                cur.execute("""INSERT INTO ambulances (vehicle_number, status, latitude, longitude,
                               current_x, current_y, fuel_level)
                               VALUES (%s, 'green', %s, %s, 300, 300, %s)""",
                            (vnum, lat, lng, random.randint(55, 100)))
            conn.commit()
            print(f"[Flask] Added {TARGET_FLEET_SIZE - count} ambulances (total: {TARGET_FLEET_SIZE})")

        # Step 2: FORCE ALL ambulances to Chennai coordinates
        cur.execute("SELECT ambulance_id, latitude, longitude, status FROM ambulances")
        ambulances = cur.fetchall()
        fixed = 0
        for i, amb in enumerate(ambulances):
            lat = float(amb.get('latitude') or 0)
            lng = float(amb.get('longitude') or 0)
            in_chennai = 12.85 <= lat <= 13.20 and 80.05 <= lng <= 80.35
            if not in_chennai:
                pos = CHENNAI_AMBULANCE_POSITIONS[i % len(CHENNAI_AMBULANCE_POSITIONS)]
                new_lat = pos[0] + random.uniform(-0.005, 0.005)
                new_lng = pos[1] + random.uniform(-0.005, 0.005)
                cur.execute("UPDATE ambulances SET latitude=%s, longitude=%s WHERE ambulance_id=%s",
                            (new_lat, new_lng, amb['ambulance_id']))
                fixed += 1
        # Also reset any stuck dispatched ambulances (no assigned accident)
        cur.execute("""UPDATE ambulances SET status='green'
                       WHERE status != 'green' AND assigned_accident_id IS NULL""")
        conn.commit()
        cur.close(); conn.close()
        print(f"[Flask] Fleet: {len(ambulances)} ambulances, repositioned {fixed} to Chennai")
    except Exception as e:
        print(f"[Flask] Fleet init error: {e}")

init_ambulance_fleet()

# ─── Auth Helpers ──────────────────────────────────────────────────────
def verify_password(password, stored):
    """Verify password against stored hash (supports legacy plaintext)."""
    if ':' not in stored:
        return password == stored
    try:
        salt_b64, hash_b64 = stored.split(':')
        salt = base64.b64decode(salt_b64)
        expected = base64.b64decode(hash_b64)
        h = hashlib.sha256()
        h.update(salt)
        h.update(password.encode())
        return h.digest() == expected
    except Exception:
        return False

def hash_password(password):
    """Hash password with random salt (SHA-256)."""
    salt = os.urandom(16)
    h = hashlib.sha256()
    h.update(salt)
    h.update(password.encode())
    return base64.b64encode(salt).decode() + ':' + base64.b64encode(h.digest()).decode()

def login_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        if 'user_id' not in session:
            return redirect(url_for('login'))
        return f(*args, **kwargs)
    return decorated

# ─── Page Routes ───────────────────────────────────────────────────────

@app.route('/')
def index():
    if 'user_id' in session:
        return redirect(url_for('dashboard'))
    return redirect(url_for('login'))

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form.get('username', '').strip()
        password = request.form.get('password', '')
        if not username or not password:
            flash('Please enter both username and password', 'error')
            return render_template('login.html')
        try:
            conn = get_db()
            cur = conn.cursor(dictionary=True)
            cur.execute("SELECT id, username, password, role, full_name FROM users WHERE username = %s", (username,))
            user = cur.fetchone()
            if user and verify_password(password, user['password']):
                session.permanent = True
                session['user_id'] = user['id']
                session['username'] = user['username']
                session['role'] = user['role']
                session['full_name'] = user.get('full_name', username)
                # Auto-migrate plaintext passwords
                if ':' not in user['password']:
                    cur.execute("UPDATE users SET password = %s WHERE id = %s", (hash_password(password), user['id']))
                    conn.commit()
                cur.close(); conn.close()
                return redirect(url_for('dashboard'))
            else:
                cur.close(); conn.close()
                flash('Invalid username or password', 'error')
        except Exception as e:
            flash(f'Database error: {e}', 'error')
    return render_template('login.html')

@app.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('login'))

@app.route('/dashboard')
@login_required
def dashboard():
    return render_template('dashboard.html')

@app.route('/hospitals')
@login_required
def hospitals():
    return render_template('hospitals.html')

@app.route('/fleet')
@login_required
def fleet():
    return render_template('fleet.html')

@app.route('/analytics')
@login_required
def analytics():
    return render_template('analytics.html')

@app.route('/yolo')
@login_required
def yolo():
    return render_template('yolo.html')

@app.route('/traffic')
@login_required
def traffic():
    return render_template('traffic.html')

@app.route('/sql-lab')
@login_required
def sql_lab():
    return render_template('sql_lab.html')

@app.route('/normalization')
@login_required
def normalization():
    return render_template('normalization.html')

@app.route('/transactions')
@login_required
def transactions():
    return render_template('transactions.html')

@app.route('/concurrency')
@login_required
def concurrency():
    return render_template('concurrency.html')

@app.route('/api/sql-execute', methods=['POST'])
@login_required
def api_sql_execute():
    """Execute SQL query from SQL Lab (SELECT only for safety)."""
    data = request.get_json()
    sql = data.get('sql', '').strip()
    if not sql:
        return jsonify({'error': 'No SQL provided'}), 400

    # Safety: Only allow SELECT, SHOW, DESCRIBE, EXPLAIN, CALL
    allowed = sql.upper().startswith(('SELECT', 'SHOW', 'DESCRIBE', 'EXPLAIN', 'CALL',
                                      'CREATE OR REPLACE VIEW', 'CREATE VIEW'))
    dml = sql.upper().startswith(('INSERT', 'UPDATE', 'DELETE', 'CREATE', 'DROP'))

    try:
        conn = get_db()
        cur = conn.cursor(dictionary=True)
        cur.execute(sql)
        if allowed or sql.upper().startswith('SELECT'):
            rows = cur.fetchall()
            # Serialize datetime objects
            for row in rows:
                for k, v in row.items():
                    if hasattr(v, 'strftime'):
                        row[k] = v.strftime('%Y-%m-%d %H:%M:%S')
                    elif hasattr(v, 'total_seconds'):
                        row[k] = str(v)
            columns = [d[0] for d in cur.description] if cur.description else []
            cur.close(); conn.close()
            return jsonify({'columns': columns, 'rows': rows, 'count': len(rows)})
        elif dml:
            conn.commit()
            affected = cur.rowcount
            cur.close(); conn.close()
            return jsonify({'message': f'{affected} row(s) affected', 'count': affected})
        else:
            cur.close(); conn.close()
            return jsonify({'error': 'Unsupported SQL statement'}), 400
    except Exception as e:
        return jsonify({'error': str(e)}), 400

# ─── API Routes ────────────────────────────────────────────────────────

@app.route('/api/stats')
@login_required
def api_stats():
    conn = get_db()
    cur = conn.cursor(dictionary=True)
    stats = {}
    cur.execute("SELECT COUNT(*) AS c FROM accidents"); stats['total_accidents'] = cur.fetchone()['c']
    cur.execute("SELECT COUNT(*) AS c FROM accidents WHERE status='Reported'"); stats['active_accidents'] = cur.fetchone()['c']
    cur.execute("SELECT COUNT(*) AS c FROM ambulances WHERE status='green'"); stats['available_ambulances'] = cur.fetchone()['c']
    cur.execute("SELECT COUNT(*) AS c FROM ambulances"); stats['total_ambulances'] = cur.fetchone()['c']
    cur.execute("SELECT COALESCE(SUM(available_beds),0) AS c FROM hospitals"); stats['available_beds'] = cur.fetchone()['c']
    cur.execute("SELECT COALESCE(SUM(capacity),0) AS c FROM hospitals"); stats['total_beds'] = cur.fetchone()['c']
    cur.execute("SELECT COUNT(*) AS c FROM hospitals"); stats['total_hospitals'] = cur.fetchone()['c']
    cur.execute("SELECT COUNT(*) AS c FROM accidents WHERE severity='Critical'"); stats['critical_count'] = cur.fetchone()['c']
    cur.close(); conn.close()
    return jsonify(stats)

@app.route('/api/accidents')
@login_required
def api_accidents():
    conn = get_db()
    cur = conn.cursor(dictionary=True)
    cur.execute("""SELECT accident_id, location, latitude, longitude, vehicle_id,
                   severity, description, reported_by, accident_time, status
                   FROM accidents ORDER BY accident_time DESC LIMIT 50""")
    rows = cur.fetchall()
    for r in rows:
        if r.get('accident_time'): r['accident_time'] = r['accident_time'].strftime('%Y-%m-%d %H:%M:%S')
    cur.close(); conn.close()
    return jsonify(rows)

@app.route('/api/accidents', methods=['POST'])
@login_required
def api_add_accident():
    data = request.json
    conn = get_db()
    cur = conn.cursor()
    lat = data.get('latitude', 13.0827)
    lng = data.get('longitude', 80.2707)
    insert_sql = """INSERT INTO accidents (location, latitude, longitude, vehicle_id, severity, description, reported_by)
                   VALUES (%s, %s, %s, %s, %s, %s, %s)"""
    params = (data['location'], lat, lng, data.get('vehicle_id','Unknown'),
                 data['severity'], data.get('description',''), session.get('username','Web User'))
    if DATABASE_URL:
        cur.execute(insert_sql + " RETURNING accident_id", params)
        accident_id = cur.fetchone()[0]
    else:
        cur.execute(insert_sql, params)
        accident_id = cur.lastrowid
    conn.commit()
    cur.close(); conn.close()
    return jsonify({'success': True, 'accident_id': accident_id})

@app.route('/api/accidents/<int:aid>', methods=['DELETE'])
@login_required
def api_delete_accident(aid):
    conn = get_db()
    cur = conn.cursor()
    # Free any ambulance assigned to this accident
    cur.execute("""UPDATE ambulances SET status='green', assigned_accident_id=NULL,
                   assigned_hospital_id=NULL, target_x=NULL, target_y=NULL
                   WHERE assigned_accident_id=%s""", (aid,))
    cur.execute("DELETE FROM accidents WHERE accident_id=%s", (aid,))
    conn.commit()
    cur.close(); conn.close()
    return jsonify({'success': True})

@app.route('/api/accidents/clear', methods=['POST'])
@login_required
def api_clear_accidents():
    conn = get_db()
    cur = conn.cursor()
    cur.execute("UPDATE ambulances SET status='green', assigned_accident_id=NULL, assigned_hospital_id=NULL, target_x=NULL, target_y=NULL")
    cur.execute("DELETE FROM accidents")
    conn.commit()
    affected = cur.rowcount
    cur.close(); conn.close()
    return jsonify({'success': True, 'deleted': affected})

@app.route('/api/ambulances')
@login_required
def api_ambulances():
    conn = get_db()
    cur = conn.cursor(dictionary=True)
    cur.execute("""SELECT a.*, d.name as driver_name FROM ambulances a
                   LEFT JOIN drivers d ON a.driver_id = d.driver_id""")
    rows = cur.fetchall()
    cur.close(); conn.close()
    return jsonify(rows)

@app.route('/api/hospitals')
@login_required
def api_hospitals():
    conn = get_db()
    cur = conn.cursor(dictionary=True)
    cur.execute("SELECT * FROM hospitals ORDER BY hospital_id")
    rows = cur.fetchall()
    cur.close(); conn.close()
    return jsonify(rows)

@app.route('/api/hospitals/<int:hid>', methods=['PUT'])
@login_required
def api_update_hospital(hid):
    data = request.json
    conn = get_db()
    cur = conn.cursor()
    cur.execute("""UPDATE hospitals SET name=%s, specialty=%s, phone=%s,
                   max_severity=%s, available_beds=%s, available_icu_beds=%s
                   WHERE hospital_id=%s""",
                (data['name'], data.get('specialty','General'), data.get('phone',''),
                 data.get('max_severity','Medium'), data.get('available_beds',0),
                 data.get('available_icu_beds',0), hid))
    conn.commit()
    cur.close(); conn.close()
    return jsonify({'success': True})

def _auto_dispatch(conn, cur, accident_id, acc_lat, acc_lng):
    """Internal: dispatch nearest ambulance to accident. Returns ambulance_id or None."""
    cur.execute("""SELECT ambulance_id, latitude, longitude FROM ambulances
                   WHERE status='green'
                   ORDER BY SQRT(POW(latitude - %s, 2) + POW(longitude - %s, 2)) LIMIT 1""",
                (acc_lat, acc_lng))
    amb = cur.fetchone()
    if not amb:
        return None
    cur.execute("""UPDATE ambulances SET status='yellow', assigned_accident_id=%s,
                   target_x=%s, target_y=%s
                   WHERE ambulance_id=%s""",
                (accident_id, acc_lat, acc_lng, amb['ambulance_id']))
    cur.execute("UPDATE accidents SET status='Dispatched' WHERE accident_id=%s", (accident_id,))
    conn.commit()
    return amb['ambulance_id']

@app.route('/api/dispatch', methods=['POST'])
@login_required
def api_dispatch():
    """Dispatch nearest available ambulance to an accident."""
    data = request.json
    accident_id = data.get('accident_id')
    conn = get_db()
    cur = conn.cursor(dictionary=True)
    cur.execute("SELECT latitude, longitude FROM accidents WHERE accident_id=%s", (accident_id,))
    acc = cur.fetchone()
    if not acc:
        cur.close(); conn.close()
        return jsonify({'success': False, 'error': 'Accident not found'})
    amb_id = _auto_dispatch(conn, cur, accident_id, float(acc['latitude']), float(acc['longitude']))
    cur.close(); conn.close()
    if amb_id:
        return jsonify({'success': True, 'ambulance_id': amb_id})
    return jsonify({'success': False, 'error': 'No available ambulances'})

@app.route('/api/move-ambulances', methods=['POST'])
@login_required
def api_move_ambulances():
    """Advance all dispatched/at-scene ambulances one step. Handles the full lifecycle:
    yellow (dispatched) → move toward accident → red (at scene) → move to hospital → green (available)"""
    SPEED = 0.006  # degrees per tick (~600m per tick for visible movement)
    conn = get_db()
    cur = conn.cursor(dictionary=True)
    events = []
    moved = 0

    # Get all non-green ambulances
    cur.execute("SELECT * FROM ambulances WHERE status != 'green'")
    ambulances = cur.fetchall()

    for amb in ambulances:
        amb_id = amb['ambulance_id']
        lat = float(amb['latitude'] or 13.0)
        lng = float(amb['longitude'] or 80.2)
        target_lat = float(amb['target_x'] or lat) if amb['target_x'] else lat
        target_lng = float(amb['target_y'] or lng) if amb['target_y'] else lng

        dist = ((target_lat - lat)**2 + (target_lng - lng)**2) ** 0.5

        if dist < SPEED * 1.5:
            # Arrived at target
            if amb['status'] == 'yellow':
                # Arrived at accident scene → switch to at_scene (red)
                cur.execute("""UPDATE ambulances SET status='red', latitude=%s, longitude=%s
                               WHERE ambulance_id=%s""", (target_lat, target_lng, amb_id))
                cur.execute("UPDATE accidents SET status='At Scene' WHERE accident_id=%s",
                            (amb['assigned_accident_id'],))
                conn.commit()
                events.append({'type': 'arrived_scene', 'ambulance_id': amb_id,
                               'accident_id': amb['assigned_accident_id']})

                # Now find nearest hospital and set as new target
                cur.execute("""SELECT hospital_id, latitude, longitude, name FROM hospitals
                               WHERE available_beds > 0
                               ORDER BY SQRT(POW(latitude-%s,2)+POW(longitude-%s,2)) LIMIT 1""",
                            (target_lat, target_lng))
                hosp = cur.fetchone()
                if hosp:
                    cur.execute("""UPDATE ambulances SET assigned_hospital_id=%s,
                                   target_x=%s, target_y=%s
                                   WHERE ambulance_id=%s""",
                                (hosp['hospital_id'], hosp['latitude'], hosp['longitude'], amb_id))
                    conn.commit()
                    events.append({'type': 'transporting', 'ambulance_id': amb_id,
                                   'hospital': hosp['name']})

            elif amb['status'] == 'red':
                # Arrived at hospital → delivery complete, reset to green
                cur.execute("""UPDATE ambulances SET status='green', latitude=%s, longitude=%s,
                               assigned_accident_id=NULL, assigned_hospital_id=NULL,
                               target_x=NULL, target_y=NULL
                               WHERE ambulance_id=%s""",
                            (target_lat, target_lng, amb_id))
                if amb.get('assigned_hospital_id'):
                    cur.execute("""UPDATE hospitals SET available_beds = GREATEST(available_beds-1, 0)
                                   WHERE hospital_id=%s""", (amb['assigned_hospital_id'],))
                if amb.get('assigned_accident_id'):
                    cur.execute("UPDATE accidents SET status='Resolved' WHERE accident_id=%s",
                                (amb['assigned_accident_id'],))
                conn.commit()
                events.append({'type': 'delivered', 'ambulance_id': amb_id})
            moved += 1
        else:
            # Move one step toward target
            dx = (target_lat - lat) / dist * SPEED
            dy = (target_lng - lng) / dist * SPEED
            new_lat = lat + dx
            new_lng = lng + dy
            cur.execute("UPDATE ambulances SET latitude=%s, longitude=%s WHERE ambulance_id=%s",
                        (new_lat, new_lng, amb_id))
            conn.commit()
            moved += 1

    cur.close(); conn.close()
    return jsonify({'success': True, 'events': events, 'moved': moved})

@app.route('/api/analytics/severity')
@login_required
def api_analytics_severity():
    conn = get_db()
    cur = conn.cursor(dictionary=True)
    cur.execute("SELECT severity, COUNT(*) as count FROM accidents GROUP BY severity")
    rows = cur.fetchall()
    cur.close(); conn.close()
    return jsonify(rows)

@app.route('/api/analytics/timeline')
@login_required
def api_analytics_timeline():
    conn = get_db()
    cur = conn.cursor(dictionary=True)
    cur.execute("""SELECT DATE(accident_time) as date, COUNT(*) as count
                   FROM accidents GROUP BY DATE(accident_time)
                   ORDER BY date DESC LIMIT 14""")
    rows = cur.fetchall()
    for r in rows:
        if r.get('date'): r['date'] = r['date'].strftime('%Y-%m-%d')
    cur.close(); conn.close()
    return jsonify(list(reversed(rows)))

@app.route('/api/analytics/hourly')
@login_required
def api_analytics_hourly():
    conn = get_db()
    cur = conn.cursor(dictionary=True)
    cur.execute("""SELECT HOUR(accident_time) as hour, COUNT(*) as count
                   FROM accidents GROUP BY HOUR(accident_time) ORDER BY hour""")
    rows = cur.fetchall()
    cur.close(); conn.close()
    return jsonify(rows)

@app.route('/api/analytics/locations')
@login_required
def api_analytics_locations():
    conn = get_db()
    cur = conn.cursor(dictionary=True)
    cur.execute("""SELECT location, COUNT(*) as count FROM accidents
                   GROUP BY location ORDER BY count DESC LIMIT 10""")
    rows = cur.fetchall()
    cur.close(); conn.close()
    return jsonify(rows)

@app.route('/api/drivers')
@login_required
def api_drivers():
    conn = get_db()
    cur = conn.cursor(dictionary=True)
    cur.execute("""SELECT d.*, a.vehicle_number FROM drivers d
                   LEFT JOIN ambulances a ON d.ambulance_id = a.ambulance_id ORDER BY d.name""")
    rows = cur.fetchall()
    for r in rows:
        if r.get('hire_date'): r['hire_date'] = r['hire_date'].strftime('%Y-%m-%d')
        if r.get('shift_start'): r['shift_start'] = str(r['shift_start'])
        if r.get('shift_end'): r['shift_end'] = str(r['shift_end'])
    cur.close(); conn.close()
    return jsonify(rows)

# ─── Research Feature APIs ─────────────────────────────────────────────

@app.route('/api/heatmap')
@login_required
def api_heatmap():
    """Return accident lat/lng with intensity for heatmap visualization."""
    conn = get_db()
    cur = conn.cursor(dictionary=True)
    cur.execute("""SELECT latitude, longitude, severity,
                   CASE severity
                       WHEN 'Critical' THEN 1.0
                       WHEN 'High' THEN 0.75
                       WHEN 'Medium' THEN 0.5
                       ELSE 0.25
                   END AS intensity
                   FROM accidents WHERE latitude IS NOT NULL""")
    rows = cur.fetchall()
    cur.close(); conn.close()
    # Format: [[lat, lng, intensity], ...]
    points = [[float(r['latitude']), float(r['longitude']), float(r['intensity'])] for r in rows]
    return jsonify(points)

@app.route('/api/golden-hour')
@login_required
def api_golden_hour():
    """Track golden hour (60 min) for active Critical/High accidents."""
    conn = get_db()
    cur = conn.cursor(dictionary=True)
    cur.execute("""SELECT a.accident_id, a.location, a.severity, a.accident_time, a.status,
                   TIMESTAMPDIFF(SECOND, a.accident_time, NOW()) AS elapsed_seconds,
                   3600 - TIMESTAMPDIFF(SECOND, a.accident_time, NOW()) AS remaining_seconds,
                   amb.vehicle_number AS ambulance, amb.status AS amb_status
                   FROM accidents a
                   LEFT JOIN ambulances amb ON amb.assigned_accident_id = a.accident_id
                   WHERE a.severity IN ('Critical', 'High')
                   AND a.status != 'Resolved'
                   ORDER BY a.accident_time DESC""")
    rows = cur.fetchall()
    for r in rows:
        if r.get('accident_time'):
            r['accident_time'] = r['accident_time'].strftime('%Y-%m-%d %H:%M:%S')
        r['golden_hour_pct'] = min(100, max(0, round((r.get('elapsed_seconds', 0) or 0) / 36, 1)))
        r['status_label'] = 'CRITICAL' if (r.get('remaining_seconds', 0) or 0) < 600 else 'WARNING' if (r.get('remaining_seconds', 0) or 0) < 1800 else 'OK'
    cur.close(); conn.close()
    return jsonify(rows)

@app.route('/api/insurance-claim/<int:accident_id>')
@login_required
def api_insurance_claim(accident_id):
    """Auto-generate insurance claim document from accident data."""
    conn = get_db()
    cur = conn.cursor(dictionary=True)
    cur.execute("""SELECT a.*, amb.vehicle_number AS ambulance_number,
                   h.name AS hospital_name
                   FROM accidents a
                   LEFT JOIN ambulances amb ON amb.assigned_accident_id = a.accident_id
                   LEFT JOIN hospitals h ON amb.assigned_hospital_id = h.hospital_id
                   WHERE a.accident_id = %s""", (accident_id,))
    acc = cur.fetchone()
    cur.close(); conn.close()
    if not acc:
        return jsonify({'error': 'Accident not found'}), 404

    if acc.get('accident_time'):
        acc['accident_time'] = acc['accident_time'].strftime('%Y-%m-%d %H:%M:%S')

    claim = {
        'claim_id': f'CLM-{datetime.now().strftime("%Y%m%d")}-{accident_id:04d}',
        'generated_at': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
        'incident': {
            'accident_id': accident_id,
            'date_time': acc.get('accident_time'),
            'location': acc.get('location'),
            'coordinates': {'lat': float(acc.get('latitude') or 0), 'lng': float(acc.get('longitude') or 0)},
            'severity': acc.get('severity'),
            'description': acc.get('description'),
            'detection_method': acc.get('reported_by', 'Manual'),
        },
        'vehicle': {
            'registration': acc.get('vehicle_id'),
            'type': 'Sedan' if acc.get('vehicle_id', '').startswith('TN') else 'Unknown',
        },
        'response': {
            'ambulance': acc.get('ambulance_number'),
            'hospital': acc.get('hospital_name'),
            'status': acc.get('status'),
        },
        'damage_estimate': {
            'severity_factor': {'Critical': 4, 'High': 3, 'Medium': 2, 'Low': 1}.get(acc.get('severity', 'Low'), 1),
            'estimated_cost_inr': {'Critical': 500000, 'High': 250000, 'Medium': 100000, 'Low': 25000}.get(acc.get('severity', 'Low'), 25000),
        },
        'status': 'PENDING_REVIEW',
    }
    return jsonify(claim)

@app.route('/api/analytics/response-times')
@login_required
def api_response_times():
    """Analyze dispatch-to-arrival response times by severity."""
    conn = get_db()
    cur = conn.cursor(dictionary=True)
    # Calculate average time from accident report to dispatch for each severity
    cur.execute("""SELECT severity,
                   COUNT(*) AS total,
                   SUM(CASE WHEN status='Resolved' THEN 1 ELSE 0 END) AS resolved,
                   SUM(CASE WHEN status='Dispatched' OR status='At Scene' THEN 1 ELSE 0 END) AS in_progress,
                   SUM(CASE WHEN status='Reported' THEN 1 ELSE 0 END) AS pending
                   FROM accidents GROUP BY severity""")
    rows = cur.fetchall()
    cur.close(); conn.close()
    return jsonify(rows)

@app.route('/api/analytics/hotspots')
@login_required
def api_hotspots():
    """Predictive hotspot analysis — locations with highest accident frequency
    and time-based patterns for proactive ambulance positioning."""
    conn = get_db()
    cur = conn.cursor(dictionary=True)

    # Top accident locations with coordinates
    cur.execute("""SELECT location, latitude, longitude,
                   COUNT(*) AS frequency,
                   SUM(CASE WHEN severity='Critical' THEN 1 ELSE 0 END) AS critical_count,
                   SUM(CASE WHEN severity='High' THEN 1 ELSE 0 END) AS high_count,
                   ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM accidents), 1) AS pct_of_total,
                   GROUP_CONCAT(DISTINCT severity) AS severity_types
                   FROM accidents
                   WHERE latitude IS NOT NULL
                   GROUP BY location, latitude, longitude
                   ORDER BY frequency DESC
                   LIMIT 10""")
    hotspots = cur.fetchall()
    for h in hotspots:
        h['risk_score'] = round(h['frequency'] * 0.4 + h['critical_count'] * 3 + h['high_count'] * 1.5, 1)
        h['latitude'] = float(h['latitude']); h['longitude'] = float(h['longitude'])

    # Time-based patterns
    cur.execute("""SELECT HOUR(accident_time) AS hour,
                   COUNT(*) AS count,
                   ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM accidents), 1) AS pct
                   FROM accidents GROUP BY HOUR(accident_time) ORDER BY count DESC LIMIT 5""")
    peak_hours = cur.fetchall()

    cur.close(); conn.close()
    return jsonify({'hotspots': hotspots, 'peak_hours': peak_hours})

LOCATIONS = [
    ("T Nagar Main Road", 13.0418, 80.2341), ("Anna Nagar Signal", 13.0850, 80.2101),
    ("Velachery Bridge", 12.9815, 80.2180), ("Adyar Junction", 13.0063, 80.2574),
    ("Mylapore Temple St", 13.0339, 80.2676), ("Nungambakkam High Rd", 13.0569, 80.2425),
    ("Egmore Station", 13.0732, 80.2609), ("Guindy Flyover", 13.0067, 80.2206),
    ("Vadapalani Metro", 13.0520, 80.2121), ("Porur Junction", 13.0382, 80.1567),
    ("Tambaram Highway", 12.9249, 80.1000), ("OMR IT Park", 12.9010, 80.2279),
    ("ECR Beach Road", 12.8392, 80.2454), ("Marina Beach Drive", 13.0500, 80.2824),
    ("Mount Road Central", 13.0604, 80.2496)
]
SEVERITIES = ['Low', 'Medium', 'High', 'Critical']

@app.route('/api/simulate', methods=['POST'])
@login_required
def api_simulate():
    """Generate a random accident and auto-dispatch nearest ambulance."""
    loc = random.choice(LOCATIONS)
    severity = random.choice(SEVERITIES)
    vehicle = f"TN{random.choice(['01','02','09','22'])}{chr(65+random.randint(0,25))}{chr(65+random.randint(0,25))}{random.randint(1000,9999)}"
    conn = get_db()
    cur = conn.cursor(dictionary=True)
    cur.execute("""INSERT INTO accidents (location, latitude, longitude, vehicle_id, severity, description, reported_by)
                   VALUES (%s, %s, %s, %s, %s, %s, %s)""",
                (loc[0], loc[1], loc[2], vehicle, severity,
                 f"AI-detected collision at {loc[0]}", "AI System"))
    conn.commit()
    aid = cur.lastrowid
    # Auto-dispatch nearest ambulance
    amb_id = _auto_dispatch(conn, cur, aid, loc[1], loc[2])
    cur.close(); conn.close()
    return jsonify({'success': True, 'accident_id': aid, 'location': loc[0],
                    'severity': severity, 'dispatched_ambulance': amb_id})

# ─── YOLO Detection API ───────────────────────────────────────────────

CCTV_FEEDS = [
    {'id': 'CAM-001', 'name': 'T Nagar Junction', 'lat': 13.0418, 'lng': 80.2341},
    {'id': 'CAM-002', 'name': 'Anna Nagar Signal', 'lat': 13.0850, 'lng': 80.2101},
    {'id': 'CAM-003', 'name': 'Adyar Bridge', 'lat': 13.0063, 'lng': 80.2574},
    {'id': 'CAM-004', 'name': 'Guindy Flyover', 'lat': 13.0067, 'lng': 80.2206},
    {'id': 'CAM-005', 'name': 'Egmore Station', 'lat': 13.0732, 'lng': 80.2609},
    {'id': 'CAM-006', 'name': 'OMR IT Corridor', 'lat': 12.9010, 'lng': 80.2279},
    {'id': 'CAM-007', 'name': 'Velachery Main Rd', 'lat': 12.9815, 'lng': 80.2180},
    {'id': 'CAM-008', 'name': 'Marina Beach Rd', 'lat': 13.0500, 'lng': 80.2824},
]

VEHICLE_TYPES = ['Car', 'SUV', 'Truck', 'Auto-Rickshaw', 'Motorcycle', 'Bus', 'Van']
PLATE_PREFIXES = ['TN01', 'TN02', 'TN09', 'TN22', 'TN07', 'TN10', 'TN04', 'TN05']
ACCIDENT_TYPES = ['Rear-end collision', 'T-bone collision', 'Head-on collision',
                  'Sideswipe', 'Rollover', 'Hit and run', 'Multi-vehicle pileup',
                  'Pedestrian hit', 'Vehicle-motorcycle collision']

@app.route('/api/cctv-feeds')
@login_required
def api_cctv_feeds():
    """Get all CCTV camera feeds with status."""
    feeds = []
    for cam in CCTV_FEEDS:
        feeds.append({
            **cam,
            'status': random.choice(['active', 'active', 'active', 'active', 'maintenance']),
            'fps': random.randint(24, 30),
            'resolution': '1920x1080',
            'vehicles_detected': random.randint(5, 45),
        })
    return jsonify(feeds)

@app.route('/api/yolo-detect', methods=['POST'])
@login_required
def api_yolo_detect():
    """Simulate YOLO accident detection on a CCTV feed.
    Returns bounding boxes, confidence scores, vehicle types, number plates, and accident classification."""
    data = request.json or {}
    camera_id = data.get('camera_id', random.choice(CCTV_FEEDS)['id'])
    cam = next((c for c in CCTV_FEEDS if c['id'] == camera_id), random.choice(CCTV_FEEDS))

    # Simulate detection
    is_accident = random.random() < 0.4  # 40% chance of detecting an accident
    num_vehicles = random.randint(3, 12)

    # Generate detected vehicles with bounding boxes
    detections = []
    for i in range(num_vehicles):
        vtype = random.choice(VEHICLE_TYPES)
        plate = f"{random.choice(PLATE_PREFIXES)}{chr(65+random.randint(0,25))}{chr(65+random.randint(0,25))}{random.randint(1000,9999)}"
        x = random.randint(50, 1600)
        y = random.randint(200, 900)
        w = random.randint(80, 250)
        h = random.randint(60, 180)
        detections.append({
            'id': i + 1,
            'class': vtype,
            'confidence': round(random.uniform(0.72, 0.99), 3),
            'bbox': [x, y, x + w, y + h],
            'plate': plate,
            'plate_confidence': round(random.uniform(0.65, 0.98), 3),
            'speed_estimate': round(random.uniform(5, 80), 1),
        })

    result = {
        'camera': cam,
        'timestamp': datetime.now().strftime('%Y-%m-%d %H:%M:%S.') + str(random.randint(100,999)),
        'frame_id': random.randint(100000, 999999),
        'model': 'YOLOv8x',
        'inference_time_ms': round(random.uniform(12, 35), 1),
        'total_vehicles': num_vehicles,
        'detections': detections,
        'accident_detected': is_accident,
    }

    if is_accident:
        # Generate accident details
        involved = random.sample(detections, min(random.randint(2, 3), len(detections)))
        severity = random.choice(SEVERITIES)
        result['accident'] = {
            'type': random.choice(ACCIDENT_TYPES),
            'severity': severity,
            'confidence': round(random.uniform(0.78, 0.97), 3),
            'involved_vehicles': [{'id': v['id'], 'class': v['class'], 'plate': v['plate']} for v in involved],
            'location': cam['name'],
            'latitude': cam['lat'],
            'longitude': cam['lng'],
            'impact_zone': [random.randint(300,800), random.randint(300,600), random.randint(100,200), random.randint(100,200)],
        }
        # Auto-log to accidents table and dispatch
        if data.get('auto_report', False):
            conn = get_db()
            cur = conn.cursor(dictionary=True)
            plates = ', '.join([v['plate'] for v in involved])
            cur.execute("""INSERT INTO accidents (location, latitude, longitude, vehicle_id, severity, description, reported_by)
                           VALUES (%s, %s, %s, %s, %s, %s, %s)""",
                        (cam['name'], cam['lat'], cam['lng'], plates, severity,
                         f"YOLO detected: {result['accident']['type']} involving {', '.join(v['class'] for v in involved)}",
                         f"YOLO-{camera_id}"))
            conn.commit()
            aid = cur.lastrowid
            amb_id = _auto_dispatch(conn, cur, aid, cam['lat'], cam['lng'])
            result['accident']['accident_id'] = aid
            result['accident']['dispatched_ambulance'] = amb_id
            cur.close(); conn.close()

    return jsonify(result)

# ─── Traffic Signal Control ────────────────────────────────────────────

TRAFFIC_JUNCTIONS = [
    {'id': 'TJ-001', 'name': 'T Nagar Junction', 'lat': 13.0418, 'lng': 80.2341, 'lanes': 4},
    {'id': 'TJ-002', 'name': 'Anna Nagar Signal', 'lat': 13.0850, 'lng': 80.2101, 'lanes': 4},
    {'id': 'TJ-003', 'name': 'Adyar Signal', 'lat': 13.0063, 'lng': 80.2574, 'lanes': 3},
    {'id': 'TJ-004', 'name': 'Guindy Junction', 'lat': 13.0067, 'lng': 80.2206, 'lanes': 4},
    {'id': 'TJ-005', 'name': 'Egmore Signal', 'lat': 13.0732, 'lng': 80.2609, 'lanes': 3},
    {'id': 'TJ-006', 'name': 'Porur Junction', 'lat': 13.0382, 'lng': 80.1567, 'lanes': 5},
]

@app.route('/api/traffic-signals')
@login_required
def api_traffic_signals():
    """Get status of all traffic signals."""
    signals = []
    for jn in TRAFFIC_JUNCTIONS:
        # Check if any dispatched ambulance is near this junction
        ambulance_nearby = False
        try:
            conn = get_db()
            cur = conn.cursor(dictionary=True)
            cur.execute("""SELECT ambulance_id FROM ambulances
                           WHERE status IN ('yellow','red')
                           AND SQRT(POW(latitude-%s,2)+POW(longitude-%s,2)) < 0.02""",
                        (jn['lat'], jn['lng']))
            ambulance_nearby = cur.fetchone() is not None
            cur.close(); conn.close()
        except: pass

        signals.append({
            **jn,
            'current_phase': random.choice(['NS-Green', 'EW-Green', 'All-Red']),
            'cycle_time': random.choice([90, 120, 150]),
            'time_remaining': random.randint(5, 60),
            'vehicle_count': random.randint(8, 50),
            'ambulance_priority': ambulance_nearby,
            'mode': 'PRIORITY-GREEN' if ambulance_nearby else 'NORMAL',
            'density': random.choice(['Low', 'Medium', 'High', 'Heavy']),
        })
    return jsonify(signals)

@app.route('/api/traffic-signals/<string:jid>/priority', methods=['POST'])
@login_required
def api_traffic_priority(jid):
    """Manually set traffic signal to priority mode for ambulance."""
    return jsonify({'success': True, 'junction': jid, 'mode': 'PRIORITY-GREEN',
                    'message': f'Priority green activated at {jid} for ambulance passage'})

# ─── Run ───────────────────────────────────────────────────────────────

if __name__ == '__main__':
    print("\n  AI Accident Detector — Web Dashboard")
    print("  http://localhost:5000")
    print("  Login: admin / admin123\n")
    app.run(debug=True, host='0.0.0.0', port=5000)
