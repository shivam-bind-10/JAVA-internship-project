package com.studentexpense.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern styling utilities, currency formatting (₹ Rupees),
 * and component factories for Java Swing UI.
 */
public class ModernUIUtils {

    // Currency constant
    public static final String CURRENCY = "₹";

    public static String formatCurrency(double amount) {
        return String.format("₹%.2f", amount);
    }

    // Color Palette
    public static final Color COLOR_PRIMARY = new Color(37, 99, 235);      // #2563EB Royal Blue
    public static final Color COLOR_PRIMARY_HOVER = new Color(29, 78, 216); // #1D4ED8
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);     // #10B981 Emerald Green
    public static final Color COLOR_SUCCESS_HOVER = new Color(5, 150, 105);
    public static final Color COLOR_DANGER = new Color(239, 68, 68);       // #EF4444 Rose Red
    public static final Color COLOR_DANGER_HOVER = new Color(220, 38, 38);
    public static final Color COLOR_WARNING = new Color(245, 158, 11);     // #F59E0B Amber
    public static final Color COLOR_BG = new Color(248, 250, 252);         // Slate 50
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_BORDER = new Color(226, 232, 240);     // Slate 200
    public static final Color COLOR_TEXT_DARK = new Color(15, 23, 42);     // Slate 900
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139); // Slate 500
    public static final Color COLOR_ROW_ALT = new Color(248, 250, 252);
    public static final Color COLOR_ROW_SEL = new Color(224, 231, 255);

    // Sidebar Specific Colors
    public static final Color COLOR_SIDEBAR_BG = new Color(15, 23, 42);        // Slate 900
    public static final Color COLOR_SIDEBAR_TEXT = new Color(226, 232, 240);   // Slate 200
    public static final Color COLOR_SIDEBAR_HOVER = new Color(30, 41, 59);     // Slate 800
    public static final Color COLOR_SIDEBAR_ACTIVE = new Color(37, 99, 235);   // Royal Blue

    // Fonts
    public static final String FONT_NAME = "Segoe UI";
    public static final Font FONT_TITLE = new Font(FONT_NAME, Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font(FONT_NAME, Font.BOLD, 14);
    public static final Font FONT_BODY = new Font(FONT_NAME, Font.PLAIN, 12);
    public static final Font FONT_BODY_BOLD = new Font(FONT_NAME, Font.BOLD, 12);
    public static final Font FONT_SMALL = new Font(FONT_NAME, Font.PLAIN, 11);
    public static final Font FONT_METRIC = new Font(FONT_NAME, Font.BOLD, 22);
    public static final Font FONT_NAV = new Font(FONT_NAME, Font.BOLD, 13);

    /**
     * Creates a rounded card panel with shadow/border.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD_BG);
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 14, 14));
                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 14, 14));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        return panel;
    }

    /**
     * Creates a customized modern button.
     */
    public static JButton createCustomButton(String text, Color bg, Color hoverBg, Color textCol) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hoverBg : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(textCol);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static JButton createPrimaryButton(String text) {
        return createCustomButton(text, COLOR_PRIMARY, COLOR_PRIMARY_HOVER, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        return createCustomButton(text, new Color(241, 245, 249), new Color(226, 232, 240), COLOR_TEXT_DARK);
    }

    public static JButton createSuccessButton(String text) {
        return createCustomButton(text, COLOR_SUCCESS, COLOR_SUCCESS_HOVER, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createCustomButton(text, COLOR_DANGER, COLOR_DANGER_HOVER, Color.WHITE);
    }

    /**
     * Creates a modern sidebar navigation button with active state styling.
     */
    public static JButton createSidebarNavButton(String text, boolean active) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isSelected = "true".equals(getClientProperty("active"));
                if (isSelected) {
                    g2.setColor(COLOR_SIDEBAR_ACTIVE);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                } else if (getModel().isRollover()) {
                    g2.setColor(COLOR_SIDEBAR_HOVER);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_NAV);
        btn.putClientProperty("active", active ? "true" : "false");
        btn.setForeground(active ? Color.WHITE : COLOR_SIDEBAR_TEXT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return btn;
    }

    /**
     * Creates a KPI metric display card in Rupees.
     */
    public static JPanel createMetricCard(String title, String initialValue, String subtitle, Color accentColor, String icon) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(5, 5));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_BODY_BOLD);
        lblTitle.setForeground(COLOR_TEXT_MUTED);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(lblIcon, BorderLayout.EAST);

        JLabel lblValue = new JLabel(initialValue);
        lblValue.setFont(FONT_METRIC);
        lblValue.setForeground(accentColor);
        lblValue.setName("metricValue");

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(COLOR_TEXT_MUTED);
        lblSub.setName("metricSub");

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.add(lblValue);
        centerPanel.add(Box.createVerticalStrut(4));
        centerPanel.add(lblSub);

        card.add(topPanel, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Creates an inviting clean empty state panel when 0 data is present.
     */
    public static JPanel createEmptyStatePanel(String icon, String title, String subtitle, String actionText, Runnable onAction) {
        JPanel card = createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(FONT_BODY);
        lblSub.setForeground(COLOR_TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalStrut(20));
        card.add(lblIcon);
        card.add(Box.createVerticalStrut(10));
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(lblSub);

        if (actionText != null && onAction != null) {
            card.add(Box.createVerticalStrut(16));
            JButton btn = createPrimaryButton(actionText);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.addActionListener(e -> onAction.run());
            card.add(btn);
        }
        card.add(Box.createVerticalStrut(20));

        return card;
    }

    /**
     * Configures modern table styling with alternate rows and clean header.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setGridColor(COLOR_BORDER);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(COLOR_ROW_SEL);
        table.setSelectionForeground(COLOR_TEXT_DARK);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(COLOR_TEXT_DARK);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : COLOR_ROW_ALT);
                    c.setForeground(COLOR_TEXT_DARK);
                }
                return c;
            }
        });
    }

    public static JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    public static <T> JComboBox<T> createStyledComboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(Color.WHITE);
        return cb;
    }
}
