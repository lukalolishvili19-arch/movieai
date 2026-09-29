package com.movieai.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(type = "string", allowableValues = {"movie", "tv"})
public enum MediaType {
    MOVIE("movie"),
    TV("tv");

    private final String value;

    MediaType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static MediaType fromValue(String raw) {
        if (raw != null) {
            for (MediaType type : values()) {
                if (type.value.equalsIgnoreCase(raw.trim())) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("Unsupported media type: " + raw);
    }
}
