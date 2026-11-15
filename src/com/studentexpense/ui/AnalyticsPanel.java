package com.studentexpense.ui;

import com.studentexpense.model.Category;
import com.studentexpense.model.Transaction;
import com.studentexpense.service.AnalyticsService;
import com.studentexpense.service.AnalyticsService.CategoryBudgetStatus;
import com.studentexpense.service.ExpenseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Visual analytics panel with donut & bar charts, daily burn rates,
 * spending tips, and full financial statement export in Indian Rupees (₹).
 */
public class AnalyticsPanel extends JPanel {

    private final ExpenseManager expenseManager;
    private final MainFrame mainFrame;

    private JLabel lblHighestCat;
    private JLabel lblDailyAvg;
    private JLabel lblHealthScore;
    private JLabel lblHealthAdvice;

    private CustomChartPanel donutChart;
    private CustomChartPanel barChart;

    public AnalyticsPanel(ExpenseManager expenseManager, MainFrame mainFrame) {
        this.expenseManager = expenseManager;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 16));
        setBackground(ModernUIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshData();
    }

    private void initUI() {
        // --- 1. Header ---
        JPanel topPanel = new JPanel(new BorderLayout(15, 12));
        topPanel.setOpaque(false);

        JPanel headerLeft = new JPanel(new BorderLayout());
        headerLeft.setOpaque(false);

        JLabel lblTitle = new JLabel("Visual Analytics & Spending Insights");
        lblTitle.setFont(ModernUIUtils.FONT_TITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_TEXT_DARK);

        JLabel lblSub = new JLabel("Understand where your pocket money goes with visual charts, burn rate, and student tips");
        lblSub.setFont(ModernUIUtils.FONT_BODY);
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        headerLeft.add(lblTitle, BorderLayout.NORTH);
        headerLeft.add(lblSub, BorderLayout.SOUTH);

        JButton btnExportReport = ModernUIUtils.createPrimaryButton("📄 Export Statement Report");
        btnExportReport.addActionListener(e -> handleExportReport());

        topPanel.add(headerLeft, BorderLayout.WEST);
        topPanel.add(btnExportReport, BorderLayout.EAST);

        // --- 2. Key Insights Row in Rupees (₹) ---
        JPanel insightsGrid = new JPanel(new GridLayout(1, 3, 14, 0));
        insightsGrid.setOpaque(false);

        JPanel cardTop = ModernUIUtils.createCardPanel();
        cardTop.setLayout(new BorderLayout(0, 4));
        JLabel lblT1 = new JLabel("TOP EXPENSE CATEGORY");
        lblT1.setFont(ModernUIUtils.FONT_BODY_BOLD);
        lblT1.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);
        lblHighestCat = new JLabel("None");
        lblHighestCat.setFont(ModernUIUtils.FONT_TITLE);
        lblHighestCat.setForeground(ModernUIUtils.COLOR_DANGER);
        cardTop.add(lblT1, BorderLayout.NORTH);
        cardTop.add(lblHighestCat, BorderLayout.CENTER);

        JPanel cardAvg = ModernUIUtils.createCardPanel();
        cardAvg.setLayout(new BorderLayout(0, 4));
        JLabel lblT2 = new JLabel("DAILY SPEND BURN RATE");
        lblT2.setFont(ModernUIUtils.FONT_BODY_BOLD);
        lblT2.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);
        lblDailyAvg = new JLabel("₹0.00 / day");
        lblDailyAvg.setFont(ModernUIUtils.FONT_TITLE);
        lblDailyAvg.setForeground(ModernUIUtils.COLOR_PRIMARY);
        cardAvg.add(lblT2, BorderLayout.NORTH);
        cardAvg.add(lblDailyAvg, BorderLayout.CENTER);

        JPanel cardHealth = ModernUIUtils.createCardPanel();
        cardHealth.setLayout(new BorderLayout(0, 4));
        JLabel lblT3 = new JLabel("STUDENT FINANCIAL SCORE");
        lblT3.setFont(ModernUIUtils.FONT_BODY_BOLD);
        lblT3.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);
        lblHealthScore = new JLabel("0 / 100");
        lblHealthScore.setFont(ModernUIUtils.FONT_TITLE);
        lblHealthScore.setForeground(ModernUIUtils.COLOR_SUCCESS);

        lblHealthAdvice = new JLabel("Financial advice will appear here");
        lblHealthAdvice.setFont(ModernUIUtils.FONT_SMALL);
        lblHealthAdvice.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        cardHealth.add(lblT3, BorderLayout.NORTH);
        cardHealth.add(lblHealthScore, BorderLayout.CENTER);
        cardHealth.add(lblHealthAdvice, BorderLayout.SOUTH);

        insightsGrid.add(cardTop);
        insightsGrid.add(cardAvg);
        insightsGrid.add(cardHealth);

        JPanel northBlock = new JPanel();
        northBlock.setLayout(new BoxLayout(northBlock, BoxLayout.Y_AXIS));
        northBlock.setOpaque(false);
        northBlock.add(topPanel);
        northBlock.add(Box.createVerticalStrut(14));
        northBlock.add(insightsGrid);

        add(northBlock, BorderLayout.NORTH);

        // --- 3. Dual Charts Grid ---
        JPanel chartsGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsGrid.setOpaque(false);

        JPanel donutCard = ModernUIUtils.createCardPanel();
        donutCard.setLayout(new BorderLayout(0, 8));
        JLabel lblDonutTitle = new JLabel("Category Distribution (Overall)");
        lblDonutTitle.setFont(ModernUIUtils.FONT_SUBTITLE);
        lblDonutTitle.setForeground(ModernUIUtils.COLOR_TEXT_DARK);
        donutCard.add(lblDonutTitle, BorderLayout.NORTH);
        donutChart = new CustomChartPanel();
        donutCard.add(donutChart, BorderLayout.CENTER);

        JPanel barCard = ModernUIUtils.createCardPanel();
        barCard.setLayout(new BorderLayout(0, 8));
        JLabel lblBarTitle = new JLabel("Budget vs Actual Spending (Current Month)");
        lblBarTitle.setFont(ModernUIUtils.FONT_SUBTITLE);
        lblBarTitle.setForeground(ModernUIUtils.COLOR_TEXT_DARK);
        barCard.add(lblBarTitle, BorderLayout.NORTH);
        barChart = new CustomChartPanel();
        barCard.add(barChart, BorderLayout.CENTER);

        chartsGrid.add(donutCard);
        chartsGrid.add(barCard);

        add(chartsGrid, BorderLayout.CENTER);
    }

    public void refreshData() {
        AnalyticsService analytics = expenseManager.getAnalyticsService();
        List<Transaction> transactions = expenseManager.getTransactions();

        Category topCat = analytics.getHighestSpendingCategory(transactions);
        if (topCat != null) {
            Map<Category, Double> catMap = analytics.getExpensesByCategory(transactions);
            double topAmount = catMap.getOrDefault(topCat, 0.0);
            lblHighestCat.setText(topCat.getDisplayName() + " (" + String.format("₹%.2f", topAmount) + ")");
        } else {
            lblHighestCat.setText("None");
        }

        double dailyAvg = analytics.calculateDailyAverageExpense(transactions, YearMonth.now());
        lblDailyAvg.setText(String.format("₹%.2f / day", dailyAvg));

        int score = analytics.calculateFinancialHealthScore(transactions, expenseManager.getBudgets());
        lblHealthScore.setText(score + " / 100");
        lblHealthAdvice.setText(analytics.getFinancialHealthAdvice(score));

        Map<Category, Double> catMap = analytics.getExpensesByCategory(transactions);
        donutChart.setDonutData(catMap);

        List<CategoryBudgetStatus> statuses = analytics.getBudgetStatuses(
                transactions, expenseManager.getBudgets(), YearMonth.now());
        barChart.setBarData(statuses);
    }

    private void handleExportReport() {
        AnalyticsService analytics = expenseManager.getAnalyticsService();
        String report = analytics.generateFullReport(
                expenseManager.getTransactions(),
                expenseManager.getBudgets(),
                expenseManager.getGoals());

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("student_financial_report_" + LocalDate.now() + ".txt"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                expenseManager.getPersistenceService().exportFinancialStatement(report, fileToSave.getAbsolutePath());
                JOptionPane.showMessageDialog(this,
                        "Financial Statement Report saved successfully to:\n" + fileToSave.getAbsolutePath(),
                        "Report Exported", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to save report: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
