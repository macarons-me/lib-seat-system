package com.studyroom.enums;

public enum SeatStatus {
    FREE("空闲"),
    RESERVED("已预约"),
    IN_USE("使用中"),
    DISABLED("停用");

    private final String displayName;

    SeatStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}