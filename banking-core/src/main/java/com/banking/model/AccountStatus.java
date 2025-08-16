package com.banking.model;

import java.util.Locale;

public enum AccountStatus {
    ACTIVE,
    FROZEN,
    CLOSED,
    DORMANT;

    private static final AccountStatus DEFAULT = ACTIVE;

    public static AccountStatus safeValueOf(String value) {
        if (value == null || value.isBlank()) return DEFAULT;
        try {
            return AccountStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return DEFAULT;
        }
    }
}