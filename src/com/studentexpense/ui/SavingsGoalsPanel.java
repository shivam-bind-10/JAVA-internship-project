package com.studentexpense.ui;

import com.studentexpense.model.SavingsGoal;
import com.studentexpense.service.ExpenseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Visual tracking of student savings targets with progress bars,
 * target date countdowns, and quick deposit/withdrawal actions in Indian Rupees (₹).
 */
public class SavingsGoalsPanel extends JPanel {

    private final ExpenseManager expenseManager;
    private final MainFrame mainFrame;
    private JPanel goalsContainer;

    public SavingsGoalsPanel(ExpenseManager expenseManager, MainFrame mainFrame) {
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

        JLabel lblTitle = new JLabel("Student Savings Goals & Targets");
        lblTitle.setFont(ModernUIUtils.FONT_TITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_TEXT_DARK);

        JLabel lblSub = new JLabel("Set aside funds for academic tech, textbooks, semester trips, and emergencies");
        lblSub.setFont(ModernUIUtils.FONT_BODY);
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        headerLeft.add(lblTitle, BorderLayout.NORTH);
        headerLeft.add(lblSub, BorderLayout.SOUTH);

        JButton btnAddGoal = ModernUIUtils.createPrimaryButton("+ Add New Goal");
        btnAddGoal.addActionListener(e -> showAddGoalDialog(null));

        topPanel.add(headerLeft, BorderLayout.WEST);
        topPanel.add(btnAddGoal, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. Scrollable Goals Cards Container ---
        goalsContainer = new JPanel();
        goalsContainer.setLayout(new BoxLayout(goalsContainer, BoxLayout.Y_AXIS));
        goalsContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(goalsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        goalsContainer.removeAll();
        List<SavingsGoal> goals = expenseManager.getGoals();

        if (goals.isEmpty()) {
            JPanel emptyCard = ModernUIUtils.createEmptyStatePanel(
                    "🏆",
                    "No Savings Goals Active",
                    "Build financial resilience by setting target funds for a new laptop, semester trip, or emergency reserve.",
                    "+ Create Your First Goal",
                    () -> showAddGoalDialog(null)
            );
            goalsContainer.add(emptyCard);
        } else {
            for (SavingsGoal goal : goals) {
                goalsContainer.add(createGoalCard(goal));
                goalsContainer.add(Box.createVerticalStrut(12));
            }
        }

        goalsContainer.revalidate();
        goalsContainer.repaint();
    }

    private JPanel createGoalCard(SavingsGoal goal) {
        JPanel card = ModernUIUtils.createCardPanel();
        card.setLayout(new BorderLayout(12, 10));

        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);

        JLabel lblName = new JLabel("🎯 " + goal.getName());
        lblName.setFont(ModernUIUtils.FONT_TITLE);
        lblName.setForeground(ModernUIUtils.COLOR_TEXT_DARK);

        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), goal.getTargetDate());
        String countdownText = daysLeft > 0 ? (daysLeft + " days remaining") : (daysLeft == 0 ? "Due today!" : "Deadline passed");
        JLabel lblDeadline = new JLabel("Due: " + goal.getTargetDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + " (" + countdownText + ")");
        lblDeadline.setFont(ModernUIUtils.FONT_BODY_BOLD);
        lblDeadline.setForeground(daysLeft >= 0 ? ModernUIUtils.COLOR_PRIMARY : ModernUIUtils.COLOR_DANGER);

        cardHeader.add(lblName, BorderLayout.WEST);
        cardHeader.add(lblDeadline, BorderLayout.EAST);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JPanel amountsRow = new JPanel(new BorderLayout());
        amountsRow.setOpaque(false);

        JLabel lblAmounts = new JLabel(String.format("Saved: ₹%.2f of ₹%.2f target (Remaining: ₹%.2f)",
                goal.getCurrentAmount(), goal.getTargetAmount(), goal.getRemainingAmount()));
        lblAmounts.setFont(ModernUIUtils.FONT_BODY_BOLD);

        JLabel lblPct = new JLabel(goal.getProgressPercentage() + "% Complete" + (goal.isCompleted() ? " 🎉" : ""));
        lblPct.setFont(ModernUIUtils.FONT_BODY_BOLD);
        lblPct.setForeground(goal.isCompleted() ? ModernUIUtils.COLOR_SUCCESS : ModernUIUtils.COLOR_PRIMARY);

        amountsRow.add(lblAmounts, BorderLayout.WEST);
        amountsRow.add(lblPct, BorderLayout.EAST);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(goal.getProgressPercentage());
        bar.setPreferredSize(new Dimension(100, 12));
        bar.setForeground(goal.isCompleted() ? ModernUIUtils.COLOR_SUCCESS : ModernUIUtils.COLOR_PRIMARY);
        bar.setBorder(BorderFactory.createEmptyBorder());

