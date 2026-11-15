package com.studentexpense.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Categorization of student expenses and income sources.
 */
public enum Category {
    // Expense Categories
    FOOD("Food & Canteen", TransactionType.EXPENSE, "🍴"),
    BOOKS("Books & Stationery", TransactionType.EXPENSE, "📚"),
    RENT("Hostel & Room Rent", TransactionType.EXPENSE, "🏠"),
    TRANSPORT("Travel & Transport", TransactionType.EXPENSE, "🚌"),
    ENTERTAINMENT("Entertainment & Outing", TransactionType.EXPENSE, "🎬"),
    FEES("Tuition & College Fees", TransactionType.EXPENSE, "🎓"),
    UTILITIES("Recharge & Utilities", TransactionType.EXPENSE, "⚡"),
    HEALTH("Health & Personal Care", TransactionType.EXPENSE, "💊"),
    OTHER_EXPENSE("Other Expenses", TransactionType.EXPENSE, "📦"),

    // Income Categories
    ALLOWANCE("Pocket Money / Allowance", TransactionType.INCOME, "💵"),
    PART_TIME("Part-Time / Freelancing", TransactionType.INCOME, "💻"),
    SCHOLARSHIP("Scholarship & Awards", TransactionType.INCOME, "🏆"),
    OTHER_INCOME("Other Income", TransactionType.INCOME, "💰");

    private final String displayName;
    private final TransactionType type;
    private final String iconSymbol;

    Category(String displayName, TransactionType type, String iconSymbol) {
        this.displayName = displayName;
        this.type = type;
        this.iconSymbol = iconSymbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TransactionType getType() {
        return type;
    }

    public String getIconSymbol() {
        return iconSymbol;
    }

    public boolean isExpense() {
        return this.type == TransactionType.EXPENSE;
    }

    public boolean isIncome() {
        return this.type == TransactionType.INCOME;
    }

    public static List<Category> getExpenseCategories() {
        List<Category> list = new ArrayList<>();
        for (Category c : values()) {
            if (c.isExpense()) {
                list.add(c);
            }
        }
        return list;
    }

    public static List<Category> getIncomeCategories() {
        List<Category> list = new ArrayList<>();
        for (Category c : values()) {
            if (c.isIncome()) {
                list.add(c);
            }
        }
        return list;
    }

    public static Category fromDisplayName(String text) {
        for (Category c : values()) {
            if (c.displayName.equalsIgnoreCase(text) || c.name().equalsIgnoreCase(text)) {
                return c;
            }
        }
        return OTHER_EXPENSE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

