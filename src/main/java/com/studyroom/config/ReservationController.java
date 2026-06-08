package com.studyroom.config;

import com.studyroom.entity.Reservation;
import com.studyroom.entity.Seat;
import com.studyroom.entity.Student;
import com.studyroom.enums.ReservationStatus;
import com.studyroom.service.ReservationService;
import com.studyroom.service.SeatService;
import com.studyroom.service.StudentService;
import com.studyroom.ui.util.DialogUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 预约管理视图控制器
 */
@Component
public class ReservationController {

    private final ReservationService reservationService;
    private final SeatService seatService;
    private final StudentService studentService;

    private final TableView<Reservation> tableView = new TableView<>();
    private final ObservableList<Reservation> dataList = FXCollections.observableArrayList();

    public ReservationController(ReservationService reservationService,
                                 SeatService seatService,
                                 StudentService studentService) {
        this.reservationService = reservationService;
        this.seatService = seatService;
        this.studentService = studentService;
    }

    public VBox createView() {
        VBox view = new VBox(15);
        view.setPadding(new Insets(20));

        Label title = new Label("预约管理");
        title.setFont(Font.font(20));
        view.getChildren().add(title);

        // 查询工具栏
        HBox toolbar = createToolbar();
        view.getChildren().add(toolbar);

        // 表格
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        createTableColumns();
        tableView.setItems(dataList);

        VBox.setVgrow(tableView, javafx.scene.layout.Priority.ALWAYS);
        view.getChildren().add(tableView);

        // 操作按钮
        HBox actions = createActionButtons();
        view.getChildren().add(actions);

        refreshData();
        return view;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(5, 0, 5, 0));

        DatePicker startDate = new DatePicker(LocalDate.now().minusDays(7));
        DatePicker endDate = new DatePicker(LocalDate.now().plusDays(7));
        Button queryBtn = new Button("查询");

        queryBtn.setOnAction(e -> {
            LocalDateTime start = startDate.getValue().atStartOfDay();
            LocalDateTime end = endDate.getValue().atTime(23, 59, 59);
            List<Reservation> list = reservationService.findByTimeRange(start, end);
            dataList.setAll(list);
        });

        toolbar.getChildren().addAll(
                new Label("开始日期:"), startDate,
                new Label("结束日期:"), endDate,
                queryBtn
        );

