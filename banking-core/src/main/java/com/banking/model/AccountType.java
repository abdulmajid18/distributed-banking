package com.banking.model;

import java.util.Locale;

public enum AccountType {
    SAVINGS,
    CHECKING,
    BUSINESS,
    LOAN,
    FIXED_DEPOSIT;

    private static final AccountType DEFAULT = SAVINGS;

    public static AccountType safeValueOf(String value) {
        if (value == null || value.isBlank()) return DEFAULT;
        try {
            return AccountType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return DEFAULT;
        }
    }
}
