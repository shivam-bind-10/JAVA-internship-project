package com.studentexpense.ui;

import com.studentexpense.model.*;
import com.studentexpense.service.AnalyticsService;
import com.studentexpense.service.ExpenseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Clean, naive, and highly intuitive Main Application Window.
 * Displays prominent Total Income, Total Expense, and Net Balance KPI cards
 * at the top at all times, with a direct 4-tab interface below.
 */
public class MainFrame extends JFrame implements ExpenseManager.DataChangeListener {

    private final ExpenseManager expenseManager;

    // Top Summary KPI labels (always visible)
    private JLabel lblTotalIncome;
    private JLabel lblTotalExpense;
    private JLabel lblNetBalance;
    private JLabel lblHealthBadge;

    // Tabs
    private JTabbedPane tabbedPane;
    private TrackerPanel trackerPanel;
    private BudgetPanel budgetPanel;
    private SavingsGoalsPanel savingsGoalsPanel;
    private BillSplitterPanel billSplitterPanel;

    private JLabel lblStatusLeft;
    private JLabel lblStatusRight;

    public MainFrame(ExpenseManager expenseManager) {
        this.expenseManager = expenseManager;
        this.expenseManager.addListener(this);

        setTitle("Student Expense and Budget Management System (₹ Rupees)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);

        initUI();
        updateSummaryKpis();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, ModernUIUtils.COLOR_BG_START, 0, getHeight(), ModernUIUtils.COLOR_BG_END));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        rootPanel.setLayout(new BorderLayout(0, 12));
        rootPanel.setBorder(new EmptyBorder(12, 16, 10, 16));

