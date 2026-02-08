import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * 🚨 EMERGENCY RESPONSE UI THEME 🚨
 * Premium Design System for AI Accident Detection & Ambulance Dispatch
 * 
 * A stunning, modern interface with:
 * - Rich gradients and glowing effects
 * - Emergency-themed color palette
 * - Animated status indicators
 * - Professional medical aesthetics
 * - Corporate-ready polish
 * 
 * FULLY COMPATIBLE WITH MAIN.JAVA
 */
public class UITheme {

    // ═══════════════════════════════════════════════════════════════════════════
    // 🎨 EMERGENCY RESPONSE COLOR PALETTE - Stunning & Professional
    // ═══════════════════════════════════════════════════════════════════════════

    /** Deep midnight background - premium dark */
    public static final Color DARK_BG = new Color(10, 12, 16);

    /** Card background with depth */
    public static final Color CARD_BG = new Color(17, 20, 26);

    /** Elevated panel background */
    public static final Color PANEL_BG = new Color(24, 28, 36);

    /** Surface elevation */
    public static final Color SURFACE_BG = new Color(32, 37, 48);

    /** Premium gradient start - electric blue */
    public static final Color GRADIENT_START = new Color(59, 130, 246);

    /** Premium gradient end - purple */
    public static final Color GRADIENT_END = new Color(147, 51, 234);

    /** Primary accent - vibrant cyan */
    public static final Color ACCENT = new Color(6, 182, 212);

    /** Accent hover - bright cyan */
    public static final Color ACCENT_HOVER = new Color(34, 211, 238);

    /** Accent glow effect */
    public static final Color ACCENT_GLOW = new Color(6, 182, 212, 80);

    /** Purple accent - for special actions */
    public static final Color ACCENT_PURPLE = new Color(139, 92, 246);
    public static final Color ACCENT_PURPLE_GLOW = new Color(139, 92, 246, 60);

    /** 🟢 AVAILABLE - Vibrant emerald */
    public static final Color STATUS_AVAILABLE = new Color(16, 185, 129);
    public static final Color STATUS_AVAILABLE_GLOW = new Color(16, 185, 129, 60);

    /** 🟡 DISPATCHED - Brilliant amber */
    public static final Color STATUS_DISPATCHED = new Color(251, 191, 36);
    public static final Color STATUS_DISPATCHED_GLOW = new Color(251, 191, 36, 60);

    /** 🔴 CRITICAL - Intense red */
    public static final Color STATUS_CRITICAL = new Color(239, 68, 68);
    public static final Color STATUS_CRITICAL_GLOW = new Color(239, 68, 68, 80);

    /** 🔵 EN ROUTE - Deep blue */
    public static final Color STATUS_ENROUTE = new Color(59, 130, 246);
    public static final Color STATUS_ENROUTE_GLOW = new Color(59, 130, 246, 60);

    /** ⚕️ MEDICAL - Cross red */
    public static final Color MEDICAL_RED = new Color(220, 38, 38);
    public static final Color MEDICAL_RED_GLOW = new Color(220, 38, 38, 70);

    /** Warning - Bright orange */
    public static final Color WARNING = new Color(249, 115, 22);
    public static final Color WARNING_GLOW = new Color(249, 115, 22, 60);

    /** Success - Fresh green */
    public static final Color SUCCESS = new Color(34, 197, 94);
    public static final Color SUCCESS_GLOW = new Color(34, 197, 94, 60);

    /** Info - Sky blue */
    public static final Color INFO = new Color(56, 189, 248);
    public static final Color INFO_GLOW = new Color(56, 189, 248, 60);

