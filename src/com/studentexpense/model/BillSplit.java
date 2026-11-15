package com.studentexpense.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles group expense splitting among roommates/friends in Indian Rupees (₹).
 */
public class BillSplit {
    private String id;
    private String description;
    private double totalAmount;
    private String paidBy;
    private List<String> participants;
    private LocalDate date;

    public BillSplit(String description, double totalAmount, String paidBy, List<String> participants, LocalDate date) {
        this(UUID.randomUUID().toString().substring(0, 8), description, totalAmount, paidBy, participants, date);
    }

    public BillSplit(String id, String description, double totalAmount, String paidBy, List<String> participants, LocalDate date) {
        this.id = id;
        this.description = description;
        this.totalAmount = totalAmount;
        this.paidBy = paidBy;
        this.participants = participants != null ? participants : new ArrayList<>();
        this.date = date != null ? date : LocalDate.now();
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getSharePerPerson() {
        if (participants == null || participants.isEmpty()) return totalAmount;
        return totalAmount / participants.size();
    }

    public String getFormattedSummary() {
        double share = getSharePerPerson();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Bill: %s (₹%.2f) paid by %s on %s\n",
                description, totalAmount, paidBy, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        sb.append(String.format("Split among %d people (₹%.2f each):\n", participants.size(), share));
        for (String person : participants) {
            if (person.equalsIgnoreCase(paidBy)) {
                sb.append(String.format(" - %s (Payer): Paid ₹%.2f, gets back ₹%.2f\n",
                        person, totalAmount, totalAmount - share));
            } else {
                sb.append(String.format(" - %s: Owes ₹%.2f to %s\n", person, share, paidBy));
            }
        }
        return sb.toString();
    }
}
