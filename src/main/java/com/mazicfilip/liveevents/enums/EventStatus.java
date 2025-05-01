package com.mazicfilip.liveevents.enums;

public enum EventStatus {
    LIVE, NOT_LIVE;

    public static EventStatus fromString(String value) {
        return switch (value.toLowerCase()) {
            case "live" -> LIVE;
            case "not_live" -> NOT_LIVE;
            default -> throw new IllegalArgumentException("Invalid status: " + value);
        };
    }
}
