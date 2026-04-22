import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Random;

/**
 * Main - Premium dark-themed command center with AI auto-accident generation
 */
public class Main extends JFrame {
    private DatabaseManager dbManager;
    private WeatherService weatherService;
    private AmbulanceMovementSync ambulanceSync;
    private NotificationManager notificationManager;

    private JTable accidentTable;
    private JTable ambulanceTable;
    private JTable hospitalTable;
    private DefaultTableModel accidentModel;
    private DefaultTableModel ambulanceModel;
    private DefaultTableModel hospitalModel;

    private JLabel weatherLabel;
    private JLabel statsLabel;

    // AI Detection Panel components
    private JPanel aiDetectionPanel;
    private JLabel aiStatusLabel;
    private JLabel aiLocationLabel;
    private JLabel aiThreatLabel;
    private JLabel aiConfidenceLabel;
    private int aiScanProgress = 0;
    private int aiPhase = 0; // 0=Scanning, 1=Analyzing, 2=Detected
    private String[] scanLocations;
    private int currentScanIndex = 0;

    private JComboBox<String> intervalCombo;
    private JToggleButton autoGenToggle;
    private Timer refreshTimer;
    private Timer accidentGenTimer;
    private Timer aiAnimationTimer;
    private int countdownValue;
    private int currentUserId;

    private static final String[] LOCATIONS = {
            "T Nagar Main Road", "Anna Nagar Signal", "Velachery Bridge", "Adyar Junction",
            "Mylapore Temple St", "Nungambakkam High Rd", "Egmore Station", "Guindy Flyover",
            "Vadapalani Metro", "Porur Junction", "Tambaram Highway", "OMR IT Park",
            "ECR Beach Road", "Marina Beach Drive", "Mount Road Central"
    };

    private static final String[] SEVERITIES = { "Low", "Medium", "High", "Critical" };
    private static final String[] VEHICLE_PREFIXES = { "TN01", "TN02", "TN09", "TN22", "TN07" };

    private Random random = new Random();

    public Main(int userId, String userRole) {
        this.currentUserId = userId;
        this.dbManager = DatabaseManager.getInstance();
        this.weatherService = new WeatherService();
        this.ambulanceSync = new AmbulanceMovementSync();
        this.notificationManager = NotificationManager.getInstance();

        initializeUI();
        startRefreshTimer();
        ambulanceSync.start();

        notificationManager.showInfo("Welcome", "Logged in as " + userRole);
    }

