import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

/**
 * HospitalManagementPanel - Manage hospital beds, specialties, and availability
 * Features: Add/remove beds, view real-time availability, specialty matching
 */
public class HospitalManagementPanel extends JPanel {
    private DatabaseManager dbManager;
    private JTable hospitalTable;
    private DefaultTableModel tableModel;
    private javax.swing.Timer refreshTimer;

    // Stats labels
    private JLabel totalBedsLabel;
    private JLabel availableBedsLabel;
    private JLabel totalICULabel;
    private JLabel availableICULabel;

    public HospitalManagementPanel() {
        this.dbManager = DatabaseManager.getInstance();
        setLayout(new BorderLayout(0, UITheme.SPACE_LG));
        setBackground(UITheme.DARK_BG);
        setBorder(new EmptyBorder(UITheme.SPACE_XL, UITheme.SPACE_XL, UITheme.SPACE_XL, UITheme.SPACE_XL));

        initializeUI();
        loadHospitalData();
        startAutoRefresh();
    }

    private void initializeUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = UITheme.createTitleLabel("🏥 Hospital Management");
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshBtn = UITheme.createSecondaryButton("↻ Refresh");
        refreshBtn.addActionListener(e -> loadHospitalData());
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Main content with stats and table
        JPanel contentPanel = new JPanel(new BorderLayout(0, UITheme.SPACE_LG));
        contentPanel.setOpaque(false);

        // Stats cards
        contentPanel.add(createStatsPanel(), BorderLayout.NORTH);

