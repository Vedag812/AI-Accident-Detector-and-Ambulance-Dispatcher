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

        JLabel titleLabel = new JLabel("🗺️ Google Maps - Live Tracking");
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
        logArea.setText("📍 Map Integration Log\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "• HTML map file: accident_map.html\n" +
                "• Open in browser to view interactive map\n" +
                "• Auto-refresh: Every 3 seconds\n\n" +
                "Legend:\n" +
                "  🔴 Red Markers = Accidents\n" +
                "  🔵 Blue Markers = Hospitals\n" +
                "  🟢 Green Markers = Available Ambulances\n" +
                "  🟡 Yellow Markers = Dispatched Ambulances\n\n");

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        scrollPane.getViewport().setBackground(UITheme.CARD_BG);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        JButton openBtn = createButton("🌐 Open in Browser", UITheme.ACCENT);
        openBtn.addActionListener(e -> openMapInBrowser());

        JButton refreshBtn = createButton("🔄 Force Refresh", UITheme.STATUS_AVAILABLE);
        refreshBtn.addActionListener(e -> {
            generateMapHTML();
            log("🔄 Manual refresh triggered");
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
        String apiKey = config.getGoogleMapsApiKey();
        StringBuilder accidentMarkers = new StringBuilder();
        StringBuilder hospitalMarkers = new StringBuilder();
        StringBuilder ambulanceMarkers = new StringBuilder();
        StringBuilder routeLines = new StringBuilder();

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
                String severityIcon = severity.equals("Critical") ? "red"
                        : severity.equals("High") ? "orange" : severity.equals("Medium") ? "yellow" : "green";

                accidentMarkers.append(String.format(
                        "new google.maps.Marker({position:{lat:%.6f,lng:%.6f},map:map," +
                                "title:'%s - %s',icon:'http://maps.google.com/mapfiles/ms/icons/%s-dot.png'});\n",
                        lat, lng, severity, location, severityIcon));
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
                        "new google.maps.Marker({position:{lat:%.6f,lng:%.6f},map:map," +
                                "title:'%s (Beds: %d)',icon:'http://maps.google.com/mapfiles/ms/icons/blue-dot.png'});\n",
                        lat, lng, name, beds));
                hospCount++;
            }

            // Fetch ambulances with GPS and status
            rs = dbManager.executeQuery(
                    "SELECT a.ambulance_id, a.latitude, a.longitude, a.status, a.target_x, a.target_y, " +
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

                String icon = status.equals("green") ? "green" : status.equals("yellow") ? "yellow" : "red";
                String statusText = status.equals("green") ? "Available"
                        : status.equals("yellow") ? "En Route" : "At Scene";

                ambulanceMarkers.append(String.format(
                        "var amb%d = new google.maps.Marker({position:{lat:%.6f,lng:%.6f},map:map," +
                                "title:'%s | %s | %s',icon:'http://maps.google.com/mapfiles/ms/icons/%s-dot.png'," +
                                "animation:google.maps.Animation.DROP});\n",
                        rs.getInt("ambulance_id"), lat, lng, vehicleNum, statusText, driverName, icon));

                // Add route line for dispatched ambulances
                if (!status.equals("green")) {
                    int targetX = rs.getInt("target_x");
                    int targetY = rs.getInt("target_y");
                    if (targetX > 0 && targetY > 0) {
                        double targetLat = 13.0827 + (targetX - 200) * 0.0005;
                        double targetLng = 80.2707 + (targetY - 200) * 0.0005;
                        routeLines.append(String.format(
                                "new google.maps.Polyline({path:[{lat:%.6f,lng:%.6f},{lat:%.6f,lng:%.6f}]," +
                                        "geodesic:true,strokeColor:'%s',strokeOpacity:0.8,strokeWeight:3,map:map});\n",
                                lat, lng, targetLat, targetLng,
                                status.equals("yellow") ? "#F59E0B" : "#E11D48"));
                    }
                }
                ambCount++;
            }

            log(String.format("📍 Updated: %d accidents, %d hospitals, %d ambulances", accCount, hospCount, ambCount));

        } catch (SQLException e) {
            log("❌ Error fetching data: " + e.getMessage());
        }

        String html = "<!DOCTYPE html>\n" +
                "<html><head><title>Accident Alert System - Live Map</title>\n" +
                "<meta charset='utf-8'><meta http-equiv='refresh' content='3'>\n" +
                "<style>\n" +
                "body{margin:0;font-family:'Segoe UI',sans-serif;background:#f8f9fa;}\n" +
                "#map{height:100vh;width:100%;}\n" +
                ".info{position:fixed;top:20px;left:20px;background:rgba(255,255,255,0.95);\n" +
                "padding:20px;border-radius:12px;color:#0f172a;z-index:1000;box-shadow:0 4px 20px rgba(0,0,0,0.1);border:1px solid #e2e8f0;}\n"
                +
                ".info h3{margin:0 0 15px 0;color:#2563eb;}\n" +
                ".legend-item{display:flex;align-items:center;margin:8px 0;font-size:14px;}\n" +
                ".legend-dot{width:12px;height:12px;border-radius:50%;margin-right:10px;}\n" +
                ".stats{margin-top:15px;padding-top:15px;border-top:1px solid #e2e8f0;font-size:12px;color:#64748b;}\n"
                +
                "</style></head><body>\n" +
                "<div class='info'>\n" +
                "<h3>🚑 AI Accident Detector</h3>\n" +
                "<div class='legend-item'><div class='legend-dot' style='background:#e11d48'></div>Critical</div>\n" +
                "<div class='legend-item'><div class='legend-dot' style='background:#f59e0b'></div>High Priority</div>\n"
                +
                "<div class='legend-item'><div class='legend-dot' style='background:#3b82f6'></div>Hospitals</div>\n" +
                "<div class='legend-item'><div class='legend-dot' style='background:#10b981'></div>Available Ambulances</div>\n"
                +
                "<div class='legend-item'><div class='legend-dot' style='background:#f59e0b'></div>En Route</div>\n" +
                "<div class='stats'>Live GPS Tracking | Updated: " +
                new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()) + "</div>\n" +
                "</div>\n" +
                "<div id='map'></div>\n" +
                "<script>\n" +
                "function initMap(){\n" +
                "var map=new google.maps.Map(document.getElementById('map'),{\n" +
                "zoom:13,center:{lat:13.0827,lng:80.2707},\n" +
                "styles:[{featureType:'water',stylers:[{color:'#e9e9e9'}]}," +
                "{featureType:'landscape',stylers:[{color:'#f5f5f5'}]}," +
                "{featureType:'road',stylers:[{visibility:'on'}]}]" +
                "});\n" +
                accidentMarkers.toString() +
                hospitalMarkers.toString() +
                ambulanceMarkers.toString() +
                routeLines.toString() +
                "}\n" +
                "</script>\n" +
                "<script async defer src='https://maps.googleapis.com/maps/api/js?key=" + apiKey
                + "&callback=initMap'></script>\n" +
                "</body></html>";

        try {
            File htmlFile = new File("accident_map.html");
            BufferedWriter writer = new BufferedWriter(new FileWriter(htmlFile));
            writer.write(html);
            writer.close();
        } catch (IOException e) {
            log("❌ Error writing HTML: " + e.getMessage());
        }
    }

    private void openMapInBrowser() {
        try {
            File htmlFile = new File("accident_map.html");
            if (htmlFile.exists()) {
                Desktop.getDesktop().browse(htmlFile.toURI());
                log("🌐 Opened map in browser");
            }
        } catch (Exception e) {
            log("❌ Error opening browser: " + e.getMessage());
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


