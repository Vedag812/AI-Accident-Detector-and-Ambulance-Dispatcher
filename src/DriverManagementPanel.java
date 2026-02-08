import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

/**
 * DriverManagementPanel - Manage driver assignments, shifts, and availability
 */
public class DriverManagementPanel extends JPanel {
    private DatabaseManager dbManager;
    private AmbulanceDriverManager driverMgr;
    private JTable driverTable;
    private DefaultTableModel tableModel;
    private javax.swing.Timer refreshTimer;

    public DriverManagementPanel() {
        this.dbManager = DatabaseManager.getInstance();
        this.driverMgr = AmbulanceDriverManager.getInstance();
        setLayout(new BorderLayout(0, UITheme.SPACE_LG));
        setBackground(UITheme.DARK_BG);
        setBorder(new EmptyBorder(UITheme.SPACE_XL, UITheme.SPACE_XL, UITheme.SPACE_XL, UITheme.SPACE_XL));

        initializeUI();
        loadDrivers();
        startAutoRefresh();
    }

    private void initializeUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = UITheme.createTitleLabel("👨‍✈️ Driver Management");
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton addDriverBtn = UITheme.createPrimaryButton("+ Add Driver");
        addDriverBtn.addActionListener(e -> showAddDriverDialog());
        headerPanel.add(addDriverBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Stats and table
        JPanel contentPanel = new JPanel(new BorderLayout(0, UITheme.SPACE_LG));
        contentPanel.setOpaque(false);

        contentPanel.add(createStatsPanel(), BorderLayout.NORTH);
        contentPanel.add(createTablePanel(), BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
        add(createActionPanel(), BorderLayout.SOUTH);
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, UITheme.SPACE_LG, 0));
        statsPanel.setOpaque(false);

        Map<String, Integer> stats = driverMgr.getDriverStats();

        statsPanel.add(createStatCard("Total Drivers", String.valueOf(stats.getOrDefault("total", 0)), UITheme.ACCENT));
        statsPanel.add(
                createStatCard("Available", String.valueOf(stats.getOrDefault("available", 0)), UITheme.STATUS_AVAILABLE));
        statsPanel.add(
                createStatCard("On Duty", String.valueOf(stats.getOrDefault("on_duty", 0)), UITheme.STATUS_DISPATCHED));
        statsPanel
                .add(createStatCard("On Leave", String.valueOf(stats.getOrDefault("on_leave", 0)), UITheme.STATUS_CRITICAL));

