package com.studyroom.config;

import com.studyroom.entity.Seat;
import com.studyroom.enums.SeatStatus;
import com.studyroom.service.ReservationService;
import com.studyroom.service.SeatService;
import com.studyroom.ui.component.SeatButton;
import com.studyroom.ui.util.DialogUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 座位布局视图控制器
 */
@Component
public class SeatLayoutController {

    private final SeatService seatService;
    private final ReservationService reservationService;

    private final Map<Long, SeatButton> seatButtonMap = new HashMap<>();
    private VBox mainView;

    public SeatLayoutController(SeatService seatService, ReservationService reservationService) {
        this.seatService = seatService;
        this.reservationService = reservationService;
    }

    public VBox createView() {
        mainView = new VBox(15);
        mainView.setPadding(new Insets(20));

        Label title = new Label("自习室座位布局");
        title.setFont(Font.font(20));
        mainView.getChildren().add(title);

        // 图例说明
        HBox legend = createLegend();
        mainView.getChildren().add(legend);

        // 功能按钮区
        HBox toolbar = createToolbar();
        mainView.getChildren().add(toolbar);

        // 座位布局网格
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        VBox layoutContainer = createSeatLayout();
        scrollPane.setContent(layoutContainer);
        mainView.getChildren().add(scrollPane);

        return mainView;
    }

    private HBox createLegend() {
        HBox legend = new HBox(15);
        legend.setPadding(new Insets(10));
        legend.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5;");

        legend.getChildren().addAll(
                createLegendItem("空闲", "#4CAF50"),
                createLegendItem("已预约", "#FF9800"),
                createLegendItem("使用中", "#F44336"),
                createLegendItem("停用", "#9E9E9E")
        );
        return legend;
    }

    private Label createLegendItem(String text, String color) {
        Label label = new Label(text);
        label.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-padding: 5 15; -fx-font-weight: bold; -fx-background-radius: 3;");
        return label;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(5, 0, 5, 0));

        ComboBox<String> areaFilter = new ComboBox<>();
        areaFilter.getItems().addAll("全部区域", "A区", "B区", "C区");
        areaFilter.setValue("全部区域");
        areaFilter.setOnAction(e -> refreshLayout(areaFilter.getValue()));

        Button refreshBtn = new Button("刷新布局");
        refreshBtn.setOnAction(e -> refreshLayout(areaFilter.getValue()));

