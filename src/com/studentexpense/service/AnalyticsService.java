package com.studentexpense.service;

import com.studentexpense.model.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Provides financial calculations, budget tracking, statistical breakdowns,
 * and financial health scoring for students in Indian Rupees (₹).
 */
public class AnalyticsService {

    public static class CategoryBudgetStatus {
        private final Category category;
        private final double budgetLimit;
        private final double amountSpent;

        public CategoryBudgetStatus(Category category, double budgetLimit, double amountSpent) {
            this.category = category;
            this.budgetLimit = budgetLimit;
            this.amountSpent = amountSpent;
        }

        public Category getCategory() {
            return category;
        }

        public double getBudgetLimit() {
            return budgetLimit;
        }

        public double getAmountSpent() {
            return amountSpent;
        }

        public double getRemaining() {
            return budgetLimit - amountSpent;
        }

        public double getUsagePercentage() {
            if (budgetLimit <= 0) return amountSpent > 0 ? 100.0 : 0.0;
            return (amountSpent / budgetLimit) * 100.0;
        }

        public boolean isExceeded() {
            return budgetLimit > 0 && amountSpent > budgetLimit;
        }

        public boolean isWarning() {
            return budgetLimit > 0 && !isExceeded() && getUsagePercentage() >= 75.0;
        }
    }

    public double calculateTotalIncome(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double calculateTotalExpenses(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double calculateNetBalance(List<Transaction> transactions) {
        return calculateTotalIncome(transactions) - calculateTotalExpenses(transactions);
    }

    public Map<Category, Double> getExpensesByCategory(List<Transaction> transactions) {
        Map<Category, Double> map = new EnumMap<>(Category.class);
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE) {
                map.put(t.getCategory(), map.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
            }
        }
        return map;
    }

    public Map<Category, Double> getExpensesForMonth(List<Transaction> transactions, YearMonth ym) {
        Map<Category, Double> map = new EnumMap<>(Category.class);
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE && YearMonth.from(t.getDate()).equals(ym)) {
                map.put(t.getCategory(), map.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
            }
        }
        return map;
    }

    public List<CategoryBudgetStatus> getBudgetStatuses(List<Transaction> transactions, List<Budget> budgets, YearMonth ym) {
        Map<Category, Double> monthlyExpenses = getExpensesForMonth(transactions, ym);
        List<CategoryBudgetStatus> statuses = new ArrayList<>();

        for (Budget b : budgets) {
            double spent = monthlyExpenses.getOrDefault(b.getCategory(), 0.0);
            statuses.add(new CategoryBudgetStatus(b.getCategory(), b.getMonthlyLimit(), spent));
        }

        // Sort so exceeded and warning budgets appear first
        statuses.sort((a, b) -> Double.compare(b.getUsagePercentage(), a.getUsagePercentage()));
        return statuses;
    }

    public List<String> getBudgetAlerts(List<Transaction> transactions, List<Budget> budgets, YearMonth ym) {
        List<String> alerts = new ArrayList<>();
        List<CategoryBudgetStatus> statuses = getBudgetStatuses(transactions, budgets, ym);

        for (CategoryBudgetStatus status : statuses) {
            if (status.isExceeded()) {
                alerts.add(String.format("OVER BUDGET: %s - Spent ₹%.2f of ₹%.2f limit (Over by ₹%.2f)",
                        status.getCategory().getDisplayName(),
                        status.getAmountSpent(),
                        status.getBudgetLimit(),
                        status.getAmountSpent() - status.getBudgetLimit()));
            } else if (status.isWarning()) {
                alerts.add(String.format("BUDGET WARNING: %s - Spent ₹%.2f of ₹%.2f limit (%.1f%% utilized)",
                        status.getCategory().getDisplayName(),
                        status.getAmountSpent(),
                        status.getBudgetLimit(),
                        status.getUsagePercentage()));
            }
        }
        return alerts;
    }

    public Category getHighestSpendingCategory(List<Transaction> transactions) {
        Map<Category, Double> map = getExpensesByCategory(transactions);
        return map.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public double calculateDailyAverageExpense(List<Transaction> transactions, YearMonth ym) {
        double monthlyExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE && YearMonth.from(t.getDate()).equals(ym))
                .mapToDouble(Transaction::getAmount)
                .sum();
        int dayCount = ym.equals(YearMonth.now()) ? LocalDate.now().getDayOfMonth() : ym.lengthOfMonth();
        return dayCount > 0 ? monthlyExpense / dayCount : 0.0;
    }

    /**
     * Calculates an academic student financial health rating from 0 to 100.
     */
    public int calculateFinancialHealthScore(List<Transaction> transactions, List<Budget> budgets) {
        double income = calculateTotalIncome(transactions);
        double expense = calculateTotalExpenses(transactions);

        if (income <= 0 && expense <= 0) return 0; // Fresh clean state
        if (income <= 0) return 20;

        int score = 0;

        // 1. Savings Rate Score (0 - 40 pts)
        double savingsRate = (income - expense) / income;
        if (savingsRate >= 0.30) {
            score += 40;
        } else if (savingsRate >= 0.15) {
            score += 30;
        } else if (savingsRate >= 0.05) {
            score += 20;
        } else if (savingsRate >= 0) {
            score += 10;
        }

        // 2. Budget Adherence Score (0 - 40 pts)
        List<CategoryBudgetStatus> statuses = getBudgetStatuses(transactions, budgets, YearMonth.now());
        if (statuses.isEmpty()) {
            score += 25; // Default neutral if no budgets set
        } else {
            long compliantCount = statuses.stream().filter(s -> !s.isExceeded()).count();
            double complianceRatio = (double) compliantCount / statuses.size();
            score += (int) (complianceRatio * 40);
        }

        // 3. Activity & Consistency Score (0 - 20 pts)
        if (transactions.size() >= 10) {
            score += 20;
        } else if (transactions.size() >= 5) {
            score += 12;
        } else {
            score += 5;
        }

        return Math.min(100, Math.max(0, score));
    }