        return statsPanel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = UITheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_SECONDARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(UITheme.FONT_DISPLAY);
        valueLbl.setForeground(color);
        valueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(UITheme.SPACE_SM));
        card.add(valueLbl);

        return card;
    }

    private JPanel createTablePanel() {
        JPanel tableCard = UITheme.createCard();
        tableCard.setLayout(new BorderLayout());

        String[] columns = { "ID", "Name", "License", "Phone", "Status", "Ambulance", "Trips", "Rating", "Shift" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        driverTable = UITheme.createStyledTable(tableModel);
        driverTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        driverTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        driverTable.getColumnModel().getColumn(4).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(driverTable);
        UITheme.styleScrollPane(scrollPane);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        return tableCard;
    }

    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.SPACE_MD, 0));
        actionPanel.setOpaque(false);

        JButton assignBtn = UITheme.createPrimaryButton("Assign to Ambulance");
        assignBtn.addActionListener(e -> assignToAmbulance());
        actionPanel.add(assignBtn);

        JButton unassignBtn = UITheme.createSecondaryButton("Unassign");
        unassignBtn.addActionListener(e -> unassignDriver());
        actionPanel.add(unassignBtn);

        JButton statusBtn = UITheme.createWarningButton("Change Status");
        statusBtn.addActionListener(e -> changeDriverStatus());
        actionPanel.add(statusBtn);

        JButton editBtn = UITheme.createGhostButton("Edit Shift");
        editBtn.addActionListener(e -> editShift());
        actionPanel.add(editBtn);

        return actionPanel;
    }

    private void loadDrivers() {
        tableModel.setRowCount(0);

        java.util.List<Map<String, Object>> drivers = driverMgr.getAllDrivers();
        for (Map<String, Object> driver : drivers) {
            String status = (String) driver.get("status");
            String statusDisplay = getStatusEmoji(status) + " " + status;
            String ambulance = driver.get("vehicle_number") != null ? (String) driver.get("vehicle_number")
                    : "Unassigned";

            tableModel.addRow(new Object[] {
                    driver.get("driver_id"),
                    driver.get("name"),
                    driver.get("license_number"),
                    driver.get("phone"),
                    statusDisplay,
                    ambulance,
                    driver.get("total_trips"),
                    driver.get("rating") + "★",
                    driver.get("shift_start") + " - " + driver.get("shift_end")
            });
        }
    }

    private String getStatusEmoji(String status) {
        if (status == null)
            return "⚪";
        switch (status) {
            case "available":
                return "🟢";
            case "on_duty":
                return "🟡";
            case "off_duty":
                return "🔴";
            case "on_leave":
                return "🟣";
            default:
                return "⚪";
        }
    }

    private void showAddDriverDialog() {
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBackground(UITheme.CARD_BG);

        JTextField nameField = new JTextField();
        JTextField licenseField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("License Number:"));
        formPanel.add(licenseField);
        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Add New Driver", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int driverId = driverMgr.addDriver(
                    nameField.getText(), licenseField.getText(),
                    phoneField.getText(), emailField.getText());
            if (driverId > 0) {
                loadDrivers();
                JOptionPane.showMessageDialog(this, "Driver added successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void assignToAmbulance() {
        int selectedRow = driverTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a driver", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int driverId = (int) tableModel.getValueAt(selectedRow, 0);

        // Get available ambulances
        try {
            ResultSet rs = dbManager.executeQuery(
                    "SELECT ambulance_id, vehicle_number FROM ambulances WHERE driver_id IS NULL");

            java.util.List<String> ambulances = new ArrayList<>();
            while (rs.next()) {
                ambulances.add(rs.getInt("ambulance_id") + " - " + rs.getString("vehicle_number"));
            }

            if (ambulances.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No available ambulances", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String selected = (String) JOptionPane.showInputDialog(
                    this, "Select Ambulance:", "Assign Driver",
                    JOptionPane.QUESTION_MESSAGE, null,
                    ambulances.toArray(), ambulances.get(0));

            if (selected != null) {
                int ambulanceId = Integer.parseInt(selected.split(" - ")[0]);
                driverMgr.assignToAmbulance(driverId, ambulanceId);
                loadDrivers();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void unassignDriver() {
        int selectedRow = driverTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a driver", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int driverId = (int) tableModel.getValueAt(selectedRow, 0);
        driverMgr.unassignFromAmbulance(driverId);
        loadDrivers();
    }

    private void changeDriverStatus() {
        int selectedRow = driverTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a driver", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int driverId = (int) tableModel.getValueAt(selectedRow, 0);
        String[] statuses = { "available", "on_duty", "off_duty", "on_leave" };

        String selected = (String) JOptionPane.showInputDialog(
                this, "Select Status:", "Change Status",
                JOptionPane.QUESTION_MESSAGE, null, statuses, statuses[0]);

        if (selected != null) {
            driverMgr.updateStatus(driverId, selected);
            loadDrivers();
        }
    }

    private void editShift() {
        int selectedRow = driverTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a driver", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int driverId = (int) tableModel.getValueAt(selectedRow, 0);

        JPanel shiftPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField startField = new JTextField("08:00:00");
        JTextField endField = new JTextField("20:00:00");

        shiftPanel.add(new JLabel("Shift Start (HH:MM:SS):"));
        shiftPanel.add(startField);
        shiftPanel.add(new JLabel("Shift End (HH:MM:SS):"));
        shiftPanel.add(endField);

        int result = JOptionPane.showConfirmDialog(this, shiftPanel, "Edit Shift", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            driverMgr.updateShift(driverId, startField.getText(), endField.getText());
            loadDrivers();
        }
    }

    private void startAutoRefresh() {
        refreshTimer = new javax.swing.Timer(15000, e -> loadDrivers());
        refreshTimer.start();
    }

    public void stopRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }
}


