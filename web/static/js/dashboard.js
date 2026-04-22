/* ═══════════════════════════════════════════════════════
   Dashboard JS — Uber/Ola Smooth Movement
   ═══════════════════════════════════════════════════════ */

let map, heatLayer;
// Persistent stores - NEVER destroyed, only moved
const accidentMarkers = {};
const hospitalMarkers = {};
const ambulanceMarkers = {};
const ambulanceRoutes = {};   // persistent route lines per ambulance ID
const ambulanceAnim = {};     // { id: { from, to, startTime, duration } }
let aiInterval = null, moveInterval = null;
let aiRunning = false, heatmapVisible = false;

document.addEventListener('DOMContentLoaded', () => {
    initMap();
    refreshData();
    // Full data refresh every 5s (not 3s — less flicker)
    setInterval(refreshData, 5000);
    // Start 60fps animation loop
    requestAnimationFrame(renderFrame);
});

// ─── Map ────────────────────────────────────────────────
function initMap() {
    map = L.map('map', { zoomControl: false }).setView([13.05, 80.24], 12);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap', maxZoom: 19
    }).addTo(map);
    L.control.zoom({ position: 'topright' }).addTo(map);
}

// ─── Icons ──────────────────────────────────────────────
function accIcon(sev) {
    const c = { Critical:'#dc2626', High:'#ea580c', Medium:'#d97706', Low:'#16a34a' }[sev] || '#d97706';
    return L.divIcon({
        html: `<div style="width:16px;height:16px;background:${c};border-radius:50%;
               border:3px solid white;box-shadow:0 0 8px ${c}88,0 2px 6px rgba(0,0,0,.3);
               ${sev==='Critical'?'animation:pulse 1s infinite':''}"></div>`,
        className:'', iconSize:[16,16], iconAnchor:[8,8]
    });
}
const hospIcon = L.divIcon({
    html:'<div style="font-size:22px;filter:drop-shadow(0 2px 4px rgba(0,0,0,.4))">🏥</div>',
    className:'', iconSize:[22,22], iconAnchor:[11,11]
});
function ambIcon(st) {
    const bg = {green:'#16a34a',yellow:'#d97706',red:'#dc2626'}[st]||'#16a34a';
    return L.divIcon({
        html:`<div style="font-size:20px;background:${bg};border-radius:6px;padding:2px 4px;
              line-height:1;filter:drop-shadow(0 2px 4px rgba(0,0,0,.35))">🚑</div>`,
        className:'', iconSize:[28,28], iconAnchor:[14,14]
    });
}

// ─── OSRM ───────────────────────────────────────────────
async function getRoute(lat1,lng1,lat2,lng2) {
    try {
        const r = await fetch(`https://router.project-osrm.org/route/v1/driving/${lng1},${lat1};${lng2},${lat2}?overview=full&geometries=geojson`);
        const d = await r.json();
        if (d.routes?.[0]) return d.routes[0].geometry.coordinates.map(c=>[c[1],c[0]]);
    } catch {}
    return [[lat1,lng1],[lat2,lng2]];
}

// ═══════════════════════════════════════════════════════
// 60fps ANIMATION LOOP — smooth Uber-like gliding
// Each ambulance interpolates between 'from' and 'to'
// positions over 2.5s with ease-in-out
// ═══════════════════════════════════════════════════════
const ANIM_DURATION = 1200; // ms — slightly > tick interval for seamless overlap

function renderFrame(ts) {
    for (const id in ambulanceAnim) {
        const a = ambulanceAnim[id], m = ambulanceMarkers[id];
        if (!m || !a.to) continue;
        const t = Math.min(1, (ts - a.startTime) / a.duration);
        // Ease-in-out cubic for natural Uber feel
        const e = t < 0.5 ? 4*t*t*t : 1 - Math.pow(-2*t+2,3)/2;
        m.setLatLng([
            a.from[0] + (a.to[0]-a.from[0]) * e,
            a.from[1] + (a.to[1]-a.from[1]) * e
        ]);
    }
    requestAnimationFrame(renderFrame);
}

