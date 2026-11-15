package com.studentexpense.service;

import com.studentexpense.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistent storage of transactions, budgets, and savings goals
 * using CSV format with zero mock data by default (starts clean).
 */
public class DataPersistenceService {
    private static final String DATA_DIR = "data";
    private static final String TRANSACTIONS_FILE = DATA_DIR + "/transactions.csv";
    private static final String BUDGETS_FILE = DATA_DIR + "/budgets.csv";
    private static final String GOALS_FILE = DATA_DIR + "/goals.csv";

    public DataPersistenceService() {
        initDataDirectory();
    }

    private void initDataDirectory() {
        try {
            Path path = Paths.get(DATA_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("Could not create data directory: " + e.getMessage());
        }
    }

    // ---------------- TRANSACTIONS ----------------

    public List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        File file = new File(TRANSACTIONS_FILE);

        if (!file.exists()) {
            return transactions; // Return empty by default (Zero mock data)
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Transaction t = Transaction.fromCsvRow(line);
                if (t != null) {
                    transactions.add(t);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading transactions file: " + e.getMessage());
        }

        return transactions;
    }

    public void saveTransactions(List<Transaction> transactions) {
        initDataDirectory();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(TRANSACTIONS_FILE), StandardCharsets.UTF_8))) {
            writer.println("ID,Title,Amount,Type,Category,Date,PaymentMethod,Notes");
            for (Transaction t : transactions) {
                writer.println(t.toCsvRow());
            }
        } catch (IOException e) {
            System.err.println("Error saving transactions: " + e.getMessage());
        }
    }

    // ---------------- BUDGETS ----------------

    public List<Budget> loadBudgets() {
        List<Budget> budgets = new ArrayList<>();
        File file = new File(BUDGETS_FILE);

        if (!file.exists()) {
            return budgets; // Return empty by default
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Budget b = Budget.fromCsvRow(line);
                if (b != null) {
                    budgets.add(b);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading budgets file: " + e.getMessage());
        }

        return budgets;
    }

    public void saveBudgets(List<Budget> budgets) {
        initDataDirectory();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(BUDGETS_FILE), StandardCharsets.UTF_8))) {
            writer.println("Category,MonthlyLimit,YearMonth");
            for (Budget b : budgets) {
                writer.println(b.toCsvRow());
            }
        } catch (IOException e) {
            System.err.println("Error saving budgets: " + e.getMessage());
        }
    }

    // ---------------- SAVINGS GOALS ----------------

    public List<SavingsGoal> loadGoals() {
        List<SavingsGoal> goals = new ArrayList<>();
        File file = new File(GOALS_FILE);

        if (!file.exists()) {
            return goals; // Return empty by default
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                SavingsGoal g = SavingsGoal.fromCsvRow(line);
                if (g != null) {
                    goals.add(g);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading goals file: " + e.getMessage());
        }

        return goals;
    }

    public void saveGoals(List<SavingsGoal> goals) {
        initDataDirectory();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(GOALS_FILE), StandardCharsets.UTF_8))) {
            writer.println("ID,Name,TargetAmount,CurrentAmount,TargetDate,Notes");
            for (SavingsGoal g : goals) {
                writer.println(g.toCsvRow());
            }
        } catch (IOException e) {
            System.err.println("Error saving goals: " + e.getMessage());
        }
    }

    // ---------------- OPTIONAL DEMO DATA GENERATOR (Loaded only when user requests) ----------------

    public List<Transaction> generateInitialTransactions() {
        List<Transaction> list = new ArrayList<>();
        LocalDate now = LocalDate.now();

        // Incomes (Indian Rupees)
        list.add(new Transaction("Monthly Pocket Money", 12000.00, TransactionType.INCOME, Category.ALLOWANCE, now.withDayOfMonth(1), PaymentMethod.NET_BANKING, "Monthly allowance from parents"));
        list.add(new Transaction("Campus Coding / Tutoring Gig", 3500.00, TransactionType.INCOME, Category.PART_TIME, now.minusDays(8), PaymentMethod.UPI, "Web dev freelancing & math tutoring"));
        list.add(new Transaction("College Merit Scholarship", 4000.00, TransactionType.INCOME, Category.SCHOLARSHIP, now.withDayOfMonth(3), PaymentMethod.NET_BANKING, "Semester scholarship installment"));

        // Expenses (Indian Rupees)
        list.add(new Transaction("Semester Textbooks & Lab Manuals", 1850.00, TransactionType.EXPENSE, Category.BOOKS, now.minusDays(15), PaymentMethod.UPI, "Data Structures and Java books"));
        list.add(new Transaction("Hostel Canteen Meals & Snacks", 1250.00, TransactionType.EXPENSE, Category.FOOD, now.minusDays(12), PaymentMethod.UPI, "Weekly canteen breakfast & lunch"));
        list.add(new Transaction("Hostel / PG Room Rent Share", 5500.00, TransactionType.EXPENSE, Category.RENT, now.withDayOfMonth(2), PaymentMethod.NET_BANKING, "Hostel room rent"));
        list.add(new Transaction("Monthly Metro / Bus Pass", 850.00, TransactionType.EXPENSE, Category.TRANSPORT, now.withDayOfMonth(5), PaymentMethod.DEBIT_CARD, "Student transit smartcard pass"));
        list.add(new Transaction("Weekend Movie & Outing", 650.00, TransactionType.EXPENSE, Category.ENTERTAINMENT, now.minusDays(6), PaymentMethod.UPI, "Movie tickets & snacks with friends"));
        list.add(new Transaction("Stationery & Record Notebooks", 320.00, TransactionType.EXPENSE, Category.BOOKS, now.minusDays(4), PaymentMethod.CASH, "Notebooks and project record sheets"));
        list.add(new Transaction("Mobile Recharge & Dorm Wi-Fi", 499.00, TransactionType.EXPENSE, Category.UTILITIES, now.minusDays(10), PaymentMethod.UPI, "Monthly 5G unlimited plan"));
        list.add(new Transaction("Pharmacy & Health Essentials", 280.00, TransactionType.EXPENSE, Category.HEALTH, now.minusDays(2), PaymentMethod.CASH, "First aid and medicines"));

        saveTransactions(list);
        return list;
    }

    public List<Budget> generateInitialBudgets() {
        List<Budget> list = new ArrayList<>();
        YearMonth currentYm = YearMonth.now();

        list.add(new Budget(Category.FOOD, 3000.00, currentYm));
        list.add(new Budget(Category.BOOKS, 2500.00, currentYm));
        list.add(new Budget(Category.RENT, 6000.00, currentYm));
        list.add(new Budget(Category.TRANSPORT, 1200.00, currentYm));
        list.add(new Budget(Category.ENTERTAINMENT, 1000.00, currentYm));
        list.add(new Budget(Category.UTILITIES, 800.00, currentYm));
        list.add(new Budget(Category.HEALTH, 600.00, currentYm));

        saveBudgets(list);
        return list;
    }

    public List<SavingsGoal> generateInitialGoals() {
        List<SavingsGoal> list = new ArrayList<>();
        LocalDate now = LocalDate.now();

        list.add(new SavingsGoal("Coding Laptop Upgrade", 55000.00, 22000.00, now.plusMonths(4), "Saving for 16GB RAM laptop for ML coursework"));
        list.add(new SavingsGoal("Semester Hackathon Trip", 6000.00, 4200.00, now.plusMonths(2), "Travel & registration for inter-college hackathon"));
        list.add(new SavingsGoal("Student Emergency Reserve", 8000.00, 5000.00, now.plusMonths(6), "Emergency buffer for unexpected expenses"));

        saveGoals(list);
        return list;
    }

    /**
     * Exports a comprehensive formatted text report file.
     */
    public File exportFinancialStatement(String content, String filename) throws IOException {
        File file = new File(filename);
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.print(content);
        }
        return file;
    }
}
