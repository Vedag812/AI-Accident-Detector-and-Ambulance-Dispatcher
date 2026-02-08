import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

/**
 * FleetManagementPanel - Manage ambulance fleet, fuel, and maintenance
 */
public class FleetManagementPanel extends JPanel {
    private DatabaseManager dbManager;
    private VehicleManager vehicleMgr;
    private JTable fleetTable;
    private DefaultTableModel tableModel;
    private JTable maintenanceTable;
    private DefaultTableModel maintenanceModel;
    private javax.swing.Timer refreshTimer;

    public FleetManagementPanel() {
        this.dbManager = DatabaseManager.getInstance();
        this.vehicleMgr = VehicleManager.getInstance();
        setLayout(new BorderLayout(0, UITheme.SPACE_LG));
        setBackground(UITheme.DARK_BG);
        setBorder(new EmptyBorder(UITheme.SPACE_XL, UITheme.SPACE_XL, UITheme.SPACE_XL, UITheme.SPACE_XL));

        initializeUI();
        loadFleetData();
        startAutoRefresh();
    }

    private void initializeUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = UITheme.createTitleLabel("🚑 Fleet Management");
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshBtn = UITheme.createSecondaryButton("↻ Refresh");
        refreshBtn.addActionListener(e -> loadFleetData());
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(0, UITheme.SPACE_LG));
        contentPanel.setOpaque(false);

        // Stats
        contentPanel.add(createStatsPanel(), BorderLayout.NORTH);

        // Tables - split view
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setBackground(UITheme.DARK_BG);
        splitPane.setBorder(null);

        splitPane.setTopComponent(createFleetTablePanel());
        splitPane.setBottomComponent(createMaintenancePanel());

        contentPanel.add(splitPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 5, UITheme.SPACE_LG, 0));
        statsPanel.setOpaque(false);

        Map<String, Object> stats = vehicleMgr.getVehicleStats();

        statsPanel.add(createStatCard("Total Fleet", String.valueOf(stats.getOrDefault("total", 0)), UITheme.ACCENT));
        statsPanel.add(
                createStatCard("Available", String.valueOf(stats.getOrDefault("available", 0)), UITheme.STATUS_AVAILABLE));
        statsPanel
                .add(createStatCard("Low Fuel", String.valueOf(stats.getOrDefault("low_fuel", 0)), UITheme.STATUS_CRITICAL));

        double avgFuel = stats.get("avg_fuel") != null ? ((Number) stats.get("avg_fuel")).doubleValue() : 0;
        statsPanel.add(createStatCard("Avg Fuel", String.format("%.0f%%", avgFuel), UITheme.STATUS_DISPATCHED));
        statsPanel.add(createStatCard("Pending Service", String.valueOf(stats.getOrDefault("pending_maintenance", 0)),
                UITheme.GRADIENT_END));

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
        valueLbl.setFont(UITheme.FONT_HEADER);
        valueLbl.setForeground(color);
        valueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(UITheme.SPACE_SM));
        card.add(valueLbl);

        return card;
    }

    private JPanel createFleetTablePanel() {
        JPanel tableCard = UITheme.createCard();
        tableCard.setLayout(new BorderLayout());

        JLabel tableTitle = UITheme.createHeaderLabel("Fleet Status");
        tableTitle.setBorder(new EmptyBorder(0, 0, UITheme.SPACE_MD, 0));
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] columns = { "ID", "Vehicle #", "Status", "Fuel", "Driver", "Location (X,Y)", "Last Service" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        fleetTable = UITheme.createStyledTable(tableModel);

        // Custom fuel cell renderer
        fleetTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel(new BorderLayout(5, 0));
                panel.setOpaque(true);
                panel.setBackground(isSelected ? UITheme.ACCENT : UITheme.CARD_BG);

                int fuel = Integer.parseInt(value.toString().replace("%", ""));

                JProgressBar bar = new JProgressBar(0, 100);
                bar.setValue(fuel);
                bar.setStringPainted(true);
                bar.setString(fuel + "%");
                bar.setForeground(
                        fuel < 30 ? UITheme.STATUS_CRITICAL : fuel < 60 ? UITheme.STATUS_DISPATCHED : UITheme.STATUS_AVAILABLE);
                bar.setBackground(UITheme.PANEL_BG);
                bar.setBorderPainted(false);

                panel.add(bar, BorderLayout.CENTER);
                return panel;
            }
        });

        JScrollPane scrollPane = new JScrollPane(fleetTable);
        UITheme.styleScrollPane(scrollPane);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Action buttons
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.SPACE_MD, UITheme.SPACE_SM));
        actionsPanel.setOpaque(false);

        JButton refuelBtn = UITheme.createSuccessButton("⛽ Refuel");
        refuelBtn.addActionListener(e -> refuelAmbulance());
        actionsPanel.add(refuelBtn);

        JButton scheduleBtn = UITheme.createPrimaryButton("🔧 Schedule Service");
        scheduleBtn.addActionListener(e -> scheduleService());
        actionsPanel.add(scheduleBtn);

        JButton trackBtn = UITheme.createSecondaryButton("📍 Track on Map");
        trackBtn.addActionListener(e -> trackOnMap());
        actionsPanel.add(trackBtn);

        tableCard.add(actionsPanel, BorderLayout.SOUTH);

        return tableCard;
    }

    private JPanel createMaintenancePanel() {
        JPanel maintenanceCard = UITheme.createCard();
        maintenanceCard.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = UITheme.createHeaderLabel("Scheduled Maintenance");
        headerPanel.add(title, BorderLayout.WEST);

        JButton addBtn = UITheme.createGhostButton("+ Add");
        addBtn.addActionListener(e -> addMaintenance());
        headerPanel.add(addBtn, BorderLayout.EAST);

        maintenanceCard.add(headerPanel, BorderLayout.NORTH);

        String[] columns = { "ID", "Vehicle #", "Type", "Date", "Status" };
        maintenanceModel = new DefaultTableModel(columns, 0);
        maintenanceTable = UITheme.createStyledTable(maintenanceModel);

        JScrollPane scrollPane = new JScrollPane(maintenanceTable);
        UITheme.styleScrollPane(scrollPane);
        maintenanceCard.add(scrollPane, BorderLayout.CENTER);

        // Maintenance actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsPanel.setOpaque(false);

        JButton startBtn = UITheme.createWarningButton("Start Service");
        startBtn.addActionListener(e -> updateMaintenanceStatus("In Progress"));
        actionsPanel.add(startBtn);

        JButton completeBtn = UITheme.createSuccessButton("Mark Complete");
        completeBtn.addActionListener(e -> updateMaintenanceStatus("Completed"));
        actionsPanel.add(completeBtn);

        maintenanceCard.add(actionsPanel, BorderLayout.SOUTH);

        return maintenanceCard;
    }

    private void loadFleetData() {
        // Load fleet
        tableModel.setRowCount(0);
        java.util.List<Map<String, Object>> ambulances = vehicleMgr.getAllAmbulances();

        for (Map<String, Object> amb : ambulances) {
            String status = (String) amb.get("status");
            String statusDisplay = UITheme.getAmbulanceStatusIcon(status);
            String driverName = amb.get("driver_name") != null ? (String) amb.get("driver_name") : "Unassigned";

            tableModel.addRow(new Object[] {
                    amb.get("ambulance_id"),
                    amb.get("vehicle_number"),
                    statusDisplay,
                    amb.get("fuel_level") + "%",
                    driverName,
                    amb.get("current_x") + ", " + amb.get("current_y"),
                    "N/A"
            });
        }

        // Load maintenance
        maintenanceModel.setRowCount(0);
        java.util.List<Map<String, Object>> maintenanceList = vehicleMgr.getScheduledMaintenance();

        for (Map<String, Object> m : maintenanceList) {
            maintenanceModel.addRow(new Object[] {
                    m.get("maintenance_id"),
                    m.get("vehicle_number"),
                    m.get("maintenance_type"),
                    m.get("service_date"),
                    m.get("status")
            });
        }
    }

    private void refuelAmbulance() {
        int selectedRow = fleetTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an ambulance", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ambulanceId = (int) tableModel.getValueAt(selectedRow, 0);
        vehicleMgr.refuel(ambulanceId);
        loadFleetData();
        JOptionPane.showMessageDialog(this, "Ambulance refueled to 100%", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void scheduleService() {
        int selectedRow = fleetTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an ambulance", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ambulanceId = (int) tableModel.getValueAt(selectedRow, 0);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        String[] types = { "Oil Change", "Tire Rotation", "Full Service", "Repair", "Inspection" };
        JComboBox<String> typeCombo = new JComboBox<>(types);
        JTextField dateField = new JTextField(java.time.LocalDate.now().plusDays(7).toString());
        JTextArea descArea = new JTextArea(3, 20);

        formPanel.add(new JLabel("Service Type:"));
        formPanel.add(typeCombo);
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        formPanel.add(dateField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(new JScrollPane(descArea));

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Schedule Maintenance",
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            vehicleMgr.scheduleMaintenance(ambulanceId, (String) typeCombo.getSelectedItem(),
                    descArea.getText(), dateField.getText());
            loadFleetData();
            JOptionPane.showMessageDialog(this, "Maintenance scheduled!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateMaintenanceStatus(String status) {
        int selectedRow = maintenanceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a maintenance record", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maintenanceId = (int) maintenanceModel.getValueAt(selectedRow, 0);
        vehicleMgr.updateMaintenanceStatus(maintenanceId, status);
        loadFleetData();
    }

    private void addMaintenance() {
        scheduleService();
    }

    private void trackOnMap() {
        JOptionPane.showMessageDialog(this, "Opening map view to track selected ambulance...", "Track",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void startAutoRefresh() {
        refreshTimer = new javax.swing.Timer(15000, e -> loadFleetData());
        refreshTimer.start();
    }

    public void stopRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }
}


