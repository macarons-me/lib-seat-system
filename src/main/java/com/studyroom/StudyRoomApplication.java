package com.studyroom;

import com.studyroom.config.JavaFXConfig;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 自习室座位管理系统 - 主启动类
 * 整合 Spring Boot 与 JavaFX
 */
@SpringBootApplication
public class StudyRoomApplication extends Application {

    private ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        // 先启动 JavaFX Application 线程
        launch(args);
    }

    @Override
    public void init() {
        // 在 JavaFX 启动时初始化 Spring 上下文
        springContext = SpringApplication.run(StudyRoomApplication.class);
        JavaFXConfig.setApplicationContext(springContext);
    }

    @Override
    public void start(Stage primaryStage) {
        // 通过 JavaFXConfig 启动主界面
        JavaFXConfig.launchMainView(this, primaryStage);
    }

    @Override
    public void stop() {
        // 关闭 Spring 上下文
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }
}