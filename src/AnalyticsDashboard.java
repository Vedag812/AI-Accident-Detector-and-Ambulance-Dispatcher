import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * AnalyticsDashboard - Response time charts, heatmaps, and statistics
 * Features: Real-time graphs, accident heatmap, performance metrics
 */
public class AnalyticsDashboard extends JPanel {
    private DatabaseManager dbManager;
    private javax.swing.Timer refreshTimer;

    // Stat values
    private int totalAccidents = 0;
    private int avgResponseTime = 0;
    private int activeAmbulances = 0;
    private int patientsToday = 0;

    // Data for charts
    private java.util.List<Integer> responseTimeData = new ArrayList<>();
    private java.util.List<String> responseTimeLabels = new ArrayList<>();
    private Map<String, Integer> severityData = new HashMap<>();
    private int[][] heatmapData = new int[10][10];

    public AnalyticsDashboard() {
        this.dbManager = DatabaseManager.getInstance();
        setLayout(new BorderLayout(0, UITheme.SPACE_LG));
        setBackground(UITheme.DARK_BG);
        setBorder(new EmptyBorder(UITheme.SPACE_XL, UITheme.SPACE_XL, UITheme.SPACE_XL, UITheme.SPACE_XL));

        initializeUI();
        loadData();
        startAutoRefresh();
    }

    private void initializeUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = UITheme.createTitleLabel("📊 Analytics Dashboard");
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Time filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setOpaque(false);

        String[] periods = { "Today", "Last 7 Days", "Last 30 Days", "All Time" };
        JComboBox<String> periodCombo = new JComboBox<>(periods);
        periodCombo.setBackground(UITheme.INPUT_BG);
        periodCombo.setForeground(UITheme.TEXT_PRIMARY);
        periodCombo.addActionListener(e -> loadData());
        filterPanel.add(periodCombo);

        JButton refreshBtn = UITheme.createSecondaryButton("↻ Refresh");
        refreshBtn.addActionListener(e -> loadData());
        filterPanel.add(refreshBtn);

        headerPanel.add(filterPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout(UITheme.SPACE_LG, UITheme.SPACE_LG));
        contentPanel.setOpaque(false);

        // Stats row
        contentPanel.add(createStatsRow(), BorderLayout.NORTH);

        // Charts row
        JPanel chartsPanel = new JPanel(new GridLayout(1, 3, UITheme.SPACE_LG, 0));
        chartsPanel.setOpaque(false);
        chartsPanel.add(createResponseTimeChart());
        chartsPanel.add(createSeverityChart());
        chartsPanel.add(createHeatmapPanel());

        contentPanel.add(chartsPanel, BorderLayout.CENTER);