// ─── Data Refresh (stats + feed + map) ──────────────────
async function refreshData() {
    const [stats, accidents, ambulances, hospitals] = await Promise.all([
        api('/api/stats'), api('/api/accidents'),
        api('/api/ambulances'), api('/api/hospitals')
    ]);
    if (stats) updateStats(stats);
    if (accidents) updateFeed(accidents);
    if (accidents && ambulances && hospitals) syncMap(accidents, ambulances, hospitals);
}

function updateStats(s) {
    document.getElementById('stat-accidents').textContent = s.active_accidents||0;
    document.getElementById('stat-ambulances').textContent = `${s.available_ambulances}/${s.total_ambulances}`;
    document.getElementById('stat-beds').textContent = s.available_beds||0;
    document.getElementById('stat-critical').textContent = s.critical_count||0;
    const b = document.getElementById('accident-badge');
    if (b) { b.textContent = s.active_accidents||0; b.style.display = s.active_accidents>0?'inline':'none'; }
}

// ═══════════════════════════════════════════════════════
// SYNC MAP — no destroy/recreate, just add/update/remove
// Routes are persistent per ambulance (not cleared every refresh!)
// ═══════════════════════════════════════════════════════
async function syncMap(accidents, ambulances, hospitals) {
    // ── Accidents ──
    const accIds = new Set(accidents.map(a=>a.accident_id));
    for (const id in accidentMarkers) {
        if (!accIds.has(+id)) { map.removeLayer(accidentMarkers[id]); delete accidentMarkers[id]; }
    }
    accidents.forEach(a => {
        if (!a.latitude||!a.longitude||accidentMarkers[a.accident_id]) return;
        accidentMarkers[a.accident_id] = L.marker([a.latitude,a.longitude],{icon:accIcon(a.severity)})
            .bindPopup(`<div style="font-family:Inter,system-ui;min-width:200px">
                <b>🚨 #${a.accident_id}</b><br>
                <b>Location:</b> ${a.location}<br>
                <b>Severity:</b> <span style="color:${a.severity==='Critical'?'#dc2626':'#d97706'}">${a.severity}</span><br>
                <b>Status:</b> ${a.status||'Reported'}<br>
                <button onclick="dispatchToAccident(${a.accident_id})" style="margin-top:6px;padding:5px 12px;background:#2563eb;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:11px;font-weight:600">🚑 Dispatch</button>
                <button onclick="deleteAccident(${a.accident_id})" style="margin-left:4px;padding:5px 12px;background:#dc2626;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:11px;font-weight:600">🗑 Delete</button>
            </div>`).addTo(map);
    });

    // ── Hospitals (add once, never removed) ──
    hospitals.forEach(h => {
        if (!h.latitude||!h.longitude||hospitalMarkers[h.hospital_id]) return;
        hospitalMarkers[h.hospital_id] = L.marker([h.latitude,h.longitude],{icon:hospIcon})
            .bindPopup(`<b>🏥 ${h.name}</b><br>Beds: ${h.available_beds}/${h.capacity}`).addTo(map);
    });

    // ── Ambulances: smooth glide + persistent routes ──
    const ambIds = new Set(ambulances.map(a=>a.ambulance_id));
    // Remove gone ambulances
    for (const id in ambulanceMarkers) {
        if (!ambIds.has(+id)) {
            map.removeLayer(ambulanceMarkers[id]); delete ambulanceMarkers[id];
            if (ambulanceRoutes[id]) { map.removeLayer(ambulanceRoutes[id]); delete ambulanceRoutes[id]; }
            delete ambulanceAnim[id];
        }
    }

    const now = performance.now();
    for (const a of ambulances) {
        if (!a.latitude||!a.longitude) continue;
        const pos = [a.latitude, a.longitude];
        const id = a.ambulance_id;

        if (ambulanceMarkers[id]) {
            // EXISTING — set animation target (the renderFrame loop handles smooth glide)
            const cur = ambulanceMarkers[id].getLatLng();
            const dist = Math.abs(cur.lat-pos[0]) + Math.abs(cur.lng-pos[1]);
            // Only animate if position actually changed
            if (dist > 0.00005) {
                ambulanceAnim[id] = { from:[cur.lat,cur.lng], to:pos, startTime:now, duration:ANIM_DURATION };
            }
            ambulanceMarkers[id].setIcon(ambIcon(a.status));
        } else {
            // NEW marker — place directly
            ambulanceMarkers[id] = L.marker(pos,{icon:ambIcon(a.status)})
                .bindPopup(`<b>🚑 ${a.vehicle_number||'Amb #'+id}</b><br>Fuel: ${a.fuel_level||0}%`).addTo(map);
            ambulanceAnim[id] = { from:pos, to:pos, startTime:now, duration:ANIM_DURATION };
        }

        // ── PERSISTENT ROUTES — redraw when target changes or position moves ──
        const hasTarget = a.status !== 'green' && a.target_x && a.target_y;
        if (hasTarget) {
            const tKey = `${a.target_x},${a.target_y}`;
            const oldKey = ambulanceRoutes[id]?._targetKey;
            // Redraw route if target changed
            if (oldKey !== tKey) {
                if (ambulanceRoutes[id]) map.removeLayer(ambulanceRoutes[id]);
                const tLat = parseFloat(a.target_x), tLng = parseFloat(a.target_y);
                const color = a.status==='yellow'?'#2563eb':'#dc2626';
                getRoute(a.latitude,a.longitude,tLat,tLng).then(pts => {
                    if (ambulanceRoutes[id]) map.removeLayer(ambulanceRoutes[id]);
                    const line = L.polyline(pts,{color,weight:4,opacity:0.7,dashArray:a.status==='yellow'?'10,6':'6,4'}).addTo(map);
                    line._targetKey = tKey;
                    ambulanceRoutes[id] = line;
                });
            }
        } else {
            // Not dispatched — remove route if exists
            if (ambulanceRoutes[id]) { map.removeLayer(ambulanceRoutes[id]); delete ambulanceRoutes[id]; }
        }
    }

    document.getElementById('map-stats').textContent =
        `${accidents.length} accidents • ${hospitals.length} hospitals • ${ambulances.length} ambulances`;
}

