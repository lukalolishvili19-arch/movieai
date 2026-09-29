package com.movieai.service.browse;

/** Runtime buckets used by browse and recommendation filters (minutes; TV uses episode runtime). */
public enum RuntimeRange {
    ANY("any", null, null),
    UNDER_30("under-30", null, 29),
    BETWEEN_30_60("30-60", 30, 60),
    OVER_60("over-60", 61, null),
    UNDER_90("under-90", null, 89),
    BETWEEN_90_120("90-120", 90, 120),
    OVER_120("over-120", 121, null);

    private final String value;
    private final Integer min;
    private final Integer max;

    RuntimeRange(String value, Integer min, Integer max) {
        this.value = value;
        this.min = min;
        this.max = max;
    }

    public String value() {
        return value;
    }

    public Integer min() {
        return min;
    }

    public Integer max() {
        return max;
    }

    public static RuntimeRange fromValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return ANY;
        }
        for (RuntimeRange r : values()) {
            if (r.value.equalsIgnoreCase(raw.trim())) {
                return r;
            }
        }
        throw new IllegalArgumentException(
                "runtime must be one of any, under-30, 30-60, over-60, under-90, 90-120, over-120");
    }
}
