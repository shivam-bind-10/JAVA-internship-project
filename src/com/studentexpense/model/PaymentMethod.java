package com.studentexpense.model;

/**
 * Common payment methods used by college students.
 */
public enum PaymentMethod {
    UPI("UPI / Online Payment"),
    CASH("Cash"),
    DEBIT_CARD("Debit Card"),
    CREDIT_CARD("Credit Card"),
    NET_BANKING("Net Banking");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PaymentMethod fromDisplayName(String text) {
        for (PaymentMethod method : values()) {
            if (method.displayName.equalsIgnoreCase(text) || method.name().equalsIgnoreCase(text)) {
                return method;
            }
        }
        return CASH;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

