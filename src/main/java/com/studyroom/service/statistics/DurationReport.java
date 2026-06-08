package com.studyroom.service.statistics;

import com.studyroom.entity.Reservation;
import com.studyroom.enums.ReservationStatus;
import com.studyroom.repository.ReservationRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生座位使用时长统计
 */
@Component
public class DurationReport implements StatusReport {

    private final ReservationRepository reservationRepository;

    public DurationReport(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public String getReportType() {
        return "使用时长统计";
    }

    @Override
    public Map<String, Object> generateReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("reportType", getReportType());
        report.put("generatedTime", LocalDateTime.now().toString());

        List<Reservation> all = reservationRepository.findAll();
        // 统计已完成或已签到的记录（有签到时间可计算时长）
        Map<String, List<Reservation>> byStudent = all.stream()
                .filter(r -> r.getCheckinTime() != null)
                .collect(Collectors.groupingBy(r -> r.getStudent().getName() + "(" + r.getStudent().getStudentNo() + ")"));

        List<Map<String, Object>> studentDurations = new ArrayList<>();
        for (Map.Entry<String, List<Reservation>> entry : byStudent.entrySet()) {
            long totalMinutes = 0;
            int count = entry.getValue().size();
            for (Reservation r : entry.getValue()) {
                LocalDateTime checkin = r.getCheckinTime();
                LocalDateTime checkout = r.getCheckoutTime() != null ? r.getCheckoutTime() : LocalDateTime.now();
                totalMinutes += ChronoUnit.MINUTES.between(checkin, checkout);
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("学生", entry.getKey());
            item.put("预约次数", count);
            item.put("总时长(分钟)", totalMinutes);
            item.put("总时长(小时)", String.format("%.1f", totalMinutes / 60.0));
            studentDurations.add(item);
        }

        // 按时长降序排列
        studentDurations.sort((a, b) -> Long.compare(
                (Long) b.get("总时长(分钟)"), (Long) a.get("总时长(分钟)")));

        report.put("学生使用时长排名", studentDurations);
        return report;
    }
}