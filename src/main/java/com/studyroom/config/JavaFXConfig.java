package com.studyroom.config;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * JavaFX 与 Spring Boot 集成配置
 */
@Component
public class JavaFXConfig implements ApplicationListener<ContextClosedEvent> {

    private static ApplicationContext applicationContext;
    private static Stage primaryStage;

    public static void setApplicationContext(ApplicationContext ctx) {
        applicationContext = ctx;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * 启动 JavaFX 主界面
     */
    public static void launchMainView(Application app, Stage stage) {
        setPrimaryStage(stage);

        // 创建 TabPane 作为主容器
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // 获取所有 Controller Bean
        SeatLayoutController seatLayout = applicationContext.getBean(SeatLayoutController.class);
        ReservationController reservationCtrl = applicationContext.getBean(ReservationController.class);
        StatisticsController statisticsCtrl = applicationContext.getBean(StatisticsController.class);

        // 创建 Tab
        Tab seatTab = new Tab("座位布局", seatLayout.createView());
        Tab reservationTab = new Tab("预约管理", reservationCtrl.createView());
        Tab statisticsTab = new Tab("数据统计", statisticsCtrl.createView());

        tabPane.getTabs().addAll(seatTab, reservationTab, statisticsTab);

        BorderPane root = new BorderPane(tabPane);
        Scene scene = new Scene(root, 1200, 800);

        stage.setTitle("自习室座位管理系统");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        Platform.exit();
    }
}