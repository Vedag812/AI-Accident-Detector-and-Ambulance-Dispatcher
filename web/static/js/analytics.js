/* ═══════════════════════════════════════════════
   Analytics JS — Chart.js visualizations
   ═══════════════════════════════════════════════ */

const chartColors = {
    blue: 'rgba(59,130,246,0.8)',
    blueBg: 'rgba(59,130,246,0.1)',
    purple: 'rgba(139,92,246,0.8)',
    purpleBg: 'rgba(139,92,246,0.1)',
    green: 'rgba(16,185,129,0.8)',
    yellow: 'rgba(245,158,11,0.8)',
    orange: 'rgba(249,115,22,0.8)',
    red: 'rgba(239,68,68,0.8)',
    text: '#94a3b8',
    grid: 'rgba(51,65,85,0.3)',
    white: '#f1f5f9'
};

Chart.defaults.color = chartColors.text;
Chart.defaults.font.family = 'Inter, sans-serif';
Chart.defaults.plugins.legend.labels.usePointStyle = true;
Chart.defaults.plugins.legend.labels.pointStyle = 'circle';
Chart.defaults.plugins.legend.labels.padding = 16;

let charts = {};

document.addEventListener('DOMContentLoaded', () => loadAnalytics());

// ─── Load All Analytics ─────────────────────────
async function loadAnalytics() {
    const [stats, severity, timeline, hourly, locations] = await Promise.all([
        api('/api/stats'),
        api('/api/analytics/severity'),
        api('/api/analytics/timeline'),
        api('/api/analytics/hourly'),
        api('/api/analytics/locations')
    ]);

    if (stats) {
        document.getElementById('a-total').textContent = stats.total_accidents || 0;
        document.getElementById('a-critical').textContent = stats.critical_count || 0;
        document.getElementById('a-hospitals').textContent = stats.total_hospitals || 0;
        document.getElementById('a-fleet').textContent = stats.total_ambulances || 0;
    }

    if (severity) renderSeverityChart(severity);
    if (timeline) renderTimelineChart(timeline);
    if (hourly) renderHourlyChart(hourly);
    if (locations) renderLocationsChart(locations);
}

// ─── Severity Doughnut ──────────────────────────
function renderSeverityChart(data) {
    if (charts.severity) charts.severity.destroy();
    const colorMap = { Critical: chartColors.red, High: chartColors.orange, Medium: chartColors.yellow, Low: chartColors.green };

    charts.severity = new Chart(document.getElementById('chart-severity'), {
        type: 'doughnut',
        data: {
            labels: data.map(d => d.severity),
            datasets: [{
                data: data.map(d => d.count),
                backgroundColor: data.map(d => colorMap[d.severity] || chartColors.blue),
                borderWidth: 0,
                spacing: 4,
                borderRadius: 6,
                hoverOffset: 10
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '65%',
            plugins: {
                legend: { position: 'bottom' }
            }
        }
    });
}

// ─── Timeline Area Chart ────────────────────────
function renderTimelineChart(data) {
    if (charts.timeline) charts.timeline.destroy();

    charts.timeline = new Chart(document.getElementById('chart-timeline'), {
        type: 'line',
        data: {
            labels: data.map(d => d.date ? d.date.substring(5) : ''),
            datasets: [{
                label: 'Accidents',
                data: data.map(d => d.count),
                borderColor: chartColors.blue,
                backgroundColor: chartColors.blueBg,
                fill: true,
                tension: 0.4,
                borderWidth: 2.5,
                pointRadius: 4,
                pointHoverRadius: 7,
                pointBackgroundColor: chartColors.blue,
                pointBorderColor: '#0f172a',
                pointBorderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: { grid: { color: chartColors.grid, drawBorder: false } },
                y: {
                    beginAtZero: true,
                    grid: { color: chartColors.grid, drawBorder: false },
                    ticks: { stepSize: 1 }
                }
            },
            plugins: {
                legend: { display: false }
            }
        }
    });
}

// ─── Hourly Bar Chart ───────────────────────────
function renderHourlyChart(data) {
    if (charts.hourly) charts.hourly.destroy();

    const hours = Array.from({length: 24}, (_, i) => i);
    const counts = hours.map(h => {
        const found = data.find(d => d.hour === h);
        return found ? found.count : 0;
    });

    charts.hourly = new Chart(document.getElementById('chart-hourly'), {
        type: 'bar',
        data: {
            labels: hours.map(h => `${h}:00`),
            datasets: [{
                label: 'Accidents',
                data: counts,
                backgroundColor: counts.map(c =>
                    c > 5 ? chartColors.red : c > 3 ? chartColors.orange : c > 1 ? chartColors.yellow : chartColors.blue
                ),
                borderRadius: 4,
                borderSkipped: false,
                barPercentage: 0.7
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: { grid: { display: false } },
                y: {
                    beginAtZero: true,
                    grid: { color: chartColors.grid, drawBorder: false },
                    ticks: { stepSize: 1 }
                }
            },
            plugins: {
                legend: { display: false }
            }
        }
    });
}

// ─── Top Locations Horizontal Bar ───────────────
function renderLocationsChart(data) {
    if (charts.locations) charts.locations.destroy();

    const gradient = data.map((_, i) => {
        const ratio = i / Math.max(data.length - 1, 1);
        const r = Math.round(59 + (239 - 59) * ratio);
        const g = Math.round(130 + (68 - 130) * ratio);
        const b = Math.round(246 + (68 - 246) * ratio);
        return `rgba(${r},${g},${b},0.8)`;
    });

    charts.locations = new Chart(document.getElementById('chart-locations'), {
        type: 'bar',
        data: {
            labels: data.map(d => d.location ? (d.location.length > 20 ? d.location.substring(0,20)+'...' : d.location) : 'Unknown'),
            datasets: [{
                label: 'Accidents',
                data: data.map(d => d.count),
                backgroundColor: gradient,
                borderRadius: 4,
                borderSkipped: false
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            indexAxis: 'y',
            scales: {
                x: {
                    beginAtZero: true,
                    grid: { color: chartColors.grid, drawBorder: false },
                    ticks: { stepSize: 1 }
                },
                y: { grid: { display: false } }
            },
            plugins: {
                legend: { display: false }
            }
        }
    });
}
