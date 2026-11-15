package com.studentexpense.model;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Represents monthly budget limit assigned to an expense category.
 */
public class Budget {
    private static final DateTimeFormatter YM_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private Category category;
    private double monthlyLimit;
    private YearMonth yearMonth;

    public Budget(Category category, double monthlyLimit) {
        this(category, monthlyLimit, YearMonth.now());
    }

    public Budget(Category category, double monthlyLimit, YearMonth yearMonth) {
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.yearMonth = yearMonth != null ? yearMonth : YearMonth.now();
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(YearMonth yearMonth) {
        this.yearMonth = yearMonth;
    }

    public String toCsvRow() {
        return String.format("%s,%.2f,%s",
                category.name(),
                monthlyLimit,
                yearMonth.format(YM_FORMAT));
    }

    public static Budget fromCsvRow(String csvLine) {
        try {
            String[] tokens = csvLine.split(",");
            if (tokens.length < 2) return null;
            Category cat = Category.valueOf(tokens[0].trim());
            double limit = Double.parseDouble(tokens[1].trim());
            YearMonth ym = tokens.length >= 3 ? YearMonth.parse(tokens[2].trim(), YM_FORMAT) : YearMonth.now();
            return new Budget(cat, limit, ym);
        } catch (Exception e) {
            System.err.println("Error parsing budget CSV line: " + csvLine + " -> " + e.getMessage());
            return null;
        }
    }
}

