package com.studentexpense.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Represents a single financial transaction (Income or Expense) in Indian Rupees (₹).
 */
public class Transaction {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String id;
    private String title;
    private double amount;
    private TransactionType type;
    private Category category;
    private LocalDate date;
    private PaymentMethod paymentMethod;
    private String notes;

    public Transaction(String title, double amount, TransactionType type, Category category, LocalDate date, PaymentMethod paymentMethod, String notes) {
        this(UUID.randomUUID().toString().substring(0, 8), title, amount, type, category, date, paymentMethod, notes);
    }

    public Transaction(String id, String title, double amount, TransactionType type, Category category, LocalDate date, PaymentMethod paymentMethod, String notes) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date != null ? date : LocalDate.now();
        this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.UPI;
        this.notes = notes != null ? notes : "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFormattedDate() {
        return date.format(DATE_FORMATTER);
    }

    public String getFormattedAmount() {
        return String.format("₹%.2f", amount);
    }

    /**
     * Converts transaction to CSV row.
     */
    public String toCsvRow() {
        return String.format("%s,\"%s\",%.2f,%s,%s,%s,%s,\"%s\"",
                id,
                title.replace("\"", "\"\""),
                amount,
                type.name(),
                category.name(),
                getFormattedDate(),
                paymentMethod.name(),
                notes.replace("\"", "\"\""));
    }

    /**
     * Parses a CSV row into a Transaction object.
     */
    public static Transaction fromCsvRow(String csvLine) {
        try {
            String[] tokens = csvLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            if (tokens.length < 8) return null;

            String id = tokens[0].trim();
            String title = unquote(tokens[1].trim());
            double amount = Double.parseDouble(tokens[2].trim());
            TransactionType type = TransactionType.valueOf(tokens[3].trim());
            Category category = Category.valueOf(tokens[4].trim());
            LocalDate date = LocalDate.parse(tokens[5].trim(), DATE_FORMATTER);
            PaymentMethod paymentMethod = PaymentMethod.valueOf(tokens[6].trim());
            String notes = unquote(tokens[7].trim());

            return new Transaction(id, title, amount, type, category, date, paymentMethod, notes);
        } catch (Exception e) {
            System.err.println("Error parsing transaction CSV line: " + csvLine + " -> " + e.getMessage());
            return null;
        }
    }

    private static String unquote(String s) {
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\"\"", "\"");
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s | ₹%.2f | %s",
                id, getFormattedDate(), type, category.getDisplayName(), amount, title);
    }
}