    public String getFinancialHealthAdvice(int score) {
        if (score == 0) {
            return "Get started! Record your monthly allowance/stipend and track your daily college expenses.";
        } else if (score >= 85) {
            return "Excellent Financial Discipline! You are maintaining strong savings and staying strictly within your student budget.";
        } else if (score >= 70) {
            return "Good Budgeting! You have positive cashflow. Keep an eye on dining out and non-essential entertainment expenses.";
        } else if (score >= 50) {
            return "Moderate Warning! Your expenses are close to matching your income. Review your high-spending categories to boost savings.";
        } else {
            return "Critical Budget Alert! Expenses exceed or rival income. Consider reducing non-essential expenditures and seeking campus opportunities.";
        }
    }

    /**
     * Generates a printable academic financial summary report.
     */
    public String generateFullReport(List<Transaction> transactions, List<Budget> budgets, List<SavingsGoal> goals) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        String sep = "=======================================================================\n";
        String line = "-----------------------------------------------------------------------\n";

        sb.append(sep);
        sb.append("         STUDENT EXPENSE & BUDGET MANAGEMENT SYSTEM - SUMMARY REPORT    \n");
        sb.append("                  Generated on: ").append(LocalDate.now().format(dtf)).append("\n");
        sb.append(sep);

        double totalIncome = calculateTotalIncome(transactions);
        double totalExpense = calculateTotalExpenses(transactions);
        double netBalance = totalIncome - totalExpense;
        double savingsRate = totalIncome > 0 ? ((netBalance / totalIncome) * 100.0) : 0;
        int healthScore = calculateFinancialHealthScore(transactions, budgets);

        sb.append("1. FINANCIAL OVERVIEW\n");
        sb.append(line);
        sb.append(String.format("  Total Inflow / Income    : ₹%,10.2f\n", totalIncome));
        sb.append(String.format("  Total Outflow / Expense  : ₹%,10.2f\n", totalExpense));
        sb.append(String.format("  Net Remaining Balance    : ₹%,10.2f\n", netBalance));
        sb.append(String.format("  Student Savings Rate     : %10.1f%%\n", savingsRate));
        sb.append(String.format("  Financial Health Score   : %10d / 100\n", healthScore));
        sb.append("  Health Assessment        : ").append(getFinancialHealthAdvice(healthScore)).append("\n\n");

        sb.append("2. EXPENSE BREAKDOWN BY CATEGORY\n");
        sb.append(line);
        sb.append(String.format("  %-28s %14s %12s\n", "Category", "Amount (₹)", "Share (%)"));
        sb.append(line);
        Map<Category, Double> catMap = getExpensesByCategory(transactions);
        if (catMap.isEmpty()) {
            sb.append("  No expenses recorded yet.\n");
        } else {
            for (Map.Entry<Category, Double> entry : catMap.entrySet()) {
                double pct = totalExpense > 0 ? (entry.getValue() / totalExpense) * 100.0 : 0;
                sb.append(String.format("  %-28s %14.2f %11.1f%%\n",
                        entry.getKey().getDisplayName(), entry.getValue(), pct));
            }
        }
        sb.append("\n");

        sb.append("3. MONTHLY BUDGET MONITORING (").append(YearMonth.now().toString()).append(")\n");
        sb.append(line);
        sb.append(String.format("  %-24s %12s %12s %12s %10s\n", "Category", "Limit (₹)", "Spent (₹)", "Remaining", "Status"));
        sb.append(line);
        List<CategoryBudgetStatus> statuses = getBudgetStatuses(transactions, budgets, YearMonth.now());
        if (statuses.isEmpty()) {
            sb.append("  No category budgets configured.\n");
        } else {
            for (CategoryBudgetStatus s : statuses) {
                String status = s.isExceeded() ? "EXCEEDED!" : (s.isWarning() ? "WARNING" : "SAFE");
                sb.append(String.format("  %-24s %12.2f %12.2f %12.2f %10s\n",
                        s.getCategory().getDisplayName(),
                        s.getBudgetLimit(),
                        s.getAmountSpent(),
                        s.getRemaining(),
                        status));
            }
        }
        sb.append("\n");

        sb.append("4. SAVINGS GOALS PROGRESS\n");
        sb.append(line);
        sb.append(String.format("  %-24s %12s %12s %10s %12s\n", "Goal Name", "Target (₹)", "Saved (₹)", "Progress", "Deadline"));
        sb.append(line);
        if (goals.isEmpty()) {
            sb.append("  No savings goals configured.\n");
        } else {
            for (SavingsGoal g : goals) {
                sb.append(String.format("  %-24s %12.2f %12.2f %9d%% %12s\n",
                        g.getName(),
                        g.getTargetAmount(),
                        g.getCurrentAmount(),
                        g.getProgressPercentage(),
                        g.getTargetDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
            }
        }
        sb.append(sep);
        sb.append("               End of Student Financial Assessment Report              \n");
        sb.append(sep);

        return sb.toString();
    }
}
