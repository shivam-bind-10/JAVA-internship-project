package com.studentexpense.cli;

import com.studentexpense.model.*;
import com.studentexpense.service.AnalyticsService;
import com.studentexpense.service.AnalyticsService.CategoryBudgetStatus;
import com.studentexpense.service.ExpenseManager;
import com.studentexpense.ui.MainFrame;

import javax.swing.*;
import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Terminal-based Command Line Interface (CLI) in Indian Rupees (₹)
 * for headless environments and console evaluation during academic project viva/demos.
 */
public class CommandLineInterface {

    private final ExpenseManager expenseManager;
    private final Scanner scanner;

    public CommandLineInterface(ExpenseManager expenseManager) {
        this.expenseManager = expenseManager;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        printBanner();
        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("👉 Enter choice (1-9): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    viewDashboard();
                    break;
                case "2":
                    listTransactions();
                    break;
                case "3":
                    addTransaction();
                    break;
                case "4":
                    manageBudgets();
                    break;
                case "5":
                    manageSavingsGoals();
                    break;
                case "6":
                    splitBill();
                    break;
                case "7":
                    exportReport();
                    break;
                case "8":
                    launchGui();
                    break;
                case "9":
                    System.out.println("\nSaving data and exiting... Good luck with your college submission!");
                    expenseManager.saveAll();
                    running = false;
                    break;
                default:
                    System.out.println("❌ Invalid option. Please choose between 1 and 9.\n");
            }
        }
    }

    private void printBanner() {
        System.out.println("==========================================================================");
        System.out.println("   🎓 STUDENT EXPENSE AND BUDGET MANAGEMENT SYSTEM (JAVA MINI PROJECT)   ");
        System.out.println("==========================================================================");
        System.out.println(" Welcome to Terminal CLI Mode (₹ Rupees). Powered by Core Java 21.");
        System.out.println();
    }

    private void printMenu() {
        System.out.println("--------------------------------------------------------------------------");
        System.out.println(" MAIN MENU:");
        System.out.println("  [1] View Financial Dashboard & Summary");
        System.out.println("  [2] List All Transactions (Search & Filter)");
        System.out.println("  [3] Record New Transaction (Expense / Income)");
        System.out.println("  [4] Monthly Budget Limits & Over-budget Alerts");
        System.out.println("  [5] Student Savings Goals Tracker");
        System.out.println("  [6] Roommate / Flat Bill Splitter");
        System.out.println("  [7] Generate & Export Financial Statement");
        System.out.println("  [8] Switch to Graphical User Interface (GUI)");
        System.out.println("  [9] Exit Application");
        System.out.println("--------------------------------------------------------------------------");
    }

    private void viewDashboard() {
        AnalyticsService analytics = expenseManager.getAnalyticsService();
        List<Transaction> txs = expenseManager.getTransactions();
        List<Budget> budgets = expenseManager.getBudgets();

        double income = analytics.calculateTotalIncome(txs);
        double expense = analytics.calculateTotalExpenses(txs);
        double balance = income - expense;
        int score = analytics.calculateFinancialHealthScore(txs, budgets);

        System.out.println("\n=========================== FINANCIAL DASHBOARD ==========================");
        System.out.printf("  Total Inflow (Income)    : ₹%,10.2f\n", income);
        System.out.printf("  Total Outflow (Expenses) : ₹%,10.2f\n", expense);
        System.out.printf("  Current Net Balance      : ₹%,10.2f\n", balance);
        System.out.printf("  Financial Health Score   : %d / 100\n", score);
        System.out.println("  Advisor Note             : " + analytics.getFinancialHealthAdvice(score));

        List<String> alerts = analytics.getBudgetAlerts(txs, budgets, YearMonth.now());
        if (!alerts.isEmpty()) {
            System.out.println("\n  ⚠️ ACTIVE BUDGET ALERTS:");
            for (String a : alerts) {
                System.out.println("    - " + a);
            }
        }
        System.out.println("==========================================================================\n");
    }

    private void listTransactions() {
        List<Transaction> txs = expenseManager.getTransactions();
        System.out.println("\n================================ TRANSACTION LEDGER ================================");
        System.out.printf(" %-8s %-12s %-24s %-9s %-20s %12s\n", "ID", "Date", "Title", "Type", "Category", "Amount (₹)");
        System.out.println("------------------------------------------------------------------------------------");

        if (txs.isEmpty()) {
            System.out.println("  No transactions recorded yet. Choose option [3] to add your first transaction.");
        } else {
            for (Transaction t : txs) {
                String amt = (t.getType() == TransactionType.INCOME ? "+₹" : "-₹") + String.format("%.2f", t.getAmount());
                System.out.printf(" %-8s %-12s %-24s %-9s %-20s %12s\n",
                        t.getId(),
                        t.getFormattedDate(),
                        truncate(t.getTitle(), 22),
                        t.getType(),
                        truncate(t.getCategory().getDisplayName(), 18),
                        amt);
            }
        }
        System.out.println("------------------------------------------------------------------------------------");
        System.out.printf(" Total Records: %d\n\n", txs.size());
    }

    private void addTransaction() {
        System.out.println("\n--- RECORD NEW TRANSACTION ---");
        System.out.print("Select Type (1 for Expense, 2 for Income): ");
        String typeInput = scanner.nextLine().trim();
        TransactionType type = "2".equals(typeInput) ? TransactionType.INCOME : TransactionType.EXPENSE;

        System.out.print("Enter Title / Description (e.g. Canteen Lunch): ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("❌ Title cannot be empty.\n");
            return;
        }

        System.out.print("Enter Amount (₹): ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine().trim());
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid amount.\n");
            return;
        }

        List<Category> cats = (type == TransactionType.INCOME) ? Category.getIncomeCategories() : Category.getExpenseCategories();
        System.out.println("Select Category:");
        for (int i = 0; i < cats.size(); i++) {
            System.out.printf("  [%d] %s\n", i + 1, cats.get(i).getDisplayName());
        }
        System.out.print("Choice: ");
        int catIdx = 1;
        try {
            catIdx = Integer.parseInt(scanner.nextLine().trim());
            if (catIdx < 1 || catIdx > cats.size()) catIdx = 1;
        } catch (Exception ignored) {}
        Category selectedCat = cats.get(catIdx - 1);

        System.out.print("Enter Date (yyyy-MM-dd, press enter for today): ");
        String dateStr = scanner.nextLine().trim();
        LocalDate date = LocalDate.now();
        if (!dateStr.isEmpty()) {
            try {
                date = LocalDate.parse(dateStr, Transaction.DATE_FORMATTER);
            } catch (Exception e) {
                System.out.println("⚠️ Invalid date. Defaulting to today.");
            }
        }

        System.out.println("Select Payment Method:");
        PaymentMethod[] methods = PaymentMethod.values();
        for (int i = 0; i < methods.length; i++) {
            System.out.printf("  [%d] %s\n", i + 1, methods[i].getDisplayName());
        }
        System.out.print("Choice: ");
        int mIdx = 1;
        try {
            mIdx = Integer.parseInt(scanner.nextLine().trim());
            if (mIdx < 1 || mIdx > methods.length) mIdx = 1;
        } catch (Exception ignored) {}
        PaymentMethod selectedMethod = methods[mIdx - 1];

        System.out.print("Enter Notes / Tag (optional): ");
        String notes = scanner.nextLine().trim();

        Transaction t = new Transaction(title, amount, type, selectedCat, date, selectedMethod, notes);
        expenseManager.addTransaction(t);
        System.out.println("✅ Transaction saved successfully! ID: " + t.getId() + "\n");
    }

    private void manageBudgets() {
        AnalyticsService analytics = expenseManager.getAnalyticsService();
        List<CategoryBudgetStatus> statuses = analytics.getBudgetStatuses(
                expenseManager.getTransactions(),
                expenseManager.getBudgets(),
                YearMonth.now());

        System.out.println("\n============================= MONTHLY BUDGETS ============================");
        System.out.printf(" %-22s %10s %10s %10s %10s %10s\n", "Category", "Limit (₹)", "Spent (₹)", "Rem (₹)", "Usage%", "Status");
        System.out.println("--------------------------------------------------------------------------");
        if (statuses.isEmpty()) {
            System.out.println("  No category budgets configured yet.");
        } else {
            for (CategoryBudgetStatus s : statuses) {
                String status = s.isExceeded() ? "EXCEEDED" : (s.isWarning() ? "WARNING" : "SAFE");
                System.out.printf(" %-22s %10.2f %10.2f %10.2f %9.1f%% %10s\n",
                        truncate(s.getCategory().getDisplayName(), 20),
                        s.getBudgetLimit(),
                        s.getAmountSpent(),
                        s.getRemaining(),
                        s.getUsagePercentage(),
                        status);
            }
        }
        System.out.println("--------------------------------------------------------------------------");

        System.out.print("Do you want to set/update a category budget? (y/n): ");
        String ans = scanner.nextLine().trim();
        if ("y".equalsIgnoreCase(ans)) {
            List<Category> expenseCats = Category.getExpenseCategories();
            for (int i = 0; i < expenseCats.size(); i++) {
                System.out.printf("  [%d] %s\n", i + 1, expenseCats.get(i).getDisplayName());
            }
            System.out.print("Select Category: ");
            int c = 1;
            try {
                c = Integer.parseInt(scanner.nextLine().trim());
                if (c < 1 || c > expenseCats.size()) return;
            } catch (Exception e) { return; }
            Category cat = expenseCats.get(c - 1);

            System.out.printf("Enter monthly limit for %s (₹): ", cat.getDisplayName());
            try {
                double limit = Double.parseDouble(scanner.nextLine().trim());
                expenseManager.setBudget(cat, limit);
                System.out.println("✅ Budget limit updated successfully!\n");
            } catch (Exception e) {
                System.out.println("❌ Invalid limit.\n");
            }
        }
    }

    private void manageSavingsGoals() {
        List<SavingsGoal> goals = expenseManager.getGoals();
        System.out.println("\n========================== STUDENT SAVINGS GOALS =========================");
        System.out.printf(" %-6s %-22s %10s %10s %9s %12s\n", "ID", "Goal", "Target (₹)", "Saved (₹)", "Progress", "Target Date");
        System.out.println("--------------------------------------------------------------------------");
        if (goals.isEmpty()) {
            System.out.println("  No savings goals configured yet.");
        } else {
            for (SavingsGoal g : goals) {
                System.out.printf(" %-6s %-22s %10.2f %10.2f %8d%% %12s\n",
                        g.getId(),
                        truncate(g.getName(), 20),
                        g.getTargetAmount(),
                        g.getCurrentAmount(),
                        g.getProgressPercentage(),
                        g.getTargetDate().toString());
            }
        }
        System.out.println("--------------------------------------------------------------------------");

        System.out.println("Actions: [1] Deposit to Goal  [2] Add New Goal  [3] Return to Menu");
        System.out.print("Choice: ");
        String act = scanner.nextLine().trim();

        if ("1".equals(act)) {
            System.out.print("Enter Goal ID: ");
            String id = scanner.nextLine().trim();
            System.out.print("Enter Deposit Amount (₹): ");
            try {
                double dep = Double.parseDouble(scanner.nextLine().trim());
                expenseManager.depositToGoal(id, dep);
                System.out.println("✅ Deposit recorded!\n");
            } catch (Exception e) {
                System.out.println("❌ Invalid amount.\n");
            }
        } else if ("2".equals(act)) {
            System.out.print("Enter Goal Name (e.g. Study Tablet): ");
            String name = scanner.nextLine().trim();
            System.out.print("Target Amount (₹): ");
            try {
                double target = Double.parseDouble(scanner.nextLine().trim());
                SavingsGoal g = new SavingsGoal(name, target, 0.0, LocalDate.now().plusMonths(3), "Created via CLI");
                expenseManager.addGoal(g);
                System.out.println("✅ Savings goal created!\n");
            } catch (Exception e) {
                System.out.println("❌ Invalid input.\n");
            }
        }
    }

    private void splitBill() {
        System.out.println("\n--- ROOMMATE & GROUP BILL SPLITTER ---");
        System.out.print("Bill Description (e.g. WiFi Bill): ");
        String desc = scanner.nextLine().trim();
        System.out.print("Total Bill Amount (₹): ");
        double total;
        try {
            total = Double.parseDouble(scanner.nextLine().trim());
            if (total <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            System.out.println("❌ Invalid amount.\n");
            return;
        }

        System.out.print("Who paid the bill? (e.g. Rahul): ");
        String payer = scanner.nextLine().trim();

        System.out.print("Enter participants (comma-separated, e.g. Rahul, Aman, Sneha): ");
        String partStr = scanner.nextLine().trim();
        List<String> participants = new ArrayList<>();
        for (String p : partStr.split(",")) {
            String clean = p.trim();
            if (!clean.isEmpty() && !participants.contains(clean)) participants.add(clean);
        }
        if (!participants.contains(payer)) participants.add(0, payer);

        BillSplit split = new BillSplit(desc, total, payer, participants, LocalDate.now());
        expenseManager.addBillSplit(split);

        System.out.println("\n" + split.getFormattedSummary());

        System.out.print("Would you like to log your share (₹" + String.format("%.2f", split.getSharePerPerson()) + ") into your expense ledger? (y/n): ");
        if ("y".equalsIgnoreCase(scanner.nextLine().trim())) {
            Transaction t = new Transaction("Split: " + desc, split.getSharePerPerson(),
                    TransactionType.EXPENSE, Category.FOOD, LocalDate.now(), PaymentMethod.UPI, "Roommate split");
            expenseManager.addTransaction(t);
            System.out.println("✅ Expense recorded in ledger!\n");
        }
    }

    private void exportReport() {
        AnalyticsService analytics = expenseManager.getAnalyticsService();
        String report = analytics.generateFullReport(
                expenseManager.getTransactions(),
                expenseManager.getBudgets(),
                expenseManager.getGoals());

        System.out.println("\n" + report);

        String filename = "student_report_" + LocalDate.now() + ".txt";
        try {
            File f = expenseManager.getPersistenceService().exportFinancialStatement(report, filename);
            System.out.println("✅ Report exported to disk: " + f.getAbsolutePath() + "\n");
        } catch (Exception e) {
            System.out.println("❌ Export error: " + e.getMessage() + "\n");
        }
    }

    private void launchGui() {
        System.out.println("\nLaunching Java Swing Graphical User Interface (₹ Rupees)...");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            MainFrame frame = new MainFrame(expenseManager);
            frame.setVisible(true);
        });
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        return text.substring(0, max - 2) + "..";
    }
}
