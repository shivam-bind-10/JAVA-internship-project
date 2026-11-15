package com.studentexpense.ui;

import com.studentexpense.model.*;
import com.studentexpense.service.AnalyticsService;
import com.studentexpense.service.ExpenseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Overview dashboard displaying KPIs in Indian Rupees (₹),
 * dynamic budget alerts, visual donut breakdown, and recent transaction history.
 */
public class DashboardPanel extends JPanel {

    private final ExpenseManager expenseManager;
    private final MainFrame mainFrame;

    private JLabel lblIncomeVal;
    private JLabel lblExpenseVal;
    private JLabel lblBalanceVal;
    private JLabel lblHealthVal;
    private JLabel lblHealthSub;

    private JPanel alertContainer;
    private CustomChartPanel donutChart;
    private DefaultTableModel recentTableModel;
    private JTable recentTable;
    private JPanel emptyStatePlaceholder;
    private JScrollPane tableScrollPane;

    public DashboardPanel(ExpenseManager expenseManager, MainFrame mainFrame) {
        this.expenseManager = expenseManager;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 16));
        setBackground(ModernUIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshData();
    }

    private void initUI() {
        // --- 1. Top Section: Header & Action Buttons ---
        JPanel topContainer = new JPanel(new BorderLayout(15, 12));
        topContainer.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Student Financial Dashboard");
        lblTitle.setFont(ModernUIUtils.FONT_TITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Live overview of your daily student cashflow, balances, and budgets");
        lblSubtitle.setFont(ModernUIUtils.FONT_BODY);
        lblSubtitle.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        JButton btnAddExpense = ModernUIUtils.createDangerButton("+ Add Expense");
        btnAddExpense.addActionListener(e -> mainFrame.showAddTransactionDialog(TransactionType.EXPENSE));

        JButton btnAddIncome = ModernUIUtils.createSuccessButton("+ Add Income");
        btnAddIncome.addActionListener(e -> mainFrame.showAddTransactionDialog(TransactionType.INCOME));

        JButton btnSplit = ModernUIUtils.createPrimaryButton("⚡ Split Bill");
        btnSplit.addActionListener(e -> mainFrame.switchToTab(5));

        actionPanel.add(btnAddIncome);
        actionPanel.add(btnAddExpense);
        actionPanel.add(btnSplit);

        topContainer.add(headerPanel, BorderLayout.WEST);
        topContainer.add(actionPanel, BorderLayout.EAST);

        // --- 2. Metric KPI Cards Grid in Rupees (₹) ---
        JPanel metricsGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        metricsGrid.setOpaque(false);

        JPanel cardIncome = ModernUIUtils.createMetricCard("TOTAL INFLOW", "₹0.00", "Monthly student income", ModernUIUtils.COLOR_SUCCESS, "💵");
        lblIncomeVal = findLabelByName(cardIncome, "metricValue");

        JPanel cardExpense = ModernUIUtils.createMetricCard("TOTAL OUTFLOW", "₹0.00", "Total expenses recorded", ModernUIUtils.COLOR_DANGER, "💸");
        lblExpenseVal = findLabelByName(cardExpense, "metricValue");

        JPanel cardBalance = ModernUIUtils.createMetricCard("NET BALANCE", "₹0.00", "Available cash / savings", ModernUIUtils.COLOR_PRIMARY, "🏦");
        lblBalanceVal = findLabelByName(cardBalance, "metricValue");

        JPanel cardHealth = ModernUIUtils.createMetricCard("FINANCIAL HEALTH", "0 / 100", "Discipline rating", ModernUIUtils.COLOR_WARNING, "⭐");
        lblHealthVal = findLabelByName(cardHealth, "metricValue");
        lblHealthSub = findLabelByName(cardHealth, "metricSub");

        metricsGrid.add(cardIncome);
        metricsGrid.add(cardExpense);
        metricsGrid.add(cardBalance);
        metricsGrid.add(cardHealth);

        // --- 3. Alert / Welcome Container ---
        alertContainer = new JPanel();
        alertContainer.setLayout(new BoxLayout(alertContainer, BoxLayout.Y_AXIS));
        alertContainer.setOpaque(false);

        JPanel topBlock = new JPanel();
        topBlock.setLayout(new BoxLayout(topBlock, BoxLayout.Y_AXIS));
        topBlock.setOpaque(false);
        topBlock.add(topContainer);
        topBlock.add(Box.createVerticalStrut(14));
        topBlock.add(metricsGrid);
        topBlock.add(Box.createVerticalStrut(10));
        topBlock.add(alertContainer);

        add(topBlock, BorderLayout.NORTH);

        // --- 4. Middle Section: Donut Chart & Recent Transactions ---
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        centerGrid.setOpaque(false);

        // Left Card: Chart
        JPanel chartCard = ModernUIUtils.createCardPanel();
        chartCard.setLayout(new BorderLayout(0, 10));

        JLabel lblChartTitle = new JLabel("Expense Breakdown by Category");
        lblChartTitle.setFont(ModernUIUtils.FONT_SUBTITLE);
        lblChartTitle.setForeground(ModernUIUtils.COLOR_TEXT_DARK);
        chartCard.add(lblChartTitle, BorderLayout.NORTH);

        donutChart = new CustomChartPanel();
        chartCard.add(donutChart, BorderLayout.CENTER);

        // Right Card: Recent Transactions
        JPanel recentCard = ModernUIUtils.createCardPanel();
        recentCard.setLayout(new BorderLayout(0, 10));

        JPanel recentTop = new JPanel(new BorderLayout());
        recentTop.setOpaque(false);

        JLabel lblRecentTitle = new JLabel("Recent Transactions");
        lblRecentTitle.setFont(ModernUIUtils.FONT_SUBTITLE);
        lblRecentTitle.setForeground(ModernUIUtils.COLOR_TEXT_DARK);

        JButton btnViewAll = ModernUIUtils.createSecondaryButton("View All →");
        btnViewAll.addActionListener(e -> mainFrame.switchToTab(1));

        recentTop.add(lblRecentTitle, BorderLayout.WEST);
        recentTop.add(btnViewAll, BorderLayout.EAST);
        recentCard.add(recentTop, BorderLayout.NORTH);

        String[] cols = {"Date", "Title", "Category", "Amount (₹)"};
        recentTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        recentTable = new JTable(recentTableModel);
        ModernUIUtils.styleTable(recentTable);

        tableScrollPane = new JScrollPane(recentTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.getViewport().setBackground(Color.WHITE);

        emptyStatePlaceholder = new JPanel(new BorderLayout());
        emptyStatePlaceholder.setOpaque(false);
        JLabel lblEmptyPlaceholder = new JLabel("No transactions recorded yet. Click '+ Add Expense' or '+ Add Income' above.", SwingConstants.CENTER);
        lblEmptyPlaceholder.setFont(ModernUIUtils.FONT_BODY);
        lblEmptyPlaceholder.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);
        emptyStatePlaceholder.add(lblEmptyPlaceholder, BorderLayout.CENTER);

        recentCard.add(tableScrollPane, BorderLayout.CENTER);

        centerGrid.add(chartCard);
        centerGrid.add(recentCard);

        add(centerGrid, BorderLayout.CENTER);
    }

    public void refreshData() {
        AnalyticsService analytics = expenseManager.getAnalyticsService();
        List<Transaction> transactions = expenseManager.getTransactions();
        List<Budget> budgets = expenseManager.getBudgets();

        // 1. Update Metrics
        double totalIncome = analytics.calculateTotalIncome(transactions);
        double totalExpense = analytics.calculateTotalExpenses(transactions);
        double netBalance = totalIncome - totalExpense;
        int healthScore = analytics.calculateFinancialHealthScore(transactions, budgets);

        lblIncomeVal.setText(String.format("₹%.2f", totalIncome));
        lblExpenseVal.setText(String.format("₹%.2f", totalExpense));
        lblBalanceVal.setText(String.format("₹%.2f", netBalance));
        lblBalanceVal.setForeground(netBalance >= 0 ? ModernUIUtils.COLOR_PRIMARY : ModernUIUtils.COLOR_DANGER);

        lblHealthVal.setText(healthScore + " / 100");
        lblHealthSub.setText(analytics.getFinancialHealthAdvice(healthScore));

        // 2. Alerts or Welcome Banner
        alertContainer.removeAll();
        if (transactions.isEmpty()) {
            JPanel welcomeBanner = new JPanel(new BorderLayout(8, 0));
            welcomeBanner.setBackground(new Color(239, 246, 255)); // Light Blue
            welcomeBanner.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ModernUIUtils.COLOR_PRIMARY, 1, true),
                    new EmptyBorder(8, 14, 8, 14)));

            JLabel lblWelcome = new JLabel("👋 Welcome! Your ledger is currently at ₹0.00. Use '+ Add Income' to log your pocket money/allowance, or '+ Add Expense' for your first spending.");
            lblWelcome.setFont(ModernUIUtils.FONT_BODY_BOLD);
            lblWelcome.setForeground(ModernUIUtils.COLOR_PRIMARY);

            welcomeBanner.add(lblWelcome, BorderLayout.CENTER);
            alertContainer.add(welcomeBanner);
        } else {
            List<String> alerts = analytics.getBudgetAlerts(transactions, budgets, YearMonth.now());
            for (String alert : alerts) {
                boolean isExceeded = alert.contains("OVER BUDGET");
                JPanel banner = new JPanel(new BorderLayout(8, 0));
                banner.setBackground(isExceeded ? new Color(254, 242, 242) : new Color(255, 251, 235));
                banner.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(isExceeded ? ModernUIUtils.COLOR_DANGER : ModernUIUtils.COLOR_WARNING, 1, true),
                        new EmptyBorder(6, 12, 6, 12)));

                JLabel lblAlert = new JLabel((isExceeded ? "⚠️ " : "⚡ ") + alert);
                lblAlert.setFont(ModernUIUtils.FONT_BODY_BOLD);
                lblAlert.setForeground(isExceeded ? ModernUIUtils.COLOR_DANGER : new Color(180, 83, 9));

                banner.add(lblAlert, BorderLayout.CENTER);
                alertContainer.add(banner);
                alertContainer.add(Box.createVerticalStrut(4));
            }
        }
        alertContainer.revalidate();
        alertContainer.repaint();

        // 3. Update Chart
        Map<Category, Double> catMap = analytics.getExpensesByCategory(transactions);
        donutChart.setDonutData(catMap);

        // 4. Update Recent Transactions Table
        recentTableModel.setRowCount(0);
        int count = 0;
        for (Transaction t : transactions) {
            if (count++ >= 6) break;
            String amountPrefix = t.getType() == TransactionType.INCOME ? "+₹" : "-₹";
            recentTableModel.addRow(new Object[]{
                    t.getFormattedDate(),
                    t.getTitle(),
                    t.getCategory().getDisplayName(),
                    amountPrefix + String.format("%.2f", t.getAmount())
            });
        }
    }

    private JLabel findLabelByName(Container container, String name) {
        for (Component c : container.getComponents()) {
            if (name.equals(c.getName()) && c instanceof JLabel) {
                return (JLabel) c;
            } else if (c instanceof Container) {
                JLabel found = findLabelByName((Container) c, name);
                if (found != null) return found;
            }
        }
        return new JLabel();
    }
}