        return toolbar;
    }

    @SuppressWarnings("unchecked")
    private void createTableColumns() {
        TableColumn<Reservation, Long> idCol = new TableColumn<>("编号");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Reservation, String> seatCol = new TableColumn<>("座位编号");
        seatCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getSeat().getSeatNumber()));

        TableColumn<Reservation, String> studentCol = new TableColumn<>("学生");
        studentCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStudent().getName()
                        + "(" + data.getValue().getStudent().getStudentNo() + ")"));

        TableColumn<Reservation, String> startCol = new TableColumn<>("开始时间");
        startCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStartTime()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        TableColumn<Reservation, String> endCol = new TableColumn<>("结束时间");
        endCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEndTime()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        TableColumn<Reservation, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus().getDisplayName()));

        TableColumn<Reservation, String> checkinCol = new TableColumn<>("签到时间");
        checkinCol.setCellValueFactory(data -> {
            if (data.getValue().getCheckinTime() != null) {
                return new SimpleStringProperty(data.getValue().getCheckinTime()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return new SimpleStringProperty("-");
        });

        TableColumn<Reservation, String> checkoutCol = new TableColumn<>("离座时间");
        checkoutCol.setCellValueFactory(data -> {
            if (data.getValue().getCheckoutTime() != null) {
                return new SimpleStringProperty(data.getValue().getCheckoutTime()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return new SimpleStringProperty("-");
        });

        tableView.getColumns().addAll(idCol, seatCol, studentCol, startCol, endCol,
                statusCol, checkinCol, checkoutCol);
    }

    private HBox createActionButtons() {
        HBox actions = new HBox(10);
        actions.setPadding(new Insets(5, 0, 5, 0));

        Button addBtn = new Button("新增预约");
        addBtn.setOnAction(e -> showAddReservationDialog());

        Button checkinBtn = new Button("办理签到");
        checkinBtn.setOnAction(e -> performCheckIn());

        Button checkoutBtn = new Button("办理离座");
        checkoutBtn.setOnAction(e -> performCheckOut());

        Button cancelBtn = new Button("取消预约");
        cancelBtn.setOnAction(e -> performCancel());

        Button refreshBtn = new Button("刷新");
        refreshBtn.setOnAction(e -> refreshData());

        actions.getChildren().addAll(addBtn, checkinBtn, checkoutBtn, cancelBtn, refreshBtn);
        return actions;
    }

    private void performCheckIn() {
        Reservation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("提示", "请先选择一条预约记录");
            return;
        }
        try {
            reservationService.checkIn(selected.getId());
            DialogUtil.showInfo("签到成功", "签到成功！");
            refreshData();
        } catch (Exception ex) {
            DialogUtil.showError("操作失败", ex.getMessage());
        }
    }

    private void performCheckOut() {
        Reservation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("提示", "请先选择一条预约记录");
            return;
        }
        try {
            reservationService.checkOut(selected.getId());
            DialogUtil.showInfo("离座成功", "离座成功！");
            refreshData();
        } catch (Exception ex) {
            DialogUtil.showError("操作失败", ex.getMessage());
        }
    }

    private void performCancel() {
        Reservation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("提示", "请先选择一条预约记录");
            return;
        }
        if (DialogUtil.showConfirm("确认取消", "确定要取消该预约吗？")) {
            try {
                reservationService.cancel(selected.getId());
                DialogUtil.showInfo("已取消", "预约已取消");
                refreshData();
            } catch (Exception ex) {
                DialogUtil.showError("操作失败", ex.getMessage());
            }
        }
    }

    private void showAddReservationDialog() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("新增预约");

        ButtonType confirmType = new ButtonType("确认预约", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<Seat> seatCombo = new ComboBox<>();
        seatCombo.getItems().addAll(seatService.findAll().stream()
                .filter(s -> s.getStatus() == com.studyroom.enums.SeatStatus.FREE)
                .collect(Collectors.toList()));
        seatCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Seat item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getSeatNumber() + "(" + item.getArea() + ")");
            }
        });
        seatCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Seat item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getSeatNumber() + "(" + item.getArea() + ")");
            }
        });

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

        grid.add(new Label("座位:"), 0, 0);
        grid.add(seatCombo, 1, 0);
        grid.add(new Label("学号*:"), 0, 1);
        grid.add(studentNoField, 1, 1);
        grid.add(new Label("姓名*:"), 0, 2);
        grid.add(nameField, 1, 2);
        grid.add(new Label("电话:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("日期:"), 0, 4);
        grid.add(startDatePicker, 1, 4);
        grid.add(new Label("开始时间:"), 0, 5);
        HBox startTimeBox = new HBox(5, startHourSpinner, new Label("时"), startMinSpinner, new Label("分"));
        grid.add(startTimeBox, 1, 5);
        grid.add(new Label("时长(小时):"), 0, 6);
        grid.add(durationSpinner, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == confirmType) {
                Map<String, String> result = new HashMap<>();
                result.put("seatId", seatCombo.getValue() != null ?
                        String.valueOf(seatCombo.getValue().getId()) : "");
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
            String seatIdStr = data.get("seatId");
            String studentNo = data.get("studentNo");
            String name = data.get("name");
            if (seatIdStr.isEmpty() || studentNo.isEmpty() || name.isEmpty()) {
                DialogUtil.showWarning("输入错误", "座位、学号和姓名为必填项");
                return;
            }
            try {
                Long seatId = Long.parseLong(seatIdStr);
                LocalDate date = LocalDate.parse(data.get("date"));
                int hour = Integer.parseInt(data.get("startHour"));
                int min = Integer.parseInt(data.get("startMin"));
                int duration = Integer.parseInt(data.get("duration"));

                LocalDateTime startTime = LocalDateTime.of(date, LocalTime.of(hour, min));
                LocalDateTime endTime = startTime.plusHours(duration);

                reservationService.reserve(seatId, studentNo, name,
                        data.get("phone"), startTime, endTime);
                DialogUtil.showInfo("预约成功", "预约成功！");
                refreshData();
            } catch (Exception ex) {
                DialogUtil.showError("预约失败", ex.getMessage());
            }
        });
    }

    private void refreshData() {
        dataList.setAll(reservationService.findAll());
    }
}