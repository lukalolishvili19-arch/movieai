package com.movieai.service.catalog;

public enum TimeWindow {
    DAY, WEEK;

    public String value() {
        return name().toLowerCase();
    }

    public static TimeWindow fromValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return DAY;
        }
        return switch (raw.trim().toLowerCase()) {
            case "day" -> DAY;
            case "week" -> WEEK;
            default -> throw new IllegalArgumentException("period must be 'day' or 'week'");
        };
    }
}