        toolbar.getChildren().addAll(new Label("区域筛选:"), areaFilter, refreshBtn);
        return toolbar;
    }

    private VBox createSeatLayout() {
        return buildSeatGrid("全部区域");
    }

    private VBox buildSeatGrid(String area) {
        VBox container = new VBox(10);

        List<Seat> seats;
        if ("全部区域".equals(area)) {
            seats = seatService.findAll();
        } else {
            seats = seatService.findByArea(area);
        }

        // 按区域分组
        Map<String, List<Seat>> byArea = seats.stream()
                .collect(Collectors.groupingBy(Seat::getArea));

        for (Map.Entry<String, List<Seat>> entry : byArea.entrySet()) {
            VBox areaBox = new VBox(5);
            Label areaTitle = new Label(entry.getKey());
            areaTitle.setFont(Font.font(16));
            areaTitle.setStyle("-fx-font-weight: bold;");

            // 按行列排列
            Map<Integer, Map<Integer, Seat>> grid = new TreeMap<>();
            for (Seat s : entry.getValue()) {
                grid.computeIfAbsent(s.getRowIndex(), k -> new TreeMap<>())
                        .put(s.getColIndex(), s);
            }

            GridPane gridPane = new GridPane();
            gridPane.setHgap(8);
            gridPane.setVgap(8);
            gridPane.setPadding(new Insets(10));

            for (Map.Entry<Integer, Map<Integer, Seat>> rowEntry : grid.entrySet()) {
                int row = rowEntry.getKey();
                for (Map.Entry<Integer, Seat> colEntry : rowEntry.getValue().entrySet()) {
                    int col = colEntry.getKey();
                    Seat seat = colEntry.getValue();
                    SeatButton btn = new SeatButton(seat);
                    seatButtonMap.put(seat.getId(), btn);

                    btn.setOnAction(e -> handleSeatAction(seat));
                    gridPane.add(btn, col, row);
                }
            }

            areaBox.getChildren().addAll(areaTitle, gridPane);
            container.getChildren().add(areaBox);
        }

        return container;
    }

    private void refreshLayout(String area) {
        seatButtonMap.clear();
        VBox newGrid = buildSeatGrid(area);

        // 查找 ScrollPane 更新内容
        ScrollPane scrollPane = (ScrollPane) mainView.getChildren().get(3);
        scrollPane.setContent(newGrid);
    }

    private void handleSeatAction(Seat seat) {
        ContextMenu menu = new ContextMenu();

        switch (seat.getStatus()) {
            case FREE -> {
                MenuItem reserveItem = new MenuItem("预约座位");
                reserveItem.setOnAction(e -> showReserveDialog(seat));
                menu.getItems().add(reserveItem);

                MenuItem disableItem = new MenuItem("停用座位");
                disableItem.setOnAction(e -> {
                    seatService.updateStatus(seat.getId(), SeatStatus.DISABLED);
                    refreshLayout(getCurrentAreaFilter());
                });
                menu.getItems().add(disableItem);
            }
            case RESERVED -> {
                // 查找对应的预约
                var reservations = reservationService.findAll().stream()
                        .filter(r -> r.getSeat().getId().equals(seat.getId())
                                && r.getStatus() == com.studyroom.enums.ReservationStatus.RESERVED)
                        .toList();
                if (!reservations.isEmpty()) {
                    var res = reservations.getFirst();
                    MenuItem checkinItem = new MenuItem("办理签到");
                    checkinItem.setOnAction(e -> {
                        try {
                            reservationService.checkIn(res.getId());
                            DialogUtil.showInfo("签到成功", "签到成功！");
                            refreshLayout(getCurrentAreaFilter());
                        } catch (Exception ex) {
                            DialogUtil.showError("操作失败", ex.getMessage());
                        }
                    });
                    menu.getItems().add(checkinItem);

                    MenuItem cancelItem = new MenuItem("取消预约");
                    cancelItem.setOnAction(e -> {
                        try {
                            reservationService.cancel(res.getId());
                            DialogUtil.showInfo("已取消", "预约已取消");
                            refreshLayout(getCurrentAreaFilter());
                        } catch (Exception ex) {
                            DialogUtil.showError("操作失败", ex.getMessage());
                        }
                    });
                    menu.getItems().add(cancelItem);
                }
            }
            case IN_USE -> {
                var reservations = reservationService.findAll().stream()
                        .filter(r -> r.getSeat().getId().equals(seat.getId())
                                && r.getStatus() == com.studyroom.enums.ReservationStatus.CHECKED_IN)
                        .toList();
                if (!reservations.isEmpty()) {
                    var res = reservations.getFirst();
                    MenuItem checkoutItem = new MenuItem("办理离座");
                    checkoutItem.setOnAction(e -> {
                        try {
                            reservationService.checkOut(res.getId());
                            DialogUtil.showInfo("离座成功", "离座成功！");
                            refreshLayout(getCurrentAreaFilter());
                        } catch (Exception ex) {
                            DialogUtil.showError("操作失败", ex.getMessage());
                        }
                    });
                    menu.getItems().add(checkoutItem);
                }
            }
            case DISABLED -> {
                MenuItem enableItem = new MenuItem("启用座位");
                enableItem.setOnAction(e -> {
                    seatService.updateStatus(seat.getId(), SeatStatus.FREE);
                    refreshLayout(getCurrentAreaFilter());
                });
                menu.getItems().add(enableItem);
            }
        }

        menu.show(mainView.getScene().getWindow());
    }

    private String getCurrentAreaFilter() {
        HBox toolbar = (HBox) mainView.getChildren().get(2);
        ComboBox<String> combo = (ComboBox<String>) toolbar.getChildren().get(1);
        return combo.getValue();
    }

    private void showReserveDialog(Seat seat) {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("预约座位");
        dialog.setHeaderText("预约座位: " + seat.getSeatNumber() + " - " + seat.getArea());

        ButtonType confirmType = new ButtonType("确认预约", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField studentNoField = new TextField();
        studentNoField.setPromptText("必填");
        TextField nameField = new TextField();
        nameField.setPromptText("必填");
        TextField phoneField = new TextField();
        phoneField.setPromptText("选填");
        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        Spinner<Integer> startHourSpinner = new Spinner<>(0, 23, LocalTime.now().getHour());
        Spinner<Integer> startMinSpinner = new Spinner<>(0, 59, 0);
        Spinner<Integer> durationSpinner = new Spinner<>(1, 8, 2);

        grid.add(new Label("学号*:"), 0, 0);
        grid.add(studentNoField, 1, 0);
        grid.add(new Label("姓名*:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("电话:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("日期:"), 0, 3);
        grid.add(startDatePicker, 1, 3);
        grid.add(new Label("开始时间:"), 0, 4);
        HBox startTimeBox = new HBox(5, startHourSpinner, new Label("时"), startMinSpinner, new Label("分"));
        grid.add(startTimeBox, 1, 4);
        grid.add(new Label("时长(小时):"), 0, 5);
        grid.add(durationSpinner, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == confirmType) {
                Map<String, String> result = new HashMap<>();
                result.put("studentNo", studentNoField.getText().trim());
                result.put("name", nameField.getText().trim());
                result.put("phone", phoneField.getText().trim());
                result.put("date", startDatePicker.getValue().toString());
                result.put("startHour", startHourSpinner.getValue().toString());
                result.put("startMin", startMinSpinner.getValue().toString());
                result.put("duration", durationSpinner.getValue().toString());
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(data -> {
            String studentNo = data.get("studentNo");
            String name = data.get("name");
            if (studentNo.isEmpty() || name.isEmpty()) {
                DialogUtil.showWarning("输入错误", "学号和姓名为必填项");
                return;
            }

            try {
                LocalDate date = LocalDate.parse(data.get("date"));
                int hour = Integer.parseInt(data.get("startHour"));
                int min = Integer.parseInt(data.get("startMin"));
                int duration = Integer.parseInt(data.get("duration"));

                LocalDateTime startTime = LocalDateTime.of(date, LocalTime.of(hour, min));
                LocalDateTime endTime = startTime.plusHours(duration);

                reservationService.reserve(seat.getId(), studentNo, name,
                        data.get("phone"), startTime, endTime);
                DialogUtil.showInfo("预约成功", "座位 " + seat.getSeatNumber() + " 预约成功！");
                refreshLayout(getCurrentAreaFilter());
            } catch (Exception ex) {
                DialogUtil.showError("预约失败", ex.getMessage());
            }
        });
    }
}