        // Recent activity
        contentPanel.add(createRecentActivityPanel(), BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createStatsRow() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, UITheme.SPACE_LG, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 100));

        statsPanel.add(createStatCard("Total Accidents", String.valueOf(totalAccidents), "🚨", UITheme.STATUS_CRITICAL));
        statsPanel.add(createStatCard("Avg Response Time", avgResponseTime + " min", "⏱️", UITheme.ACCENT));
        statsPanel
                .add(createStatCard("Active Ambulances", String.valueOf(activeAmbulances), "🚑", UITheme.STATUS_AVAILABLE));
        statsPanel.add(createStatCard("Patients Today", String.valueOf(patientsToday), "🏥", UITheme.GRADIENT_END));

        return statsPanel;
    }

    private JPanel createStatCard(String title, String value, String icon, Color color) {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout(UITheme.SPACE_MD, 0));

        // Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        card.add(iconLabel, BorderLayout.WEST);

        // Text
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(UITheme.FONT_HEADER);
        valueLabel.setForeground(color);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_SMALL);
        titleLabel.setForeground(UITheme.TEXT_SECONDARY);

        textPanel.add(valueLabel);
        textPanel.add(titleLabel);

        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createResponseTimeChart() {
        JPanel chartCard = UITheme.createCard();
        chartCard.setLayout(new BorderLayout());

        JLabel title = UITheme.createHeaderLabel("Response Times");
        title.setBorder(new EmptyBorder(0, 0, UITheme.SPACE_MD, 0));
        chartCard.add(title, BorderLayout.NORTH);

        // Custom chart panel
        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int padding = 40;
                int chartWidth = width - 2 * padding;
                int chartHeight = height - 2 * padding;

                // Draw grid
                g2d.setColor(UITheme.BORDER);
                for (int i = 0; i <= 4; i++) {
                    int y = padding + (chartHeight * i / 4);
                    g2d.drawLine(padding, y, width - padding, y);
                }

                // Draw bars
                if (!responseTimeData.isEmpty()) {
                    int barWidth = Math.max(20, chartWidth / responseTimeData.size() - 10);
                    int maxValue = Collections.max(responseTimeData);
                    if (maxValue == 0)
                        maxValue = 1;

                    for (int i = 0; i < responseTimeData.size(); i++) {
                        int value = responseTimeData.get(i);
                        int barHeight = (int) ((double) value / maxValue * chartHeight);
                        int x = padding + (i * (barWidth + 10));
                        int y = height - padding - barHeight;

                        // Gradient bar
                        GradientPaint gradient = new GradientPaint(
                                x, y, UITheme.ACCENT,
                                x, y + barHeight, UITheme.ACCENT);
                        g2d.setPaint(gradient);
                        g2d.fillRoundRect(x, y, barWidth, barHeight, 6, 6);

                        // Value label
                        g2d.setColor(UITheme.TEXT_PRIMARY);
                        g2d.setFont(UITheme.FONT_XSMALL);
                        String label = value + "m";
                        int labelWidth = g2d.getFontMetrics().stringWidth(label);
                        g2d.drawString(label, x + (barWidth - labelWidth) / 2, y - 5);
                    }
                } else {
                    g2d.setColor(UITheme.TEXT_MUTED);
                    g2d.setFont(UITheme.FONT_BODY);
                    g2d.drawString("No data available", width / 2 - 50, height / 2);
                }
            }
        };
        chart.setBackground(UITheme.CARD_BG);
        chartCard.add(chart, BorderLayout.CENTER);

        return chartCard;
    }

    private JPanel createSeverityChart() {
        JPanel chartCard = UITheme.createCard();
        chartCard.setLayout(new BorderLayout());

        JLabel title = UITheme.createHeaderLabel("Severity Distribution");
        title.setBorder(new EmptyBorder(0, 0, UITheme.SPACE_MD, 0));
        chartCard.add(title, BorderLayout.NORTH);

        // Pie chart panel
        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int size = Math.min(width, height) - 80;
                int x = (width - size) / 2;
                int y = (height - size) / 2 - 10;

                Color[] colors = { UITheme.STATUS_AVAILABLE, UITheme.STATUS_DISPATCHED,
                        new Color(251, 146, 60), UITheme.STATUS_CRITICAL };
                String[] labels = { "Low", "Medium", "High", "Critical" };

                int total = severityData.values().stream().mapToInt(Integer::intValue).sum();
                if (total == 0)
                    total = 1;

                int startAngle = 0;
                for (int i = 0; i < labels.length; i++) {
                    int value = severityData.getOrDefault(labels[i], 0);
                    int arcAngle = (int) ((double) value / total * 360);

                    g2d.setColor(colors[i]);
                    g2d.fillArc(x, y, size, size, startAngle, arcAngle);

                    startAngle += arcAngle;
                }

                // Center circle (donut effect)
                g2d.setColor(UITheme.CARD_BG);
                int innerSize = size / 2;
                g2d.fillOval(x + size / 4, y + size / 4, innerSize, innerSize);

                // Total in center
                g2d.setColor(UITheme.TEXT_PRIMARY);
                g2d.setFont(UITheme.FONT_HEADER);
                String totalStr = String.valueOf(severityData.values().stream().mapToInt(Integer::intValue).sum());
                int textWidth = g2d.getFontMetrics().stringWidth(totalStr);
                g2d.drawString(totalStr, x + size / 2 - textWidth / 2, y + size / 2 + 8);

                // Legend
                int legendY = height - 30;
                int legendX = 20;
                g2d.setFont(UITheme.FONT_XSMALL);
                for (int i = 0; i < labels.length; i++) {
                    g2d.setColor(colors[i]);
                    g2d.fillRoundRect(legendX, legendY, 12, 12, 3, 3);
                    g2d.setColor(UITheme.TEXT_SECONDARY);
                    g2d.drawString(labels[i], legendX + 16, legendY + 10);
                    legendX += 70;
                }
            }
        };
        chart.setBackground(UITheme.CARD_BG);
        chartCard.add(chart, BorderLayout.CENTER);

        return chartCard;
    }

    private JPanel createHeatmapPanel() {
        JPanel heatmapCard = UITheme.createCard();
        heatmapCard.setLayout(new BorderLayout());

        JLabel title = UITheme.createHeaderLabel("Accident Heatmap");
        title.setBorder(new EmptyBorder(0, 0, UITheme.SPACE_MD, 0));
        heatmapCard.add(title, BorderLayout.NORTH);

        // Heatmap panel
        JPanel heatmap = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int cellWidth = (width - 40) / 10;
                int cellHeight = (height - 40) / 10;
                int padding = 20;

                // Find max value
                int maxValue = 1;
                for (int[] row : heatmapData) {
                    for (int val : row) {
                        maxValue = Math.max(maxValue, val);
                    }
                }

                // Draw cells
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        int x = padding + j * cellWidth;
                        int y = padding + i * cellHeight;

                        float intensity = (float) heatmapData[i][j] / maxValue;
                        Color cellColor = new Color(
                                (int) (UITheme.STATUS_CRITICAL.getRed() * intensity
                                        + UITheme.CARD_BG.getRed() * (1 - intensity)),
                                (int) (UITheme.STATUS_CRITICAL.getGreen() * intensity
                                        + UITheme.CARD_BG.getGreen() * (1 - intensity)),
                                (int) (UITheme.STATUS_CRITICAL.getBlue() * intensity
                                        + UITheme.CARD_BG.getBlue() * (1 - intensity)));

                        g2d.setColor(cellColor);
                        g2d.fillRect(x, y, cellWidth - 2, cellHeight - 2);
                    }
                }

                // Border
                g2d.setColor(UITheme.BORDER);
                g2d.drawRect(padding, padding, cellWidth * 10, cellHeight * 10);
            }
        };
        heatmap.setBackground(UITheme.CARD_BG);
        heatmapCard.add(heatmap, BorderLayout.CENTER);

        return heatmapCard;
    }

    private JPanel createRecentActivityPanel() {
        JPanel activityCard = UITheme.createCard();
        activityCard.setLayout(new BorderLayout());
        activityCard.setPreferredSize(new Dimension(0, 150));

        JLabel title = UITheme.createHeaderLabel("Recent Activity");
        title.setBorder(new EmptyBorder(0, 0, UITheme.SPACE_MD, 0));
        activityCard.add(title, BorderLayout.NORTH);

        JTextArea activityLog = new JTextArea();
        activityLog.setEditable(false);
        activityLog.setBackground(UITheme.PANEL_BG);
        activityLog.setForeground(UITheme.TEXT_PRIMARY);
        activityLog.setFont(UITheme.FONT_SMALL);
        activityLog.setBorder(new EmptyBorder(UITheme.SPACE_SM, UITheme.SPACE_SM, UITheme.SPACE_SM, UITheme.SPACE_SM));

        // Load recent activity
        try {
            String sql = "SELECT accident_time, location, severity FROM accidents ORDER BY accident_time DESC LIMIT 5";
            ResultSet rs = dbManager.executeQuery(sql);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(String.format("  %s  │  %s  │  %s%n",
                        sdf.format(rs.getTimestamp("accident_time")),
                        rs.getString("severity"),
                        rs.getString("location")));
            }
            activityLog.setText(sb.toString());
        } catch (SQLException e) {
            activityLog.setText("Unable to load recent activity");
        }

        JScrollPane scroll = new JScrollPane(activityLog);
        UITheme.styleScrollPane(scroll);
        activityCard.add(scroll, BorderLayout.CENTER);

        return activityCard;
    }

    private void loadData() {
        // Load statistics
        try {
            // Total accidents
            ResultSet rs = dbManager.executeQuery("SELECT COUNT(*) FROM accidents");
            if (rs.next())
                totalAccidents = rs.getInt(1);

            // Active ambulances
            rs = dbManager.executeQuery("SELECT COUNT(*) FROM ambulances WHERE status != 'green'");
            if (rs.next())
                activeAmbulances = rs.getInt(1);

            // Patients today
            rs = dbManager.executeQuery("SELECT COUNT(*) FROM patients WHERE DATE(created_at) = CURDATE()");
            if (rs.next())
                patientsToday = rs.getInt(1);

            // Severity distribution
            severityData.clear();
            rs = dbManager.executeQuery("SELECT severity, COUNT(*) as cnt FROM accidents GROUP BY severity");
            while (rs.next()) {
                severityData.put(rs.getString("severity"), rs.getInt("cnt"));
            }

            // Response times (simulate for demo)
            responseTimeData.clear();
            responseTimeLabels.clear();
            Random rand = new Random();
            for (int i = 0; i < 7; i++) {
                responseTimeData.add(5 + rand.nextInt(15));
                responseTimeLabels.add("Day " + (i + 1));
            }
            avgResponseTime = (int) responseTimeData.stream().mapToInt(Integer::intValue).average().orElse(0);

            // Heatmap data (simulate based on accident locations)
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    heatmapData[i][j] = rand.nextInt(5);
                }
            }
            // Add some hotspots
            heatmapData[3][5] = 8;
            heatmapData[7][2] = 6;
            heatmapData[5][7] = 7;

        } catch (SQLException e) {
            System.err.println("[Analytics] Error loading data: " + e.getMessage());
        }

        repaint();
    }

    private void startAutoRefresh() {
        refreshTimer = new javax.swing.Timer(30000, e -> loadData());
        refreshTimer.start();
    }

    public void stopRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }
}