        JLabel lblNotes = new JLabel(goal.getNotes().isEmpty() ? " " : "Notes: " + goal.getNotes());
        lblNotes.setFont(ModernUIUtils.FONT_SMALL);
        lblNotes.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        centerPanel.add(amountsRow);
        centerPanel.add(Box.createVerticalStrut(6));
        centerPanel.add(bar);
        centerPanel.add(Box.createVerticalStrut(6));
        centerPanel.add(lblNotes);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);

        JButton btnDeposit = ModernUIUtils.createSuccessButton("+ Deposit Funds");
        btnDeposit.addActionListener(e -> showDepositDialog(goal));

        JButton btnWithdraw = ModernUIUtils.createSecondaryButton("Withdraw");
        btnWithdraw.addActionListener(e -> showWithdrawDialog(goal));

        JButton btnEdit = ModernUIUtils.createSecondaryButton("✏️ Edit");
        btnEdit.addActionListener(e -> showAddGoalDialog(goal));

        JButton btnDelete = ModernUIUtils.createDangerButton("🗑️ Delete");
        btnDelete.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete goal: \"" + goal.getName() + "\"?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                expenseManager.deleteGoal(goal.getId());
            }
        });

        footer.add(btnDeposit);
        footer.add(btnWithdraw);
        footer.add(btnEdit);
        footer.add(btnDelete);

        card.add(cardHeader, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private void showDepositDialog(SavingsGoal goal) {
        String input = JOptionPane.showInputDialog(this,
                "Enter amount to deposit into \"" + goal.getName() + "\" (₹):",
                "Deposit Funds", JOptionPane.PLAIN_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                double amount = Double.parseDouble(input.trim());
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Deposit amount must be greater than zero.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                expenseManager.depositToGoal(goal.getId(), amount);
                JOptionPane.showMessageDialog(this,
                        String.format("Deposited ₹%.2f into \"%s\"! Keep up the great savings!", amount, goal.getName()),
                        "Deposit Recorded", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showWithdrawDialog(SavingsGoal goal) {
        String input = JOptionPane.showInputDialog(this,
                "Enter amount to withdraw from \"" + goal.getName() + "\" (₹):",
                "Withdraw Funds", JOptionPane.PLAIN_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                double amount = Double.parseDouble(input.trim());
                if (amount <= 0 || amount > goal.getCurrentAmount()) {
                    JOptionPane.showMessageDialog(this,
                            String.format("Invalid withdrawal amount. Maximum available: ₹%.2f", goal.getCurrentAmount()),
                            "Invalid Amount", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                goal.withdraw(amount);
                expenseManager.saveAll();
                refreshData();
                JOptionPane.showMessageDialog(this,
                        String.format("Withdrew ₹%.2f from \"%s\".", amount, goal.getName()),
                        "Withdrawal Recorded", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddGoalDialog(SavingsGoal existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Add Savings Goal" : "Edit Savings Goal", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(440, 320);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 24, 10, 24));

        JLabel lblName = new JLabel("Goal Name:");
        lblName.setFont(ModernUIUtils.FONT_BODY_BOLD);
        JTextField txtName = ModernUIUtils.createStyledTextField();
        if (existing != null) txtName.setText(existing.getName());

        JLabel lblTarget = new JLabel("Target Amount (₹):");
        lblTarget.setFont(ModernUIUtils.FONT_BODY_BOLD);
        JTextField txtTarget = ModernUIUtils.createStyledTextField();
        if (existing != null) txtTarget.setText(String.format("%.2f", existing.getTargetAmount()));

        JLabel lblCurrent = new JLabel("Initial Saved (₹):");
        lblCurrent.setFont(ModernUIUtils.FONT_BODY_BOLD);
        JTextField txtCurrent = ModernUIUtils.createStyledTextField();
        txtCurrent.setText(existing != null ? String.format("%.2f", existing.getCurrentAmount()) : "0.00");

        JLabel lblMonths = new JLabel("Target in (Months):");
        lblMonths.setFont(ModernUIUtils.FONT_BODY_BOLD);
        JTextField txtMonths = ModernUIUtils.createStyledTextField();
        txtMonths.setText("3");

        form.add(lblName);
        form.add(txtName);
        form.add(lblTarget);
        form.add(txtTarget);
        form.add(lblCurrent);
        form.add(txtCurrent);
        form.add(lblMonths);
        form.add(txtMonths);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        JButton btnSave = ModernUIUtils.createPrimaryButton("Save Goal");
        JButton btnCancel = ModernUIUtils.createSecondaryButton("Cancel");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a goal name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                double target = Double.parseDouble(txtTarget.getText().trim());
                double current = Double.parseDouble(txtCurrent.getText().trim());
                int months = Integer.parseInt(txtMonths.getText().trim());

                if (target <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Target amount must be positive.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (existing != null) {
                    existing.setName(name);
                    existing.setTargetAmount(target);
                    existing.setCurrentAmount(current);
                    expenseManager.updateGoal(existing);
                } else {
                    SavingsGoal newGoal = new SavingsGoal(name, target, current, LocalDate.now().plusMonths(months), "Student Goal");
                    expenseManager.addGoal(newGoal);
                }

                dialog.dispose();
                refreshData();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please check all numbers are formatted properly.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
