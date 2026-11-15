package com.studentexpense.service;

import com.studentexpense.model.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Core business manager maintaining application state,
 * synchronizing persistence, and publishing updates to UI observers.
 */
public class ExpenseManager {

    public interface DataChangeListener {
        void onDataChanged();
    }

    private final DataPersistenceService persistenceService;
    private final AnalyticsService analyticsService;

    private final List<Transaction> transactions = new ArrayList<>();
    private final List<Budget> budgets = new ArrayList<>();
    private final List<SavingsGoal> goals = new ArrayList<>();
    private final List<BillSplit> billSplits = new ArrayList<>();
    private final List<DataChangeListener> listeners = new ArrayList<>();

    public ExpenseManager() {
        this.persistenceService = new DataPersistenceService();
        this.analyticsService = new AnalyticsService();
        loadAll();
    }

    public synchronized void addListener(DataChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void removeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (DataChangeListener listener : listeners) {
            try {
                listener.onDataChanged();
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    public synchronized void loadAll() {
        transactions.clear();
        transactions.addAll(persistenceService.loadTransactions());

        budgets.clear();
        budgets.addAll(persistenceService.loadBudgets());

        goals.clear();
        goals.addAll(persistenceService.loadGoals());

        // Sort transactions descending by date
        sortTransactions();
        notifyListeners();
    }

    public synchronized void saveAll() {
        persistenceService.saveTransactions(transactions);
        persistenceService.saveBudgets(budgets);
        persistenceService.saveGoals(goals);
    }

    private void sortTransactions() {
        transactions.sort((a, b) -> b.getDate().compareTo(a.getDate()));
    }

    // ---------------- TRANSACTION CRUD ----------------

    public synchronized List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public synchronized void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        sortTransactions();
        persistenceService.saveTransactions(transactions);
        notifyListeners();
    }

    public synchronized boolean updateTransaction(Transaction updated) {
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equals(updated.getId())) {
                transactions.set(i, updated);
                sortTransactions();
                persistenceService.saveTransactions(transactions);
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteTransaction(String id) {
        boolean removed = transactions.removeIf(t -> t.getId().equals(id));
        if (removed) {
            persistenceService.saveTransactions(transactions);
            notifyListeners();
        }
        return removed;
    }

    // ---------------- BUDGET CRUD ----------------

    public synchronized List<Budget> getBudgets() {
        return Collections.unmodifiableList(budgets);
    }

    public synchronized void setBudget(Category category, double limit) {
        YearMonth ym = YearMonth.now();
        boolean found = false;
        for (Budget b : budgets) {
            if (b.getCategory() == category) {
                b.setMonthlyLimit(limit);
                b.setYearMonth(ym);
                found = true;
                break;
            }
        }
        if (!found) {
            budgets.add(new Budget(category, limit, ym));
        }
        persistenceService.saveBudgets(budgets);
        notifyListeners();
    }

    public synchronized double getBudgetForCategory(Category category) {
        for (Budget b : budgets) {
            if (b.getCategory() == category) {
                return b.getMonthlyLimit();
            }
        }
        return 0.0;
    }

    // ---------------- SAVINGS GOALS CRUD ----------------

    public synchronized List<SavingsGoal> getGoals() {
        return Collections.unmodifiableList(goals);
    }

    public synchronized void addGoal(SavingsGoal goal) {
        goals.add(goal);
        persistenceService.saveGoals(goals);
        notifyListeners();
    }

    public synchronized boolean updateGoal(SavingsGoal updated) {
        for (int i = 0; i < goals.size(); i++) {
            if (goals.get(i).getId().equals(updated.getId())) {
                goals.set(i, updated);
                persistenceService.saveGoals(goals);
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteGoal(String id) {
        boolean removed = goals.removeIf(g -> g.getId().equals(id));
        if (removed) {
            persistenceService.saveGoals(goals);
            notifyListeners();
        }
        return removed;
    }

    public synchronized void depositToGoal(String id, double amount) {
        for (SavingsGoal g : goals) {
            if (g.getId().equals(id)) {
                g.deposit(amount);
                // Also create an associated expense or transfer entry
                Transaction t = new Transaction("Savings Deposit: " + g.getName(), amount,
                        TransactionType.EXPENSE, Category.OTHER_EXPENSE, LocalDate.now(),
                        PaymentMethod.UPI, "Contribution to savings goal: " + g.getName());
                transactions.add(t);
                sortTransactions();
                saveAll();
                notifyListeners();
                break;
            }
        }
    }

    // ---------------- BILL SPLIT CRUD ----------------

    public synchronized List<BillSplit> getBillSplits() {
        return Collections.unmodifiableList(billSplits);
    }

    public synchronized void addBillSplit(BillSplit split) {
        billSplits.add(0, split);
        notifyListeners();
    }

    // ---------------- UTILITIES ----------------

    public synchronized void resetToSampleData() {
        transactions.clear();
        transactions.addAll(persistenceService.generateInitialTransactions());

        budgets.clear();
        budgets.addAll(persistenceService.generateInitialBudgets());

        goals.clear();
        goals.addAll(persistenceService.generateInitialGoals());

        billSplits.clear();
        notifyListeners();
    }

    public synchronized void clearAllData() {
        transactions.clear();
        budgets.clear();
        goals.clear();
        billSplits.clear();
        saveAll();
        notifyListeners();
    }

    public DataPersistenceService getPersistenceService() {
        return persistenceService;
    }

    public AnalyticsService getAnalyticsService() {
        return analyticsService;
    }
}