    /** Primary text - crisp white */
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252);

    /** Secondary text - soft gray */
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184);

    /** Muted text */
    public static final Color TEXT_MUTED = new Color(100, 116, 139);

    /** Tertiary text (alias for muted) */
    public static final Color TEXT_TERTIARY = TEXT_MUTED;

    /** Border - subtle */
    public static final Color BORDER = new Color(51, 65, 85);

    /** Border color (alias for compatibility) */
    public static final Color BORDER_COLOR = BORDER;

    /** Border bright */
    public static final Color BORDER_BRIGHT = new Color(71, 85, 105);

    /** Border hover */
    public static final Color BORDER_HOVER = BORDER_BRIGHT;

    /** Input background */
    public static final Color INPUT_BG = new Color(15, 23, 42);

    /** Input focused background */
    public static final Color INPUT_FOCUSED_BG = new Color(20, 28, 47);

    /** Overlay dark */
    public static final Color OVERLAY = new Color(0, 0, 0, 180);

    /** Glass effect */
    public static final Color GLASS = new Color(255, 255, 255, 10);

    /** Shadow deep */
    public static final Color SHADOW_DEEP = new Color(0, 0, 0, 100);

    /** Shadow medium */
    public static final Color SHADOW_MEDIUM = new Color(0, 0, 0, 60);

    /** Shadow light */
    public static final Color SHADOW_LIGHT = new Color(0, 0, 0, 30);

    /** Shadow for compatibility */
    public static final Color SHADOW = SHADOW_MEDIUM;

    // ═══════════════════════════════════════════════════════════════════════════
    // ✍️ PREMIUM TYPOGRAPHY - Crystal Clear Hierarchy
    // ═══════════════════════════════════════════════════════════════════════════

    public static final String FONT_PRIMARY = "Segoe UI";
    public static final String FONT_FAMILY = FONT_PRIMARY;
    public static final String FONT_FALLBACK = FONT_PRIMARY;
    public static final String FONT_MONO = "Consolas";
    public static final String FONT_MONO_FALLBACK = FONT_MONO;

    /** Hero display */
    public static final Font FONT_HERO = new Font(FONT_PRIMARY, Font.BOLD, 42);

    /** Large title */
    public static final Font FONT_DISPLAY = new Font(FONT_PRIMARY, Font.BOLD, 36);

    /** Page title */
    public static final Font FONT_TITLE = new Font(FONT_PRIMARY, Font.BOLD, 28);

    /** Section header */
    public static final Font FONT_HEADER = new Font(FONT_PRIMARY, Font.BOLD, 20);

    /** Subheader */
    public static final Font FONT_SUBHEADER = new Font(FONT_PRIMARY, Font.BOLD, 16);

    /** Panel title */
    public static final Font FONT_PANEL_TITLE = FONT_SUBHEADER;

    /** Subtitle */
    public static final Font FONT_SUBTITLE = new Font(FONT_PRIMARY, Font.PLAIN, 14);

    /** Body text */
    public static final Font FONT_BODY = new Font(FONT_PRIMARY, Font.PLAIN, 15);

    /** Body medium */
    public static final Font FONT_BODY_MEDIUM = new Font(FONT_PRIMARY, Font.PLAIN, 14);

    /** Small text */
    public static final Font FONT_SMALL = new Font(FONT_PRIMARY, Font.PLAIN, 13);

    /** Tiny text */
    public static final Font FONT_TINY = new Font(FONT_PRIMARY, Font.PLAIN, 11);

    /** Extra small text (alias for FONT_TINY) */
    public static final Font FONT_XSMALL = FONT_TINY;

    /** Button text */
    public static final Font FONT_BUTTON = new Font(FONT_PRIMARY, Font.BOLD, 14);

    /** Badge text */
    public static final Font FONT_BADGE = new Font(FONT_PRIMARY, Font.BOLD, 12);

    /** Stats/Numbers - Large */
    public static final Font FONT_STATS_LARGE = new Font(FONT_MONO, Font.BOLD, 48);

    /** Stats/Numbers - Medium */
    public static final Font FONT_STATS_MEDIUM = new Font(FONT_MONO, Font.BOLD, 32);

    /** Stats/Numbers - Small */
    public static final Font FONT_STATS_SMALL = new Font(FONT_MONO, Font.BOLD, 20);

    /** Countdown timer */
    public static final Font FONT_COUNTDOWN = FONT_STATS_MEDIUM;

    /** Table header */
    public static final Font FONT_TABLE_HEADER = new Font(FONT_PRIMARY, Font.BOLD, 12);

    /** Table body */
    public static final Font FONT_TABLE = new Font(FONT_PRIMARY, Font.PLAIN, 14);

    /** Code/console text */
    public static final Font FONT_CODE = new Font(FONT_MONO, Font.PLAIN, 12);

    // ═══════════════════════════════════════════════════════════════════════════
    // 📏 REFINED SPACING SYSTEM - Perfect Rhythm
    // ═══════════════════════════════════════════════════════════════════════════

    public static final int SPACE_XXS = 2;
    public static final int SPACE_XS = 4;
    public static final int SPACE_SM = 8;
    public static final int SPACE_MD = 12;
    public static final int SPACE_LG = 16;
    public static final int SPACE_XL = 24;
    public static final int SPACE_2XL = 32;
    public static final int SPACE_3XL = 48;
    public static final int SPACE_4XL = 64;

    /** Standard padding */
    public static final int PADDING = SPACE_XL;

    public static final int RADIUS_SM = 6;
    public static final int RADIUS_MD = 10;
    public static final int RADIUS_LG = 14;
    public static final int RADIUS_XL = 18;
    public static final int RADIUS_2XL = 24;
    public static final int RADIUS_FULL = 9999;

    /** Border radius (default) */
    public static final int BORDER_RADIUS = RADIUS_MD;
    public static final int BORDER_RADIUS_LARGE = RADIUS_LG;
    public static final int BORDER_RADIUS_XL = RADIUS_XL;

    public static final int BUTTON_HEIGHT = 44;
    public static final int BUTTON_HEIGHT_SM = 36;
    public static final int BUTTON_HEIGHT_SMALL = BUTTON_HEIGHT_SM;
    public static final int BUTTON_HEIGHT_LG = 52;
    public static final int BUTTON_HEIGHT_LARGE = BUTTON_HEIGHT_LG;

    public static final int INPUT_HEIGHT = 48;
    public static final int TABLE_ROW_HEIGHT = 48;

    // ═══════════════════════════════════════════════════════════════════════════
    // 🎨 PREMIUM UI COMPONENTS - Visually Stunning
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * 🌟 Create a GRADIENT PRIMARY button with glow effect
     */
    public static JButton createPrimaryButton(String text) {
        return createPrimaryButton(text, null);
    }

    public static JButton createPrimaryButton(String text, String icon) {
        JButton button = new JButton(icon != null ? icon + " " + text : text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();

                // Glow effect
                if (getModel().isRollover()) {
                    g2d.setColor(ACCENT_GLOW);
                    g2d.fillRoundRect(-2, -2, w + 4, h + 4, RADIUS_LG + 2, RADIUS_LG + 2);
                }

                // Shadow
                g2d.setColor(SHADOW_MEDIUM);
                g2d.fillRoundRect(0, 3, w, h, RADIUS_LG, RADIUS_LG);

                // Gradient background
                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, GRADIENT_START.darker(),
                            w, h, GRADIENT_END.darker());
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, GRADIENT_START.brighter(),
                            w, h, GRADIENT_END.brighter());
                } else {
                    gradient = new GradientPaint(0, 0, GRADIENT_START, w, h, GRADIENT_END);
                }
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, w, h - 3, RADIUS_LG, RADIUS_LG);

                // Glass overlay
                g2d.setColor(GLASS);
                g2d.fillRoundRect(0, 0, w, h / 2, RADIUS_LG, RADIUS_LG);

                // Text with shadow
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String btnText = getText();
                int textX = (w - fm.stringWidth(btnText)) / 2;
                int textY = (h + fm.getAscent() - fm.getDescent()) / 2 - 1;

                // Text shadow
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(btnText, textX, textY + 2);

                // Text
                g2d.setColor(Color.WHITE);
                g2d.drawString(btnText, textX, textY);

                g2d.dispose();
            }
        };

        styleButton(button);
        button.setPreferredSize(new Dimension(160, BUTTON_HEIGHT));
        return button;
    }

    /**
     * ⚠️ Create CRITICAL/EMERGENCY button (pulsing red)
     */
    public static JButton createCriticalButton(String text, String icon) {
        JButton button = new JButton(icon != null ? icon + " " + text : text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();

                // Critical glow
                g2d.setColor(STATUS_CRITICAL_GLOW);
                g2d.fillRoundRect(-3, -3, w + 6, h + 6, RADIUS_LG + 3, RADIUS_LG + 3);

                // Shadow
                g2d.setColor(SHADOW_DEEP);
                g2d.fillRoundRect(0, 4, w, h, RADIUS_LG, RADIUS_LG);

                // Background
                Color bgColor = getModel().isPressed() ? STATUS_CRITICAL.darker()
                        : getModel().isRollover() ? STATUS_CRITICAL.brighter() : STATUS_CRITICAL;
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, w, h - 4, RADIUS_LG, RADIUS_LG);

                // Glass effect
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRoundRect(0, 0, w, h / 2, RADIUS_LG, RADIUS_LG);

                // Text
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String btnText = getText();
                int textX = (w - fm.stringWidth(btnText)) / 2;
                int textY = (h + fm.getAscent() - fm.getDescent()) / 2 - 2;

                g2d.setColor(new Color(0, 0, 0, 120));
                g2d.drawString(btnText, textX, textY + 2);
                g2d.setColor(Color.WHITE);
                g2d.drawString(btnText, textX, textY);

                g2d.dispose();
            }
        };

        styleButton(button);
        button.setPreferredSize(new Dimension(180, BUTTON_HEIGHT_LG));
        return button;
    }

    /**
     * 🟢 Create SUCCESS button (green) - single param
     */
    public static JButton createSuccessButton(String text) {
        return createSuccessButton(text, null);
    }

    /**
     * 🟢 Create SUCCESS button (green)
     */
    public static JButton createSuccessButton(String text, String icon) {
        return createColoredButton(text, icon, STATUS_AVAILABLE, STATUS_AVAILABLE_GLOW);
    }

    /**
     * 🟡 Create WARNING button (amber) - single param
     */
    public static JButton createWarningButton(String text) {
        return createWarningButton(text, null);
    }

    /**
     * 🟡 Create WARNING button (amber)
     */
    public static JButton createWarningButton(String text, String icon) {
        return createColoredButton(text, icon, STATUS_DISPATCHED, STATUS_DISPATCHED_GLOW);
    }

    /**
     * 🔴 Create DANGER button (red) - single param
     */
    public static JButton createDangerButton(String text) {
        return createDangerButton(text, null);
    }

    /**
     * 🔴 Create DANGER button (red)
     */
    public static JButton createDangerButton(String text, String icon) {
        return createColoredButton(text, icon, STATUS_CRITICAL, STATUS_CRITICAL_GLOW);
    }

    /**
     * Create a colored button with glow
     */
    public static JButton createColoredButton(String text, Color color) {
        return createColoredButton(text, null, color, new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
    }

    /**
     * Create a colored button with glow and icon
     */
    private static JButton createColoredButton(String text, String icon, Color color, Color glow) {
        JButton button = new JButton(icon != null ? icon + " " + text : text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();

                // Glow
                if (getModel().isRollover()) {
                    g2d.setColor(glow);
                    g2d.fillRoundRect(-2, -2, w + 4, h + 4, RADIUS_LG + 2, RADIUS_LG + 2);
                }

                // Shadow
                g2d.setColor(SHADOW_MEDIUM);
                g2d.fillRoundRect(0, 3, w, h, RADIUS_LG, RADIUS_LG);

                // Background
                Color bgColor = getModel().isPressed() ? color.darker()
                        : getModel().isRollover() ? color.brighter() : color;
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, w, h - 3, RADIUS_LG, RADIUS_LG);

                // Glass
                g2d.setColor(new Color(255, 255, 255, 15));
                g2d.fillRoundRect(0, 0, w, h / 2, RADIUS_LG, RADIUS_LG);

                // Text
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String btnText = getText();
                int textX = (w - fm.stringWidth(btnText)) / 2;
                int textY = (h + fm.getAscent() - fm.getDescent()) / 2 - 1;

                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.drawString(btnText, textX, textY + 1);
                g2d.setColor(Color.WHITE);
                g2d.drawString(btnText, textX, textY);

                g2d.dispose();
            }
        };

        styleButton(button);
        return button;
    }

    /**
     * Create outlined/ghost button
     */
    public static JButton createOutlinedButton(String text, String icon) {
        JButton button = new JButton(icon != null ? icon + " " + text : text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();

                // Background on hover
                if (getModel().isRollover()) {
                    g2d.setColor(SURFACE_BG);
                    g2d.fillRoundRect(0, 0, w, h, RADIUS_LG, RADIUS_LG);
                }

                // Border
                g2d.setColor(getModel().isRollover() ? ACCENT : BORDER_BRIGHT);
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(1, 1, w - 3, h - 3, RADIUS_LG, RADIUS_LG);

                // Text
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String btnText = getText();
                int textX = (w - fm.stringWidth(btnText)) / 2;
                int textY = (h + fm.getAscent() - fm.getDescent()) / 2;

                g2d.setColor(getModel().isRollover() ? ACCENT : TEXT_SECONDARY);
                g2d.drawString(btnText, textX, textY);

                g2d.dispose();
            }
        };

        styleButton(button);
        return button;
    }

    /**
     * 🔘 SECONDARY BUTTON (alias for createOutlinedButton)
     */
    public static JButton createSecondaryButton(String text) {
        return createOutlinedButton(text, null);
    }

    /**
     * 👻 GHOST BUTTON (alias for createOutlinedButton)
     */
    public static JButton createGhostButton(String text) {
        return createOutlinedButton(text, null);
    }

    /**
     * Apply button styling
     */
    private static void styleButton(JButton button) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(FONT_BUTTON);
        button.setForeground(TEXT_PRIMARY);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, BUTTON_HEIGHT));
    }

    /**
     * 📝 Create modern text field with smooth animations
     */
    public static JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
            private boolean isFocused = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();

                // Glow when focused
                if (isFocused) {
                    g2d.setColor(ACCENT_GLOW);
                    g2d.fillRoundRect(-1, -1, w + 2, h + 2, RADIUS_MD + 1, RADIUS_MD + 1);
                }

                // Background
                g2d.setColor(INPUT_BG);
                g2d.fillRoundRect(0, 0, w, h, RADIUS_MD, RADIUS_MD);

                // Border
                g2d.setStroke(new BasicStroke(2f));
                g2d.setColor(isFocused ? ACCENT : BORDER);
                g2d.drawRoundRect(1, 1, w - 3, h - 3, RADIUS_MD, RADIUS_MD);

                super.paintComponent(g);

                // Placeholder
                if (getText().isEmpty() && !isFocused) {
                    g2d.setColor(TEXT_MUTED);
                    g2d.setFont(getFont());
                    g2d.drawString(placeholder, SPACE_LG, h / 2 + 5);
                }

                g2d.dispose();
            }

            @Override
            protected void processFocusEvent(java.awt.event.FocusEvent e) {
                super.processFocusEvent(e);
                isFocused = (e.getID() == java.awt.event.FocusEvent.FOCUS_GAINED);
                repaint();
            }
        };

        styleTextField(field);
        return field;
    }

    /**
     * Create password field
     */
    public static JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            private boolean isFocused = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();

                // Glow when focused
                if (isFocused) {
                    g2d.setColor(ACCENT_GLOW);
                    g2d.fillRoundRect(-1, -1, w + 2, h + 2, RADIUS_MD + 1, RADIUS_MD + 1);
                }

                // Background
                g2d.setColor(INPUT_BG);
                g2d.fillRoundRect(0, 0, w, h, RADIUS_MD, RADIUS_MD);

                // Border
                g2d.setStroke(new BasicStroke(2f));
                g2d.setColor(isFocused ? ACCENT : BORDER);
                g2d.drawRoundRect(1, 1, w - 3, h - 3, RADIUS_MD, RADIUS_MD);

                super.paintComponent(g);

                // Placeholder
                if (getPassword().length == 0 && !isFocused) {
                    g2d.setColor(TEXT_MUTED);
                    g2d.setFont(getFont());
                    g2d.drawString(placeholder, SPACE_LG, h / 2 + 5);
                }

                g2d.dispose();
            }

            @Override
            protected void processFocusEvent(java.awt.event.FocusEvent e) {
                super.processFocusEvent(e);
                isFocused = (e.getID() == java.awt.event.FocusEvent.FOCUS_GAINED);
                repaint();
            }
        };

        styleTextField(field);
        return field;
    }

    /**
     * Apply text field styling
     */
    private static void styleTextField(JTextField field) {
        field.setOpaque(false);
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT);
        field.setBorder(new EmptyBorder(SPACE_MD, SPACE_LG, SPACE_MD, SPACE_LG));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_HEIGHT));
        field.setPreferredSize(new Dimension(350, INPUT_HEIGHT));
    }

    /**
     * Create a label with secondary text
     */
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SMALL);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    /**
     * 🎴 Create PREMIUM CARD with gradient border and glass effect
     */
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();

                // Shadow - large and diffused
                g2d.setColor(SHADOW_MEDIUM);
                g2d.fillRoundRect(2, 6, w - 4, h - 4, RADIUS_XL, RADIUS_XL);

                // Card background
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, w, h, RADIUS_XL, RADIUS_XL);

                // Gradient border
                GradientPaint borderGradient = new GradientPaint(
                        0, 0, new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 80),
                        w, h, new Color(GRADIENT_END.getRed(), GRADIENT_END.getGreen(), GRADIENT_END.getBlue(), 80));
                g2d.setPaint(borderGradient);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, w - 3, h - 3, RADIUS_XL, RADIUS_XL);

                // Glass highlight
                g2d.setColor(GLASS);
                g2d.fillRoundRect(0, 0, w, h / 3, RADIUS_XL, RADIUS_XL);

                g2d.dispose();
            }
        };

        card.setOpaque(false);
        card.setBorder(new EmptyBorder(SPACE_XL, SPACE_XL, SPACE_XL, SPACE_XL));
        return card;
    }

    /**
     * Create simple panel
     */
    public static JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL_BG);
        panel.setBorder(new EmptyBorder(SPACE_LG, SPACE_LG, SPACE_LG, SPACE_LG));
        return panel;
    }

    /**
     * 🏷️ Create HEADER LABEL - Section header styling
     */
    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_HEADER);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    /**
     * 🏷️ Create TITLE LABEL - Large title styling
     */
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    /**
     * 📊 Create STATUS CARD with icon and stats
     */
    public static JPanel createStatusCard(String title, String value, String icon, Color color) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();

                // Shadow
                g2d.setColor(SHADOW_LIGHT);
                g2d.fillRoundRect(2, 4, w - 4, h - 4, RADIUS_LG, RADIUS_LG);

                // Background
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, w, h, RADIUS_LG, RADIUS_LG);

                // Colored left border
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, 6, h, RADIUS_LG, RADIUS_LG);

                // Icon background circle with glow
                int iconX = 20;
                int iconY = h / 2 - 24;
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
                g2d.fillRoundRect(iconX - 4, iconY - 4, 56, 56, RADIUS_FULL, RADIUS_FULL);
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
                g2d.fillRoundRect(iconX, iconY, 48, 48, RADIUS_FULL, RADIUS_FULL);

                // Icon
                g2d.setColor(color);
                g2d.setFont(new Font(FONT_PRIMARY, Font.PLAIN, 28));
                g2d.drawString(icon, iconX + 10, iconY + 34);

                // Title
                g2d.setColor(TEXT_SECONDARY);
                g2d.setFont(FONT_SMALL);
                g2d.drawString(title, 90, h / 2 - 8);

                // Value
                g2d.setColor(TEXT_PRIMARY);
                g2d.setFont(FONT_STATS_SMALL);
                g2d.drawString(value, 90, h / 2 + 24);

                g2d.dispose();
            }
        };

        card.setOpaque(false);
        card.setPreferredSize(new Dimension(280, 90));
        card.setLayout(null);

        return card;
    }

    /**
     * 🏥 Create EMERGENCY ALERT PANEL
     */
    public static JPanel createEmergencyAlert(String message, boolean isPulsing) {
        JPanel alert = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();

                // Pulsing glow effect
                if (isPulsing) {
                    g2d.setColor(STATUS_CRITICAL_GLOW);
                    g2d.fillRoundRect(-4, -4, w + 8, h + 8, RADIUS_LG + 4, RADIUS_LG + 4);
                }

                // Background gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0,
                        new Color(STATUS_CRITICAL.getRed(), STATUS_CRITICAL.getGreen(), STATUS_CRITICAL.getBlue(), 40),
                        0, h,
                        new Color(STATUS_CRITICAL.getRed(), STATUS_CRITICAL.getGreen(), STATUS_CRITICAL.getBlue(), 20));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, w, h, RADIUS_LG, RADIUS_LG);

                // Border
                g2d.setColor(STATUS_CRITICAL);
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(1, 1, w - 3, h - 3, RADIUS_LG, RADIUS_LG);

                // Warning stripe pattern (optional)
                g2d.setColor(
                        new Color(STATUS_CRITICAL.getRed(), STATUS_CRITICAL.getGreen(), STATUS_CRITICAL.getBlue(), 30));
                for (int i = 0; i < w; i += 20) {
                    g2d.fillRect(i, 0, 10, h);
                }

                g2d.dispose();
            }
        };

        alert.setOpaque(false);
        alert.setLayout(new BorderLayout(SPACE_MD, 0));
        alert.setBorder(new EmptyBorder(SPACE_LG, SPACE_XL, SPACE_LG, SPACE_XL));

        // Add icon
        JLabel iconLabel = new JLabel("🚨");
        iconLabel.setFont(new Font(FONT_PRIMARY, Font.PLAIN, 24));
        iconLabel.setForeground(STATUS_CRITICAL);
        alert.add(iconLabel, BorderLayout.WEST);

        // Add message
        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(FONT_BODY_MEDIUM);
        msgLabel.setForeground(TEXT_PRIMARY);
        alert.add(msgLabel, BorderLayout.CENTER);

        return alert;
    }

    /**
     * 📱 Create INFO BADGE (pills)
     */
    public static JLabel createBadge(String text, Color color, String icon) {
        JLabel badge = new JLabel(icon != null ? icon + " " + text : text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 25));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS_FULL, RADIUS_FULL);

                // Border
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, RADIUS_FULL, RADIUS_FULL);

                super.paintComponent(g);
                g2d.dispose();
            }
        };

        badge.setFont(FONT_BADGE);
        badge.setForeground(color);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(SPACE_XS, SPACE_LG, SPACE_XS, SPACE_LG));
        badge.setHorizontalAlignment(SwingConstants.CENTER);

        return badge;
    }

    /**
     * 🎯 Create STATUS INDICATOR (pulsing dot)
     */
    public static JPanel createStatusIndicator(String status, String label) {
        Color color = getStatusColor(status);
        Color glow = getStatusGlowColor(status);

        JPanel indicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Pulsing glow
                g2d.setColor(glow);
                g2d.fillOval(2, 5, 16, 16);

                // Dot
                g2d.setColor(color);
                g2d.fillOval(5, 8, 10, 10);

                g2d.dispose();
            }
        };

        indicator.setOpaque(false);
        indicator.setLayout(new FlowLayout(FlowLayout.LEFT, SPACE_SM, 0));
        indicator.setPreferredSize(new Dimension(150, 28));

        // Add spacer for the dot
        JLabel spacer = new JLabel("      ");
        indicator.add(spacer);

        // Label
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(FONT_BODY_MEDIUM);
        labelComp.setForeground(TEXT_PRIMARY);
        indicator.add(labelComp);

        return indicator;
    }

    /**
     * 📋 Create PREMIUM TABLE
     */
    public static JTable createStyledTable(javax.swing.table.DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                if (!isRowSelected(row)) {
                    // Alternating rows with subtle difference
                    c.setBackground(row % 2 == 0 ? CARD_BG : new Color(20, 24, 32));
                } else {
                    // Selected row with accent color
                    c.setBackground(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 35));
                }

                // Add padding effect
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(new EmptyBorder(SPACE_SM, SPACE_MD, SPACE_SM, SPACE_MD));
                }

                return c;
            }
        };

        table.setBackground(CARD_BG);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(FONT_TABLE);
        table.setRowHeight(TABLE_ROW_HEIGHT);
        table.setShowGrid(true);
        table.setGridColor(new Color(BORDER.getRed(), BORDER.getGreen(), BORDER.getBlue(), 50));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 40));
        table.setSelectionForeground(TEXT_PRIMARY);

        // Header
        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setBackground(PANEL_BG);
        header.setForeground(TEXT_SECONDARY);
        header.setFont(FONT_TABLE_HEADER);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_BRIGHT));
        header.setPreferredSize(new Dimension(header.getWidth(), 44));

        return table;
    }

    /**
     * Style scroll pane
     */
    public static void styleScrollPane(JScrollPane scroll) {
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setBackground(CARD_BG);

        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
    }

    /**
     * Modern scrollbar
     */
    static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = BORDER_BRIGHT;
            this.trackColor = CARD_BG;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(thumbColor);
            g2d.fillRoundRect(thumbBounds.x + 3, thumbBounds.y + 3,
                    thumbBounds.width - 6, thumbBounds.height - 6, 8, 8);
            g2d.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            // Invisible track
        }
    }

    /**
     * Create titled section panel
     */
    public static JPanel createSection(String title, String icon) {
        JPanel section = new JPanel();
        section.setLayout(new BorderLayout(0, SPACE_LG));
        section.setOpaque(false);

        // Title bar
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, SPACE_MD, 0));
        titleBar.setOpaque(false);

        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font(FONT_PRIMARY, Font.PLAIN, 20));
            titleBar.add(iconLabel);
        }

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleBar.add(titleLabel);

        section.add(titleBar, BorderLayout.NORTH);

        return section;
    }

    /**
     * Create divider
     */
    public static JSeparator createDivider() {
        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER_COLOR);
        separator.setBackground(BORDER_COLOR);
        return separator;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🎨 UTILITY METHODS - Colors & Icons
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get status color
     */
    public static Color getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "available":
            case "green":
                return STATUS_AVAILABLE;
            case "dispatched":
            case "yellow":
                return STATUS_DISPATCHED;
            case "critical":
            case "emergency":
            case "red":
                return STATUS_CRITICAL;
            case "enroute":
            case "blue":
                return STATUS_ENROUTE;
            default:
                return TEXT_SECONDARY;
        }
    }

    /**
     * Get status glow color
     */
    public static Color getStatusGlowColor(String status) {
        switch (status.toLowerCase()) {
            case "available":
            case "green":
                return STATUS_AVAILABLE_GLOW;
            case "dispatched":
            case "yellow":
                return STATUS_DISPATCHED_GLOW;
            case "critical":
            case "emergency":
            case "red":
                return STATUS_CRITICAL_GLOW;
            case "enroute":
            case "blue":
                return STATUS_ENROUTE_GLOW;
            default:
                return SHADOW_LIGHT;
        }
    }

    /**
     * Get severity icon
     */
    public static String getSeverityIcon(String severity) {
        switch (severity) {
            case "Critical":
                return "🔴";
            case "High":
                return "🟠";
            case "Medium":
                return "🟡";
            case "Low":
                return "🟢";
            default:
                return "⚪";
        }
    }

    /**
     * Get severity color
     */
    public static Color getSeverityColor(String severity) {
        switch (severity) {
            case "Critical":
                return STATUS_CRITICAL;
            case "High":
                return new Color(251, 146, 60);
            case "Medium":
                return STATUS_DISPATCHED;
            case "Low":
                return STATUS_AVAILABLE;
            default:
                return TEXT_SECONDARY;
        }
    }

    /**
     * Get ambulance status icon
     */
    public static String getAmbulanceStatusIcon(String status) {
        switch (status) {
            case "green":
                return "🟢 Available";
            case "yellow":
                return "🟡 Dispatched";
            case "red":
                return "🔴 On Scene";
            case "blue":
                return "🔵 En Route";
            default:
                return "⚪ Unknown";
        }
    }

    /**
     * Emergency icons
     */
    public static final String ICON_AMBULANCE = "🚑";
    public static final String ICON_HOSPITAL = "🏥";
    public static final String ICON_EMERGENCY = "🚨";
    public static final String ICON_LOCATION = "📍";
    public static final String ICON_TIME = "⏱️";
    public static final String ICON_ALERT = "⚠️";
    public static final String ICON_CHECK = "✓";
    public static final String ICON_MAP = "🗺️";
    public static final String ICON_PHONE = "📞";
    public static final String ICON_PERSON = "👤";
    public static final String ICON_MEDICAL = "⚕️";
    public static final String ICON_HEARTBEAT = "💓";
    public static final String ICON_SIREN = "🔔";
}