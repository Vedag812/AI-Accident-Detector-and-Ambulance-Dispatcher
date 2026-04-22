import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLOperationsPanel - Interactive SQL query explorer with 4 category tabs.
 * Demonstrates relational algebra, DML, subqueries, joins, views,
 * stored functions, triggers, cursors, and exception handling.
 *
 * Designed to match the existing UITheme styling throughout the application.
 */
public class SQLOperationsPanel extends JPanel {
    private DatabaseManager dbManager;
    private JTable resultsTable;
    private DefaultTableModel resultsModel;
    private JTextArea sqlDisplay;
    private JLabel statusLabel;
    private JLabel rowCountLabel;
    private JPanel buttonContentPanel; // holds the buttons for each tab

    // Tab tracking
    private int activeTabIndex = 0;
    private JButton[] tabButtons;

    // Tab accent colors
    private static final Color TAB_COLOR_1 = new Color(59, 130, 246);   // Blue
    private static final Color TAB_COLOR_2 = new Color(16, 185, 129);   // Green
    private static final Color TAB_COLOR_3 = new Color(245, 158, 11);   // Amber
    private static final Color TAB_COLOR_4 = new Color(124, 58, 237);   // Purple

    public SQLOperationsPanel() {
        this.dbManager = DatabaseManager.getInstance();
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.DARK_BG);
        initializeUI();
        ensureStoredProcedures();
    }

    private void initializeUI() {
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════════════════════════

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UITheme.CARD_BG, getWidth(), 0, UITheme.PANEL_BG);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(UITheme.BORDER);
                g2d.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2d.dispose();
            }
        };
        header.setBorder(new EmptyBorder(18, 28, 14, 28));

        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("SQL Operations Lab");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UITheme.TEXT_PRIMARY);
        leftPanel.add(title);

        JLabel subtitle = new JLabel("Relational Algebra  •  DML  •  Joins & Views  •  Procedures & Triggers");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(UITheme.TEXT_SECONDARY);
        leftPanel.add(subtitle);

        header.add(leftPanel, BorderLayout.WEST);

        // Row count badge
        rowCountLabel = new JLabel("0 rows");
        rowCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rowCountLabel.setForeground(UITheme.TEXT_SECONDARY);
        header.add(rowCountLabel, BorderLayout.EAST);

        return header;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MAIN CONTENT: sidebar tabs + buttons area + results
    // ═══════════════════════════════════════════════════════════════════════

    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(UITheme.DARK_BG);

        // Left sidebar with tab buttons
        main.add(createSidebar(), BorderLayout.WEST);

        // Right content: button grid on top, results on bottom
        JPanel rightContent = new JPanel(new BorderLayout(0, 12));
        rightContent.setBackground(UITheme.DARK_BG);
        rightContent.setBorder(new EmptyBorder(12, 12, 12, 16));

        // Button area (swappable per tab)
        buttonContentPanel = new JPanel(new CardLayout());
        buttonContentPanel.setOpaque(false);
        buttonContentPanel.add(createRelationalOpsButtons(), "tab0");
        buttonContentPanel.add(createDMLButtons(), "tab1");
        buttonContentPanel.add(createJoinsViewsButtons(), "tab2");
        buttonContentPanel.add(createFunctionsTriggersButtons(), "tab3");

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(buttonContentPanel, BorderLayout.CENTER);
        topSection.setPreferredSize(new Dimension(0, 160));

        rightContent.add(topSection, BorderLayout.NORTH);

        // Results area: split between table and SQL display
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                createResultsTablePanel(), createSQLDisplayPanel());
        splitPane.setDividerLocation(280);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);
        splitPane.setBackground(UITheme.DARK_BG);
        rightContent.add(splitPane, BorderLayout.CENTER);

        main.add(rightContent, BorderLayout.CENTER);
        return main;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SIDEBAR TABS
    // ═══════════════════════════════════════════════════════════════════════

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBackground(UITheme.CARD_BG);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER),
                new EmptyBorder(14, 10, 14, 10)));

        JLabel catLabel = new JLabel("CATEGORIES");
        catLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        catLabel.setForeground(UITheme.TEXT_MUTED);
        catLabel.setBorder(new EmptyBorder(4, 10, 10, 0));
        catLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(catLabel);

        String[] tabNames = {
            "Relational Ops",
            "DML & Constraints",
            "Joins & Views",
            "Functions & Triggers"
        };
        Color[] tabColors = { TAB_COLOR_1, TAB_COLOR_2, TAB_COLOR_3, TAB_COLOR_4 };
        String[] tabDesc = {
            "Select, Project, Union...",
            "INSERT, UPDATE, DELETE...",
            "Subqueries, Views...",
            "Procedures, Cursors..."
        };

        tabButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            tabButtons[i] = createSidebarTab(tabNames[i], tabDesc[i], tabColors[i], i == 0);
            tabButtons[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            tabButtons[i].addActionListener(e -> switchTab(idx));
            sidebar.add(tabButtons[i]);
            sidebar.add(Box.createVerticalStrut(6));
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JButton createSidebarTab(String name, String desc, Color accent, boolean active) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isActive = (tabButtons != null && tabButtons[activeTabIndex] == this);
                boolean hover = getModel().isRollover();

                if (isActive) {
                    g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 18));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2d.setColor(accent);
                    g2d.fillRoundRect(0, 6, 4, getHeight() - 12, 4, 4);
                } else if (hover) {
                    g2d.setColor(UITheme.SURFACE_BG);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }

                // Title
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2d.setColor(isActive ? accent : UITheme.TEXT_PRIMARY);
                g2d.drawString(name, 16, 22);

                // Subtitle
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2d.setColor(UITheme.TEXT_MUTED);
                g2d.drawString(desc, 16, 38);

                g2d.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(190, 50));
        btn.setMaximumSize(new Dimension(190, 50));
        return btn;
    }

    private void switchTab(int index) {
        activeTabIndex = index;
        CardLayout cl = (CardLayout) buttonContentPanel.getLayout();
        cl.show(buttonContentPanel, "tab" + index);
        for (JButton tb : tabButtons) tb.repaint();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TAB 1: RELATIONAL OPERATIONS (5 operations)
    // ═══════════════════════════════════════════════════════════════════════

    private JPanel createRelationalOpsButtons() {
        JPanel panel = createButtonGrid("Relational Algebra Operations", TAB_COLOR_1);
        JPanel grid = (JPanel) ((JPanel) panel.getComponent(1)).getComponent(0);

        grid.add(createQueryButton("Select (σ)", "Filter accidents by severity",
                TAB_COLOR_1, () -> executeAndDisplay(
                "-- SELECT Operation (σ): Filter rows matching a condition\n" +
                "-- σ_{severity='Critical'}(accidents)\n\n" +
                "SELECT accident_id, location, severity, vehicle_id, accident_time\n" +
                "FROM accidents\n" +
                "WHERE severity = 'Critical'\n" +
                "ORDER BY accident_time DESC;",

                "SELECT accident_id, location, severity, vehicle_id, accident_time " +
                "FROM accidents WHERE severity = 'Critical' ORDER BY accident_time DESC")));

        grid.add(createQueryButton("Project (π)", "Pick specific columns only",
                TAB_COLOR_1, () -> executeAndDisplay(
                "-- PROJECT Operation (π): Select specific attributes\n" +
                "-- π_{location, severity}(accidents)\n\n" +
                "SELECT DISTINCT location, severity\n" +
                "FROM accidents\n" +
                "ORDER BY location;",

                "SELECT DISTINCT location, severity FROM accidents ORDER BY location")));

        grid.add(createQueryButton("Union (∪)", "Combine names from two tables",
                TAB_COLOR_1, () -> executeAndDisplay(
                "-- UNION Operation (∪): Combine results from two queries\n" +
                "-- π_{name}(hospitals) ∪ π_{name}(drivers)\n\n" +
                "SELECT name, 'Hospital' AS source FROM hospitals\n" +
                "UNION\n" +
                "SELECT name, 'Driver' AS source FROM drivers\n" +
                "ORDER BY source, name;",

                "SELECT name, 'Hospital' AS source FROM hospitals " +
                "UNION " +
                "SELECT name, 'Driver' AS source FROM drivers " +
                "ORDER BY source, name")));

        grid.add(createQueryButton("Intersection (∩)", "Common hospital IDs in requests",
                TAB_COLOR_1, () -> executeAndDisplay(
                "-- INTERSECTION Operation (∩): Find common elements\n" +
                "-- Hospitals that have both ambulances assigned AND beds available\n\n" +
                "SELECT h.hospital_id, h.name, h.available_beds\n" +
                "FROM hospitals h\n" +
                "WHERE h.hospital_id IN (\n" +
                "    SELECT DISTINCT assigned_hospital_id\n" +
                "    FROM ambulances\n" +
                "    WHERE assigned_hospital_id IS NOT NULL\n" +
                ")\n" +
                "AND h.available_beds > 0\n" +
                "ORDER BY h.name;",

                "SELECT h.hospital_id, h.name, h.available_beds FROM hospitals h " +
                "WHERE h.hospital_id IN (SELECT DISTINCT assigned_hospital_id FROM ambulances " +
                "WHERE assigned_hospital_id IS NOT NULL) AND h.available_beds > 0 ORDER BY h.name")));

        grid.add(createQueryButton("Natural Join (⋈)", "Ambulances joined with drivers",
                TAB_COLOR_1, () -> executeAndDisplay(
                "-- NATURAL JOIN Operation (⋈): Join tables on matching columns\n" +
                "-- ambulances ⋈ drivers (on driver_id = ambulance's driver_id)\n\n" +
                "SELECT a.ambulance_id, a.vehicle_number, a.status AS ambulance_status,\n" +
                "       d.name AS driver_name, d.phone AS driver_phone,\n" +
                "       d.status AS driver_status, d.rating\n" +
                "FROM ambulances a\n" +
                "INNER JOIN drivers d ON a.driver_id = d.driver_id\n" +
                "ORDER BY a.ambulance_id;",

                "SELECT a.ambulance_id, a.vehicle_number, a.status AS ambulance_status, " +
                "d.name AS driver_name, d.phone AS driver_phone, d.status AS driver_status, d.rating " +
                "FROM ambulances a INNER JOIN drivers d ON a.driver_id = d.driver_id ORDER BY a.ambulance_id")));

        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TAB 2: DML, CONSTRAINTS & SETS
    // ═══════════════════════════════════════════════════════════════════════

    private JPanel createDMLButtons() {
        JPanel panel = createButtonGrid("DML, Constraints & Set Operations", TAB_COLOR_2);
        JPanel grid = (JPanel) ((JPanel) panel.getComponent(1)).getComponent(0);

        grid.add(createQueryButton("INSERT Demo", "Add a sample accident record",
                TAB_COLOR_2, () -> executeDMLAndDisplay(
                "-- INSERT: Add a new accident record\n\n" +
                "INSERT INTO accidents\n" +
                "  (location, vehicle_id, severity, description, reported_by)\n" +
                "VALUES\n" +
                "  ('SQL Lab Test Road', 'TN99ZZ0001', 'Medium',\n" +
                "   'Demo accident inserted from SQL Lab', 'SQL Lab User');\n\n" +
                "-- Then verify the insert:\n" +
                "SELECT * FROM accidents WHERE reported_by = 'SQL Lab User'\n" +
                "ORDER BY accident_time DESC LIMIT 5;",

                "INSERT INTO accidents (location, vehicle_id, severity, description, reported_by) " +
                "VALUES ('SQL Lab Test Road', 'TN99ZZ0001', 'Medium', 'Demo accident inserted from SQL Lab', 'SQL Lab User')",

                "SELECT accident_id, location, vehicle_id, severity, reported_by, accident_time " +
                "FROM accidents WHERE reported_by = 'SQL Lab User' ORDER BY accident_time DESC LIMIT 5")));

        grid.add(createQueryButton("UPDATE Demo", "Update hospital bed count",
                TAB_COLOR_2, () -> executeDMLAndDisplay(
                "-- UPDATE: Modify hospital available beds\n\n" +
                "UPDATE hospitals\n" +
                "SET available_beds = available_beds - 1\n" +
                "WHERE hospital_id = 1\n" +
                "  AND available_beds > 0;\n\n" +
                "-- Verify the update:\n" +
                "SELECT hospital_id, name, available_beds, capacity\n" +
                "FROM hospitals ORDER BY hospital_id;",

                "UPDATE hospitals SET available_beds = available_beds - 1 WHERE hospital_id = 1 AND available_beds > 0",

                "SELECT hospital_id, name, available_beds, capacity FROM hospitals ORDER BY hospital_id")));

        grid.add(createQueryButton("DELETE Demo", "Remove test accidents",
                TAB_COLOR_2, () -> executeDMLAndDisplay(
                "-- DELETE: Remove test records created by SQL Lab\n\n" +
                "DELETE FROM accidents\n" +
                "WHERE reported_by = 'SQL Lab User';\n\n" +
                "-- Verify deletion:\n" +
                "SELECT COUNT(*) AS remaining_sql_lab_records\n" +
                "FROM accidents\n" +
                "WHERE reported_by = 'SQL Lab User';",

                "DELETE FROM accidents WHERE reported_by = 'SQL Lab User'",

                "SELECT COUNT(*) AS remaining_sql_lab_records FROM accidents WHERE reported_by = 'SQL Lab User'")));

        grid.add(createQueryButton("Show Constraints", "List all foreign keys",
                TAB_COLOR_2, () -> executeAndDisplay(
                "-- CONSTRAINTS: Show all foreign key relationships\n\n" +
                "SELECT TABLE_NAME, COLUMN_NAME,\n" +
                "       CONSTRAINT_NAME,\n" +
                "       REFERENCED_TABLE_NAME,\n" +
                "       REFERENCED_COLUMN_NAME\n" +
                "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE\n" +
                "WHERE REFERENCED_TABLE_NAME IS NOT NULL\n" +
                "  AND TABLE_SCHEMA = DATABASE()\n" +
                "ORDER BY TABLE_NAME;",

                "SELECT TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME, REFERENCED_TABLE_NAME, " +
                "REFERENCED_COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                "WHERE REFERENCED_TABLE_NAME IS NOT NULL AND TABLE_SCHEMA = DATABASE() ORDER BY TABLE_NAME")));

        grid.add(createQueryButton("UNION ALL + IN", "Set operations demo",
                TAB_COLOR_2, () -> executeAndDisplay(
                "-- SET OPERATIONS: UNION ALL (keeps duplicates) and IN clause\n\n" +
                "-- Part 1: UNION ALL combines all rows including duplicates\n" +
                "SELECT severity AS item, 'Accident Severity' AS category FROM accidents\n" +
                "WHERE severity IN ('Critical', 'High')\n" +
                "UNION ALL\n" +
                "SELECT status, 'Ambulance Status' FROM ambulances\n" +
                "WHERE status IN ('yellow', 'red')\n" +
                "ORDER BY category, item;",

                "SELECT severity AS item, 'Accident Severity' AS category FROM accidents " +
                "WHERE severity IN ('Critical', 'High') UNION ALL " +
                "SELECT status, 'Ambulance Status' FROM ambulances " +
                "WHERE status IN ('yellow', 'red') ORDER BY category, item")));

        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TAB 3: SUBQUERIES, JOINS & VIEWS
    // ═══════════════════════════════════════════════════════════════════════

    private JPanel createJoinsViewsButtons() {
        JPanel panel = createButtonGrid("Subqueries, Joins & Views", TAB_COLOR_3);
        JPanel grid = (JPanel) ((JPanel) panel.getComponent(1)).getComponent(0);

        grid.add(createQueryButton("Correlated Subquery", "Hospitals above avg beds",
                TAB_COLOR_3, () -> executeAndDisplay(
                "-- CORRELATED SUBQUERY: Hospitals with above-average beds\n\n" +
                "SELECT h.hospital_id, h.name, h.available_beds, h.capacity,\n" +
                "       ROUND(h.available_beds * 100.0 / h.capacity, 1) AS occupancy_pct\n" +
                "FROM hospitals h\n" +
                "WHERE h.available_beds > (\n" +
                "    SELECT AVG(h2.available_beds)\n" +
                "    FROM hospitals h2\n" +
                ")\n" +
                "ORDER BY h.available_beds DESC;",

                "SELECT h.hospital_id, h.name, h.available_beds, h.capacity, " +
                "ROUND(h.available_beds * 100.0 / h.capacity, 1) AS occupancy_pct " +
                "FROM hospitals h WHERE h.available_beds > " +
                "(SELECT AVG(h2.available_beds) FROM hospitals h2) ORDER BY h.available_beds DESC")));

        grid.add(createQueryButton("Scalar Subquery", "Latest accident per severity",
                TAB_COLOR_3, () -> executeAndDisplay(
                "-- SCALAR SUBQUERY: Most recent accident for each severity\n\n" +
                "SELECT a.accident_id, a.location, a.severity, a.accident_time\n" +
                "FROM accidents a\n" +
                "WHERE a.accident_time = (\n" +
                "    SELECT MAX(a2.accident_time)\n" +
                "    FROM accidents a2\n" +
                "    WHERE a2.severity = a.severity\n" +
                ")\n" +
                "ORDER BY a.severity;",

                "SELECT a.accident_id, a.location, a.severity, a.accident_time " +
                "FROM accidents a WHERE a.accident_time = " +
                "(SELECT MAX(a2.accident_time) FROM accidents a2 WHERE a2.severity = a.severity) " +
                "ORDER BY a.severity")));

        grid.add(createQueryButton("INNER JOIN (3-table)", "Accidents + Ambulances + Hospitals",
                TAB_COLOR_3, () -> executeAndDisplay(
                "-- INNER JOIN: Link accidents, ambulances, and hospitals\n\n" +
                "SELECT a.accident_id, a.location AS accident_location,\n" +
                "       a.severity,\n" +
                "       amb.vehicle_number AS ambulance,\n" +
                "       amb.status AS ambulance_status,\n" +
                "       h.name AS hospital_name\n" +
                "FROM accidents a\n" +
                "INNER JOIN ambulances amb ON amb.assigned_accident_id = a.accident_id\n" +
                "INNER JOIN hospitals h ON amb.assigned_hospital_id = h.hospital_id\n" +
                "ORDER BY a.accident_time DESC\n" +
                "LIMIT 20;",

                "SELECT a.accident_id, a.location AS accident_location, a.severity, " +
                "amb.vehicle_number AS ambulance, amb.status AS ambulance_status, " +
                "h.name AS hospital_name FROM accidents a " +
                "INNER JOIN ambulances amb ON amb.assigned_accident_id = a.accident_id " +
                "INNER JOIN hospitals h ON amb.assigned_hospital_id = h.hospital_id " +
                "ORDER BY a.accident_time DESC LIMIT 20")));

        grid.add(createQueryButton("LEFT JOIN", "All hospitals + their ambulances",
                TAB_COLOR_3, () -> executeAndDisplay(
                "-- LEFT JOIN: All hospitals, even those without ambulances assigned\n\n" +
                "SELECT h.hospital_id, h.name AS hospital_name,\n" +
                "       h.available_beds,\n" +
                "       COUNT(amb.ambulance_id) AS assigned_ambulances,\n" +
                "       GROUP_CONCAT(amb.vehicle_number SEPARATOR ', ') AS ambulance_numbers\n" +
                "FROM hospitals h\n" +
                "LEFT JOIN ambulances amb ON amb.assigned_hospital_id = h.hospital_id\n" +
                "GROUP BY h.hospital_id, h.name, h.available_beds\n" +
                "ORDER BY assigned_ambulances DESC;",

                "SELECT h.hospital_id, h.name AS hospital_name, h.available_beds, " +
                "COUNT(amb.ambulance_id) AS assigned_ambulances, " +
                "GROUP_CONCAT(amb.vehicle_number SEPARATOR ', ') AS ambulance_numbers " +
                "FROM hospitals h " +
                "LEFT JOIN ambulances amb ON amb.assigned_hospital_id = h.hospital_id " +
                "GROUP BY h.hospital_id, h.name, h.available_beds " +
                "ORDER BY assigned_ambulances DESC")));

        grid.add(createQueryButton("Create View", "Create critical_accidents_view",
                TAB_COLOR_3, () -> executeDMLAndDisplay(
                "-- CREATE VIEW: Persistent query for critical accidents\n\n" +
                "CREATE OR REPLACE VIEW critical_accidents_view AS\n" +
                "SELECT a.accident_id, a.location, a.severity,\n" +
                "       a.vehicle_id, a.accident_time, a.reported_by,\n" +
                "       a.status\n" +
                "FROM accidents a\n" +
                "WHERE a.severity IN ('Critical', 'High')\n" +
                "ORDER BY a.accident_time DESC;",

                "CREATE OR REPLACE VIEW critical_accidents_view AS " +
                "SELECT a.accident_id, a.location, a.severity, a.vehicle_id, " +
                "a.accident_time, a.reported_by, a.status " +
                "FROM accidents a WHERE a.severity IN ('Critical', 'High')",

                "SELECT * FROM critical_accidents_view LIMIT 20")));

        grid.add(createQueryButton("Query View", "SELECT from the view",
                TAB_COLOR_3, () -> executeAndDisplay(
                "-- QUERY VIEW: Fetch data from the critical_accidents_view\n\n" +
                "SELECT * FROM critical_accidents_view\n" +
                "ORDER BY accident_time DESC\n" +
                "LIMIT 20;",

                "SELECT * FROM critical_accidents_view ORDER BY accident_time DESC LIMIT 20")));

        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TAB 4: FUNCTIONS, TRIGGERS, CURSORS & EXCEPTION HANDLING
    // ═══════════════════════════════════════════════════════════════════════

    private JPanel createFunctionsTriggersButtons() {
        JPanel panel = createButtonGrid("Functions, Triggers, Cursors & Exception Handling", TAB_COLOR_4);
        JPanel grid = (JPanel) ((JPanel) panel.getComponent(1)).getComponent(0);

        grid.add(createQueryButton("Create Function", "get_severity_count(sev)",
                TAB_COLOR_4, () -> {
            String displaySQL =
                "-- STORED FUNCTION: Count accidents by severity\n\n" +
                "DELIMITER $$\n" +
                "CREATE FUNCTION get_severity_count(sev VARCHAR(20))\n" +
                "RETURNS INT\n" +
                "DETERMINISTIC\n" +
                "READS SQL DATA\n" +
                "BEGIN\n" +
                "    DECLARE cnt INT DEFAULT 0;\n" +
                "    SELECT COUNT(*) INTO cnt\n" +
                "    FROM accidents\n" +
                "    WHERE severity = sev;\n" +
                "    RETURN cnt;\n" +
                "END $$\n" +
                "DELIMITER ;";

            try {
                // Drop if exists, then create
                try { dbManager.getConnection().createStatement().execute("DROP FUNCTION IF EXISTS get_severity_count"); } catch (Exception ignored) {}
                String createFunc = "CREATE FUNCTION get_severity_count(sev VARCHAR(20)) " +
                        "RETURNS INT DETERMINISTIC READS SQL DATA " +
                        "BEGIN DECLARE cnt INT DEFAULT 0; " +
                        "SELECT COUNT(*) INTO cnt FROM accidents WHERE severity = sev; " +
                        "RETURN cnt; END";
                dbManager.getConnection().createStatement().execute(createFunc);

                sqlDisplay.setText(displaySQL);
                setStatus("Function 'get_severity_count' created successfully", true);

                // Show verification
                executeSelect("SELECT 'Critical' AS severity, get_severity_count('Critical') AS count " +
                              "UNION ALL SELECT 'High', get_severity_count('High') " +
                              "UNION ALL SELECT 'Medium', get_severity_count('Medium') " +
                              "UNION ALL SELECT 'Low', get_severity_count('Low')");
            } catch (SQLException e) {
                sqlDisplay.setText(displaySQL);
                setStatus("Error: " + e.getMessage(), false);
            }
        }));

        grid.add(createQueryButton("Call Function", "Execute get_severity_count",
                TAB_COLOR_4, () -> executeAndDisplay(
                "-- CALL STORED FUNCTION: Get accident counts per severity\n\n" +
                "SELECT 'Critical' AS severity_level,\n" +
                "       get_severity_count('Critical') AS total_count\n" +
                "UNION ALL\n" +
                "SELECT 'High', get_severity_count('High')\n" +
                "UNION ALL\n" +
                "SELECT 'Medium', get_severity_count('Medium')\n" +
                "UNION ALL\n" +
                "SELECT 'Low', get_severity_count('Low');",

                "SELECT 'Critical' AS severity_level, get_severity_count('Critical') AS total_count " +
                "UNION ALL SELECT 'High', get_severity_count('High') " +
                "UNION ALL SELECT 'Medium', get_severity_count('Medium') " +
                "UNION ALL SELECT 'Low', get_severity_count('Low')")));

        grid.add(createQueryButton("Create Trigger", "Audit log on accident insert",
                TAB_COLOR_4, () -> {
            String displaySQL =
                "-- TRIGGER: Log every new accident into audit_logs\n\n" +
                "DELIMITER $$\n" +
                "CREATE TRIGGER after_accident_audit\n" +
                "AFTER INSERT ON accidents\n" +
                "FOR EACH ROW\n" +
                "BEGIN\n" +
                "    INSERT INTO audit_logs (user_id, action, details)\n" +
                "    VALUES (\n" +
                "        1,\n" +
                "        'ACCIDENT_REPORTED',\n" +
                "        CONCAT('New ', NEW.severity, ' accident at ',\n" +
                "               NEW.location, ' | Vehicle: ', NEW.vehicle_id)\n" +
                "    );\n" +
                "END $$\n" +
                "DELIMITER ;";

            try {
                try { dbManager.getConnection().createStatement().execute("DROP TRIGGER IF EXISTS after_accident_audit"); } catch (Exception ignored) {}
                String createTrig = "CREATE TRIGGER after_accident_audit AFTER INSERT ON accidents " +
                        "FOR EACH ROW BEGIN " +
                        "INSERT INTO audit_logs (user_id, action, details) VALUES " +
                        "(1, 'ACCIDENT_REPORTED', CONCAT('New ', NEW.severity, ' accident at ', " +
                        "NEW.location, ' | Vehicle: ', NEW.vehicle_id)); END";
                dbManager.getConnection().createStatement().execute(createTrig);
                sqlDisplay.setText(displaySQL);
                setStatus("Trigger 'after_accident_audit' created successfully", true);

                executeSelect("SELECT 'Trigger created' AS result, 'after_accident_audit' AS trigger_name, " +
                              "'accidents' AS on_table, 'AFTER INSERT' AS timing");
            } catch (SQLException e) {
                sqlDisplay.setText(displaySQL);
                setStatus("Error: " + e.getMessage(), false);
            }
        }));

        grid.add(createQueryButton("Test Trigger", "Insert and check audit log",
                TAB_COLOR_4, () -> executeDMLAndDisplay(
                "-- TEST TRIGGER: Insert an accident and verify audit_logs\n\n" +
                "INSERT INTO accidents\n" +
                "  (location, vehicle_id, severity, description, reported_by)\n" +
                "VALUES\n" +
                "  ('Trigger Test Road', 'TN99TR0001', 'High',\n" +
                "   'Inserted to test trigger', 'Trigger Test');\n\n" +
                "-- Check latest audit logs:\n" +
                "SELECT * FROM audit_logs\n" +
                "ORDER BY timestamp DESC LIMIT 5;",

                "INSERT INTO accidents (location, vehicle_id, severity, description, reported_by) " +
                "VALUES ('Trigger Test Road', 'TN99TR0001', 'High', 'Inserted to test trigger', 'Trigger Test')",

                "SELECT log_id, user_id, action, details, timestamp FROM audit_logs ORDER BY timestamp DESC LIMIT 5")));

        grid.add(createQueryButton("Cursor Demo", "Iterate over hospitals",
                TAB_COLOR_4, () -> {
            String displaySQL =
                "-- CURSOR: Stored Procedure that iterates over hospitals\n\n" +
                "DELIMITER $$\n" +
                "CREATE PROCEDURE list_hospitals_cursor()\n" +
                "BEGIN\n" +
                "    DECLARE done INT DEFAULT 0;\n" +
                "    DECLARE h_id INT;\n" +
                "    DECLARE h_name VARCHAR(100);\n" +
                "    DECLARE h_beds INT;\n" +
                "    DECLARE cur CURSOR FOR\n" +
                "        SELECT hospital_id, name, available_beds FROM hospitals;\n" +
                "    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;\n\n" +
                "    -- Create temp table for results\n" +
                "    DROP TEMPORARY TABLE IF EXISTS cursor_results;\n" +
                "    CREATE TEMPORARY TABLE cursor_results (\n" +
                "        hospital_id INT, name VARCHAR(100),\n" +
                "        beds INT, status VARCHAR(20)\n" +
                "    );\n\n" +
                "    OPEN cur;\n" +
                "    read_loop: LOOP\n" +
                "        FETCH cur INTO h_id, h_name, h_beds;\n" +
                "        IF done THEN LEAVE read_loop; END IF;\n" +
                "        INSERT INTO cursor_results VALUES (\n" +
                "            h_id, h_name, h_beds,\n" +
                "            CASE WHEN h_beds > 50 THEN 'Available'\n" +
                "                 WHEN h_beds > 10 THEN 'Limited'\n" +
                "                 ELSE 'Critical' END\n" +
                "        );\n" +
                "    END LOOP;\n" +
                "    CLOSE cur;\n\n" +
                "    SELECT * FROM cursor_results;\n" +
                "    DROP TEMPORARY TABLE cursor_results;\n" +
                "END $$\n" +
                "DELIMITER ;";

            try {
                try { dbManager.getConnection().createStatement().execute("DROP PROCEDURE IF EXISTS list_hospitals_cursor"); } catch (Exception ignored) {}

                String createProc = "CREATE PROCEDURE list_hospitals_cursor() " +
                    "BEGIN " +
                    "DECLARE done INT DEFAULT 0; " +
                    "DECLARE h_id INT; " +
                    "DECLARE h_name VARCHAR(100); " +
                    "DECLARE h_beds INT; " +
                    "DECLARE cur CURSOR FOR SELECT hospital_id, name, available_beds FROM hospitals; " +
                    "DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1; " +
                    "DROP TEMPORARY TABLE IF EXISTS cursor_results; " +
                    "CREATE TEMPORARY TABLE cursor_results (hospital_id INT, name VARCHAR(100), beds INT, status VARCHAR(20)); " +
                    "OPEN cur; " +
                    "read_loop: LOOP " +
                    "FETCH cur INTO h_id, h_name, h_beds; " +
                    "IF done THEN LEAVE read_loop; END IF; " +
                    "INSERT INTO cursor_results VALUES (h_id, h_name, h_beds, " +
                    "CASE WHEN h_beds > 50 THEN 'Available' WHEN h_beds > 10 THEN 'Limited' ELSE 'Critical' END); " +
                    "END LOOP; " +
                    "CLOSE cur; " +
                    "SELECT * FROM cursor_results; " +
                    "DROP TEMPORARY TABLE cursor_results; " +
                    "END";

                dbManager.getConnection().createStatement().execute(createProc);
                sqlDisplay.setText(displaySQL);
                setStatus("Procedure 'list_hospitals_cursor' created. Executing...", true);

                // Call procedure and get results
                CallableStatement cs = dbManager.getConnection().prepareCall("{CALL list_hospitals_cursor()}");
                boolean hasResults = cs.execute();
                if (hasResults) {
                    ResultSet rs = cs.getResultSet();
                    populateTableFromRS(rs);
                }
            } catch (SQLException e) {
                sqlDisplay.setText(displaySQL);
                setStatus("Error: " + e.getMessage(), false);
            }
        }));

        grid.add(createQueryButton("Exception Handling", "Safe division with handler",
                TAB_COLOR_4, () -> {
            String displaySQL =
                "-- EXCEPTION HANDLING: Stored Procedure with error handler\n\n" +
                "DELIMITER $$\n" +
                "CREATE PROCEDURE safe_update_beds(\n" +
                "    IN p_hospital_id INT,\n" +
                "    IN p_beds_to_reduce INT\n" +
                ")\n" +
                "BEGIN\n" +
                "    DECLARE current_beds INT DEFAULT 0;\n" +
                "    DECLARE EXIT HANDLER FOR SQLEXCEPTION\n" +
                "    BEGIN\n" +
                "        SELECT 'ERROR' AS status,\n" +
                "               'An unexpected error occurred' AS message;\n" +
                "        ROLLBACK;\n" +
                "    END;\n\n" +
                "    START TRANSACTION;\n\n" +
                "    SELECT available_beds INTO current_beds\n" +
                "    FROM hospitals\n" +
                "    WHERE hospital_id = p_hospital_id;\n\n" +
                "    IF current_beds IS NULL THEN\n" +
                "        SELECT 'NOT_FOUND' AS status,\n" +
                "               'Hospital not found' AS message;\n" +
                "    ELSEIF current_beds < p_beds_to_reduce THEN\n" +
                "        SELECT 'INSUFFICIENT' AS status,\n" +
                "               CONCAT('Only ', current_beds, ' beds available') AS message;\n" +
                "    ELSE\n" +
                "        UPDATE hospitals\n" +
                "        SET available_beds = available_beds - p_beds_to_reduce\n" +
                "        WHERE hospital_id = p_hospital_id;\n" +
                "        SELECT 'SUCCESS' AS status,\n" +
                "               CONCAT('Reduced ', p_beds_to_reduce, ' beds. Remaining: ',\n" +
                "                      current_beds - p_beds_to_reduce) AS message;\n" +
                "        COMMIT;\n" +
                "    END IF;\n" +
                "END $$\n" +
                "DELIMITER ;";

            try {
                try { dbManager.getConnection().createStatement().execute("DROP PROCEDURE IF EXISTS safe_update_beds"); } catch (Exception ignored) {}

                String createProc = "CREATE PROCEDURE safe_update_beds(IN p_hospital_id INT, IN p_beds_to_reduce INT) " +
                    "BEGIN " +
                    "DECLARE current_beds INT DEFAULT 0; " +
                    "DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN SELECT 'ERROR' AS status, 'An unexpected error occurred' AS message; ROLLBACK; END; " +
                    "START TRANSACTION; " +
                    "SELECT available_beds INTO current_beds FROM hospitals WHERE hospital_id = p_hospital_id; " +
                    "IF current_beds IS NULL THEN SELECT 'NOT_FOUND' AS status, 'Hospital not found' AS message; " +
                    "ELSEIF current_beds < p_beds_to_reduce THEN SELECT 'INSUFFICIENT' AS status, CONCAT('Only ', current_beds, ' beds available') AS message; " +
                    "ELSE UPDATE hospitals SET available_beds = available_beds - p_beds_to_reduce WHERE hospital_id = p_hospital_id; " +
                    "SELECT 'SUCCESS' AS status, CONCAT('Reduced ', p_beds_to_reduce, ' beds. Remaining: ', current_beds - p_beds_to_reduce) AS message; COMMIT; " +
                    "END IF; END";

                dbManager.getConnection().createStatement().execute(createProc);
                sqlDisplay.setText(displaySQL);
                setStatus("Procedure 'safe_update_beds' created. Testing with hospital_id=1, beds=2...", true);

                CallableStatement cs = dbManager.getConnection().prepareCall("{CALL safe_update_beds(1, 2)}");
                boolean hasResults = cs.execute();
                if (hasResults) {
                    ResultSet rs = cs.getResultSet();
                    populateTableFromRS(rs);
                }
            } catch (SQLException e) {
                sqlDisplay.setText(displaySQL);
                setStatus("Error: " + e.getMessage(), false);
            }
        }));

        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BUTTON GRID HELPER
    // ═══════════════════════════════════════════════════════════════════════

    private JPanel createButtonGrid(String title, Color accent) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(4, 0, 4, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(accent);
        wrapper.add(titleLabel, BorderLayout.NORTH);

        JPanel scrollContent = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        scrollContent.setOpaque(false);

        JScrollPane scroll = new JScrollPane(scrollContent,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JButton createQueryButton(String title, String description, Color accent, Runnable action) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean hover = getModel().isRollover();
                boolean pressed = getModel().isPressed();

                // Background
                if (pressed) {
                    g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
                } else if (hover) {
                    g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 15));
                } else {
                    g2d.setColor(UITheme.CARD_BG);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Border
                g2d.setColor(hover ? accent : UITheme.BORDER);
                g2d.setStroke(new BasicStroke(hover ? 1.5f : 1f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                // Left accent bar
                g2d.setColor(accent);
                g2d.fillRoundRect(0, 8, 3, getHeight() - 16, 3, 3);

                // Title
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2d.setColor(hover ? accent : UITheme.TEXT_PRIMARY);
                g2d.drawString(title, 14, 28);

                // Description
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2d.setColor(UITheme.TEXT_MUTED);
                String desc = description.length() > 28 ? description.substring(0, 25) + "..." : description;
                g2d.drawString(desc, 14, 44);

                // Arrow
                g2d.setColor(hover ? accent : UITheme.TEXT_MUTED);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2d.drawString("→", getWidth() - 22, 36);

                g2d.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(190, 58));
        btn.addActionListener(e -> {
            setStatus("Executing query...", true);
            // Run on background thread to keep UI responsive
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    action.run();
                    return null;
                }
            };
            worker.execute();
        });
        return btn;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // RESULTS TABLE
    // ═══════════════════════════════════════════════════════════════════════

    private JPanel createResultsTablePanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(UITheme.CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2d.setColor(UITheme.BORDER);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 16, 10, 16));

        JLabel label = new JLabel("Query Results");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(UITheme.TEXT_PRIMARY);
        label.setBorder(new EmptyBorder(0, 0, 8, 0));
        panel.add(label, BorderLayout.NORTH);

        resultsModel = new DefaultTableModel();
        resultsTable = UITheme.createStyledTable(resultsModel);
        JScrollPane scroll = new JScrollPane(resultsTable);
        UITheme.styleScrollPane(scroll);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SQL DISPLAY AREA
    // ═══════════════════════════════════════════════════════════════════════

    private JPanel createSQLDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(30, 41, 59));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel label = new JLabel("SQL Query");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(148, 163, 184));
        label.setBorder(new EmptyBorder(0, 0, 6, 0));
        panel.add(label, BorderLayout.NORTH);

        sqlDisplay = new JTextArea();
        sqlDisplay.setEditable(false);
        sqlDisplay.setFont(new Font("Consolas", Font.PLAIN, 13));
        sqlDisplay.setBackground(new Color(30, 41, 59));
        sqlDisplay.setForeground(new Color(226, 232, 240));
        sqlDisplay.setCaretColor(new Color(226, 232, 240));
        sqlDisplay.setBorder(new EmptyBorder(8, 8, 8, 8));
        sqlDisplay.setLineWrap(true);
        sqlDisplay.setWrapStyleWord(true);
        sqlDisplay.setText("-- Click any query button above to see the SQL and results here\n" +
                           "-- Results will appear in the table above");

        JScrollPane scroll = new JScrollPane(sqlDisplay);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(30, 41, 59));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FOOTER
    // ═══════════════════════════════════════════════════════════════════════

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(UITheme.CARD_BG);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(UITheme.BORDER);
                g.drawLine(0, 0, getWidth(), 0);
            }
        };
        footer.setBorder(new EmptyBorder(8, 20, 8, 20));

        statusLabel = new JLabel("Ready — click a query button to begin");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(UITheme.TEXT_SECONDARY);
        footer.add(statusLabel, BorderLayout.WEST);

        JButton clearBtn = UITheme.createSecondaryButton("Clear Results");
        clearBtn.setPreferredSize(new Dimension(130, 32));
        clearBtn.addActionListener(e -> {
            resultsModel.setColumnCount(0);
            resultsModel.setRowCount(0);
            sqlDisplay.setText("-- Click any query button to see SQL and results here");
            setStatus("Cleared", true);
            rowCountLabel.setText("0 rows");
        });
        footer.add(clearBtn, BorderLayout.EAST);

        return footer;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SQL EXECUTION HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    private void executeAndDisplay(String displaySQL, String execSQL) {
        SwingUtilities.invokeLater(() -> sqlDisplay.setText(displaySQL));
        executeSelect(execSQL);
    }

    private void executeDMLAndDisplay(String displaySQL, String dmlSQL, String verifySQL) {
        SwingUtilities.invokeLater(() -> sqlDisplay.setText(displaySQL));
        try {
            Statement stmt = dbManager.getConnection().createStatement();
            int affected = stmt.executeUpdate(dmlSQL);
            setStatus("DML executed — " + affected + " row(s) affected. Showing verification query...", true);
            executeSelect(verifySQL);
        } catch (SQLException e) {
            setStatus("Error: " + e.getMessage(), false);
        }
    }

    private void executeSelect(String sql) {
        try {
            ResultSet rs = dbManager.executeQuery(sql);
            populateTableFromRS(rs);
        } catch (SQLException e) {
            setStatus("Query error: " + e.getMessage(), false);
        }
    }

    private void populateTableFromRS(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();

        // Build column names
        String[] columns = new String[colCount];
        for (int i = 1; i <= colCount; i++) {
            columns[i - 1] = meta.getColumnLabel(i);
        }

        // Build row data
        List<Object[]> rows = new ArrayList<>();
        while (rs.next()) {
            Object[] row = new Object[colCount];
            for (int i = 1; i <= colCount; i++) {
                row[i - 1] = rs.getObject(i);
            }
            rows.add(row);
        }

        int rowCount = rows.size();
        SwingUtilities.invokeLater(() -> {
            resultsModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int r, int c) { return false; }
            };
            for (Object[] row : rows) {
                resultsModel.addRow(row);
            }
            resultsTable.setModel(resultsModel);
            rowCountLabel.setText(rowCount + " row" + (rowCount != 1 ? "s" : ""));
            setStatus("Query executed successfully — " + rowCount + " row(s) returned", true);
        });
    }

    private void setStatus(String text, boolean success) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(text);
            statusLabel.setForeground(success ? UITheme.STATUS_AVAILABLE : UITheme.STATUS_CRITICAL);
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ENSURE STORED PROCEDURES EXIST
    // ═══════════════════════════════════════════════════════════════════════

    private void ensureStoredProcedures() {
        // Pre-create the view so "Query View" button works even if user
        // hasn't clicked "Create View" yet
        try {
            dbManager.getConnection().createStatement().execute(
                "CREATE OR REPLACE VIEW critical_accidents_view AS " +
                "SELECT accident_id, location, severity, vehicle_id, accident_time, reported_by, status " +
                "FROM accidents WHERE severity IN ('Critical', 'High')");
        } catch (SQLException e) {
            System.err.println("[SQLOperationsPanel] Note: Could not pre-create view: " + e.getMessage());
        }
    }
}
