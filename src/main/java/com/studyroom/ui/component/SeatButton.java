package com.studyroom.ui.component;

import com.studyroom.entity.Seat;
import com.studyroom.enums.SeatStatus;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;

/**
 * 自定义座位按钮组件 —— 不同状态显示不同颜色
 */
public class SeatButton extends Button {

    private final Seat seat;

    private static final Color COLOR_FREE = Color.web("#4CAF50");       // 绿色 - 空闲
    private static final Color COLOR_RESERVED = Color.web("#FF9800");   // 橙色 - 已预约
    private static final Color COLOR_IN_USE = Color.web("#F44336");     // 红色 - 使用中
    private static final Color COLOR_DISABLED = Color.web("#9E9E9E");   // 灰色 - 停用

    public SeatButton(Seat seat) {
        super(seat.getSeatNumber());
        this.seat = seat;
        setPrefSize(80, 60);
        setPadding(new Insets(5));
        setTextFill(Color.WHITE);
        setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;");
        updateAppearance();
    }

    public void updateAppearance() {
        SeatStatus status = seat.getStatus();
        BackgroundFill fill = new BackgroundFill(getColorForStatus(status), new CornerRadii(5), null);
        setBackground(new Background(fill));
        setTooltip(new javafx.scene.control.Tooltip(
                "座位: " + seat.getSeatNumber() +
                "\n区域: " + seat.getArea() +
                "\n状态: " + status.getDisplayName()));
    }

    private Color getColorForStatus(SeatStatus status) {
        return switch (status) {
            case FREE -> COLOR_FREE;
            case RESERVED -> COLOR_RESERVED;
            case IN_USE -> COLOR_IN_USE;
            case DISABLED -> COLOR_DISABLED;
        };
    }

    public Seat getSeat() {
        return seat;
    }
}