        // --- 1. TOP APP BAR: Brand Title & Quick Utilities ---
        JPanel topAppBar = new JPanel(new BorderLayout(15, 0));
        topAppBar.setOpaque(false);
        topAppBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(12, 16, 12, 16)));
        topAppBar.setBackground(new Color(255, 255, 255, 190));

        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel lblLogo = new JLabel("🎓");
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));

        JPanel brandText = new JPanel(new GridLayout(3, 1, 0, 2));
        brandText.setOpaque(false);

        JLabel lblBadge = new JLabel("SMART MONEY FOR STUDENTS");
        lblBadge.setFont(new Font(ModernUIUtils.FONT_NAME, Font.BOLD, 10));
        lblBadge.setForeground(ModernUIUtils.COLOR_PRIMARY);
        lblBadge.setOpaque(true);
        lblBadge.setBackground(new Color(219, 234, 254));
        lblBadge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JLabel lblTitle = new JLabel("Student Expense & Budget Management System");
        lblTitle.setFont(new Font(ModernUIUtils.FONT_NAME, Font.BOLD, 18));
        lblTitle.setForeground(ModernUIUtils.COLOR_TEXT_DARK);

        JLabel lblSub = new JLabel("Simple, direct personal finance tracker for college students | All amounts in Indian Rupees (₹)");
        lblSub.setFont(ModernUIUtils.FONT_SMALL);
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        brandText.add(lblBadge);
        brandText.add(lblTitle);
        brandText.add(lblSub);

        brandPanel.add(lblLogo);
        brandPanel.add(brandText);

        JPanel topTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topTools.setOpaque(false);

        JButton btnDemo = ModernUIUtils.createPillButton("🔄 Load Demo Data", new Color(241, 245, 249), new Color(226, 232, 240), ModernUIUtils.COLOR_TEXT_DARK);
        btnDemo.setFont(ModernUIUtils.FONT_SMALL);
        btnDemo.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this,
                    "Populate the system with realistic student demo records in Rupees (₹)?",
                    "Load Demo Data", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                expenseManager.resetToSampleData();
                JOptionPane.showMessageDialog(this, "Sample student data loaded successfully!", "Data Loaded", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton btnClear = ModernUIUtils.createPillButton("🗑️ Reset to 0", ModernUIUtils.COLOR_DANGER, new Color(220, 38, 38), Color.WHITE);
        btnClear.setFont(ModernUIUtils.FONT_SMALL);
        btnClear.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to clear all records and reset to ₹0.00?",
                    "Reset All Data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (c == JOptionPane.YES_OPTION) {
                expenseManager.clearAllData();
                JOptionPane.showMessageDialog(this, "All records cleared. System is now at ₹0.00.", "Reset Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton btnAbout = ModernUIUtils.createPillButton("ℹ️ About", new Color(239, 246, 255), new Color(191, 219, 254), ModernUIUtils.COLOR_PRIMARY);
        btnAbout.setFont(ModernUIUtils.FONT_SMALL);
        btnAbout.addActionListener(e -> showAboutDialog());

        topTools.add(btnDemo);
        topTools.add(btnClear);
        topTools.add(btnAbout);

        topAppBar.add(brandPanel, BorderLayout.WEST);
        topAppBar.add(topTools, BorderLayout.EAST);

        // --- 2. THREE BIG PROMINENT KPI CARDS (TOTAL INCOME, TOTAL EXPENSE, BALANCE) ---
        JPanel kpiContainer = new JPanel(new GridLayout(1, 3, 14, 0));
        kpiContainer.setOpaque(false);

        JPanel cardIncome = createTopKpiCard(
                "🟢 TOTAL INCOME (MONEY IN)",
                "₹ 0.00",
                "Pocket money, stipend, freelance & allowance",
                new Color(5, 150, 105),
                new Color(236, 253, 245),
                new Color(167, 243, 208)
        );
        lblTotalIncome = (JLabel) cardIncome.getClientProperty("valueLabel");

        JPanel cardExpense = createTopKpiCard(
                "🔴 TOTAL EXPENSES (MONEY OUT)",
                "₹ 0.00",
                "Food, canteen, books, rent & daily spending",
                new Color(220, 38, 38),
                new Color(254, 242, 242),
                new Color(254, 202, 202)
        );
        lblTotalExpense = (JLabel) cardExpense.getClientProperty("valueLabel");

        JPanel cardBalance = createTopKpiCard(
                "🔵 NET REMAINING BALANCE",
                "₹ 0.00",
                "Available cash left in pocket to spend",
                new Color(37, 99, 235),
                new Color(239, 246, 255),
                new Color(191, 219, 254)
        );
        lblNetBalance = (JLabel) cardBalance.getClientProperty("valueLabel");

        kpiContainer.add(cardIncome);
        kpiContainer.add(cardExpense);
        kpiContainer.add(cardBalance);

        JPanel northBlock = new JPanel();
        northBlock.setLayout(new BoxLayout(northBlock, BoxLayout.Y_AXIS));
        northBlock.setOpaque(false);
        northBlock.add(topAppBar);
        northBlock.add(Box.createVerticalStrut(10));
        northBlock.add(kpiContainer);
        northBlock.add(Box.createVerticalStrut(10));

        rootPanel.add(northBlock, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        ModernUIUtils.styleTabbedPane(tabbedPane);

        trackerPanel = new TrackerPanel(expenseManager);
        budgetPanel = new BudgetPanel(expenseManager, this);
        savingsGoalsPanel = new SavingsGoalsPanel(expenseManager, this);
        billSplitterPanel = new BillSplitterPanel(expenseManager, this);

        tabbedPane.addTab("  💸  Tracker  ", trackerPanel);
        tabbedPane.addTab("  🎯  Budgets  ", budgetPanel);
        tabbedPane.addTab("  🏆  Goals  ", savingsGoalsPanel);
        tabbedPane.addTab("  ⚡  Split Bills  ", billSplitterPanel);

        rootPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ModernUIUtils.COLOR_BORDER),
                new EmptyBorder(8, 16, 8, 16)));

        lblStatusLeft = new JLabel("Currency: Indian Rupee (₹) | Data Storage: data/*.csv (Auto-saved)");
        lblStatusLeft.setFont(ModernUIUtils.FONT_SMALL);
        lblStatusLeft.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        lblStatusRight = new JLabel("Total Transactions: 0 | Net Balance: ₹0.00");
        lblStatusRight.setFont(ModernUIUtils.FONT_SMALL);
        lblStatusRight.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        statusBar.add(lblStatusLeft, BorderLayout.WEST);
        statusBar.add(lblStatusRight, BorderLayout.EAST);

        rootPanel.add(statusBar, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private JPanel createTopKpiCard(String title, String initialVal, String sub, Color textColor, Color bgColor, Color borderColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 12, 12));
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 12, 12));
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(5, 2));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font(ModernUIUtils.FONT_NAME, Font.BOLD, 12));
        lblTitle.setForeground(textColor);

        JLabel lblVal = new JLabel(initialVal);
        lblVal.setFont(new Font(ModernUIUtils.FONT_NAME, Font.BOLD, 26));
        lblVal.setForeground(textColor);

        JLabel lblSub = new JLabel(sub);
        lblSub.setFont(new Font(ModernUIUtils.FONT_NAME, Font.PLAIN, 11));
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);

        card.putClientProperty("valueLabel", lblVal);
        return card;
    }

    public void switchToTab(int index) {
        if (index >= 0 && index < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    @Override
    public void onDataChanged() {
        SwingUtilities.invokeLater(() -> {
            updateSummaryKpis();
            trackerPanel.refreshData();
            budgetPanel.refreshData();
            savingsGoalsPanel.refreshData();
        });
    }

    private void updateSummaryKpis() {
        AnalyticsService analytics = expenseManager.getAnalyticsService();
        java.util.List<Transaction> transactions = expenseManager.getTransactions();

        double totalIncome = analytics.calculateTotalIncome(transactions);
        double totalExpense = analytics.calculateTotalExpenses(transactions);
        double netBalance = totalIncome - totalExpense;

        lblTotalIncome.setText(String.format("₹ %.2f", totalIncome));
        lblTotalExpense.setText(String.format("₹ %.2f", totalExpense));
        lblNetBalance.setText(String.format("₹ %.2f", netBalance));

        if (netBalance >= 0) {
            lblNetBalance.setForeground(new Color(37, 99, 235)); // Blue
        } else {
            lblNetBalance.setForeground(new Color(220, 38, 38)); // Red if negative
        }

        lblStatusRight.setText(String.format("Total Transactions: %d | Net Balance: ₹%.2f",
                transactions.size(), netBalance));
    }

    public void showAddTransactionDialog(TransactionType initialType) {
        // Switch to the main tracker tab so the user can easily use the direct form
        switchToTab(0);
    }

    public void showEditTransactionDialog(Transaction target) {
        // Handled directly or via TrackerPanel
        switchToTab(0);
    }

    private void showAboutDialog() {
        String msg = "<html><body style='width: 340px; font-family: Segoe UI, sans-serif;'>"
                + "<h2 style='color: #2563EB; margin-bottom: 4px;'>🎓 Student Expense & Budget System</h2>"
                + "<p><b>Version:</b> 2.0.0 (Simple & Direct Edition)</p>"
                + "<p><b>Course:</b> Object-Oriented Programming in Java (Java 21)</p>"
                + "<hr style='border: 0; border-top: 1px solid #E2E8F0; margin: 8px 0;'/>"
                + "<p><b>How to use:</b></p>"
                + "<ol>"
                + "<li>Select <b>Income</b> or <b>Expense</b> in the form on the left.</li>"
                + "<li>Enter the <b>Title</b> and <b>Amount (₹)</b>.</li>"
                + "<li>Click <b>Save Transaction</b>.</li>"
                + "<li>Watch your <b>Total Income</b>, <b>Total Expense</b>, and <b>Balance</b> update at the top instantly!</li>"
                + "</ol>"
                + "<p style='color: #64748B;'>100% Core Java | Zero External Dependencies.</p>"
                + "</body></html>";
        JOptionPane.showMessageDialog(this, msg, "About Project", JOptionPane.INFORMATION_MESSAGE);
    }
}