// ─── Heatmap ────────────────────────────────────────────
async function toggleHeatmap() {
    if (heatmapVisible && heatLayer) { map.removeLayer(heatLayer); heatLayer=null; heatmapVisible=false; return; }
    const pts = await api('/api/heatmap');
    if (pts?.length) {
        heatLayer = L.heatLayer(pts,{radius:30,blur:20,maxZoom:15,max:1.0,
            gradient:{0.2:'#2196F3',0.4:'#4CAF50',0.6:'#FFC107',0.8:'#FF5722',1.0:'#F44336'}}).addTo(map);
        heatmapVisible = true;
    }
}

// ─── Move Ambulances (position tick only — NO full refresh!) ──
async function moveAmbulances() {
    const res = await api('/api/move-ambulances', 'POST');
    if (!res) return;
    // Always fetch positions for smooth continuous movement
    {
        const ambulances = await api('/api/ambulances');
        if (ambulances) {
            const now = performance.now();
            for (const a of ambulances) {
                if (!a.latitude||!a.longitude||!ambulanceMarkers[a.ambulance_id]) continue;
                const cur = ambulanceMarkers[a.ambulance_id].getLatLng();
                const pos = [a.latitude, a.longitude];
                const dist = Math.abs(cur.lat-pos[0]) + Math.abs(cur.lng-pos[1]);
                if (dist > 0.00005) {
                    ambulanceAnim[a.ambulance_id] = { from:[cur.lat,cur.lng], to:pos, startTime:now, duration:ANIM_DURATION };
                }
                ambulanceMarkers[a.ambulance_id].setIcon(ambIcon(a.status));
            }
        }
        // Show toasts for events
        if (res.events) {
            res.events.forEach(e => {
                if (e.type==='arrived_scene') showToast(`🚑 Amb #${e.ambulance_id} at scene`,'warning');
                else if (e.type==='transporting') showToast(`🏥 Transporting to ${e.hospital}`,'info');
                else if (e.type==='delivered') showToast(`✅ Patient delivered — Amb #${e.ambulance_id} free`,'success');
            });
        }
    }
}

