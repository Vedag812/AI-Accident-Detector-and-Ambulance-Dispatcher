import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * IncidentReportPanel - Create and view incident reports
 * Features: Report creation, outcome tracking, history view
 */
public class IncidentReportPanel extends JPanel {
    private DatabaseManager dbManager;
    private JTable reportsTable;
    private DefaultTableModel tableModel;
    private javax.swing.Timer refreshTimer;

    // Form fields
    private JComboBox<String> accidentCombo;
    private JComboBox<String> ambulanceCombo;
    private JComboBox<String> outcomeCombo;
    private JComboBox<String> trafficCombo;
    private JTextArea notesArea;
    private JTextArea complicationsArea;

    public IncidentReportPanel() {
        this.dbManager = DatabaseManager.getInstance();
        setLayout(new BorderLayout(UITheme.SPACE_LG, UITheme.SPACE_LG));
        setBackground(UITheme.DARK_BG);
        setBorder(new EmptyBorder(UITheme.SPACE_XL, UITheme.SPACE_XL, UITheme.SPACE_XL, UITheme.SPACE_XL));

        initializeUI();
        loadReports();
        startAutoRefresh();
    }

    private void initializeUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = UITheme.createTitleLabel("📋 Incident Reports");
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton newReportBtn = UITheme.createPrimaryButton("+ New Report");
        newReportBtn.addActionListener(e -> showNewReportDialog());
        headerPanel.add(newReportBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Main content - split between form and table
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setBackground(UITheme.DARK_BG);
        splitPane.setBorder(null);

        // Quick report form
        splitPane.setLeftComponent(createQuickReportPanel());

        // Reports table
        splitPane.setRightComponent(createReportsTablePanel());

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createQuickReportPanel() {
        JPanel formCard = UITheme.createCard();
        formCard.setLayout(new BorderLayout());

        JLabel formTitle = UITheme.createHeaderLabel("Quick Report");
        formTitle.setBorder(new EmptyBorder(0, 0, UITheme.SPACE_LG, 0));
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        // Accident selection
        formPanel.add(createFormRow("Accident:", accidentCombo = new JComboBox<>()));
        loadAccidents();

        // Ambulance selection
        formPanel.add(Box.createVerticalStrut(UITheme.SPACE_MD));
        formPanel.add(createFormRow("Ambulance:", ambulanceCombo = new JComboBox<>()));
        loadAmbulances();

        // Outcome
        formPanel.add(Box.createVerticalStrut(UITheme.SPACE_MD));
        outcomeCombo = new JComboBox<>(new String[] {
                "Patient Stabilized", "Patient Admitted", "Patient Deceased", "False Alarm", "Other"
        });
        formPanel.add(createFormRow("Outcome:", outcomeCombo));

        // Traffic conditions
        formPanel.add(Box.createVerticalStrut(UITheme.SPACE_MD));
        trafficCombo = new JComboBox<>(new String[] { "Light", "Moderate", "Heavy" });
        formPanel.add(createFormRow("Traffic:", trafficCombo));

        // Complications
        formPanel.add(Box.createVerticalStrut(UITheme.SPACE_MD));
        JLabel compLabel = UITheme.createLabel("Complications:");
        compLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(compLabel);

        complicationsArea = new JTextArea(3, 20);
        complicationsArea.setBackground(UITheme.INPUT_BG);
        complicationsArea.setForeground(UITheme.TEXT_PRIMARY);
        complicationsArea.setCaretColor(UITheme.ACCENT);
        complicationsArea
                .setBorder(new EmptyBorder(UITheme.SPACE_SM, UITheme.SPACE_SM, UITheme.SPACE_SM, UITheme.SPACE_SM));
        complicationsArea.setLineWrap(true);
        JScrollPane compScroll = new JScrollPane(complicationsArea);
        compScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(compScroll);

        // Notes
        formPanel.add(Box.createVerticalStrut(UITheme.SPACE_MD));
        JLabel notesLabel = UITheme.createLabel("Notes:");
        notesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(notesLabel);

        notesArea = new JTextArea(4, 20);
        notesArea.setBackground(UITheme.INPUT_BG);
        notesArea.setForeground(UITheme.TEXT_PRIMARY);
        notesArea.setCaretColor(UITheme.ACCENT);
        notesArea.setBorder(new EmptyBorder(UITheme.SPACE_SM, UITheme.SPACE_SM, UITheme.SPACE_SM, UITheme.SPACE_SM));
        notesArea.setLineWrap(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(notesScroll);

        formCard.add(formPanel, BorderLayout.CENTER);

        // Submit button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton clearBtn = UITheme.createSecondaryButton("Clear");
        clearBtn.addActionListener(e -> clearForm());
        buttonPanel.add(clearBtn);

        JButton submitBtn = UITheme.createSuccessButton("Submit Report");
        submitBtn.addActionListener(e -> submitReport());
        buttonPanel.add(submitBtn);

        formCard.add(buttonPanel, BorderLayout.SOUTH);

        return formCard;
    }

    private JPanel createFormRow(String label, JComboBox<?> combo) {
        JPanel row = new JPanel(new BorderLayout(UITheme.SPACE_MD, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = UITheme.createLabel(label);
        lbl.setPreferredSize(new Dimension(80, 25));
        row.add(lbl, BorderLayout.WEST);

        combo.setBackground(UITheme.INPUT_BG);
        combo.setForeground(UITheme.TEXT_PRIMARY);
        row.add(combo, BorderLayout.CENTER);

        return row;
    }

    private JPanel createReportsTablePanel() {
        JPanel tableCard = UITheme.createCard();
        tableCard.setLayout(new BorderLayout());

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        JLabel tableTitle = UITheme.createHeaderLabel("Report History");
        tableHeader.add(tableTitle, BorderLayout.WEST);

        JButton refreshBtn = UITheme.createGhostButton("↻ Refresh");
        refreshBtn.addActionListener(e -> loadReports());
        tableHeader.add(refreshBtn, BorderLayout.EAST);

        tableCard.add(tableHeader, BorderLayout.NORTH);

        // Table
        String[] columns = { "ID", "Accident", "Ambulance", "Response Time", "Outcome", "Created" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportsTable = UITheme.createStyledTable(tableModel);
        reportsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        reportsTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        // Double-click to view details
        reportsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewReportDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(reportsTable);
        UITheme.styleScrollPane(scrollPane);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Action buttons
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsPanel.setOpaque(false);

        JButton viewBtn = UITheme.createPrimaryButton("View Details");
        viewBtn.addActionListener(e -> viewReportDetails());
        actionsPanel.add(viewBtn);

        JButton exportBtn = UITheme.createSecondaryButton("📄 Export Report");
        exportBtn.addActionListener(e -> exportReport());
        actionsPanel.add(exportBtn);

        JButton exportAllBtn = UITheme.createSecondaryButton("📁 Export All");
        exportAllBtn.addActionListener(e -> exportAllReports());
        actionsPanel.add(exportAllBtn);

        JButton deleteBtn = UITheme.createDangerButton("Delete");
        deleteBtn.addActionListener(e -> deleteSelectedReport());
        actionsPanel.add(deleteBtn);

        tableCard.add(actionsPanel, BorderLayout.SOUTH);

        return tableCard;
    }

    private void loadAccidents() {
        accidentCombo.removeAllItems();
        try {
            ResultSet rs = dbManager.executeQuery(
                    "SELECT accident_id, location FROM accidents ORDER BY accident_time DESC LIMIT 20");
            while (rs.next()) {
                accidentCombo.addItem(rs.getInt("accident_id") + " - " + rs.getString("location"));
            }
        } catch (SQLException e) {
            System.err.println("[IncidentReport] Error loading accidents: " + e.getMessage());
        }
    }

    private void loadAmbulances() {
        ambulanceCombo.removeAllItems();
        try {
            ResultSet rs = dbManager.executeQuery("SELECT ambulance_id, vehicle_number FROM ambulances");
            while (rs.next()) {
                String vehicleNum = rs.getString("vehicle_number");
                ambulanceCombo
                        .addItem(rs.getInt("ambulance_id") + " - " + (vehicleNum != null ? vehicleNum : "Ambulance"));
            }
        } catch (SQLException e) {
            System.err.println("[IncidentReport] Error loading ambulances: " + e.getMessage());
        }
    }

    private void loadReports() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try {
            String sql = "SELECT ir.*, a.location FROM incident_reports ir " +
                    "LEFT JOIN accidents a ON ir.accident_id = a.accident_id " +
                    "ORDER BY ir.created_at DESC";
            ResultSet rs = dbManager.executeQuery(sql);

            while (rs.next()) {
                tableModel.addRow(new Object[] {
                        rs.getInt("report_id"),
                        rs.getString("location"),
                        "Amb #" + rs.getInt("ambulance_id"),
                        rs.getInt("response_time_minutes") + " min",
                        rs.getString("outcome"),
                        sdf.format(rs.getTimestamp("created_at"))
                });
            }
        } catch (SQLException e) {
            System.err.println("[IncidentReport] Error loading reports: " + e.getMessage());
        }
    }

    private void submitReport() {
        if (accidentCombo.getSelectedItem() == null || ambulanceCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select an accident and ambulance", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int accidentId = Integer.parseInt(accidentCombo.getSelectedItem().toString().split(" - ")[0]);
            int ambulanceId = Integer.parseInt(ambulanceCombo.getSelectedItem().toString().split(" - ")[0]);
            String outcome = (String) outcomeCombo.getSelectedItem();
            String traffic = (String) trafficCombo.getSelectedItem();
            String complications = complicationsArea.getText().replace("'", "''");
            String notes = notesArea.getText().replace("'", "''");

            // Calculate a simulated response time
            int responseTime = 5 + new Random().nextInt(20);

            String sql = String.format(
                    "INSERT INTO incident_reports (accident_id, ambulance_id, response_time_minutes, " +
                            "outcome, traffic_conditions, complications, notes, dispatch_time, arrival_time) VALUES " +
                            "(%d, %d, %d, '%s', '%s', '%s', '%s', NOW(), NOW())",
                    accidentId, ambulanceId, responseTime, outcome, traffic, complications, notes);

            dbManager.executeUpdate(sql);

            JOptionPane.showMessageDialog(this, "Report submitted successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadReports();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error submitting report: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        if (accidentCombo.getItemCount() > 0)
            accidentCombo.setSelectedIndex(0);
        if (ambulanceCombo.getItemCount() > 0)
            ambulanceCombo.setSelectedIndex(0);
        outcomeCombo.setSelectedIndex(0);
        trafficCombo.setSelectedIndex(0);
        complicationsArea.setText("");
        notesArea.setText("");
    }

    private void viewReportDetails() {
        int selectedRow = reportsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a report to view", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reportId = (int) tableModel.getValueAt(selectedRow, 0);

        try {
            String sql = "SELECT ir.*, a.location, a.severity FROM incident_reports ir " +
                    "LEFT JOIN accidents a ON ir.accident_id = a.accident_id " +
                    "WHERE ir.report_id = " + reportId;
            ResultSet rs = dbManager.executeQuery(sql);

            if (rs.next()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                StringBuilder details = new StringBuilder();
                details.append("═══════════════════════════════════\n");
                details.append("         INCIDENT REPORT #").append(reportId).append("\n");
                details.append("═══════════════════════════════════\n\n");
                details.append("Location: ").append(rs.getString("location")).append("\n");
                details.append("Severity: ").append(rs.getString("severity")).append("\n");
                details.append("Ambulance ID: ").append(rs.getInt("ambulance_id")).append("\n");
                details.append("Response Time: ").append(rs.getInt("response_time_minutes")).append(" minutes\n");
                details.append("Outcome: ").append(rs.getString("outcome")).append("\n");
                details.append("Traffic: ").append(rs.getString("traffic_conditions")).append("\n\n");

                String complications = rs.getString("complications");
                if (complications != null && !complications.isEmpty()) {
                    details.append("Complications:\n").append(complications).append("\n\n");
                }

                String notes = rs.getString("notes");
                if (notes != null && !notes.isEmpty()) {
                    details.append("Notes:\n").append(notes).append("\n\n");
                }

                details.append("Created: ").append(sdf.format(rs.getTimestamp("created_at"))).append("\n");

                JTextArea detailsArea = new JTextArea(details.toString());
                detailsArea.setEditable(false);
                detailsArea.setFont(UITheme.FONT_BODY);
                detailsArea.setBackground(UITheme.CARD_BG);
                detailsArea.setForeground(UITheme.TEXT_PRIMARY);

                JScrollPane scroll = new JScrollPane(detailsArea);
                scroll.setPreferredSize(new Dimension(400, 400));

                JOptionPane.showMessageDialog(this, scroll, "Report Details", JOptionPane.PLAIN_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading details: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedReport() {
        int selectedRow = reportsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a report to delete", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reportId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Report #" + reportId + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dbManager.executeUpdate("DELETE FROM incident_reports WHERE report_id = " + reportId);
                loadReports();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting report: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showNewReportDialog() {
        loadAccidents();
        loadAmbulances();
    }

    private void startAutoRefresh() {
        refreshTimer = new javax.swing.Timer(30000, e -> loadReports());
        refreshTimer.start();
    }

    public void stopRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

    /**
     * Export a single selected report to a text/HTML file
     */
    private void exportReport() {
        int selectedRow = reportsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a report to export", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reportId = (int) tableModel.getValueAt(selectedRow, 0);

        try {
            String sql = "SELECT ir.*, a.location, a.severity FROM incident_reports ir " +
                    "LEFT JOIN accidents a ON ir.accident_id = a.accident_id " +
                    "WHERE ir.report_id = " + reportId;
            ResultSet rs = dbManager.executeQuery(sql);

            if (rs.next()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat fileSdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

                // Build HTML content for better formatting
                StringBuilder html = new StringBuilder();
                html.append("<!DOCTYPE html>\n<html>\n<head>\n");
                html.append("<title>Incident Report #").append(reportId).append("</title>\n");
                html.append("<style>\n");
                html.append(
                        "body{font-family:Arial,sans-serif;max-width:800px;margin:40px auto;padding:20px;background:#f5f5f5;}\n");
                html.append(
                        ".report{background:white;padding:30px;border-radius:10px;box-shadow:0 2px 10px rgba(0,0,0,0.1);}\n");
                html.append("h1{color:#1e40af;border-bottom:2px solid #1e40af;padding-bottom:10px;}\n");
                html.append(".field{margin:15px 0;}\n");
                html.append(".label{font-weight:bold;color:#374151;}\n");
                html.append(".value{color:#111827;margin-left:10px;}\n");
                html.append(".section{background:#f8fafc;padding:15px;border-radius:5px;margin:15px 0;}\n");
                html.append(".severity-critical{color:#dc2626;font-weight:bold;}\n");
                html.append(".severity-high{color:#ea580c;font-weight:bold;}\n");
                html.append(
                        ".footer{margin-top:30px;padding-top:20px;border-top:1px solid #e5e7eb;color:#6b7280;font-size:12px;}\n");
                html.append("</style>\n</head>\n<body>\n");
                html.append("<div class='report'>\n");
                html.append("<h1>🚑 Incident Report #").append(reportId).append("</h1>\n");

                // Basic Info
                html.append("<div class='section'>\n");
                html.append("<h3>📍 Incident Details</h3>\n");
                html.append("<div class='field'><span class='label'>Location:</span><span class='value'>")
                        .append(rs.getString("location")).append("</span></div>\n");

                String severity = rs.getString("severity");
                String severityClass = severity != null && severity.equals("Critical") ? "severity-critical"
                        : severity != null && severity.equals("High") ? "severity-high" : "";
                html.append("<div class='field'><span class='label'>Severity:</span><span class='value ")
                        .append(severityClass).append("'>").append(severity).append("</span></div>\n");
                html.append("<div class='field'><span class='label'>Ambulance ID:</span><span class='value'>")
                        .append(rs.getInt("ambulance_id")).append("</span></div>\n");
                html.append("</div>\n");

                // Response Info
                html.append("<div class='section'>\n");
                html.append("<h3>⏱️ Response Information</h3>\n");
                html.append("<div class='field'><span class='label'>Response Time:</span><span class='value'>")
                        .append(rs.getInt("response_time_minutes")).append(" minutes</span></div>\n");
                html.append("<div class='field'><span class='label'>Traffic Conditions:</span><span class='value'>")
                        .append(rs.getString("traffic_conditions")).append("</span></div>\n");
                html.append("<div class='field'><span class='label'>Outcome:</span><span class='value'>")
                        .append(rs.getString("outcome")).append("</span></div>\n");
                html.append("</div>\n");

                // Complications
                String complications = rs.getString("complications");
                if (complications != null && !complications.isEmpty()) {
                    html.append("<div class='section'>\n");
                    html.append("<h3>⚠️ Complications</h3>\n");
                    html.append("<p>").append(complications).append("</p>\n");
                    html.append("</div>\n");
                }

                // Notes
                String notes = rs.getString("notes");
                if (notes != null && !notes.isEmpty()) {
                    html.append("<div class='section'>\n");
                    html.append("<h3>📝 Notes</h3>\n");
                    html.append("<p>").append(notes).append("</p>\n");
                    html.append("</div>\n");
                }

                html.append("<div class='footer'>\n");
                html.append("<p>Generated: ").append(sdf.format(new java.util.Date())).append("</p>\n");
                html.append("<p>AI Accident Detector & Ambulance Dispatcher System</p>\n");
                html.append("</div>\n");
                html.append("</div>\n</body>\n</html>");

                // Save to file
                String filename = "incident_report_" + reportId + "_" + fileSdf.format(new java.util.Date()) + ".html";
                java.io.File file = new java.io.File(filename);
                java.io.FileWriter writer = new java.io.FileWriter(file);
                writer.write(html.toString());
                writer.close();

                // Open in browser
                java.awt.Desktop.getDesktop().browse(file.toURI());

                JOptionPane.showMessageDialog(this,
                        "Report exported to:\n" + file.getAbsolutePath()
                                + "\n\nOpened in browser for printing (use Ctrl+P to save as PDF)",
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error exporting report: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Export all reports to a single HTML file
     */
    private void exportAllReports() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat fileSdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n<html>\n<head>\n");
            html.append("<title>All Incident Reports</title>\n");
            html.append("<style>\n");
            html.append(
                    "body{font-family:Arial,sans-serif;max-width:1000px;margin:40px auto;padding:20px;background:#f5f5f5;}\n");
            html.append("h1{color:#1e40af;text-align:center;}\n");
            html.append(
                    "table{width:100%;border-collapse:collapse;background:white;box-shadow:0 2px 10px rgba(0,0,0,0.1);}\n");
            html.append("th{background:#1e40af;color:white;padding:12px;text-align:left;}\n");
            html.append("td{padding:10px;border-bottom:1px solid #e5e7eb;}\n");
            html.append("tr:hover{background:#f8fafc;}\n");
            html.append(".footer{text-align:center;margin-top:30px;color:#6b7280;}\n");
            html.append("@media print{body{background:white;}}\n");
            html.append("</style>\n</head>\n<body>\n");
            html.append("<h1>🚑 Incident Reports Summary</h1>\n");
            html.append("<p style='text-align:center;color:#6b7280;'>Generated: ")
                    .append(sdf.format(new java.util.Date())).append("</p>\n");
            html.append("<table>\n<thead>\n<tr>\n");
            html.append(
                    "<th>ID</th><th>Location</th><th>Severity</th><th>Ambulance</th><th>Response Time</th><th>Outcome</th><th>Traffic</th><th>Created</th>\n");
            html.append("</tr>\n</thead>\n<tbody>\n");

            String sql = "SELECT ir.*, a.location, a.severity FROM incident_reports ir " +
                    "LEFT JOIN accidents a ON ir.accident_id = a.accident_id " +
                    "ORDER BY ir.created_at DESC";
            ResultSet rs = dbManager.executeQuery(sql);

            int count = 0;
            while (rs.next()) {
                html.append("<tr>\n");
                html.append("<td>").append(rs.getInt("report_id")).append("</td>\n");
                html.append("<td>").append(rs.getString("location")).append("</td>\n");
                html.append("<td>").append(rs.getString("severity")).append("</td>\n");
                html.append("<td>Amb #").append(rs.getInt("ambulance_id")).append("</td>\n");
                html.append("<td>").append(rs.getInt("response_time_minutes")).append(" min</td>\n");
                html.append("<td>").append(rs.getString("outcome")).append("</td>\n");
                html.append("<td>").append(rs.getString("traffic_conditions")).append("</td>\n");
                html.append("<td>").append(sdf.format(rs.getTimestamp("created_at"))).append("</td>\n");
                html.append("</tr>\n");
                count++;
            }

            html.append("</tbody>\n</table>\n");
            html.append("<div class='footer'>\n");
            html.append("<p>Total Reports: ").append(count).append("</p>\n");
            html.append("<p>AI Accident Detector & Ambulance Dispatcher System</p>\n");
            html.append("</div>\n</body>\n</html>");

            // Save to file
            String filename = "all_incident_reports_" + fileSdf.format(new java.util.Date()) + ".html";
            java.io.File file = new java.io.File(filename);
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(html.toString());
            writer.close();

            // Open in browser
            java.awt.Desktop.getDesktop().browse(file.toURI());

            JOptionPane.showMessageDialog(this,
                    "All " + count + " reports exported to:\n" + file.getAbsolutePath()
                            + "\n\nOpened in browser for printing (use Ctrl+P to save as PDF)",
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error exporting reports: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}


