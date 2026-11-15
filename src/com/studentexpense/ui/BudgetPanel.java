package com.studentexpense.ui;

import com.studentexpense.model.Category;
import com.studentexpense.service.AnalyticsService;
import com.studentexpense.service.AnalyticsService.CategoryBudgetStatus;
import com.studentexpense.service.ExpenseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.YearMonth;
import java.util.List;

/**
 * Visual budget management panel with category progress bars,
 * real-time limit threshold tracking, and alert highlights in Indian Rupees (₹).
 */
public class BudgetPanel extends JPanel {

    private final ExpenseManager expenseManager;
    private final MainFrame mainFrame;

    private JLabel lblTotalBudget;
    private JLabel lblTotalSpent;
    private JLabel lblTotalRemaining;
    private JPanel budgetListContainer;

    public BudgetPanel(ExpenseManager expenseManager, MainFrame mainFrame) {
        this.expenseManager = expenseManager;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 16));
        setBackground(ModernUIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshData();
    }

    private void initUI() {
        // --- 1. Top Header ---
        JPanel topPanel = new JPanel(new BorderLayout(15, 12));
        topPanel.setOpaque(false);

        JPanel headerLeft = new JPanel(new BorderLayout());
        headerLeft.setOpaque(false);

        JLabel lblTitle = new JLabel("Monthly Student Budget Planner");
        lblTitle.setFont(ModernUIUtils.FONT_TITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_TEXT_DARK);

        JLabel lblSub = new JLabel("Set monthly spending limits for college categories and avoid overspending");
        lblSub.setFont(ModernUIUtils.FONT_BODY);
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        headerLeft.add(lblTitle, BorderLayout.NORTH);
        headerLeft.add(lblSub, BorderLayout.SOUTH);

        JButton btnSetBudget = ModernUIUtils.createPrimaryButton("+ Set / Edit Budget");
        btnSetBudget.addActionListener(e -> showSetBudgetDialog());

        topPanel.add(headerLeft, BorderLayout.WEST);
        topPanel.add(btnSetBudget, BorderLayout.EAST);

        // --- 2. Summary KPI Cards in Rupees (₹) ---
        JPanel summaryGrid = new JPanel(new GridLayout(1, 3, 14, 0));
        summaryGrid.setOpaque(false);

        JPanel cardBudget = ModernUIUtils.createCardPanel();
        cardBudget.setLayout(new BorderLayout());
        JLabel lblBTitle = new JLabel("TOTAL BUDGETED");
        lblBTitle.setFont(ModernUIUtils.FONT_BODY_BOLD);
        lblBTitle.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);
        lblTotalBudget = new JLabel("₹0.00");
        lblTotalBudget.setFont(ModernUIUtils.FONT_METRIC);
        lblTotalBudget.setForeground(ModernUIUtils.COLOR_PRIMARY);
        cardBudget.add(lblBTitle, BorderLayout.NORTH);
        cardBudget.add(lblTotalBudget, BorderLayout.CENTER);

        JPanel cardSpent = ModernUIUtils.createCardPanel();
        cardSpent.setLayout(new BorderLayout());
        JLabel lblSTitle = new JLabel("TOTAL SPENT THIS MONTH");
        lblSTitle.setFont(ModernUIUtils.FONT_BODY_BOLD);
        lblSTitle.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);
        lblTotalSpent = new JLabel("₹0.00");
        lblTotalSpent.setFont(ModernUIUtils.FONT_METRIC);
        lblTotalSpent.setForeground(ModernUIUtils.COLOR_DANGER);
        cardSpent.add(lblSTitle, BorderLayout.NORTH);
        cardSpent.add(lblTotalSpent, BorderLayout.CENTER);

        JPanel cardRem = ModernUIUtils.createCardPanel();
        cardRem.setLayout(new BorderLayout());
        JLabel lblRTitle = new JLabel("TOTAL REMAINING");
        lblRTitle.setFont(ModernUIUtils.FONT_BODY_BOLD);
        lblRTitle.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);
        lblTotalRemaining = new JLabel("₹0.00");
        lblTotalRemaining.setFont(ModernUIUtils.FONT_METRIC);
        lblTotalRemaining.setForeground(ModernUIUtils.COLOR_SUCCESS);
        cardRem.add(lblRTitle, BorderLayout.NORTH);
        cardRem.add(lblTotalRemaining, BorderLayout.CENTER);

        summaryGrid.add(cardBudget);
        summaryGrid.add(cardSpent);
        summaryGrid.add(cardRem);

        JPanel northBlock = new JPanel();
        northBlock.setLayout(new BoxLayout(northBlock, BoxLayout.Y_AXIS));
        northBlock.setOpaque(false);
        northBlock.add(topPanel);
        northBlock.add(Box.createVerticalStrut(14));
        northBlock.add(summaryGrid);

        add(northBlock, BorderLayout.NORTH);

        // --- 3. Category Budget Items Scroll List ---
        budgetListContainer = new JPanel();
        budgetListContainer.setLayout(new BoxLayout(budgetListContainer, BoxLayout.Y_AXIS));
        budgetListContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(budgetListContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        AnalyticsService analytics = expenseManager.getAnalyticsService();
        List<CategoryBudgetStatus> statuses = analytics.getBudgetStatuses(
                expenseManager.getTransactions(),
                expenseManager.getBudgets(),
                YearMonth.now());

        double totalBudget = 0.0;
        double totalSpent = 0.0;

        budgetListContainer.removeAll();

        if (statuses.isEmpty()) {
            JPanel emptyCard = ModernUIUtils.createEmptyStatePanel(
                    "🎯",
                    "No Budgets Configured Yet",
                    "Setting category budgets helps you avoid running out of pocket money before the month ends.",
                    "+ Set Category Budget",
                    this::showSetBudgetDialog
            );
            budgetListContainer.add(emptyCard);
        } else {
            for (CategoryBudgetStatus status : statuses) {
                totalBudget += status.getBudgetLimit();
                totalSpent += status.getAmountSpent();
                budgetListContainer.add(createBudgetRowCard(status));
                budgetListContainer.add(Box.createVerticalStrut(10));
            }
        }

        lblTotalBudget.setText(String.format("₹%.2f", totalBudget));
        lblTotalSpent.setText(String.format("₹%.2f", totalSpent));
        double rem = totalBudget - totalSpent;
        lblTotalRemaining.setText(String.format("₹%.2f", rem));
        lblTotalRemaining.setForeground(rem >= 0 ? ModernUIUtils.COLOR_SUCCESS : ModernUIUtils.COLOR_DANGER);

        budgetListContainer.revalidate();
        budgetListContainer.repaint();
    }

    private JPanel createBudgetRowCard(CategoryBudgetStatus status) {
        JPanel card = ModernUIUtils.createCardPanel();
        card.setLayout(new BorderLayout(12, 8));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel lblIcon = new JLabel(status.getCategory().getIconSymbol());
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        JLabel lblName = new JLabel(status.getCategory().getDisplayName());
        lblName.setFont(ModernUIUtils.FONT_BODY_BOLD);
        lblName.setForeground(ModernUIUtils.COLOR_TEXT_DARK);

        left.add(lblIcon);
        left.add(lblName);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JLabel lblNumbers = new JLabel(String.format("Spent: ₹%.2f / Limit: ₹%.2f (Rem: ₹%.2f)",
                status.getAmountSpent(), status.getBudgetLimit(), status.getRemaining()));
        lblNumbers.setFont(ModernUIUtils.FONT_BODY);

        JLabel lblBadge = new JLabel();
        lblBadge.setFont(ModernUIUtils.FONT_SMALL);
        lblBadge.setOpaque(true);
        lblBadge.setBorder(new EmptyBorder(4, 8, 4, 8));

        if (status.isExceeded()) {
            lblBadge.setText(String.format("EXCEEDED (+%.0f%%)", status.getUsagePercentage() - 100));
            lblBadge.setBackground(new Color(254, 226, 226));
            lblBadge.setForeground(ModernUIUtils.COLOR_DANGER);
        } else if (status.isWarning()) {
            lblBadge.setText(String.format("WARNING (%.0f%%)", status.getUsagePercentage()));
            lblBadge.setBackground(new Color(254, 243, 199));
            lblBadge.setForeground(new Color(180, 83, 9));
        } else {
            lblBadge.setText(String.format("SAFE (%.0f%%)", status.getUsagePercentage()));
            lblBadge.setBackground(new Color(209, 250, 229));
            lblBadge.setForeground(new Color(6, 95, 70));
        }

        JButton btnQuickEdit = ModernUIUtils.createSecondaryButton("Edit");
        btnQuickEdit.setPreferredSize(new Dimension(65, 26));
        btnQuickEdit.setFont(ModernUIUtils.FONT_SMALL);
        btnQuickEdit.addActionListener(e -> showEditBudgetDialog(status.getCategory(), status.getBudgetLimit()));

        right.add(lblNumbers);
        right.add(lblBadge);
        right.add(btnQuickEdit);

        JPanel rowTop = new JPanel(new BorderLayout());
        rowTop.setOpaque(false);
        rowTop.add(left, BorderLayout.WEST);
        rowTop.add(right, BorderLayout.EAST);

        JProgressBar progressBar = new JProgressBar(0, 100);
        int pct = (int) Math.min(100, Math.round(status.getUsagePercentage()));
        progressBar.setValue(pct);
        progressBar.setPreferredSize(new Dimension(100, 10));
        progressBar.setStringPainted(false);
        progressBar.setBorder(BorderFactory.createEmptyBorder());

        if (status.isExceeded()) {
            progressBar.setForeground(ModernUIUtils.COLOR_DANGER);
        } else if (status.isWarning()) {
            progressBar.setForeground(ModernUIUtils.COLOR_WARNING);
        } else {
            progressBar.setForeground(ModernUIUtils.COLOR_SUCCESS);
        }

        card.add(rowTop, BorderLayout.NORTH);
        card.add(progressBar, BorderLayout.SOUTH);
        return card;
    }

    private void showSetBudgetDialog() {
        showEditBudgetDialog(null, 2000.0);
    }

    private void showEditBudgetDialog(Category preselect, double defaultLimit) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Set Category Budget", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 240);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 14));
        form.setBorder(new EmptyBorder(20, 24, 10, 24));

        JLabel lblCat = new JLabel("Category:");
        lblCat.setFont(ModernUIUtils.FONT_BODY_BOLD);
        List<Category> expenseCategories = Category.getExpenseCategories();
        JComboBox<Category> cmbCategory = new JComboBox<>(expenseCategories.toArray(new Category[0]));
        if (preselect != null) {
            cmbCategory.setSelectedItem(preselect);
            cmbCategory.setEnabled(false);
        }

        JLabel lblLimit = new JLabel("Monthly Limit (₹):");
        lblLimit.setFont(ModernUIUtils.FONT_BODY_BOLD);
        JTextField txtLimit = ModernUIUtils.createStyledTextField();
        txtLimit.setText(String.format("%.2f", defaultLimit));

        form.add(lblCat);
        form.add(cmbCategory);
        form.add(lblLimit);
        form.add(txtLimit);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        JButton btnSave = ModernUIUtils.createPrimaryButton("Save Budget");
        JButton btnCancel = ModernUIUtils.createSecondaryButton("Cancel");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            try {
                double limit = Double.parseDouble(txtLimit.getText().trim());
                if (limit < 0) {
                    JOptionPane.showMessageDialog(dialog, "Budget limit cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Category cat = (Category) cmbCategory.getSelectedItem();
                expenseManager.setBudget(cat, limit);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Budget for " + cat.getDisplayName() + " updated successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid numeric limit.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