// ─── Feed ───────────────────────────────────────────────
function updateFeed(accidents) {
    const c = document.getElementById('accident-feed');
    if (!accidents.length) {
        c.innerHTML = `<div class="empty-state"><div class="icon">📡</div><h3>No accidents</h3><p>System clear</p></div>`;
        return;
    }
    c.innerHTML = accidents.slice(0,25).map(a=>`
        <div class="feed-item">
            <div class="feed-dot ${severityClass(a.severity)}"></div>
            <div class="feed-content" onclick="focusAccident(${a.latitude},${a.longitude})" style="cursor:pointer;flex:1">
                <h4>${a.location||'Unknown'}
                    <span class="badge badge-${(a.status||'reported').toLowerCase().replace(' ','-')}" style="font-size:9px;margin-left:6px">${a.status||'Reported'}</span></h4>
                <p>${a.severity} • ${a.vehicle_id||''}</p>
            </div>
            <span class="feed-time">${timeAgo(a.accident_time)}</span>
            <button onclick="deleteAccident(${a.accident_id})" title="Delete"
                style="background:none;border:none;color:var(--red);cursor:pointer;font-size:16px;padding:4px 6px;opacity:.5"
                onmouseover="this.style.opacity=1" onmouseout="this.style.opacity=0.5">✕</button>
        </div>`).join('');
}

function focusAccident(lat,lng) { if(lat&&lng) map.setView([lat,lng],15,{animate:true}); }

// ─── Actions ────────────────────────────────────────────
async function dispatchToAccident(id) {
    const r = await api('/api/dispatch','POST',{accident_id:id});
    if (r?.success) { showToast(`🚑 Amb #${r.ambulance_id} dispatched!`,'success'); refreshData(); }
    else showToast(r?.error||'Failed','error');
}
async function deleteAccident(id) {
    const r = await api(`/api/accidents/${id}`,'DELETE');
    if (r?.success) { showToast('Deleted','success'); refreshData(); }
}
function showReportModal() { openModal('report-modal'); }
async function submitAccidentReport() {
    const loc = document.getElementById('report-location').value.trim();
    if (!loc) { showToast('Enter location','warning'); return; }
    const r = await api('/api/accidents','POST',{
        location:loc, vehicle_id:document.getElementById('report-vehicle').value,
        severity:document.getElementById('report-severity').value,
        description:document.getElementById('report-desc').value
    });
    if (r?.success) { showToast('Reported!','success'); closeModal('report-modal'); refreshData(); }
}
async function clearAllAccidents() {
    if (!confirm('Clear ALL accidents and reset fleet?')) return;
    const r = await api('/api/accidents/clear','POST');
    if (r?.success) {
        showToast(`Cleared ${r.deleted}`,'success');
        for (const id in accidentMarkers) { map.removeLayer(accidentMarkers[id]); delete accidentMarkers[id]; }
        refreshData();
    }
}

// ─── AI Toggle ──────────────────────────────────────────
function toggleAI() {
    const btn = document.getElementById('ai-toggle');
    const indicator = document.getElementById('ai-indicator');
    const statusEl = document.getElementById('ai-status');
    const detailEl = document.getElementById('ai-detail');
    if (aiRunning) {
        clearInterval(aiInterval); clearInterval(moveInterval);
        aiInterval=moveInterval=null; aiRunning=false;
        btn.textContent='▶ START'; btn.className='btn btn-success';
        indicator.className='ai-indicator';
        statusEl.textContent='AI Detection Idle'; detailEl.textContent='Paused';
    } else {
        aiRunning=true;
        btn.textContent='⏸ STOP'; btn.className='btn btn-danger';
        indicator.className='ai-indicator scanning';
        statusEl.textContent='AI Scanning Active'; detailEl.textContent='Monitoring CCTV feeds...';
        // Movement tick every 1.5s (smooth loop interpolates between ticks)
        moveInterval = setInterval(moveAmbulances, 1000);
        const interval = parseInt(document.getElementById('ai-interval').value)*1000;
        aiInterval = setInterval(async()=>{
            indicator.className='ai-indicator detecting';
            detailEl.textContent='Anomaly detected! Analyzing...';
            const r = await api('/api/simulate','POST');
            if (r?.success) {
                const msg = r.dispatched_ambulance?` → Amb #${r.dispatched_ambulance}`:' → No ambulances';
                showToast(`🔍 ${r.severity} at ${r.location}${msg}`,'warning');
                detailEl.textContent=`Last: ${r.location} — ${r.severity}${msg}`;
                refreshData();
            }
            setTimeout(()=>{ if(aiRunning){ indicator.className='ai-indicator scanning'; detailEl.textContent='Monitoring...'; }},1500);
        }, interval);
    }
}
