package com.studyroom.enums;

public enum ReservationStatus {
    RESERVED("已预约"),
    CHECKED_IN("已签到"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    TIMEOUT("已超时");

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}