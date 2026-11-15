package com.studentexpense.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Tracks student savings goals with target deadlines and progress.
 */
public class SavingsGoal {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String id;
    private String name;
    private double targetAmount;
    private double currentAmount;
    private LocalDate targetDate;
    private String notes;

    public SavingsGoal(String name, double targetAmount, double currentAmount, LocalDate targetDate, String notes) {
        this(UUID.randomUUID().toString().substring(0, 8), name, targetAmount, currentAmount, targetDate, notes);
    }

    public SavingsGoal(String id, String name, double targetAmount, double currentAmount, LocalDate targetDate, String notes) {
        this.id = id;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate != null ? targetDate : LocalDate.now().plusMonths(3);
        this.notes = notes != null ? notes : "";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = Math.max(0, currentAmount);
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            this.currentAmount += amount;
        }
    }

    public void withdraw(double amount) {
        if (amount > 0) {
            this.currentAmount = Math.max(0, this.currentAmount - amount);
        }
    }

    public double getRemainingAmount() {
        return Math.max(0, targetAmount - currentAmount);
    }

    public int getProgressPercentage() {
        if (targetAmount <= 0) return 100;
        return (int) Math.min(100, Math.round((currentAmount / targetAmount) * 100.0));
    }

    public boolean isCompleted() {
        return currentAmount >= targetAmount;
    }

    public String toCsvRow() {
        return String.format("%s,\"%s\",%.2f,%.2f,%s,\"%s\"",
                id,
                name.replace("\"", "\"\""),
                targetAmount,
                currentAmount,
                targetDate.format(DATE_FORMAT),
                notes.replace("\"", "\"\""));
    }

    public static SavingsGoal fromCsvRow(String csvLine) {
        try {
            String[] tokens = csvLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            if (tokens.length < 5) return null;

            String id = tokens[0].trim();
            String name = unquote(tokens[1].trim());
            double target = Double.parseDouble(tokens[2].trim());
            double current = Double.parseDouble(tokens[3].trim());
            LocalDate date = LocalDate.parse(tokens[4].trim(), DATE_FORMAT);
            String notes = tokens.length >= 6 ? unquote(tokens[5].trim()) : "";

            return new SavingsGoal(id, name, target, current, date, notes);
        } catch (Exception e) {
            System.err.println("Error parsing goal CSV line: " + csvLine + " -> " + e.getMessage());
            return null;
        }
    }

    private static String unquote(String s) {
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\"\"", "\"");
    }
}