    private void initializeUI() {
        setTitle("AI Accident Detector — Command Center");
        setSize(1500, 950);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.DARK_BG);
        setLayout(new BorderLayout(0, 0));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(37, 99, 235, 15),
                        getWidth(), 0, new Color(37, 99, 235, 5));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(UITheme.BORDER);
                g2d.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2d.dispose();
            }
        };
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Left side - Title
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JLabel iconTitle = new JLabel("AI Accident Detector");
        iconTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        iconTitle.setForeground(UITheme.TEXT_PRIMARY);
        leftPanel.add(iconTitle);

        JLabel subtitleLabel = new JLabel("Real-time Emergency Response Command Center");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(UITheme.TEXT_SECONDARY);
        leftPanel.add(subtitleLabel);

        // Center - AI Detection Panel
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        centerPanel.setOpaque(false);

        // Initialize scan locations
        scanLocations = LOCATIONS.clone();

        // AI Detection Panel - The main visual element
        aiDetectionPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background with light gradient
                GradientPaint bgGrad = new GradientPaint(0, 0, new Color(255, 255, 255),
                        getWidth(), getHeight(), new Color(248, 250, 252));
                g2d.setPaint(bgGrad);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                // Border
                g2d.setColor(new Color(226, 232, 240));
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);

                // Circular progress indicator
                int size = 60;
                int x = 15;
                int y = (getHeight() - size) / 2;

                // Background circle
                g2d.setColor(new Color(226, 232, 240));
                g2d.setStroke(new BasicStroke(3f));
                g2d.drawOval(x, y, size, size);

                // Progress arc
                Color progressColor = aiPhase == 2 ? UITheme.STATUS_CRITICAL
                        : aiPhase == 1 ? UITheme.STATUS_DISPATCHED : UITheme.STATUS_AVAILABLE;
                g2d.setColor(progressColor);
                g2d.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int angle = (int) (360 * aiScanProgress / 100.0);
                g2d.drawArc(x, y, size, size, 90, -angle);

                // Center icon
                g2d.setColor(progressColor);
                g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
                String icon = aiPhase == 2 ? "!!" : aiPhase == 1 ? "?" : "~";
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(icon, x + (size - fm.stringWidth(icon)) / 2, y + size / 2 + 8);

                g2d.dispose();
            }
        };
        aiDetectionPanel.setPreferredSize(new Dimension(320, 80));
        aiDetectionPanel.setLayout(null);
        aiDetectionPanel.setOpaque(false);
        aiDetectionPanel.setVisible(false); // Hidden until AI starts

        // Status label
        aiStatusLabel = new JLabel("");
        aiStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        aiStatusLabel.setForeground(UITheme.TEXT_MUTED);
        aiStatusLabel.setBounds(90, 10, 200, 20);
        aiDetectionPanel.add(aiStatusLabel);

        // Location being scanned
        aiLocationLabel = new JLabel("");
        aiLocationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        aiLocationLabel.setForeground(new Color(100, 116, 139));
        aiLocationLabel.setBounds(90, 30, 220, 18);
        aiDetectionPanel.add(aiLocationLabel);

        // Threat level
        aiThreatLabel = new JLabel("");
        aiThreatLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        aiThreatLabel.setForeground(UITheme.STATUS_AVAILABLE);
        aiThreatLabel.setBounds(90, 50, 120, 18);
        aiDetectionPanel.add(aiThreatLabel);

        // Confidence
        aiConfidenceLabel = new JLabel("");
        aiConfidenceLabel.setFont(new Font("Consolas", Font.BOLD, 11));
        aiConfidenceLabel.setForeground(new Color(100, 116, 139));
        aiConfidenceLabel.setBounds(200, 50, 100, 18);
        aiDetectionPanel.add(aiConfidenceLabel);

        // Interval dropdown
        JPanel intervalPanel = new JPanel();
        intervalPanel.setOpaque(false);
        intervalPanel.setLayout(new BoxLayout(intervalPanel, BoxLayout.Y_AXIS));

        JLabel intervalLabel = new JLabel("Scan Interval");
        intervalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        intervalLabel.setForeground(UITheme.TEXT_SECONDARY);
        intervalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] intervals = { "3 sec", "5 sec", "8 sec", "10 sec", "15 sec" };
        intervalCombo = new JComboBox<>(intervals);
        intervalCombo.setSelectedIndex(1); // default 5 sec
        intervalCombo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        intervalCombo.setMaximumSize(new Dimension(100, 30));
        intervalCombo.setAlignmentX(Component.CENTER_ALIGNMENT);

        intervalPanel.add(intervalLabel);
        intervalPanel.add(Box.createVerticalStrut(4));
        intervalPanel.add(intervalCombo);

        // Toggle button
        autoGenToggle = new JToggleButton("▶ START AI") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected()) {
                    GradientPaint gp = new GradientPaint(0, 0, UITheme.STATUS_CRITICAL,
                            0, getHeight(), UITheme.STATUS_CRITICAL.darker());
                    g2d.setPaint(gp);
                } else {
                    GradientPaint gp = new GradientPaint(0, 0, UITheme.STATUS_AVAILABLE,
                            0, getHeight(), UITheme.STATUS_AVAILABLE.darker());
                    g2d.setPaint(gp);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String text = isSelected() ? "⏹ STOP" : "▶ START";
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
        };
        autoGenToggle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        autoGenToggle.setPreferredSize(new Dimension(100, 45));
        autoGenToggle.setContentAreaFilled(false);
        autoGenToggle.setBorderPainted(false);
        autoGenToggle.setFocusPainted(false);
        autoGenToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        autoGenToggle.addActionListener(e -> toggleAutoGeneration());

        centerPanel.add(aiDetectionPanel);
        centerPanel.add(intervalPanel);
        centerPanel.add(autoGenToggle);

        // Right side - Stats & Weather
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        weatherLabel = new JLabel(weatherService.getWeatherDisplay());
        weatherLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        weatherLabel.setForeground(UITheme.TEXT_SECONDARY);
        weatherLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        statsLabel = new JLabel("");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsLabel.setForeground(UITheme.ACCENT);
        statsLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        updateStats();

        rightPanel.add(weatherLabel);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(statsLabel);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);

        // Initialize timers
        countdownValue = 5;

        // Main accident generation timer - create first
        accidentGenTimer = new Timer(1000, e -> {
            countdownValue--;

            if (countdownValue <= 0) {
                // Generate accident
                generateRandomAccident();
                countdownValue = getSelectedInterval();
                aiScanProgress = 0; // Reset animation
                aiPhase = 0;
            }
        });

        // AI Animation timer - runs every 100ms for smooth animation
        aiAnimationTimer = new Timer(100, e -> {
            if (accidentGenTimer != null && accidentGenTimer.isRunning()) {
                // Increment progress
                aiScanProgress += 2;

                // Update location being scanned
                if (aiScanProgress % 20 == 0 && scanLocations != null) {
                    currentScanIndex = (currentScanIndex + 1) % scanLocations.length;
                    aiLocationLabel.setText(scanLocations[currentScanIndex]);
                }

                // Phase transitions based on progress
                if (aiScanProgress < 60) {
                    aiPhase = 0; // Scanning
                    aiStatusLabel.setText("SCANNING");
                    aiStatusLabel.setForeground(UITheme.STATUS_AVAILABLE);
                    aiThreatLabel.setText("Threat: LOW");
                    aiThreatLabel.setForeground(UITheme.STATUS_AVAILABLE);
                    aiConfidenceLabel.setText(aiScanProgress + "%");
                } else if (aiScanProgress < 85) {
                    aiPhase = 1; // Analyzing
                    aiStatusLabel.setText("ANALYZING");
                    aiStatusLabel.setForeground(UITheme.STATUS_DISPATCHED);
                    aiThreatLabel.setText("Threat: MEDIUM");
                    aiThreatLabel.setForeground(UITheme.STATUS_DISPATCHED);
                    aiConfidenceLabel.setText(aiScanProgress + "%");
                } else if (aiScanProgress >= 100) {
                    aiPhase = 2; // Detected!
                    aiStatusLabel.setText("DETECTED!");
                    aiStatusLabel.setForeground(UITheme.STATUS_CRITICAL);
                    String[] threats = { "LOW", "MEDIUM", "HIGH", "CRITICAL" };
                    int threatIdx = random.nextInt(4);
                    aiThreatLabel.setText("Threat: " + threats[threatIdx]);
                    Color[] threatColors = { UITheme.STATUS_AVAILABLE, UITheme.STATUS_DISPATCHED,
                            new Color(249, 115, 22), UITheme.STATUS_CRITICAL };
                    aiThreatLabel.setForeground(threatColors[threatIdx]);
                    aiConfidenceLabel.setText((85 + random.nextInt(15)) + "%");
                }

                aiDetectionPanel.repaint();
            }
        });
        // Note: aiAnimationTimer is started when the toggle button is clicked

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        panel.setBackground(UITheme.DARK_BG);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        panel.add(createTableCard("Active Accidents", createAccidentsTable(), UITheme.STATUS_CRITICAL));
        panel.add(createTableCard("Ambulance Fleet", createAmbulancesTable(), UITheme.STATUS_ENROUTE));
        panel.add(createTableCard("Hospitals", createHospitalsTable(), UITheme.STATUS_AVAILABLE));

        return panel;
    }

    private JPanel createTableCard(String title, JScrollPane table, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(UITheme.CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                // Top accent line
                g2d.setColor(accentColor);
                g2d.fillRoundRect(20, 0, getWidth() - 40, 3, 3, 3);

                // Border
                g2d.setColor(new Color(226, 232, 240));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(table, BorderLayout.CENTER);

        return card;
    }

    private JScrollPane createAccidentsTable() {
        String[] columns = { "ID", "Location", "Severity", "Time" };
        accidentModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        accidentTable = UITheme.createStyledTable(accidentModel);
        refreshAccidents();

        JScrollPane scroll = new JScrollPane(accidentTable);
        UITheme.styleScrollPane(scroll);
        return scroll;
    }

    private JScrollPane createAmbulancesTable() {
        String[] columns = { "ID", "Position", "Status" };
        ambulanceModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ambulanceTable = UITheme.createStyledTable(ambulanceModel);
        refreshAmbulances();

        JScrollPane scroll = new JScrollPane(ambulanceTable);
        UITheme.styleScrollPane(scroll);
        return scroll;
    }

    private JScrollPane createHospitalsTable() {
        String[] columns = { "ID", "Name", "Beds", "Max Severity" };
        hospitalModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        hospitalTable = UITheme.createStyledTable(hospitalModel);
        refreshHospitals();

        JScrollPane scroll = new JScrollPane(hospitalTable);
        UITheme.styleScrollPane(scroll);
        return scroll;
    }

    // Using UITheme.createStyledTable() and UITheme.styleScrollPane() for all
    // tables

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(UITheme.CARD_BG);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(UITheme.BORDER);
                g.drawLine(0, 0, getWidth(), 0);
            }
        };
        panel.setBackground(UITheme.CARD_BG);

        // Primary actions row
        JPanel primaryRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        primaryRow.setOpaque(false);

        primaryRow.add(createActionButton("Report Accident", UITheme.STATUS_CRITICAL, e -> showReportAccidentDialog()));
        primaryRow.add(createActionButton("Live Map", UITheme.ACCENT, e -> showGoogleMaps()));
        primaryRow.add(createActionButton("Analytics", UITheme.ACCENT, e -> showAnalytics()));
        primaryRow.add(createActionButton("Hospitals", UITheme.ACCENT_PURPLE, e -> showHospitalManagement()));
        primaryRow.add(createActionButton("Fleet", UITheme.ACCENT_PURPLE, e -> showFleetManagement()));
        primaryRow.add(createActionButton("Drivers", UITheme.ACCENT_PURPLE, e -> showDriverManagement()));
        primaryRow.add(createActionButton("Reports", UITheme.ACCENT_PURPLE, e -> showIncidentReports()));
        primaryRow.add(createActionButton("SQL Lab", UITheme.STATUS_ENROUTE, e -> showSQLOperations()));
        primaryRow.add(createActionButton("Messages", UITheme.ACCENT, e -> showCommunicationPanel()));
        primaryRow.add(createActionButton("Refresh", UITheme.TEXT_SECONDARY, e -> refreshAllData()));
        primaryRow.add(createActionButton("Logout", UITheme.TEXT_SECONDARY, e -> logout()));

        panel.add(primaryRow, BorderLayout.CENTER);

        return panel;
    }

    private void showAnalytics() {
        JFrame frame = new JFrame("Analytics Dashboard");
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(this);
        frame.add(new AnalyticsDashboard());
        frame.setVisible(true);
    }

    private void showHospitalManagement() {
        JFrame frame = new JFrame("Hospital Management");
        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(this);
        frame.add(new HospitalManagementPanel());
        frame.setVisible(true);
    }

    private void showFleetManagement() {
        JFrame frame = new JFrame("Fleet Management");
        frame.setSize(1100, 750);
        frame.setLocationRelativeTo(this);
        frame.add(new FleetManagementPanel());
        frame.setVisible(true);
    }

    private void showDriverManagement() {
        JFrame frame = new JFrame("Driver Management");
        frame.setSize(1050, 650);
        frame.setLocationRelativeTo(this);
        frame.add(new DriverManagementPanel());
        frame.setVisible(true);
    }

    private void showIncidentReports() {
        JFrame frame = new JFrame("Incident Reports");
        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(this);
        frame.add(new IncidentReportPanel());
        frame.setVisible(true);
    }

    private void showSQLOperations() {
        JFrame frame = new JFrame("SQL Operations Lab");
        frame.setSize(1250, 800);
        frame.setLocationRelativeTo(this);
        frame.add(new SQLOperationsPanel());
        frame.setVisible(true);
    }

    private JButton createActionButton(String text, Color color, java.awt.event.ActionListener action) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover()) {
                    g2d.setColor(color);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2d.setColor(Color.WHITE);
                } else {
                    g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 15));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2d.setColor(color);
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                    g2d.setColor(color);
                }

                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(130, 40));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);
        return button;
    }

    private int getSelectedInterval() {
        String selected = (String) intervalCombo.getSelectedItem();
        return Integer.parseInt(selected.replace(" sec", ""));
    }

    private void toggleAutoGeneration() {
        if (autoGenToggle.isSelected()) {
            countdownValue = getSelectedInterval();
            aiScanProgress = 0;
            aiPhase = 0;
            accidentGenTimer.start();
            aiAnimationTimer.start();
            aiDetectionPanel.setVisible(true);
            aiStatusLabel.setText("SCANNING");
            aiStatusLabel.setForeground(UITheme.STATUS_AVAILABLE);
            aiLocationLabel.setText(scanLocations[0]);
            notificationManager.showWarning("AI Detection Active",
                    "Scanning for accidents every " + getSelectedInterval() + " seconds");
        } else {
            accidentGenTimer.stop();
            aiAnimationTimer.stop();
            aiScanProgress = 0;
            aiPhase = 0;
            aiStatusLabel.setText("");
            aiStatusLabel.setForeground(UITheme.TEXT_MUTED);
            aiLocationLabel.setText("");
            aiThreatLabel.setText("");
            aiConfidenceLabel.setText("");
            aiDetectionPanel.setVisible(false);
            aiDetectionPanel.repaint();
            notificationManager.showInfo("AI Stopped", "Detection paused");
        }
        autoGenToggle.repaint();
    }

    private void generateRandomAccident() {
        try {
            String location = LOCATIONS[random.nextInt(LOCATIONS.length)];
            String vehicleId = VEHICLE_PREFIXES[random.nextInt(VEHICLE_PREFIXES.length)] +
                    String.format("%c%c%04d",
                            (char) ('A' + random.nextInt(26)),
                            (char) ('A' + random.nextInt(26)),
                            random.nextInt(10000));
            String severity = SEVERITIES[random.nextInt(SEVERITIES.length)];
            String description = "AI-detected collision at " + location;

            String sql = "INSERT INTO accidents (location, vehicle_id, severity, description, reported_by) " +
                    "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, location);
            pstmt.setString(2, vehicleId);
            pstmt.setString(3, severity);
            pstmt.setString(4, description);
            pstmt.setString(5, "AI System");
            pstmt.executeUpdate();

            String tag = severity.equals("Critical") ? "[CRITICAL]"
                    : severity.equals("High") ? "[HIGH]" : severity.equals("Medium") ? "[MEDIUM]" : "[LOW]";

            notificationManager.showUrgent("NEW ACCIDENT DETECTED",
                    tag + " " + severity + " — " + location);

            refreshAllData();
            updateGoogleMaps();

        } catch (SQLException e) {
            System.err.println("[Main] Error generating accident: " + e.getMessage());
        }
    }

    private void updateGoogleMaps() {
        // Trigger map refresh
        try {
            GoogleMapsPanel mapPanel = new GoogleMapsPanel();
            mapPanel.refreshMap();
        } catch (Exception e) {
            System.err.println("[Main] Error updating map: " + e.getMessage());
        }
    }

    private void refreshAccidents() {
        try {
            accidentModel.setRowCount(0);
            String sql = "SELECT * FROM accidents ORDER BY accident_time DESC LIMIT 20";
            ResultSet rs = dbManager.executeQuery(sql);

            while (rs.next()) {
                String severity = rs.getString("severity");
                String icon = severity.equals("Critical") ? "●"
                        : severity.equals("High") ? "●" : severity.equals("Medium") ? "●" : "●";

                Object[] row = {
                        rs.getInt("accident_id"),
                        rs.getString("location"),
                        icon + " " + severity,
                        rs.getTimestamp("accident_time").toString().substring(11, 19)
                };
                accidentModel.addRow(row);
            }
        } catch (SQLException e) {
            System.err.println("[Main] Error refreshing accidents: " + e.getMessage());
        }
    }

    private void refreshAmbulances() {
        try {
            ambulanceModel.setRowCount(0);
            String sql = "SELECT * FROM ambulances";
            ResultSet rs = dbManager.executeQuery(sql);

            while (rs.next()) {
                String status = rs.getString("status");
                String icon = status.equals("green") ? "Available"
                        : status.equals("yellow") ? "Dispatched" : "At Scene";

                Object[] row = {
                        rs.getInt("ambulance_id"),
                        String.format("(%d, %d)", rs.getInt("current_x"), rs.getInt("current_y")),
                        icon
                };
                ambulanceModel.addRow(row);
            }
        } catch (SQLException e) {
            System.err.println("[Main] Error refreshing ambulances: " + e.getMessage());
        }
    }

    private void refreshHospitals() {
        try {
            hospitalModel.setRowCount(0);
            String sql = "SELECT * FROM hospitals";
            ResultSet rs = dbManager.executeQuery(sql);

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("hospital_id"),
                        rs.getString("name"),
                        rs.getInt("available_beds") + "/" + rs.getInt("capacity"),
                        rs.getString("max_severity")
                };
                hospitalModel.addRow(row);
            }
        } catch (SQLException e) {
            System.err.println("[Main] Error refreshing hospitals: " + e.getMessage());
        }
    }

    private void refreshAllData() {
        refreshAccidents();
        refreshAmbulances();
        refreshHospitals();
        weatherLabel.setText(weatherService.getWeatherDisplay());
        updateStats();
    }

    private void updateStats() {
        try {
            ResultSet rs = dbManager.executeQuery("SELECT COUNT(*) as total FROM accidents");
            rs.next();
            int totalAccidents = rs.getInt("total");

            rs = dbManager.executeQuery("SELECT COUNT(*) as avail FROM ambulances WHERE status = 'green'");
            rs.next();
            int availAmbulances = rs.getInt("avail");

            statsLabel.setText(String.format("%d Accidents  |  %d Units Ready", totalAccidents, availAmbulances));
        } catch (SQLException e) {
            System.err.println("[Main] Error updating stats: " + e.getMessage());
        }
    }

    private void startRefreshTimer() {
        refreshTimer = new Timer(3000, e -> refreshAllData());
        refreshTimer.start();
    }

    private void showReportAccidentDialog() {
        JDialog dialog = new JDialog(this, "Report Accident", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UITheme.CARD_BG);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        ((JPanel) dialog.getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField locationField = new JTextField();
        JTextField vehicleField = new JTextField();
        JComboBox<String> severityCombo = new JComboBox<>(SEVERITIES);
        JTextField descField = new JTextField();

        dialog.add(createDialogLabel("Location:"));
        dialog.add(locationField);
        dialog.add(createDialogLabel("Vehicle ID:"));
        dialog.add(vehicleField);
        dialog.add(createDialogLabel("Severity:"));
        dialog.add(severityCombo);
        dialog.add(createDialogLabel("Description:"));
        dialog.add(descField);

        JButton submitBtn = new JButton("Report");
        submitBtn.setBackground(UITheme.STATUS_CRITICAL);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.addActionListener(e -> {
            try {
                String sql = "INSERT INTO accidents (location, vehicle_id, severity, description, reported_by) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
                pstmt.setString(1, locationField.getText());
                pstmt.setString(2, vehicleField.getText());
                pstmt.setString(3, (String) severityCombo.getSelectedItem());
                pstmt.setString(4, descField.getText());
                pstmt.setString(5, "User #" + currentUserId);
                pstmt.executeUpdate();

                notificationManager.showUrgent("Accident Reported", "New accident at " + locationField.getText());
                dialog.dispose();
                refreshAccidents();
                updateGoogleMaps();
            } catch (SQLException ex) {
                System.err.println("[Main] Error: " + ex.getMessage());
            }
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(submitBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private JLabel createDialogLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(UITheme.TEXT_PRIMARY);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
    }

    private void showGoogleMaps() {
        JFrame mapFrame = new JFrame("Google Maps - Live View");
        mapFrame.setSize(1000, 750);
        mapFrame.setLocationRelativeTo(this);
        mapFrame.add(new GoogleMapsPanel());
        mapFrame.setVisible(true);
    }

    private void showCommunicationPanel() {
        JFrame commFrame = new JFrame("Communication Center");
        commFrame.setSize(700, 500);
        commFrame.setLocationRelativeTo(this);
        CommunicationPanel panel = new CommunicationPanel();
        panel.setCurrentUserId(currentUserId);
        commFrame.add(panel);
        commFrame.setVisible(true);
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            if (accidentGenTimer != null)
                accidentGenTimer.stop();
            if (refreshTimer != null)
                refreshTimer.stop();
            ambulanceSync.stop();
            dispose();

            SwingUtilities.invokeLater(() -> {
                LoginDialog login = LoginDialog.showLogin(null);
                if (login.isAuthenticated()) {
                    new Main(login.getUserId(), login.getUserRole()).setVisible(true);
                } else {
                    System.exit(0);
                }
            });
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDialog = LoginDialog.showLogin(null);
            if (loginDialog.isAuthenticated()) {
                new Main(loginDialog.getUserId(), loginDialog.getUserRole()).setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}
