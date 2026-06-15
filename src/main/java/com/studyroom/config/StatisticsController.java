package com.studyroom.config;

import com.studyroom.service.statistics.DurationReport;
import com.studyroom.service.statistics.StatusReport;
import com.studyroom.service.statistics.UsageRateReport;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 数据统计视图控制器
 */
@Component
public class StatisticsController {

    private final UsageRateReport usageRateReport;
    private final DurationReport durationReport;

    public StatisticsController(UsageRateReport usageRateReport, DurationReport durationReport) {
        this.usageRateReport = usageRateReport;
        this.durationReport = durationReport;
    }

    public VBox createView() {
        VBox view = new VBox(15);
        view.setPadding(new Insets(20));

        Label title = new Label("数据统计");
        title.setFont(Font.font(20));
        view.getChildren().add(title);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab usageTab = new Tab("使用率统计");
        Tab durationTab = new Tab("学生使用时长统计");

        // 切换到该 Tab 时实时刷新数据
        usageTab.setOnSelectionChanged(e -> {
            if (usageTab.isSelected()) {
                usageTab.setContent(createUsageRateView());
            }
        });
        durationTab.setOnSelectionChanged(e -> {
            if (durationTab.isSelected()) {
                durationTab.setContent(createDurationView());
            }
        });

        // 默认先加载第一个 Tab 的内容
        usageTab.setContent(createUsageRateView());

        tabPane.getTabs().addAll(usageTab, durationTab);
        view.getChildren().add(tabPane);

        return view;
    }

    private ScrollPane createUsageRateView() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        Map<String, Object> report = usageRateReport.generateReport();
        Map<String, Object> overall = (Map<String, Object>) report.get("整体统计");

        content.getChildren().add(createSectionTitle("整体使用情况"));
        for (Map.Entry<String, Object> entry : overall.entrySet()) {
            Label label = new Label(entry.getKey() + ": " + entry.getValue());
            label.setFont(Font.font(14));
            content.getChildren().add(label);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> areaStats = (List<Map<String, Object>>) report.get("各区域统计");
        content.getChildren().add(createSectionTitle("各区域使用情况"));

        for (Map<String, Object> area : areaStats) {
            VBox areaBox = new VBox(3);
            areaBox.setPadding(new Insets(8));
            areaBox.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #eee; -fx-border-radius: 3;");

            for (Map.Entry<String, Object> entry : area.entrySet()) {
                Label label = new Label(entry.getKey() + ": " + entry.getValue());
                label.setFont(Font.font(13));
                areaBox.getChildren().add(label);
            }
            content.getChildren().add(areaBox);
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private ScrollPane createDurationView() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        Map<String, Object> report = durationReport.generateReport();
        content.getChildren().add(createSectionTitle("学生使用时长排名"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rankings = (List<Map<String, Object>>) report.get("学生使用时长排名");

        if (rankings.isEmpty()) {
            content.getChildren().add(new Label("暂无数据"));
        } else {
            int rank = 1;
            for (Map<String, Object> item : rankings) {
                VBox itemBox = new VBox(3);
                itemBox.setPadding(new Insets(8));
                itemBox.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #eee; -fx-border-radius: 3;");

                Label rankLabel = new Label("第 " + (rank++) + " 名");
                rankLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                itemBox.getChildren().add(rankLabel);

                for (Map.Entry<String, Object> entry : item.entrySet()) {
                    Label label = new Label(entry.getKey() + ": " + entry.getValue());
                    label.setFont(Font.font(13));
                    itemBox.getChildren().add(label);
                }
                content.getChildren().add(itemBox);
            }
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 16));
        label.setStyle("-fx-text-fill: #333;");
        return label;
    }
}