        // Hospital table
        contentPanel.add(createTablePanel(), BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // Action buttons
        add(createActionPanel(), BorderLayout.SOUTH);
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, UITheme.SPACE_LG, 0));
        statsPanel.setOpaque(false);

        // Create stat cards with label references stored directly
        totalBedsLabel = new JLabel("0");
        JPanel totalCard = createStatCard("Total Beds", totalBedsLabel, UITheme.ACCENT);
        statsPanel.add(totalCard);

        availableBedsLabel = new JLabel("0");
        JPanel availCard = createStatCard("Available Beds", availableBedsLabel, UITheme.STATUS_AVAILABLE);
        statsPanel.add(availCard);

        totalICULabel = new JLabel("0");
        JPanel icuCard = createStatCard("Total ICU", totalICULabel, UITheme.GRADIENT_END);
        statsPanel.add(icuCard);

        availableICULabel = new JLabel("0");
        JPanel icuAvailCard = createStatCard("Available ICU", availableICULabel, UITheme.STATUS_DISPATCHED);
        statsPanel.add(icuAvailCard);

        return statsPanel;
    }

    private JPanel createStatCard(String title, JLabel valueLbl, Color color) {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout());

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_SECONDARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLbl.setFont(UITheme.FONT_DISPLAY);
        valueLbl.setForeground(color);
        valueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(titleLbl);
        inner.add(Box.createVerticalStrut(UITheme.SPACE_SM));
        inner.add(valueLbl);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = UITheme.createCard();
        tablePanel.setLayout(new BorderLayout());

        String[] columns = { "ID", "Hospital Name", "Specialty", "Capacity", "Available", "ICU Total", "ICU Avail",
                "Severity", "Phone" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        hospitalTable = UITheme.createStyledTable(tableModel);
        hospitalTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        hospitalTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        hospitalTable.getColumnModel().getColumn(2).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(hospitalTable);
        UITheme.styleScrollPane(scrollPane);

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.SPACE_MD, 0));
        actionPanel.setOpaque(false);

        JButton addBedBtn = UITheme.createSuccessButton("➕ Add Bed");
        addBedBtn.addActionListener(e -> modifyBeds(1));

        JButton removeBedBtn = UITheme.createDangerButton("➖ Remove Bed");
        removeBedBtn.addActionListener(e -> modifyBeds(-1));

        JButton addICUBtn = UITheme.createPrimaryButton("➕ Add ICU");
        addICUBtn.addActionListener(e -> modifyICUBeds(1));

        JButton removeICUBtn = UITheme.createWarningButton("➖ Remove ICU");
        removeICUBtn.addActionListener(e -> modifyICUBeds(-1));

        JButton editBtn = UITheme.createSecondaryButton("✏️ Edit Hospital");
        editBtn.addActionListener(e -> editSelectedHospital());

        actionPanel.add(addBedBtn);
        actionPanel.add(removeBedBtn);
        actionPanel.add(addICUBtn);
        actionPanel.add(removeICUBtn);
        actionPanel.add(editBtn);

        return actionPanel;
    }

    private void loadHospitalData() {
        tableModel.setRowCount(0);
        int totalBeds = 0, availBeds = 0, totalICU = 0, availICU = 0;

        try {
            String sql = "SELECT * FROM hospitals ORDER BY hospital_id";
            ResultSet rs = dbManager.executeQuery(sql);

            while (rs.next()) {
                int capacity = rs.getInt("capacity");
                int available = rs.getInt("available_beds");
                int icuTotal = rs.getInt("icu_beds");
                int icuAvail = rs.getInt("available_icu_beds");

                totalBeds += capacity;
                availBeds += available;
                totalICU += icuTotal;
                availICU += icuAvail;

                tableModel.addRow(new Object[] {
                        rs.getInt("hospital_id"),
                        rs.getString("name"),
                        rs.getString("specialty"),
                        capacity,
                        available,
                        icuTotal,
                        icuAvail,
                        rs.getString("max_severity"),
                        rs.getString("phone")
                });
            }
        } catch (SQLException e) {
            System.err.println("[HospitalManagement] Error loading data: " + e.getMessage());
        }

        // Update stats
        totalBedsLabel.setText(String.valueOf(totalBeds));
        availableBedsLabel.setText(String.valueOf(availBeds));
        totalICULabel.setText(String.valueOf(totalICU));
        availableICULabel.setText(String.valueOf(availICU));
    }

    private void modifyBeds(int change) {
        int selectedRow = hospitalTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a hospital first", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int hospitalId = (int) tableModel.getValueAt(selectedRow, 0);
        String hospitalName = (String) tableModel.getValueAt(selectedRow, 1);
        int currentBeds = (int) tableModel.getValueAt(selectedRow, 4);
        int capacity = (int) tableModel.getValueAt(selectedRow, 3);

        int newBeds = currentBeds + change;
        if (newBeds < 0 || newBeds > capacity) {
            JOptionPane.showMessageDialog(this, "Invalid bed count. Must be between 0 and " + capacity, "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            dbManager.executeUpdate(
                    "UPDATE hospitals SET available_beds = " + newBeds + " WHERE hospital_id = " + hospitalId);
            loadHospitalData();

            String action = change > 0 ? "added to" : "removed from";
            System.out.println("[HospitalManagement] Bed " + action + " " + hospitalName);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating beds: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifyICUBeds(int change) {
        int selectedRow = hospitalTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a hospital first", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int hospitalId = (int) tableModel.getValueAt(selectedRow, 0);
        int currentICU = (int) tableModel.getValueAt(selectedRow, 6);
        int maxICU = (int) tableModel.getValueAt(selectedRow, 5);

        int newICU = currentICU + change;
        if (newICU < 0 || newICU > maxICU) {
            JOptionPane.showMessageDialog(this, "Invalid ICU bed count. Must be between 0 and " + maxICU, "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            dbManager.executeUpdate(
                    "UPDATE hospitals SET available_icu_beds = " + newICU + " WHERE hospital_id = " + hospitalId);
            loadHospitalData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating ICU beds: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedHospital() {
        int selectedRow = hospitalTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a hospital first", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int hospitalId = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        String specialty = (String) tableModel.getValueAt(selectedRow, 2);
        String phone = (String) tableModel.getValueAt(selectedRow, 8);

        // Create edit dialog
        JPanel editPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        editPanel.setBackground(UITheme.CARD_BG);

        JTextField nameField = new JTextField(name);
        JTextField specialtyField = new JTextField(specialty != null ? specialty : "");
        JTextField phoneField = new JTextField(phone != null ? phone : "");
        JComboBox<String> severityCombo = new JComboBox<>(new String[] { "Low", "Medium", "High", "Critical" });
        severityCombo.setSelectedItem(tableModel.getValueAt(selectedRow, 7));

        editPanel.add(new JLabel("Name:"));
        editPanel.add(nameField);
        editPanel.add(new JLabel("Specialty:"));
        editPanel.add(specialtyField);
        editPanel.add(new JLabel("Phone:"));
        editPanel.add(phoneField);
        editPanel.add(new JLabel("Max Severity:"));
        editPanel.add(severityCombo);

        int result = JOptionPane.showConfirmDialog(this, editPanel, "Edit Hospital", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String sql = String.format(
                        "UPDATE hospitals SET name='%s', specialty='%s', phone='%s', max_severity='%s' WHERE hospital_id=%d",
                        nameField.getText(), specialtyField.getText(), phoneField.getText(),
                        severityCombo.getSelectedItem(), hospitalId);
                dbManager.executeUpdate(sql);
                loadHospitalData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error updating hospital: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Find best hospital for injury type
     */
    public Map<String, Object> findBestHospital(String injuryType, String severity) {
        Map<String, Object> result = new HashMap<>();
        try {
            String sql = "SELECT * FROM hospitals WHERE available_beds > 0 " +
                    "AND (specialty LIKE '%" + injuryType + "%' OR specialty = 'General') " +
                    "ORDER BY available_beds DESC LIMIT 1";
            ResultSet rs = dbManager.executeQuery(sql);

            if (rs.next()) {
                result.put("hospital_id", rs.getInt("hospital_id"));
                result.put("name", rs.getString("name"));
                result.put("available_beds", rs.getInt("available_beds"));
                result.put("specialty", rs.getString("specialty"));
            }
        } catch (SQLException e) {
            System.err.println("[HospitalManagement] Error finding hospital: " + e.getMessage());
        }
        return result;
    }

    private void startAutoRefresh() {
        refreshTimer = new javax.swing.Timer(10000, e -> loadHospitalData());
        refreshTimer.start();
    }

    public void stopRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }
}


