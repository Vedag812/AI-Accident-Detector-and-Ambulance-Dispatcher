import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.sql.*;

/**
 * GoogleMapsPanel - Real-time auto-refreshing Google Maps with live accident
 * markers
 */
public class GoogleMapsPanel extends JPanel {
    private ConfigManager config;
    private DatabaseManager dbManager;
    private JLabel statusLabel;
    private JTextArea logArea;
    private Timer autoRefreshTimer;
    private int refreshCount = 0;

    // All colors now come from UITheme for centralized styling

    public GoogleMapsPanel() {
        this.config = ConfigManager.getInstance();
        this.dbManager = DatabaseManager.getInstance();
        initializeUI();
        generateMapHTML();
        startAutoRefresh();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 15));
        setBackground(UITheme.PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Google Maps — Live Tracking");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        statusLabel = new JLabel("● Live - Auto-refreshing");
        statusLabel.setFont(UITheme.FONT_SMALL.deriveFont(Font.BOLD));
        statusLabel.setForeground(UITheme.STATUS_AVAILABLE);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center - Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(UITheme.CARD_BG);
        logArea.setForeground(UITheme.TEXT_SECONDARY);
        logArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        logArea.setText("Map Integration Log\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "• HTML map file: accident_map.html\n" +
                "• Open in browser to view interactive map\n" +
                "• Auto-refresh: Every 3 seconds\n\n" +
                "Legend:\n" +
                "  Red Markers = Accidents\n" +
                "  Blue Markers = Hospitals\n" +
                "  Green Markers = Available Ambulances\n" +
                "  Yellow Markers = Dispatched Ambulances\n\n");

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        scrollPane.getViewport().setBackground(UITheme.CARD_BG);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        JButton openBtn = createButton("Open in Browser", UITheme.ACCENT);
        openBtn.addActionListener(e -> openMapInBrowser());

        JButton refreshBtn = createButton("Force Refresh", UITheme.STATUS_AVAILABLE);
        refreshBtn.addActionListener(e -> {
            generateMapHTML();
            log("Manual refresh triggered");
        });

        buttonPanel.add(openBtn);
        buttonPanel.add(refreshBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getModel().isRollover() ? color : color.darker());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(180, 40));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void log(String message) {
        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        logArea.append("[" + timestamp + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void startAutoRefresh() {
        autoRefreshTimer = new Timer(3000, e -> {
            generateMapHTML();
            refreshCount++;
            statusLabel.setText("● Live - Refresh #" + refreshCount);
        });
        autoRefreshTimer.start();
    }

    public void generateMapHTML() {
        StringBuilder accidentMarkers = new StringBuilder();
        StringBuilder hospitalMarkers = new StringBuilder();
        StringBuilder ambulanceMarkers = new StringBuilder();

        try {
            // Fetch accidents with GPS coordinates
            ResultSet rs = dbManager.executeQuery(
                    "SELECT accident_id, location, severity, latitude, longitude FROM accidents ORDER BY accident_time DESC LIMIT 20");
            int accCount = 0;
            while (rs.next()) {
                double lat = rs.getDouble("latitude");
                double lng = rs.getDouble("longitude");
                if (lat == 0 || lng == 0)
                    continue;

                String severity = rs.getString("severity");
                String location = rs.getString("location").replace("'", "\\'");
                String color = severity.equals("Critical") ? "#e11d48"
                        : severity.equals("High") ? "#f59e0b" : severity.equals("Medium") ? "#eab308" : "#22c55e";

                accidentMarkers.append(String.format(
                        "L.circleMarker([%.6f, %.6f], {radius: 10, fillColor: '%s', color: '#fff', weight: 2, opacity: 1, fillOpacity: 0.8}).addTo(map).bindPopup('<b>%s</b><br>%s');\n",
                        lat, lng, color, severity + " Accident", location));
                accCount++;
            }

            // Fetch hospitals with real GPS
            rs = dbManager.executeQuery("SELECT hospital_id, name, latitude, longitude, available_beds FROM hospitals");
            int hospCount = 0;
            while (rs.next()) {
                double lat = rs.getDouble("latitude");
                double lng = rs.getDouble("longitude");
                if (lat == 0 || lng == 0)
                    continue;

                String name = rs.getString("name").replace("'", "\\'");
                int beds = rs.getInt("available_beds");

                hospitalMarkers.append(String.format(
                        "L.marker([%.6f, %.6f], {icon: L.divIcon({className: 'hospital-marker', html: '🏥', iconSize: [30, 30]})}).addTo(map).bindPopup('<b>%s</b><br>Available Beds: %d');\n",
                        lat, lng, name, beds));
                hospCount++;
            }

            // Fetch ambulances with GPS and status
            rs = dbManager.executeQuery(
                    "SELECT a.ambulance_id, a.latitude, a.longitude, a.status, " +
                            "a.vehicle_number, d.name as driver_name FROM ambulances a " +
                            "LEFT JOIN drivers d ON a.driver_id = d.driver_id");
            int ambCount = 0;
            while (rs.next()) {
                double lat = rs.getDouble("latitude");
                double lng = rs.getDouble("longitude");
                if (lat == 0 || lng == 0)
                    continue;

                String status = rs.getString("status");
                String vehicleNum = rs.getString("vehicle_number");
                String driverName = rs.getString("driver_name");
                if (driverName == null)
                    driverName = "Unassigned";

                String color = status.equals("green") ? "#22c55e" : status.equals("yellow") ? "#f59e0b" : "#e11d48";
                String statusText = status.equals("green") ? "Available"
                        : status.equals("yellow") ? "En Route" : "At Scene";

                ambulanceMarkers.append(String.format(
                        "L.circleMarker([%.6f, %.6f], {radius: 8, fillColor: '%s', color: '#fff', weight: 2, opacity: 1, fillOpacity: 0.9}).addTo(map).bindPopup('<b>%s</b><br>Status: %s<br>Driver: %s');\n",
                        lat, lng, color, vehicleNum, statusText, driverName));
                ambCount++;
            }

            log(String.format("Updated: %d accidents, %d hospitals, %d ambulances", accCount, hospCount, ambCount));

        } catch (SQLException e) {
            log("Error fetching data: " + e.getMessage());
        }

        String html = "<!DOCTYPE html>\n" +
                "<html><head><title>Accident Alert System - Live Map</title>\n" +
                "<meta charset='utf-8'><meta http-equiv='refresh' content='5'>\n" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>\n" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>\n" +
                "<style>\n" +
                "body{margin:0;font-family:'Segoe UI',sans-serif;}\n" +
                "#map{height:100vh;width:100%;}\n" +
                ".info{position:fixed;top:20px;left:20px;background:rgba(255,255,255,0.95);\n" +
                "padding:20px;border-radius:12px;color:#0f172a;z-index:1000;box-shadow:0 4px 20px rgba(0,0,0,0.15);border:1px solid #e2e8f0;max-width:220px;}\n"
                +
                ".info h3{margin:0 0 15px 0;color:#2563eb;font-size:16px;}\n" +
                ".legend-item{display:flex;align-items:center;margin:8px 0;font-size:13px;}\n" +
                ".legend-dot{width:12px;height:12px;border-radius:50%;margin-right:10px;}\n" +
                ".stats{margin-top:15px;padding-top:15px;border-top:1px solid #e2e8f0;font-size:11px;color:#64748b;}\n"
                +
                ".hospital-marker{font-size:24px;text-shadow:0 2px 4px rgba(0,0,0,0.3);}\n" +
                "</style></head><body>\n" +
                "<div class='info'>\n" +
                "<h3>AI Accident Detector</h3>\n" +
                "<div class='legend-item'><div class='legend-dot' style='background:#e11d48'></div>Critical/At Scene</div>\n"
                +
                "<div class='legend-item'><div class='legend-dot' style='background:#f59e0b'></div>High/En Route</div>\n"
                +
                "<div class='legend-item'><div class='legend-dot' style='background:#22c55e'></div>Available</div>\n" +
                "<div class='legend-item'>H Hospitals</div>\n" +
                "<div class='stats'>Live GPS Tracking<br>Updated: " +
                new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()) + "</div>\n" +
                "</div>\n" +
                "<div id='map'></div>\n" +
                "<script>\n" +
                "var map = L.map('map').setView([13.0827, 80.2707], 12);\n" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
                "    attribution: '© OpenStreetMap contributors'\n" +
                "}).addTo(map);\n" +
                accidentMarkers.toString() +
                hospitalMarkers.toString() +
                ambulanceMarkers.toString() +
                "</script>\n" +
                "</body></html>";

        try {
            File htmlFile = new File("accident_map.html");
            BufferedWriter writer = new BufferedWriter(new FileWriter(htmlFile));
            writer.write(html);
            writer.close();
        } catch (IOException e) {
            log("Error writing HTML: " + e.getMessage());
        }
    }

    private void openMapInBrowser() {
        try {
            File htmlFile = new File("accident_map.html");
            if (htmlFile.exists()) {
                Desktop.getDesktop().browse(htmlFile.toURI());
                log("Opened map in browser");
            }
        } catch (Exception e) {
            log("Error opening browser: " + e.getMessage());
        }
    }

    public void refreshMap() {
        generateMapHTML();
    }

    public void stopAutoRefresh() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }
    }
}
